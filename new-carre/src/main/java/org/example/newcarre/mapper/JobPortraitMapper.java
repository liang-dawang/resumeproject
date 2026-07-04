package org.example.newcarre.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.newcarre.dto.JobPortrait;

@Mapper
public interface JobPortraitMapper extends BaseMapper<JobPortrait> {
    @Select("SELECT * FROM job_portrait WHERE job_name = #{jobName}")
    JobPortrait selectJobPortraitByJobName(String jobName);
}
