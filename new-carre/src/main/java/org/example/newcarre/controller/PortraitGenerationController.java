package org.example.newcarre.controller;


import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.example.newcarre.Utils.Result;
import org.example.newcarre.dto.JobPortrait;
import org.example.newcarre.mapper.KmeansSentence;
import org.example.newcarre.service.PortraitGenerationService;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortraitGenerationController {
    @Resource
    private final KmeansSentence kmeansSentence;
    @Resource
    private PortraitGenerationService portraitGenerationService;

    @PostMapping("/portrait/job_name")
    public Result<List<String>> getPortrait() {
        List<String> list = kmeansSentence.findAllDistinctJobKeywords();
        return Result.ok(list);
    }
    @PostMapping("/portrait/find")
    public Result<List<JobPortrait>> getPortraitByJobName(
            @RequestBody Map<String, String> params
    ) {
        String jobName = params.get("job_name");
        String decodedJobName = null;
        if (jobName != null) {
            decodedJobName = URLDecoder.decode(jobName, StandardCharsets.UTF_8);
        }
        List<JobPortrait> list = portraitGenerationService.getJobPortraitByJobName(decodedJobName);
        return Result.ok(list);
    }



}
