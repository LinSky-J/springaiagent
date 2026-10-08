package com.jinlin.springaiagent.tools;

import java.util.*;

// 面试题第一部分 Function Calling 与 MCP 核心原理实现类
// 涵盖第一题至第八题的核心机制定义与架构对比
public class FunctionCallingAndMcpDemo {

    // 第一题至第三题 Function Calling 原理与执行闭环
    // 原理是客户端向模型提交符合 JSON Schema 规范的工具描述
    // 模型根据语义推断当前需要调用的函数名与入参 并生成结构化的调用指令
    // 客户端拦截该指令并在本地环境发起真实调用 将执行输出封装为 tool 消息再次发给大模型
    public static class FunctionCallingDefinition {
        public final String functionName;
        public final String description;
        public final Map<String, Object> jsonSchemaParameters;

        public FunctionCallingDefinition(String functionName, String description, Map<String, Object> jsonSchemaParameters) {
            this.functionName = functionName;
            this.description = description;
            this.jsonSchemaParameters = jsonSchemaParameters;
        }
    }

    // 模拟 Function Calling 客户端处理流水线
    public static class FunctionCallingDispatcher {
        private final Map<String, FunctionCallingDefinition> tools = new HashMap<>();

        public void registerTool(FunctionCallingDefinition def) {
            tools.put(def.functionName, def);
            System.out.println("Function Calling 注册工具 " + def.functionName + " 说明 " + def.description);
        }

        // 处理大模型返回的工具调用意图
        public String dispatchToolExecution(String targetTool, String argumentsJson) {
            System.out.println("模型生成结构化调用目标 " + targetTool + " 传递参数 " + argumentsJson);
            if (!tools.containsKey(targetTool)) {
                return "错误 未知工具调用 " + targetTool;
            }
            return "本地执行完成 真实业务结果 数据正常返回";
        }
    }

    // 第四题与第五题 MCP 模型上下文协议核心三要素
    // MCP 由 Anthropic 提出 作为标准化客户端与服务端上下文交换协议
    // 核心三大原语包括
    // 原语一 Resources 资源 只读数据或上下文 如文件内容 数据库元数据 供模型读取
    // 原语二 Prompts 提示词模板 预先封装好的动态交互模板
    // 原语三 Tools 工具 具备副作用的实际可执行函数或业务接口
    public static class McpServerSpecification {
        private final List<String> resources = new ArrayList<>();
        private final List<String> prompts = new ArrayList<>();
        private final List<String> tools = new ArrayList<>();

        public void exposeResource(String resourceUri, String content) {
            resources.add(resourceUri);
            System.out.println("MCP 服务端暴露 Resource 资源 " + resourceUri);
        }

        public void exposePromptTemplate(String promptName) {
            prompts.add(promptName);
            System.out.println("MCP 服务端暴露 Prompt 提示词模板 " + promptName);
        }

        public void exposeTool(String toolName) {
            tools.add(toolName);
            System.out.println("MCP 服务端暴露 Tool 执行工具 " + toolName);
        }
    }

    // 第六题至第八题 MCP 与 Function Calling 的架构定位差异
    // Function Calling 是模型供应商特定的单次函数调用参数生成机制 紧密耦合于特定 API 格式
    // MCP 则是跨进程 跨服务 跨语言的通用客户端与服务端标准协议
    // 类似于大模型领域的 USB 接口规范 一次编写可供任何兼容 MCP 的客户端即插即用
    public static class McpClientProtocolAdapter {
        public void connectMcpServer(String serverEndpoint, String transportType) {
            System.out.println("MCP 客户端建立连接 目标地址 " + serverEndpoint + " 通信通道 " + transportType);
            System.out.println("MCP 协议握手完成 协商协议版本并获取资源列表与工具清单");
        }
    }
}
