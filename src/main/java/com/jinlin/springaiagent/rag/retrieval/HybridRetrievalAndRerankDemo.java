package com.jinlin.springaiagent.rag.retrieval;

import com.jinlin.springaiagent.rag.RagCorePipelineDemo.DocumentChunk;

import java.util.*;

// RAG 检索增强 高级召回与重排序实现类
// 涵盖第十一题至第十四题的核心实现与原理解析
public class HybridRetrievalAndRerankDemo {

    // 第十一题 向量检索与关键词检索的区别
    // 关键词检索 BM25 或 TF-IDF 基于词频与逆向文档频率 擅长精确实体 专业术语 货号 错误码等字面精准匹配
    // 向量检索 Dense Retrieval 基于高维语义空间余弦距离 擅长同义词 意图相近 表达多样化的泛化语义匹配
    // 工业界结合两者优势采用混合多路检索

    // 第十二题 Query 润色与重写的目的与方法
    // 目的在于消除用户原始查询中的指代模糊 错别字 语义缺失 缩写 以及对齐知识库专业术语
    // 方法包含 HyDE 假设性文档嵌入生成 查询多路意图扩展 回退提问 Step-Back Prompting
    public static class QueryRewriter {
        public List<String> rewriteQuery(String rawQuery) {
            System.out.println("Query 重写机制 接收原始查询 " + rawQuery);
            List<String> expandedQueries = new ArrayList<>();
            // 原始查询
            expandedQueries.add(rawQuery);
            // 意图扩充查询 补全专业术语
            expandedQueries.add(rawQuery + " 详细技术架构与核心原理解析");
            // 假设性问答扩展
            expandedQueries.add(rawQuery + " 生产实践落地选型策略");
            System.out.println("Query 扩展完成 扩充生成条数 " + expandedQueries.size());
            return expandedQueries;
        }
    }

    // 第十三题 多路召回具体实现
    // 分别从向量索引 关键词倒排索引与精确匹配通道并行检索候选集
    // 然后通过 RRF 倒数排名融合算法 或 Cross-Encoder 重排模型进行融合打分
    public static class MultiRouteRetriever {

        // 模拟倒数排名融合算法 RRF 计算综合得分
        public List<DocumentChunk> rrfFusion(List<DocumentChunk> vectorResults, List<DocumentChunk> keywordResults, int topK) {
            System.out.println("多路召回融合 启动 RRF 算法合并向量检索与关键词检索结果");
            Map<String, Double> scoreMap = new HashMap<>();
            Map<String, DocumentChunk> chunkMap = new HashMap<>();
            int k = 60; // RRF 平滑常数

            // 计算向量通路得分
            for (int rank = 0; rank < vectorResults.size(); rank++) {
                DocumentChunk chunk = vectorResults.get(rank);
                chunkMap.put(chunk.chunkId, chunk);
                scoreMap.put(chunk.chunkId, scoreMap.getOrDefault(chunk.chunkId, 0.0) + (1.0 / (k + rank + 1)));
            }

            // 计算关键词通路得分
            for (int rank = 0; rank < keywordResults.size(); rank++) {
                DocumentChunk chunk = keywordResults.get(rank);
                chunkMap.put(chunk.chunkId, chunk);
                scoreMap.put(chunk.chunkId, scoreMap.getOrDefault(chunk.chunkId, 0.0) + (1.0 / (k + rank + 1)));
            }

            // 按综合得分降序排序
            List<Map.Entry<String, Double>> sortedList = new ArrayList<>(scoreMap.entrySet());
            sortedList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

            List<DocumentChunk> finalResults = new ArrayList<>();
            for (int i = 0; i < Math.min(topK, sortedList.size()); i++) {
                finalResults.add(chunkMap.get(sortedList.get(i).getKey()));
            }

            System.out.println("多路召回合并完成 最终输出 TopK 数量 " + finalResults.size());
            return finalResults;
        }
    }

    // 第十四题 RAG 检索优化策略
    // 策略一 句子窗口检索 检索定位到精确小句子 组装时展开前后各三句完整窗口
    // 策略二 自动合并检索 子块命中率超过阈值时 自动合并为完整父块提升连贯性
    // 策略三 上下文压缩剪枝 过滤与 Query 无关的冗余填充词 降低模型上下文输入负荷
    public static class RetrievalOptimizer {
        public String expandContextWindow(String matchedSentence, String fullParagraph) {
            System.out.println("上下文窗口扩展优化 命中局部句子 展开全段落背景支持");
            return fullParagraph;
        }
    }
}
