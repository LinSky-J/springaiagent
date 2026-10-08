package com.jinlin.springaiagent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.annotation.Tool;

// 基于 Spring AI 框架的 Agent 核心实现类
// 演示如何利用 Spring AI 的 ChatClient 核心组件与 Advisor 切面记忆机制构建企业级智能体
public class SpringAiAgentDemonstrator {

    // Spring AI 风格的工具组件类
    public static class SpringAiOrderTools {

        @Tool(description = "根据订单编号查询订单状态与详情")
        public String queryOrderStatus(String orderId) {
            System.out.println("Spring AI 工具执行 查询订单 " + orderId);
            return "订单编号 " + orderId + " 当前状态为已发货 预计明天送达";
        }

        @Tool(description = "根据用户编号查询该用户的会员积分")
        public int queryUserPoints(String userId) {
            System.out.println("Spring AI 工具执行 查询用户积分 " + userId);
            return 888;
        }
    }

    // 构建并运行 Spring AI 驱动的 Agent
    public static String runSpringAiAgent(String userPrompt) {
        // 配置 Spring AI 的 OpenAI 底层通讯 API
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl("https://api.xiaomimimo.com")
                .apiKey("sk-c60ttpa5pcetez3ybbjw1ihzfh9zoqwi5rt1cjaw6hwy7afb")
                .build();

        // 配置大模型推理参数
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model("mimo-v2.6-flash")
                .temperature(0.1)
                .build();

        // 构造 Spring AI 核心模型客户端
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();

        // 使用 Spring AI 的 Advisor 切面机制装配会话记忆
        InMemoryChatMemory chatMemory = new InMemoryChatMemory();
        MessageChatMemoryAdvisor memoryAdvisor = new MessageChatMemoryAdvisor(chatMemory);

        // 使用 Spring AI 核心门面 ChatClient 装配智能体
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(memoryAdvisor)
                .defaultTools(new SpringAiOrderTools())
                .build();

        return chatClient.prompt(userPrompt).call().content();
    }
}
