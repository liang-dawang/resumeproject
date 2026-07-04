package org.example.newcarre.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newcarre.dto.JdClusterSentence;
import org.example.newcarre.dto.JobPortrait;
import org.example.newcarre.mapper.JobPortraitMapper;
import org.example.newcarre.mapper.KmeansSentence;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortraitGenerationService {

    // 直接注入SpringAIAlibaba自动装配DashScopeChatModel(实现类)，去掉ChatClient
    private final ChatModel chatModel;
    private final JobPortraitMapper jobPortraitMapper;

    private final KmeansSentence jdClusterSentenceRepository;

    /**
     * 生成结构化岗位画像
     */
    public void generateAllPortraits() {
        // 1. 自动查出数据库中所有的 job_keyword
        List<String> allKeywords = jdClusterSentenceRepository.findAllDistinctJobKeywords();

        if (allKeywords == null || allKeywords.isEmpty()) {
            log.warn("数据库中暂无任何岗位关键词，跳过画像生成。");
            return;
        }

        log.info("🚀 共发现 {} 个岗位关键词，开始自动生成画像...", allKeywords.size());

        // 2. 遍历每个关键词，执行生成逻辑
        for (String keyword : allKeywords) {
            try {
                log.info("⏳ 正在处理岗位: 【{}】", keyword);
                JobPortrait portrait = generatePortrait(keyword);


                // ======================
                // ✅ 核心：保存/更新画像到数据库
                // ======================
                saveOrUpdatePortrait(keyword, portrait);

                log.info("✅ 岗位 【{}】 画像生成并保存成功！", keyword);

                log.info("✅ 岗位 【{}】 画像生成成功！", keyword);

            } catch (Exception e) {
                // 单个岗位失败不影响其他岗位，记录错误并继续
                log.error("❌ 岗位 【{}】 画像生成失败: {}", keyword, e.getMessage(), e);
            }
        }
        log.info("🎉 所有岗位画像生成任务执行完毕！");
    }

    /**
     * 核心生成逻辑：原生ChatModel调用，无ChatClient
     */
    public JobPortrait generatePortrait(String jobCategory) {
        // 1. 根据 job_keyword 查出所有的 sentence_text
        List<JdClusterSentence> sentences = jdClusterSentenceRepository.findByJobKeyword(jobCategory);

        if (sentences == null || sentences.isEmpty()) {
            log.warn("未找到岗位 [{}] 的相关职责描述，跳过", jobCategory);
            return null;
        }

        // 2. 将职责列表拼接为易读的文本格式
        String dutiesText = sentences.stream()
                .map(JdClusterSentence::getSentenceText)
                .map(s -> "- " + s)
                .collect(Collectors.joining("\n"));

        // 3. 构建 System Prompt
        String systemPrompt = """
                【角色】你是人力资源与岗位画像抽取专家，依据提供的岗位描述，为全行业岗位生成标准化的岗位画像。
                                
                【执行步骤】
                1. 【数据清洗】首先，必须从输入的职责列表中剔除以下非技术性/非职责类噪声：
                   - 纯软素质要求（如：抗压能力、沟通能力、团队协作、逻辑思维等）
                   - 公司福利与办公环境（如：五险一金、下午茶、健身房、弹性工作等）
                  
                   - 纯公司介绍、业务宣传、工作地点列表
                2. 【结构化提取】基于清洗后的有效内容，严格按照指定结构提取信息。
                                
                【字段定义（全行业通用）】
                - professionalSkills：专业技能（包含：设备操作、工艺技术、软件工具、仪器使用、编程语言、系统操作等）
                - certificateRequirement：证书要求（从业资格证、职业等级证、上岗证、英语等级、技能认证等）
                - innovationAbility：创新能力（技术改进、工艺优化、方案创新、效率提升）
                - learningAbility：学习能力（快速掌握新设备、新工艺、新技术、新标准的能力）
                - compressionAbility：抗压能力（加班适应、多任务处理、应急处理、倒班、高强度作业）
                - communicationAbility：沟通能力（团队协作、需求对接、客户沟通、文档编写、汇报）
                - responsibility：岗位职责（核心工作内容、操作流程、任务执行、项目交付、设备维护、生产作业等）
                - educationRequirement：学历要求（中专/大专/本科/硕士及以上）
                - majorRequirement：专业要求（机电、电气、机械、护理、会计、工程类等）
                - workExperience：工作经验（应届生/1-3年/3-5年/无需经验）
                - welfare：公司福利（薪资、补贴、食宿、保险、假期、办公环境）
                - jobDesc：岗位一句话简介
                                
                【硬性规则】
                1. 所有字段不能为空，若无信息填写 "无"。
                2. 严格按结构化JSON输出，不添加任何解释、说明、标题、符号。
                """;

        // 4. 构建 User Prompt
        String userPrompt = String.format("""
                【岗位分类】：%s
                【原始岗位职责列表】：
                %s
                """, jobCategory, dutiesText);

        try {
            // 初始化Bean转换器，自动生成JSON格式约束拼入提示词
            BeanOutputConverter<JobPortrait> converter = new BeanOutputConverter<>(JobPortrait.class);
            String formatRule = converter.getFormat();

            // 组装系统消息+用户消息，追加JSON格式要求
            SystemMessage sysMsg = new SystemMessage(systemPrompt + "\n【输出格式强制约束】" + formatRule);
            UserMessage userMsg = new UserMessage(userPrompt);

            // 组装DashScope动态参数
            DashScopeChatOptions options = DashScopeChatOptions.builder()
                    .withModel("qwen-max")
                    .withTemperature(0.4)
                    .withTopP(0.7)
                    .withTopK(50)
                    .build();

            // 封装Prompt（消息+模型参数）
            Prompt prompt = new Prompt(List.of(sysMsg, userMsg), options);

            // ChatModel原生调用大模型
            ChatResponse response = chatModel.call(prompt);
            String respJson = response.getResult().getOutput().getText();

            // JSON自动转为JobPortrait实体
            return converter.convert(respJson);

        } catch (Exception e) {
            log.error("解析岗位画像失败, jobCategory: {}", jobCategory, e);
            throw new RuntimeException("结构化输出解析失败", e);
        }
    }


    //    保存画像
// ======================
// 保存或更新画像
// ======================
    private void saveOrUpdatePortrait(String jobName, JobPortrait portrait) {
        if (portrait == null) {
            log.warn("❌ 岗位 [{}] 画像为空，不保存", jobName);
            return;
        }
        // ✅ 关键：清空id，新增时让id=null，触发PG自增
        portrait.setId(null);
        portrait.setJobName(jobName);

        JobPortrait exist = jobPortraitMapper.selectOne(
                new LambdaQueryWrapper<JobPortrait>()
                        .eq(JobPortrait::getJobName, jobName)
        );

        if (exist != null) {
            // ✅ 更新必须赋值数据库原有主键
            portrait.setId(exist.getId());
            jobPortraitMapper.updateById(portrait);
            log.info("🔄 岗位 [{}] 画像已更新", jobName);
        } else {
            // id=null → PG自动走nextval自增生成id
            jobPortraitMapper.insert(portrait);
            log.info("🆕 岗位 [{}] 画像已插入", jobName);
        }
    }

    public List<JobPortrait> getJobPortraitByJobName(String jobName) {
        // 1. 打印接收到的参数长度和内容，检查是否有隐藏字符
        if (jobName != null) {
            log.info("🔍 收到查询参数: [{}], 长度: {}, 十六进制: {}",
                    jobName,
                    jobName.length());
        }

        // 2. 去除首尾空格
        String trimmedJobName = jobName != null ? jobName.trim() : null;

        // 3. 执行查询
        JobPortrait portrait = jobPortraitMapper.selectJobPortraitByJobName(trimmedJobName);

        // 4. 打印结果
        if (portrait == null) {
            log.warn("❌ 数据库中未找到名称为 [{}] 的记录", trimmedJobName);
            return List.of();
        }

        return List.of(portrait);
    }
}