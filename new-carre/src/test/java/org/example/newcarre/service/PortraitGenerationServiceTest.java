package org.example.newcarre.service;

import org.example.newcarre.dto.JobPortrait;
import org.example.newcarre.service.PortraitGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PortraitGenerationServiceTest {

    @Autowired
    private PortraitGenerationService portraitGenerationService;

    @Test
    void generatePortraitWithRealLogData() {
        // 1. 将你日志中的真实数据直接放入 List


        // 2. 调用你的 Service
        JobPortrait portrait = portraitGenerationService.generatePortrait("无人机飞手");

        // 3. 打印结果，方便在控制台直观查看大模型的提取效果
        System.out.println("========== 提取结果 ==========");
        System.out.println("技术栈: " + portrait.getProfessionalSkills());
        System.out.println("核心职责: " + portrait.getResponsibility());
        System.out.println("创新能力: " + portrait.getInnovationAbility());
        System.out.println("软素质(沟通): " + portrait.getCommunicationAbility());

        System.out.println("==============================");

        // 4. 核心断言：验证大模型是否成功过滤了噪声
        assertNotNull(portrait, "返回的画像对象不能为空");



        // 验证软素质/福利是否被大模型过滤掉（如果 JobPortrait 中这些字段是 String 类型，应包含"无"）
        // 如果你的字段是 List<String>，请改为 assertTrue(portrait.getWelfare().isEmpty() || portrait.getWelfare().contains("无"));


        // 验证核心职责不为空
        assertFalse(portrait.getResponsibility().isEmpty(), "核心职责不能为空");
    }

    @Test
    void generateAllPortraits() {

        System.out.println("========== 开始完整流程测试 ==========");
        portraitGenerationService.generateAllPortraits();
        System.out.println("========== 流程执行完毕 ==========");
    }
}