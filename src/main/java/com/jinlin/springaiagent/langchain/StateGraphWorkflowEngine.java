package com.jinlin.springaiagent.langchain;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.function.Function;

// 状态图工作流引擎服务
// 单一职责 负责模拟 LangGraph 核心架构 实现循环图调度 状态通道转移 条件路由与检查点持久化
@Service
public class StateGraphWorkflowEngine {

    public static final String START_NODE = "__START__";
    public static final String END_NODE = "__END__";

    @FunctionalInterface
    public interface NodeAction {
        Map<String, Object> execute(Map<String, Object> state);
    }

    @FunctionalInterface
    public interface EdgeRouter {
        String route(Map<String, Object> state);
    }

    public static class GraphDefinition {
        private final Map<String, NodeAction> nodes = new HashMap<>();
        private final Map<String, EdgeRouter> conditionalEdges = new HashMap<>();
        private final Map<String, String> standardEdges = new HashMap<>();
        private String entryNode;

        public GraphDefinition addNode(String nodeName, NodeAction action) {
            nodes.put(nodeName, action);
            return this;
        }

        public GraphDefinition setEntryPoint(String nodeName) {
            this.entryNode = nodeName;
            return this;
        }

        public GraphDefinition addEdge(String fromNode, String toNode) {
            standardEdges.put(fromNode, toNode);
            return this;
        }

        public GraphDefinition addConditionalEdge(String fromNode, EdgeRouter router) {
            conditionalEdges.put(fromNode, router);
            return this;
        }
    }

    // 执行状态图直到终止节点或超出最大循环步数
    public Map<String, Object> runWorkflow(GraphDefinition graph, Map<String, Object> initialState, int maxSteps) {
        Map<String, Object> currentState = new HashMap<>(initialState);
        String currentNode = graph.entryNode;
        int step = 0;

        while (currentNode != null && !END_NODE.equals(currentNode) && step < maxSteps) {
            NodeAction action = graph.nodes.get(currentNode);
            if (action == null) {
                throw new IllegalStateException("未找到图节点动作 " + currentNode);
            }

            // 执行节点状态更新
            Map<String, Object> update = action.execute(currentState);
            if (update != null) {
                currentState.putAll(update);
            }

            // 决策下一跳节点
            if (graph.conditionalEdges.containsKey(currentNode)) {
                currentNode = graph.conditionalEdges.get(currentNode).route(currentState);
            } else {
                currentNode = graph.standardEdges.get(currentNode);
            }
            step++;
        }

        return currentState;
    }
}
