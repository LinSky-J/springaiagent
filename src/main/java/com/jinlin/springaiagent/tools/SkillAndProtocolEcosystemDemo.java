package com.jinlin.springaiagent.tools;

import java.util.ArrayList;
import java.util.List;

// 面试题第二部分 Skill 与前沿通信协议生态实现类
// 涵盖第九题至第十五题的核心定义与协议对比
public class SkillAndProtocolEcosystemDemo {

    // 第九题至第十一题 Skill 概念及与 FC 和 MCP 的定位对比
    // Skill 是将特定业务领域的专用提示词 最佳实践工作流 依赖脚本以及工具组合封装成的复合技能包
    // 三者定位区别
    // Function Calling 是模型供应商层面的原子接口入参提取能力 属于最底层的积木块
    // MCP 是跨平台跨进程上下文与工具连接的开放标准协议 属于通用的总线层
    // Skill 是业务领域面向特定任务的复合技能规约封装 属于应用层的复合能力单元
    public static class AgentSkillBundle {
        public final String skillName;
        public final String workflowInstructions;
        public final List<String> requiredTools;

        public AgentSkillBundle(String skillName, String workflowInstructions, List<String> requiredTools) {
            this.skillName = skillName;
            this.workflowInstructions = workflowInstructions;
            this.requiredTools = requiredTools;
        }

        public void activateSkill() {
            System.out.println("加载 Agent Skill 技能包 " + skillName + " 注入专业业务工作流与前置规则");
        }
    }

    // 第十二题 A2A 协议及与 MCP 的区别
    // A2A 即 Agent-to-Agent 智能体间协议 用于解决不同异构智能体之间的身份认证 意图握手 任务协商与利益对齐
    // MCP 关注的是 单个智能体 与 外部工具数据源 的上下游连接协议 属于纵向主从连接
    // A2A 关注的是 多个自主智能体 之间的水平通信协同与共识交互协议 属于横向对等连接
    public static class AgentToAgentProtocol {
        public void negotiateTask(String agentA, String agentB, String taskDescription) {
            System.out.println("A2A 协议握手 智能体 " + agentA + " 与智能体 " + agentB + " 协商协作任务 " + taskDescription);
        }
    }

    // 第十三题至第十五题 现代 AI 通信传输层选型对比
    // SSE 基于 HTTP 单向长连接 服务端持续推送文本片段 简单且天然兼容 HTTP2 与反向代理 局限在于无法双向交互
    // WebSocket 基于 TCP 全双工双向通信 适合高频双向文字指令交互 局限在于存在应用层队头阻塞 复杂网络环境穿透成本高
    // WebRTC 基于 UDP 与 RTP 协议 实现端到端毫秒级超低延迟媒体传输
    // WebRTC 专为实时语音对话端到端流式打断设计 彻底消除 TCP 重传等待延迟
    public static class StreamingProtocolEvaluator {
        public enum ProtocolType {
            SSE, WEBSOCKET, WEBRTC
        }

        public void evaluateProtocol(ProtocolType type) {
            switch (type) {
                case SSE -> System.out.println("协议选型 SSE 适用于常规大模型文本流式生成 单向推送 成本低");
                case WEBSOCKET -> System.out.println("协议选型 WebSocket 适用于双向复杂控制指令交互 全双工长连接");
                case WEBRTC -> System.out.println("协议选型 WebRTC 适用于实时端到端多模态语音交互 毫秒级抗丢包低延迟 支持即时插话打断");
            }
        }
    }
}
