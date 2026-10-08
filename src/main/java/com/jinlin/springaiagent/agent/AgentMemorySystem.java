package com.jinlin.springaiagent.agent;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 面试题第三部分 记忆系统架构设计
// 涵盖第八题 第九题 的代码定义与原理解析
public class AgentMemorySystem {

    // 第八题 AI Agent 的记忆机制与实际开发中的记忆模块设计
    // 记忆分类体系分为三大层次
    // 感觉工作记忆 即当前单次请求正在处理的上下文窗口 极速读写 随调用结束而销毁
    // 短期记忆 维护连续多轮对话的上下文状态 解决会话连续性与指代消解问题
    // 长期记忆 跨越多次独立会话保留关键事实 用户偏好 专业知识与操作经验
    // 实际开发中的记忆模块架构设计
    // 存储分层 内存高速缓存加关系型数据库持久化加向量数据库语义检索
    // 写入流水线 包含异步消息捕获 敏感信息脱敏 关键实体抽取 结构化提炼 向量化入库
    // 读取流水线 包含精确过滤加向量相似度计算加重排序加动态Prompt组装

    // 第九题 Agent 的长短期记忆系统怎么做的 记忆怎么存 粒度是多少 怎么用的
    // 短期记忆实现方式
    // 滑动窗口策略 基于固定轮次保留最近的对话记录 例如保留最近十轮
    // 动态 Token 预算策略 根据剩余上下文配额动态修剪历史记录
    // 摘要压缩策略 当轮次超过阈值时 调用大模型将历史记录压缩为一段摘要并置顶
    // 长期记忆实现方式
    // 存储介质 采用关系型数据库存储事实属性 采用向量库存储非结构化经验与语义文本
    // 记忆粒度分为四级
    // 第一级 原始对话轮次粒度 包含完整问答问答内容 适合近期精确回溯
    // 第二级 事件与事实三元组粒度 抽取主体属性值例如用户喜好某种技术栈 存储为键值对
    // 第三级 实体画像粒度 将零散事实聚合成用户或系统全局画像
    // 第四级 场景经验摘要粒度 将历史成功或失败的处理过程归纳为知识点
    // 记忆的使用全生命周期
    // 存储阶段 在每次交互结束后异步后台执行事实抽取与特征沉淀
    // 检索阶段 根据当前用户的输入意图 并行检索关联画像与相似历史经验
    // 注入阶段 将检索出的最相关前序记忆拼接为系统指令注入模型完成回答

    // 短期记忆管理器
    public static class ShortTermMemoryManager {
        private final ChatMemory chatMemory;

        public ShortTermMemoryManager(int windowSize) {
            // 使用内存存储实现滑动窗口短期记忆
            this.chatMemory = MessageWindowChatMemory.builder()
                    .chatMemoryStore(new InMemoryChatMemoryStore())
                    .maxMessages(windowSize)
                    .build();
        }

        public void addUserMessage(String message) {
            chatMemory.add(UserMessage.from(message));
        }

        public void addAiMessage(String message) {
            chatMemory.add(AiMessage.from(message));
        }

        public List<ChatMessage> getMessages() {
            return chatMemory.messages();
        }
    }

    // 长期记忆知识实体与画像存储库
    public static class LongTermMemoryStore {
        // 用户画像维度记忆 以用户标识为维度保存静态属性与习惯偏好
        private final Map<String, Map<String, String>> userProfileMemory = new HashMap<>();

        // 语义经验片段库 模拟向量数据库语义检索
        private final List<MemorySnippet> episodicMemory = new ArrayList<>();

        public static class MemorySnippet {
            public final String memoryId;
            public final String category;
            public final String content;

            public MemorySnippet(String memoryId, String category, String content) {
                this.memoryId = memoryId;
                this.category = category;
                this.content = content;
            }
        }

        // 保存用户画像偏好 事实级粒度
        public void saveUserProfile(String userId, String key, String value) {
            userProfileMemory.computeIfAbsent(userId, k -> new HashMap<>()).put(key, value);
            System.out.println("长期记忆写入 保存用户画像 标识 " + userId + " 属性 " + key + " 取值 " + value);
        }

        // 检索用户画像记忆
        public Map<String, String> getUserProfile(String userId) {
            return userProfileMemory.getOrDefault(userId, new HashMap<>());
        }

        // 沉淀历史场景经验 经验级粒度
        public void saveEpisodeMemory(String memoryId, String category, String content) {
            episodicMemory.add(new MemorySnippet(memoryId, category, content));
            System.out.println("长期记忆写入 经验沉淀 类别 " + category + " 内容 " + content);
        }

        // 根据关键词匹配相关经验 模拟语义检索过程
        public List<MemorySnippet> searchRelevantMemories(String keyword) {
            List<MemorySnippet> results = new ArrayList<>();
            for (MemorySnippet snippet : episodicMemory) {
                if (snippet.content.contains(keyword)) {
                    results.add(snippet);
                }
            }
            return results;
        }
    }
}
