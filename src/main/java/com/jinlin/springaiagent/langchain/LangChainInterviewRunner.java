package com.jinlin.springaiagent.langchain;

import java.util.*;

// LangChain 与 Java 平台智能体框架综合运行验证类
// 编排并端到端执行链式管道 声明式工具 反射注册 双层记忆 状态图与深度研究五大服务
public class LangChainInterviewRunner {

    // 模拟被扫描的业务工具组件
    public static class SampleBusinessTools {
        @DeclarativeAgentToolRegistryService.AgentTool(name = "queryAccountBalance", description = "查询指定用户的账户资产余额")
        public String queryAccountBalance(String userId) {
            return "用户 " + userId + " 账户可用余额为 88888 元";
        }
    }

    public static void main(String[] args) {
        System.out.println("开始执行 LangChain 及其 Java 生态企业级技术方案验证");

        // 模块一 链式管道 LCEL 管道执行验证
        EnterpriseChainPipelineService chainService = new EnterpriseChainPipelineService();
        EnterpriseChainPipelineService.ComposableChain<String, Map<String, Object>> pipeline =
                chainService.buildRAGInferenceChain(
                        input -> "系统格式化提示词 " + input,
                        prompt -> "模型推理原始输出 状态已确认 处理完毕",
                        raw -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("parsedResult", raw);
                            map.put("statusCode", 200);
                            return map;
                        }
                );
        Map<String, Object> chainResult = pipeline.invoke("用户查询请求", new HashMap<>());
        System.out.println("链式管道执行成功 解析结果 " + chainResult.get("parsedResult"));

        // 模块二 声明式工具注解扫描与反射调用验证
        DeclarativeAgentToolRegistryService toolRegistry = new DeclarativeAgentToolRegistryService();
        toolRegistry.registerToolsFromInstance(new SampleBusinessTools());
        Object toolExecResult = toolRegistry.executeTool("queryAccountBalance", "USER_1001");
        System.out.println("声明式工具注册与执行成功 返回数据 " + toolExecResult);

        // 模块三 双层记忆协同管理验证
        DualTierMemoryManagerService memoryService = new DualTierMemoryManagerService(3);
        memoryService.appendShortTermMessage("user", "你好我是张三");
        memoryService.appendShortTermMessage("assistant", "你好张三很高兴为您服务");
        memoryService.appendShortTermMessage("user", "我想了解理财产品");
        memoryService.appendShortTermMessage("assistant", "好的我们有稳健型产品");
        System.out.println("短期滑动窗口记忆容量控制完成 当前留存条数 " + memoryService.getShortTermHistory().size());

        memoryService.saveLongTermMemory("M_1", "用户张三偏好低风险稳健固收产品", new double[]{0.9, 0.1, 0.05});
        List<String> recalledMemories = memoryService.recallLongTermMemories(new double[]{0.85, 0.15, 0.0}, 0.8, 1);
        System.out.println("长期向量记忆语义回忆完成 召回内容 " + recalledMemories.get(0));

        // 模块四 状态图引擎循环调度验证
        StateGraphWorkflowEngine graphEngine = new StateGraphWorkflowEngine();
        StateGraphWorkflowEngine.GraphDefinition graph = new StateGraphWorkflowEngine.GraphDefinition();
        graph.addNode("agent_node", state -> {
            int count = (int) state.getOrDefault("loopCount", 0);
            Map<String, Object> update = new HashMap<>();
            update.put("loopCount", count + 1);
            update.put("lastAction", count < 2 ? "call_tool" : "finish");
            return update;
        });
        graph.addNode("tool_node", state -> {
            Map<String, Object> update = new HashMap<>();
            update.put("toolOutput", "已获取外部环境数据");
            return update;
        });
        graph.setEntryPoint("agent_node");
        graph.addConditionalEdge("agent_node", state -> {
            String action = (String) state.get("lastAction");
            return "call_tool".equals(action) ? "tool_node" : StateGraphWorkflowEngine.END_NODE;
        });
        graph.addEdge("tool_node", "agent_node");

        Map<String, Object> finalGraphState = graphEngine.runWorkflow(graph, new HashMap<>(), 10);
        System.out.println("状态图循环执行完成 最终循环步数 " + finalGraphState.get("loopCount"));

        // 模块五 深度研究协同工作流验证
        DeepResearchWorkflowCoordinator researchCoordinator = new DeepResearchWorkflowCoordinator();
        DeepResearchWorkflowCoordinator.DeepResearchReport report =
                researchCoordinator.executeDeepResearch("大模型落地金融行业风控体系", 2);
        System.out.println("深度研究流程执行完成 迭代轮次 " + report.iterationCount + " 搜集事实证据数 " + report.gatheredEvidence.size());

        System.out.println("LangChain 与 Java 平台全部技术验证案例运行成功");
    }
}
