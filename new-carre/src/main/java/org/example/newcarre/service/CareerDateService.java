package org.example.newcarre.service;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.example.newcarre.dto.CareerData;
import org.example.newcarre.mapper.CareerMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CareerDateService {
    @Resource
    private CareerMapper careerMapper;


    public List<CareerData> list(String jobName,
                          String workArea,
                          Integer lowSalary,
                          Integer highSalary,
                          Integer page,
                          Integer size) {

        LambdaQueryWrapper<CareerData> wrapper = new LambdaQueryWrapper<>();

        // 1. 岗位名称 模糊查询
        if (StrUtil.isNotBlank(jobName)) {
            wrapper.like(CareerData::getJobName, jobName);
        }


        // 2. 地点 精确匹配
        if (StrUtil.isNotBlank(workArea)) {
            wrapper.eq(CareerData::getWorkArea, workArea);
        }

        // 3. 最低薪资 >=
        if (lowSalary != null) {
            wrapper.ge(CareerData::getLowSalary, lowSalary);
        }

        // 4. 最高薪资 <=
        if (highSalary != null) {
            wrapper.le(CareerData::getHighSalary, highSalary);
        }

        // 分页查询
        Page<CareerData> result = careerMapper.selectPage(Page.of(page, size), wrapper);
        return result.getRecords();
    }
}
