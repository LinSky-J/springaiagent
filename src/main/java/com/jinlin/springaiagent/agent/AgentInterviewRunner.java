package com.jinlin.springaiagent.agent;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

// 面试题综合运行验证入口类
// 通过主函数串联运行并验证所有九道题目对应的核心设计与实战代码
public class AgentInterviewRunner {

    public static void main(String[] args) {
        System.out.println("开始执行 AI Agent 面试题实战代码全量验证");

        // 模块一 验证第一题至第三题以及第五题 ReAct 推理闭环与工具调用
        System.out.println("\n第一阶段 验证 ReAct 范式与工具调用联动");
        try {
            String question = "请问北京的天气怎么样 以及如果加十五度是多少度";
            System.out.println("输入提示 " + question);
            String agentReply = AgentInterviewDemonstrator.runReActAgentDemo(question);
            System.out.println("智能体回答 " + agentReply);
        } catch (Exception e) {
            System.out.println("阶段一异常 " + e.getMessage());
        }

        // 模块二 验证第四题 第六题与第七题 复杂任务规划分解与反思机制
        System.out.println("\n第二阶段 验证任务拆解与 Plan-and-Execute 规划执行器");
        try {
            AgentParadigmAndTaskDecomposition.PlanAndExecuteEngine planEngine =
                    new AgentParadigmAndTaskDecomposition.PlanAndExecuteEngine();
            List<AgentParadigmAndTaskDecomposition.SubTask> tasks =
                    planEngine.plan("构建高可用智能体客服系统");
            String planResult = planEngine.executePlan(tasks);
            System.out.println("规划执行全过程日志 \n" + planResult);
        } catch (Exception e) {
            System.out.println("阶段二异常 " + e.getMessage());
        }

        // 模块三 验证第八题与第九题 短期会话记忆与长期多粒度记忆系统
        System.out.println("\n第三阶段 验证短期滑动窗口记忆与长期持久记忆存储");
        try {
            // 短期记忆验证
            AgentMemorySystem.ShortTermMemoryManager shortTermMemory =
                    new AgentMemorySystem.ShortTermMemoryManager(4);
            shortTermMemory.addUserMessage("你好 我是一名 Java 架构师");
            shortTermMemory.addAiMessage("收到 很高兴认识你");
            shortTermMemory.addUserMessage("我正在准备关于 Agent 的面试");
            shortTermMemory.addAiMessage("预祝你面试顺利");
            shortTermMemory.addUserMessage("请问我最开始介绍自己的职业是什么");

            List<ChatMessage> history = shortTermMemory.getMessages();
            System.out.println("短期记忆当前保留消息条数 " + history.size());

            // 长期记忆验证
            AgentMemorySystem.LongTermMemoryStore longTermStore =
                    new AgentMemorySystem.LongTermMemoryStore();
            longTermStore.saveUserProfile("用户1001", "擅长语言", "Java");
            longTermStore.saveUserProfile("用户1001", "技术方向", "Spring AI与智能体开发");
            longTermStore.saveEpisodeMemory("记忆001", "项目经历", "曾经在大型金融项目中主导智能客服微服务架构");

            List<AgentMemorySystem.LongTermMemoryStore.MemorySnippet> matches =
                    longTermStore.searchRelevantMemories("智能客服");
            System.out.println("长期记忆关键词命中数 " + matches.size());
            if (!matches.isEmpty()) {
                System.out.println("命中经验内容 " + matches.get(0).content);
            }
        } catch (Exception e) {
            System.out.println("阶段三异常 " + e.getMessage());
        }

        // 模块四 验证 Spring AI 的 ChatClient 与企业级 Tool 机制
        System.out.println("\n第四阶段 验证 Spring AI 框架的 Agent 运行机制");
        try {
            String orderPrompt = "请帮我查询订单 88888 的状态";
            System.out.println("Spring AI 输入提示 " + orderPrompt);
            String springAiReply = SpringAiAgentDemonstrator.runSpringAiAgent(orderPrompt);
            System.out.println("Spring AI 智能体回答 " + springAiReply);
        } catch (Exception e) {
            System.out.println("阶段四异常 " + e.getMessage());
        }

        System.out.println("\n所有核心模块执行与验证完成");
    }
}
