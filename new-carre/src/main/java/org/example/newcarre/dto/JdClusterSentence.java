package org.example.newcarre.dto;

import lombok.Data;

/**
 * 聚类提取语句表实体
 * 对应 PostgreSQL 表：jd_cluster_sentence
 */
@Data
public class JdClusterSentence {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 岗位关键词（如：Java、电气、护理、会计）
     */
    private String jobKeyword;

    /**
     * KMeans 提取的语句
     */
    private String sentenceText;
}