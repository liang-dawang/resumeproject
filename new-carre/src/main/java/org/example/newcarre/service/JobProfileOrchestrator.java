package org.example.newcarre.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newcarre.dto.CareerData;

import org.example.newcarre.dto.JdClusterSentence;
import org.example.newcarre.mapper.CareerMapper;
import org.example.newcarre.mapper.KmeansSentence;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
//提取数据库数据
public class JobProfileOrchestrator {

    private final CareerMapper careerDataMapper; // 1. 注入 Mapper
    private final EmbeddingService embeddingService;
    private final KmeansSentence kmeansSentenceMapper;
    private final SemanticClusteringService clusteringService;

    /**
     * 全量更新所有类别的岗位画像
     */
    public void updateAllCategoryProfiles() {
        // 1. 获取数据库中所有不重复的 jobCategory (例如: "Java", "Python")
        // 注意：BaseMapper 没有直接的 distinct 方法，通常建议用 selectList + Stream 去重，或者写 XML
        List<String> categories = careerDataMapper.selectList(null)
                .stream()
                .map(CareerData::getJobCategory)
                .filter(category -> category != null && !category.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        log.info("发现 {} 个岗位类别待处理: {}", categories.size(), categories);

        for (String category : categories) {
            try {
                processSingleCategory(category);
            } catch (Exception e) {
                log.error("处理类别 [{}] 时发生异常", category, e);
            }
        }
    }

    private void processSingleCategory(String category) {
        // 1. 构建查询条件：WHERE job_category = 'Java'
        LambdaQueryWrapper<CareerData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CareerData::getJobCategory, category);

        // 2. 查询该类别下所有岗位详情
        List<CareerData> jobs = careerDataMapper.selectList(wrapper);

        if (CollectionUtils.isEmpty(jobs)) {
            log.warn("类别 [{}] 下没有数据，跳过", category);
            return;
        }

        // 3. 提取所有岗位的 jobDuty 文本，并过滤掉空内容
        List<String> allDuties = jobs.stream()
                .map(CareerData::getJobDuty)
                .filter(duty -> duty != null && !duty.trim().isEmpty())
                .collect(Collectors.toList());

        if (allDuties.isEmpty()) {
            log.warn("类别 [{}] 下有岗位数据，但没有有效的职责描述(jobDuty)，跳过", category);
            return;
        }

        log.info("正在为类别 [{}] 生成画像，共聚合了 {} 条职责描述...", category, allDuties.size());

        // 4. 核心处理：向量化 + 聚类/提取代表句
        // 注意：这里调用你在上一个问题中提到的 generateProfile 方法
        List<String> profileResult = clusteringService.generateProfile(allDuties);

        // 5. 保存结果
        saveProfileToDb(category, profileResult);
    }


    private void saveProfileToDb(String category, List<String> profile) {
        // 1. 打印日志
        log.info("✅ 类别 [{}] 画像生成完毕，代表性职责如下:", category);
        profile.forEach(s -> log.info("   - {}", s));

        // 2. 判空
        if (profile == null || profile.isEmpty()) {
            return;
        }

        // 3. 组装实体列表（自动去重，不重复插入）
        List<JdClusterSentence> insertList = new ArrayList<>();

        for (String sentence : profile) {
            // 判断数据库里是否已存在【相同岗位 + 相同语句】
            boolean exists = kmeansSentenceMapper.exists(
                    new LambdaQueryWrapper<JdClusterSentence>()
                            .eq(JdClusterSentence::getJobKeyword, category)
                            .eq(JdClusterSentence::getSentenceText, sentence)
            );

            // 不存在才加入插入列表
            if (!exists) {
                JdClusterSentence entity = new JdClusterSentence();
                entity.setJobKeyword(category);
                entity.setSentenceText(sentence);
                insertList.add(entity);
            }
        }

        // 4. 批量插入（MyBatis-Plus 官方方法）
        if (!insertList.isEmpty()) {
            kmeansSentenceMapper.insertBatch(insertList);
            log.info("✅ [{}] 入库成功：{} 条", category, insertList.size());
        } else {
            log.info("✅ [{}] 无新数据，全部已存在", category);
        }
    }
}