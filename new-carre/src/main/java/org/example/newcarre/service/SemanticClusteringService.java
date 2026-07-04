package org.example.newcarre.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import smile.clustering.CentroidClustering;
import smile.clustering.KMeans;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticClusteringService {

    private final EmbeddingService embeddingService;
    private static final Pattern SPLIT_PATTERN = Pattern.compile("[。；;\\n]+");

    /**
     * 【核心入口】为单个类别生成画像
     * @param duties 原始职责列表
     * @return 代表性职责列表
     */
    public List<String> generateProfile(List<String> duties) {
        if (CollectionUtils.isEmpty(duties)) return Collections.emptyList();

        // 1. 切分与清洗
        List<String> sentences = splitSentences(duties);
        if (CollectionUtils.isEmpty(sentences)) return Collections.emptyList();

        log.info("原始职责 {} 条，切分后有效句子 {} 条", duties.size(), sentences.size());

        // 2. 获取向量
        float[][] vectors = embeddingService.embed(sentences);

        // 3. 设定每个簇提取的代表句数量
        int topN = 1;

        // 4. 动态计算 K 值
        int k = Math.max(1, Math.min(sentences.size() / topN, 20));

        // 5. 执行聚类与提取
        return extractRepresentativesByKMeans(sentences, vectors, k, topN);
    }

    /**
     * 切分句子并过滤噪声
     */
    public List<String> splitSentences(List<String> jobDuties) {
        Set<String> stopPhrases = Set.of(
                // 标题类
                "岗位职责", "任职要求", "职位描述", "福利待遇", "工作地点", "公司介绍",
                "技术能力", "综合素质", "加分项", "优先考虑",

                // 福利类
                "员工基金", "零成本分红", "合伙人计划", "年终奖", "季度奖", "特别贡献奖",
                "休息区", "自营咖啡店", "饮品零食", "健身中心", "人体工学椅", "多屏工作站",
                "work life balance", "965", "弹性工作", "五险一金", "带薪年假",
                // 团队/公司文化类
                "名校精英", "行业专家", "专业导师", "一对一指导", "顶尖技术团队",
                "AI超算集群", "沉浸式体验", "科技驱动", "成长喜悦", "共享每一份"

        );

        return jobDuties.stream()
                .filter(Objects::nonNull)
                .flatMap(text -> Arrays.stream(SPLIT_PATTERN.split(text)))
                .map(String::trim)
                // 长度过滤
                .filter(s -> s.length() > 6 && s.length() < 200)
                // 黑名单过滤
                .filter(s -> stopPhrases.stream().noneMatch(s::contains))
                // 去除纯标题行（如 【岗位职责】、任职要求： 等）
                .filter(s -> !s.matches("^[【\\[\\(]?[\\u4e00-\\u9fa5]{2,8}[】\\]\\)]?[:：]?$"))
                // 去除纯数字编号开头且内容很短的（如 "1." "2、"）
                .filter(s -> !s.matches("^[（(]?\\d+[、).）]+.{0,10}$"))
                // 去除以"-"或"•"开头的纯列表符号行（通常是格式残留）
                .filter(s -> !s.matches("^[-•*●④].{0,12}$"))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 【核心逻辑】K-Means 聚类并提取每个簇的 Top-N 代表句
     */
    public List<String> extractRepresentativesByKMeans(List<String> sentences, float[][] embeddings, int k, int topN) {
        if (sentences == null || embeddings == null || sentences.size() != embeddings.length) {
            throw new IllegalArgumentException("Sentences and embeddings size mismatch");
        }

        // 1. 预处理：转 double 并 L2 归一化
        double[][] normalizedData = normalizeAndConvert(embeddings);

        // 2. 执行 K-Means
        CentroidClustering<double[], double[]> model = KMeans.fit(normalizedData, k, 300);

        // 3. 获取结果
        int[] labels = model.group();
        double[][] centroids = model.centers();

        List<String> representatives = new ArrayList<>();
        Set<Integer> usedIndices = new HashSet<>();

        // 4. 遍历每一个簇，提取 Top-N
        for (int i = 0; i < k; i++) {
            final int clusterIndex = i;
            final double[] currentCentroid = centroids[i];

            List<Integer> clusterIndices = new ArrayList<>();
            for (int j = 0; j < labels.length; j++) {
                if (labels[j] == clusterIndex && !usedIndices.contains(j)) {
                    clusterIndices.add(j);
                }
            }

            List<Map.Entry<Integer, Double>> topCandidates = clusterIndices.stream()
                    .map(j -> new AbstractMap.SimpleEntry<>(j, euclideanDistanceSquared(normalizedData[j], currentCentroid)))
                    .sorted(Map.Entry.comparingByValue())
                    .limit(topN)
                    .collect(Collectors.toList());

            for (Map.Entry<Integer, Double> entry : topCandidates) {
                representatives.add(sentences.get(entry.getKey()));
                usedIndices.add(entry.getKey());
            }
        }

        log.info("聚类完成 (K={}), 共提取代表句 {} 条", k, representatives.size());
        return representatives;
    }

    /**
     * 辅助方法：Float转Double并L2归一化
     */
    private double[][] normalizeAndConvert(float[][] data) {
        int rows = data.length;
        int cols = data[0].length;
        double[][] result = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            double sumSq = 0.0;
            for (int j = 0; j < cols; j++) {
                sumSq += (double) data[i][j] * data[i][j];
            }
            double norm = Math.sqrt(sumSq);
            if (norm < 1e-8) norm = 1.0;

            for (int j = 0; j < cols; j++) {
                result[i][j] = data[i][j] / norm;
            }
        }
        return result;
    }

    /**
     * 辅助方法：计算欧氏距离平方
     */
    private double euclideanDistanceSquared(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return sum;
    }
}