package com.jinlin.springaiagent.langchain;

import org.springframework.stereotype.Service;
import java.util.*;

// 智能体双层记忆协同管理服务
// 单一职责 负责短期滑动窗口对话缓冲与长期向量知识库语义回忆的协同管理
@Service
public class DualTierMemoryManagerService {

    public static class ChatMessage {
        public final String role;
        public final String content;
        public final long timestamp;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public static class LongTermMemoryEntry {
        public final String memoryId;
        public final String memoryContent;
        public final double[] embedding;

        public LongTermMemoryEntry(String memoryId, String memoryContent, double[] embedding) {
            this.memoryId = memoryId;
            this.memoryContent = memoryContent;
            this.embedding = embedding;
        }
    }

    private final int maxShortTermMessages;
    private final List<ChatMessage> shortTermBuffer = new ArrayList<>();
    private final List<LongTermMemoryEntry> longTermStore = new ArrayList<>();

    public DualTierMemoryManagerService() {
        this(6);
    }

    public DualTierMemoryManagerService(int maxShortTermMessages) {
        this.maxShortTermMessages = maxShortTermMessages;
    }

    // 追加短期对话记忆并自动维护窗口容量
    public synchronized void appendShortTermMessage(String role, String content) {
        shortTermBuffer.add(new ChatMessage(role, content));
        while (shortTermBuffer.size() > maxShortTermMessages) {
            shortTermBuffer.remove(0);
        }
    }

    public synchronized List<ChatMessage> getShortTermHistory() {
        return new ArrayList<>(shortTermBuffer);
    }

    // 存储长期记忆条目
    public synchronized void saveLongTermMemory(String id, String content, double[] embedding) {
        longTermStore.add(new LongTermMemoryEntry(id, content, embedding));
    }

    // 基于输入向量检索最相关的长期事实记忆
    public synchronized List<String> recallLongTermMemories(double[] queryEmbedding, double similarityThreshold, int topK) {
        List<Map.Entry<LongTermMemoryEntry, Double>> scored = new ArrayList<>();

        for (LongTermMemoryEntry entry : longTermStore) {
            double sim = computeCosineSimilarity(queryEmbedding, entry.embedding);
            if (sim >= similarityThreshold) {
                scored.add(new AbstractMap.SimpleEntry<>(entry, sim));
            }
        }

        scored.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        List<String> results = new ArrayList<>();
        int limit = Math.min(topK, scored.size());
        for (int i = 0; i < limit; i++) {
            results.add(scored.get(i).getKey().memoryContent);
        }
        return results;
    }

    private double computeCosineSimilarity(double[] v1, double[] v2) {
        double dot = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        if (norm1 <= 0.0 || norm2 <= 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
