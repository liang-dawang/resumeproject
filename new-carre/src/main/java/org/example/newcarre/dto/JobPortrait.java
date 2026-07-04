package org.example.newcarre.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 岗位画像表 实体类
 * 对应 PostgreSQL 表：job_portrait
 */
@Data
@TableName("job_portrait")
public class JobPortrait {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 岗位名称
     */
    private String jobName;

    /**
     * 岗位类型：1-全职 2-实习
     */
    private Integer jobType;

    /**
     * 岗位描述
     */
    private String jobDesc;

    /**
     * 专业技能
     */
    private String professionalSkills;

    /**
     * 证书要求
     */
    private String certificateRequirement;

    /**
     * 创新能力
     */
    private String innovationAbility;

    /**
     * 学习能力
     */
    private String learningAbility;

    /**
     * 抗压能力
     */
    private String compressionAbility;

    /**
     * 沟通能力
     */
    private String communicationAbility;

    /**
     * 学历要求
     */
    private String educationRequirement;

    /**
     * 专业要求
     */
    private String majorRequirement;

    /**
     * 工作经验
     */
    private String workExperience;


    /**
     * 岗位职责
     */
    private String responsibility;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}