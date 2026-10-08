package com.jinlin.springaiagent.agent;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

import java.time.Duration;

// 面试题第一部分 概念架构与推理实现
// 涵盖第一题 第二题 第三题 第五题 的代码定义与原理解析
public class AgentInterviewDemonstrator {

    // 第一题 什么是 Agent 与大模型有什么本质不同
    // 大模型是基于海量语料预训练的概率预测引擎 属于被动应答式的计算单元
    // 缺乏自主行动能力 无法主动获取最新环境状态 单次输入对应单次输出
    // Agent 是以大模型为决策中枢 具备感知 记忆 规划 和工具调用能力的智能体
    // Agent 能够自主观察外部环境 根据目标制定策略 循环执行动作并根据反馈修正行为
    // 本质区别在于 从被动的文本生成器进化为目标驱动的闭环自主执行实体

    // 第二题 Agent 的基本架构由哪些核心组件构成
    // 核心四大组件包括
    // 大脑组件 负责语义理解 任务规划 逻辑判断与决策生成
    // 规划组件 负责复杂任务拆解 反思修正 子目标规划
    // 记忆组件 负责维护短期会话上下文与长期知识沉淀
    // 工具组件 负责提供与外部世界交互的实际能力例如API查询数据库操作

    // 第三题 Workflow 与 Agent 与 Tools 的概念和区别
    // Tools 是原子能力接口 执行具体的单个外部功能
    // Workflow 是预先编排好的确定性业务流程 节点与流转规则完全固化 适合高稳定性高一致性场景
    // Agent 是动态路径决策模型 由大模型根据当前状态自主选择执行步骤 适合不确定性与开放性场景

    // 定义 Agent 所需的原子工具集
    public static class WeatherAndCalcTools {

        @Tool("根据城市名称查询该城市的实时天气")
        public String queryWeather(String city) {
            System.out.println("工具执行 获取城市天气 " + city);
            return city + " 当前晴天 温度二十五度";
        }

        @Tool("根据两个数字执行加法运算")
        public int add(int a, int b) {
            System.out.println("工具执行 计算加法 " + a + " 加 " + b);
            return a + b;
        }
    }

    // 声明智能体服务接口
    public interface AssistantAgent {
        String chat(String userMessage);
    }

    // 构建并运行基于 ReAct 模式的 Agent
    // 第五题 ReAct 是啥 具体是怎么实现的
    // ReAct 即 Reasoning 加 Acting 的协同推理范式
    // 核心循环由 思考 行动 观察 构成
    // 第一步 Thought 模型根据目标分析当前上下文 确定需要采取什么动作
    // 第二步 Action 模型输出工具调用指令并传递参数
    // 第三步 Observation 执行工具并将运行结果作为环境反馈写回上下文
    // 第四步 模型根据 Observation 展开新一轮 Thought 直到无需调用工具输出最终结果
    public static String runReActAgentDemo(String prompt) {
        // 配置小米大模型接入客户端
        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .baseUrl("https://api.xiaomimimo.com/v1")
                .apiKey("sk-c60ttpa5pcetez3ybbjw1ihzfh9zoqwi5rt1cjaw6hwy7afb")
                .modelName("mimo-v2.6-flash")
                .timeout(Duration.ofSeconds(60))
                .temperature(0.1)
                .build();

        // 配置短期会话记忆 滑动窗口保留十条消息
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // 使用 LangChain4j 装配具备工具调用与记忆能力的 Agent 实例
        AssistantAgent agent = AiServices.builder(AssistantAgent.class)
                .chatLanguageModel(chatModel)
                .chatMemory(chatMemory)
                .tools(new WeatherAndCalcTools())
                .build();

        return agent.chat(prompt);
    }
}
