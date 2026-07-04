package org.example.newcarre.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JobProfileOrchestratorTest {

    @Autowired
    private JobProfileOrchestrator orchestrator;

    @Test
    void runFullFlow() {
        System.out.println("========== 开始完整流程测试 ==========");
        orchestrator.updateAllCategoryProfiles();
        System.out.println("========== 流程执行完毕 ==========");
    }
}