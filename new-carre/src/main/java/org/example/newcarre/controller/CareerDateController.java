package org.example.newcarre.controller;


import jakarta.annotation.Resource;
import org.example.newcarre.Utils.Result;
import org.example.newcarre.dto.CareerData;
import org.example.newcarre.service.CareerDateService;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController

@RequestMapping("/api")
public class CareerDateController {
    @Resource
    private CareerDateService careerDateService;

    @GetMapping("/career/list")
    public Result<List<CareerData>> list(
            @RequestParam(required = false) String jobName,   // 模糊查询
            @RequestParam(required = false) String workArea,      // 地点
            @RequestParam(required = false) Integer lowSalary,// 最低薪资
            @RequestParam(required = false) Integer highSalary,// 最高薪资
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        List<CareerData> list = careerDateService.list(jobName, workArea, lowSalary, highSalary, page, size);
        return Result.ok(list);
    }
}
