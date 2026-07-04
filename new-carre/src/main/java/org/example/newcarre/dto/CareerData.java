package org.example.newcarre.dto;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("computer_jobs")
public class CareerData {

    // 主键自增

    private Long id;

    // 统一岗位分类
    private String jobCategory;

    // 岗位名称
    private String jobName;

    // 公司名称
    private String companyName;

    // 最低薪资
    private BigDecimal lowSalary;

    // 最高薪资
    private BigDecimal highSalary;

    // 工作地点
    private String workArea;

    // 学历要求
    private String degreeRequired;

    // 公司规模
    private String companyScale;


    // 搜索关键词
    private String jobKeywords;

    // 岗位职责
    private String jobDuty;

    // 详情链接
    private String sourceUrl;

    // 创建时间
    private LocalDateTime createTime;


}
