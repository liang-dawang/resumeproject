package org.example.newcarre.Study.Tool;

import org.example.newcarre.Study.Tool.DateTimeTools; // 改成你实际包路径
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

@SpringBootTest
class DateTimeToolsTest {

    // 标准Spring注入，无需手动写构造器
    @Autowired
    private ChatModel chatModel;

    // 所有AI调用逻辑放到@Test方法内
    @Test
    void testDateTimeToolCall() {
        // 实例化工具类注册到ChatClient
        DateTimeTools dateTimeTools = new DateTimeTools();

        String response = ChatClient.create(chatModel)
                .prompt("What day is tomorrow? Please call getCurrentDateTime tool to calculate")
                .tools(dateTimeTools) // 注册自定义Tool
                .call()
                .content();

        // 打印结果，查看模型是否调用工具、返回正确日期
        System.out.println("大模型返回结果：" + response);
    }
}