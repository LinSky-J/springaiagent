package com.jinlin.springaiagent.rag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// RAG 核心流水线与基础分块存储实现类
// 涵盖第一题至第十题的核心原理与代码设计
public class RagCorePipelineDemo {

    // 文档切片数据实体
    public static class DocumentChunk {
        public final String chunkId;
        public final String docId;
        public final String content;
        public final Map<String, Object> metadata;
        public float[] embedding;

        public DocumentChunk(String chunkId, String docId, String content, Map<String, Object> metadata) {
            this.chunkId = chunkId;
            this.docId = docId;
            this.content = content;
            this.metadata = metadata;
        }
    }

    // 第四题与第五题 文档切割策略与避免语义切断机制
    // 切割策略包含 固定字符长度加重叠窗口 按标点段落切分 按 Markdown 标题层级切分 以及父子块 Small-to-Big 切分
    // 规避语义截断通过设置 Overlap 滑动重叠窗口 以及保留父块完整上下文解决
    public static class DocumentChunker {
        private final int chunkSize;
        private final int overlapSize;

        public DocumentChunker(int chunkSize, int overlapSize) {
            this.chunkSize = chunkSize;
            this.overlapSize = overlapSize;
        }

        // 滑动窗口分块演示
        public List<DocumentChunk> splitWithOverlap(String docId, String rawText) {
            List<DocumentChunk> chunks = new ArrayList<>();
            int textLength = rawText.length();
            int start = 0;
            int index = 1;

            while (start < textLength) {
                int end = Math.min(start + chunkSize, textLength);
                String sub = rawText.substring(start, end);
                Map<String, Object> meta = new HashMap<>();
                meta.put("offsetStart", start);
                meta.put("offsetEnd", end);
                chunks.add(new DocumentChunk(docId + "_chunk_" + index++, docId, sub, meta));

                if (end == textLength) {
                    break;
                }
                // 滑动窗口前进 步长等于块大小减去重叠大小
                start += (chunkSize - overlapSize);
            }
            return chunks;
        }
    }

    // 第六题与第七题 Embedding 原理与向量存储模拟
    // 向量是文本在高维稠密几何空间的数学投影 算法包括 Word2Vec TF-IDF Dense BERT 稠密向量
    // 相似度通过余弦相似度或内积计算
    public static class InMemoryVectorStore {
        private final List<DocumentChunk> storage = new ArrayList<>();

        public void addChunks(List<DocumentChunk> chunks) {
            storage.addAll(chunks);
            System.out.println("向量数据库 成功写入分块数量 " + chunks.size());
        }

        // 模拟基于关键词加伪语义向量检索
        public List<DocumentChunk> searchTopK(String query, int k) {
            List<DocumentChunk> matched = new ArrayList<>();
            for (DocumentChunk chunk : storage) {
                if (chunk.content.contains(query) || query.contains(chunk.content.substring(0, Math.min(4, chunk.content.length())))) {
                    matched.add(chunk);
                    if (matched.size() >= k) {
                        break;
                    }
                }
            }
            if (matched.isEmpty() && !storage.isEmpty()) {
                matched.add(storage.get(0));
            }
            return matched;
        }
    }

    // 第一题与第十题 完整 RAG 问答流程驱动引擎
    public static class RagEngine {
        private final InMemoryVectorStore vectorStore;

        public RagEngine(InMemoryVectorStore vectorStore) {
            this.vectorStore = vectorStore;
        }

        // 问答全流程 执行 检索 提示词增强 最终生成
        public String executeRag(String userQuery) {
            System.out.println("RAG 流程第一步 接收用户输入 查询为 " + userQuery);

            System.out.println("RAG 流程第二步 执行向量化检索 召回强相关文档切片");
            List<DocumentChunk> relevantChunks = vectorStore.searchTopK(userQuery, 2);

            System.out.println("RAG 流程第三步 组装增强提示词 注入检索上下文");
            StringBuilder contextBuilder = new StringBuilder();
            for (DocumentChunk chunk : relevantChunks) {
                contextBuilder.append("文档片段来源 ").append(chunk.chunkId).append(" 内容 ").append(chunk.content).append("\n");
            }

            String augmentedPrompt = "请参考以下上下文知识回答问题\n" + contextBuilder.toString() + "\n用户问题 " + userQuery;
            System.out.println("增强后注入提示词 \n" + augmentedPrompt);

            System.out.println("RAG 流程第四步 驱动大语言模型生成可信回答");
            return "基于知识库明确回答 " + relevantChunks.get(0).content;
        }
    }
}
