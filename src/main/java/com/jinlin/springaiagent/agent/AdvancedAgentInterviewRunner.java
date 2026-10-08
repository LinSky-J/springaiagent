package com.jinlin.springaiagent.agent;

import java.util.HashMap;
import java.util.Map;

// 高阶面试题综合运行验证入口类
// 全量串联验证第十题至第二十四题所涉及的高可用 多智能体协作 状态中断恢复 死循环熔断 任务验真与安全防线
public class AdvancedAgentInterviewRunner {

    public static void main(String[] args) {
        System.out.println("开始执行进阶 AI Agent 面试题核心机制实战验证");

        // 模块一 多 Agent 协作与超时熔断容灾验证
        System.out.println("\n第一阶段 验证多智能体分工协同与超时保护机制");
        MultiAgentCollaborationDemo.MultiAgentOrchestrator orchestrator =
                new MultiAgentCollaborationDemo.MultiAgentOrchestrator();
        orchestrator.registerAgent(new MultiAgentCollaborationDemo.ResearchAgent());
        orchestrator.registerAgent(new MultiAgentCollaborationDemo.SolutionAgent());

        Map<String, Object> sharedState = new HashMap<>();
        String r1 = orchestrator.executeWithTimeoutAndConflictControl("调研专家", "金融智能体中台调研", sharedState, 3);
        System.out.println("调研阶段反馈 " + r1);
        String r2 = orchestrator.executeWithTimeoutAndConflictControl("架构设计专家", "输出系统架构", sharedState, 3);
        System.out.println("方案阶段反馈 " + r2);
        orchestrator.shutdown();

        // 模块二 多轮对话状态 Checkpoint 持久化与中断恢复验证
        System.out.println("\n第二阶段 验证多轮对话检查点快照与中断恢复");
        MultiAgentCollaborationDemo.ConversationStateManager stateManager =
                new MultiAgentCollaborationDemo.ConversationStateManager();
        stateManager.saveCheckpoint("会话9999", 1, "意图解析", sharedState);
        sharedState.put("当前审核状态", "通过");
        stateManager.saveCheckpoint("会话9999", 2, "风控校验", sharedState);

        MultiAgentCollaborationDemo.ConversationStateManager.StateCheckpoint resumed =
                stateManager.resumeLatestCheckpoint("会话9999");
        if (resumed != null) {
            System.out.println("成功加载恢复点 阶段 " + resumed.currentPhase + " 包含变量数 " + resumed.stateSnapshot.size());
        }

        // 模块三 路径震荡与死循环检测治理
        System.out.println("\n第三阶段 验证工具调用指纹比对与循环震荡熔断");
        AgentReliabilityAndSecurityDemo.LoopAndOscillationDetector loopDetector =
                new AgentReliabilityAndSecurityDemo.LoopAndOscillationDetector(5);
        System.out.println("首次调用工具 结果 " + loopDetector.checkAndRecord("queryDb", "userId=101"));
        System.out.println("二次相同调用 结果 " + loopDetector.checkAndRecord("queryDb", "userId=101"));
        System.out.println("三次重复触发 结果 " + loopDetector.checkAndRecord("queryDb", "userId=101"));

        // 模块四 任务幻觉拦截与真实执行凭据校验
        System.out.println("\n第四阶段 验证任务幻觉拦截与收据验签防御");
        AgentReliabilityAndSecurityDemo.TaskExecutionGuard guard =
                new AgentReliabilityAndSecurityDemo.TaskExecutionGuard();
        String validReceipt = guard.executeRealTool("sendEmailTool", () -> System.out.println("底层真实发送邮件动作触发"));
        guard.verifyCompletion(validReceipt);
        guard.verifyCompletion("FAKE_RECEIPT_TOKEN_0000");

        // 模块五 数据库查询防越权与动态脱敏过滤
        System.out.println("\n第五阶段 验证数据库越权阻断与敏感信息脱敏");
        AgentReliabilityAndSecurityDemo.DatabaseSecurityGuardrail dbGuard =
                new AgentReliabilityAndSecurityDemo.DatabaseSecurityGuardrail();
        dbGuard.validateSql("SELECT id, name FROM users WHERE id = 1");
        dbGuard.validateSql("DELETE FROM users WHERE id = 1");
        String maskedText = dbGuard.maskSensitiveData("客户张三联系电话13812345678身份证号110101199001011234");
        System.out.println("脱敏输出结果 " + maskedText);

        // 模块六 链路 Trace 耗时定位
        System.out.println("\n第六阶段 验证全链路 Trace 耗时分布观测");
        AgentReliabilityAndSecurityDemo.AgentTraceObserver traceObserver =
                new AgentReliabilityAndSecurityDemo.AgentTraceObserver();
        traceObserver.recordSpan("意图识别模型推理", 280);
        traceObserver.recordSpan("本地规则网关校验", 5);
        traceObserver.recordSpan("数据库只读查询", 35);
        traceObserver.recordSpan("最终总结模型推理", 320);
        traceObserver.printTraceReport();

        // 模块七 上下文工程拼装
        System.out.println("\n第七阶段 验证分层上下文工程流水线");
        AgentReliabilityAndSecurityDemo.ContextEngineeringPipeline contextPipeline =
                new AgentReliabilityAndSecurityDemo.ContextEngineeringPipeline();
        String fullPrompt = contextPipeline.buildFullPrompt("你是一名架构师助手", "查询知识库工具", "用户历史偏好Java", "如何设计高可用智能体");
        System.out.println("组装后提示词长度 " + fullPrompt.length());

        // 模块八 记忆压缩提炼
        System.out.println("\n第八阶段 验证长对话记忆摘要压缩");
        AgentReliabilityAndSecurityDemo.MemoryCompressionEngine compressionEngine =
                new AgentReliabilityAndSecurityDemo.MemoryCompressionEngine();
        String summary = compressionEngine.compressHistoryBySummary(java.util.List.of("你好", "我想设计Agent", "需要高可用", "请提供方案"));
        System.out.println("压缩提炼结果 " + summary);

        // 模块九 智能体评测基准
        System.out.println("\n第九阶段 验证 Agent 评测打分体系");
        AgentReliabilityAndSecurityDemo.AgentEvaluationFramework evalFramework =
                new AgentReliabilityAndSecurityDemo.AgentEvaluationFramework();
        evalFramework.evaluateCase("用例001", true, true, 3, 640);

        System.out.println("\n进阶全量面试题核心逻辑验证完成");
    }
}
