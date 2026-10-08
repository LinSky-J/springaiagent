package com.jinlin.springaiagent.tools;

import java.util.HashMap;
import java.util.List;

// 工具调用与通信协议面试题综合运行验证入口类
// 串联验证 Function Calling 流程 MCP 协议抽象 Skill 技能包 动态工具路由与自愈容错
public class ToolCallingInterviewRunner {

    public static void main(String[] args) {
        System.out.println("开始执行工具调用与 MCP 核心机制实战验证");

        // 模块一 Function Calling 注册与分发验证
        System.out.println("\n第一阶段 验证 Function Calling 工具调度闭环");
        FunctionCallingAndMcpDemo.FunctionCallingDispatcher dispatcher =
                new FunctionCallingAndMcpDemo.FunctionCallingDispatcher();
        dispatcher.registerTool(new FunctionCallingAndMcpDemo.FunctionCallingDefinition(
                "queryStockPrice", "查询股票实时价格", new HashMap<>()
        ));
        String fcResult = dispatcher.dispatchToolExecution("queryStockPrice", "symbol=XIAOMI");
        System.out.println("调度执行结果 " + fcResult);

        // 模块二 MCP 协议服务端与客户端适配验证
        System.out.println("\n第二阶段 验证 MCP 协议三原语与客户端通信");
        FunctionCallingAndMcpDemo.McpServerSpecification mcpServer =
                new FunctionCallingAndMcpDemo.McpServerSpecification();
        mcpServer.exposeResource("file:///system/config", "只读系统配置");
        mcpServer.exposePromptTemplate("financialAuditPrompt");
        mcpServer.exposeTool("executePaymentTransaction");

        FunctionCallingAndMcpDemo.McpClientProtocolAdapter mcpClient =
                new FunctionCallingAndMcpDemo.McpClientProtocolAdapter();
        mcpClient.connectMcpServer("http://127.0.0.1:8080/mcp/sse", "SSE");

        // 模块三 Agent Skill 复合技能包与 A2A 协议验证
        System.out.println("\n第三阶段 验证 Agent Skill 技能封装与 A2A 协商机制");
        SkillAndProtocolEcosystemDemo.AgentSkillBundle skillBundle =
                new SkillAndProtocolEcosystemDemo.AgentSkillBundle(
                        "CodeReviewSkill", "严格遵循 clean code 规范审查代码", List.of("gitDiffTool", "linterTool")
                );
        skillBundle.activateSkill();

        SkillAndProtocolEcosystemDemo.AgentToAgentProtocol a2a =
                new SkillAndProtocolEcosystemDemo.AgentToAgentProtocol();
        a2a.negotiateTask("产品经理Agent", "开发工程师Agent", "迭代电商购物车高可用改造");

        // 模块四 传输层协议选型测试
        System.out.println("\n第四阶段 验证流式传输层协议特性");
        SkillAndProtocolEcosystemDemo.StreamingProtocolEvaluator protocolEvaluator =
                new SkillAndProtocolEcosystemDemo.StreamingProtocolEvaluator();
        protocolEvaluator.evaluateProtocol(SkillAndProtocolEcosystemDemo.StreamingProtocolEvaluator.ProtocolType.SSE);
        protocolEvaluator.evaluateProtocol(SkillAndProtocolEcosystemDemo.StreamingProtocolEvaluator.ProtocolType.WEBRTC);

        // 模块五 动态工具路由与 Token 剪枝验证
        System.out.println("\n第五阶段 验证海量工具动态语义路由");
        ToolRoutingAndFaultToleranceDemo.DynamicToolRouter router =
                new ToolRoutingAndFaultToleranceDemo.DynamicToolRouter();
        router.registerTool("weatherQueryTool", "查询各大城市天气预报与气温");
        router.registerTool("currencyExchangeTool", "查询全球货币汇率并换算");
        router.registerTool("flightBookingTool", "预订国内国际机票与航班查询");

        List<String> selected = router.selectTopKTools("我想订一张去北京的机票", 1);
        System.out.println("语义路由命中核心工具 " + selected);

        // 模块六 工具调用参数错误自愈与容错验证
        System.out.println("\n第六阶段 验证参数缺失与错误回灌自愈闭环");
        ToolRoutingAndFaultToleranceDemo.ToolExecutionFaultToleranceEngine faultEngine =
                new ToolRoutingAndFaultToleranceDemo.ToolExecutionFaultToleranceEngine();
        String healedResult = faultEngine.executeWithSelfHealing("secureBankApi", "account=622200", 3);
        System.out.println("自愈修复最终结果 " + healedResult);

        System.out.println("\n工具调用与 MCP 核心面试题代码验证全部完成");
    }
}
