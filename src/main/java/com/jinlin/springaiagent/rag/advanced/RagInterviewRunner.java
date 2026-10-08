package com.jinlin.springaiagent.rag.advanced;

import com.jinlin.springaiagent.rag.RagCorePipelineDemo;
import com.jinlin.springaiagent.rag.retrieval.HybridRetrievalAndRerankDemo;

import java.util.List;

// RAG 全流程面试题综合运行验证入口类
// 串联运行并验证分块切割 向量存储 混合召回 RRF 融合 知识图谱 防幻觉评测与冲突仲裁
public class RagInterviewRunner {

    public static void main(String[] args) {
        System.out.println("开始执行 RAG 全链路核心面试题代码全量验证");

        // 模块一 验证文档分块切割与滑动窗口 Overlap 机制
        System.out.println("\n第一阶段 验证文档分块与 Overlap 滑动窗口");
        RagCorePipelineDemo.DocumentChunker chunker = new RagCorePipelineDemo.DocumentChunker(20, 5);
        String sampleText = "Spring AI 是一个用于构建现代人工智能应用的开发框架 它融合了大模型交互与企业级架构能力";
        List<RagCorePipelineDemo.DocumentChunk> chunks = chunker.splitWithOverlap("doc_01", sampleText);
        System.out.println("分块总数 " + chunks.size() + " 首块内容 " + chunks.get(0).content);

        // 模块二 验证基础 RAG 检索增强全流程
        System.out.println("\n第二阶段 验证 RAG 检索 注入与增强生成闭环");
        RagCorePipelineDemo.InMemoryVectorStore vectorStore = new RagCorePipelineDemo.InMemoryVectorStore();
        vectorStore.addChunks(chunks);
        RagCorePipelineDemo.RagEngine ragEngine = new RagCorePipelineDemo.RagEngine(vectorStore);
        String ragAnswer = ragEngine.executeRag("Spring AI 框架特点");
        System.out.println("RAG 问答结果 " + ragAnswer);

        // 模块三 验证 Query 重写与多路召回 RRF 融合
        System.out.println("\n第三阶段 验证 Query 重写扩展与多路 RRF 融合召回");
        HybridRetrievalAndRerankDemo.QueryRewriter rewriter = new HybridRetrievalAndRerankDemo.QueryRewriter();
        List<String> queries = rewriter.rewriteQuery("如何选型向量数据库");

        HybridRetrievalAndRerankDemo.MultiRouteRetriever retriever = new HybridRetrievalAndRerankDemo.MultiRouteRetriever();
        List<RagCorePipelineDemo.DocumentChunk> fused = retriever.rrfFusion(chunks, chunks, 2);
        System.out.println("RRF 最终胜出分块数 " + fused.size());

        // 模块四 验证 Graph RAG 知识图谱多跳关系
        System.out.println("\n第四阶段 验证 Graph RAG 实体关系图谱推导");
        AdvancedRagAndEvaluationDemo.GraphRagKnowledgeGraph graph = new AdvancedRagAndEvaluationDemo.GraphRagKnowledgeGraph();
        graph.addRelation("小米公司", "自研大模型", "MiMo");
        graph.addRelation("MiMo", "支持能力", "函数调用与智能体");
        List<String> hops = graph.queryMultiHop("小米公司");
        System.out.println("多跳推导关系数 " + hops.size());

        // 模块五 验证防幻觉校验与 RAG Triad 量化评测
        System.out.println("\n第五阶段 验证防幻觉安全防线与量化评测体系");
        AdvancedRagAndEvaluationDemo.HallucinationGuardrail guardrail = new AdvancedRagAndEvaluationDemo.HallucinationGuardrail();
        guardrail.verifyContextRelevance(0.85);
        guardrail.verifyContextRelevance(0.40);

        AdvancedRagAndEvaluationDemo.RagEvaluator evaluator = new AdvancedRagAndEvaluationDemo.RagEvaluator();
        AdvancedRagAndEvaluationDemo.RagEvaluator.RagEvaluationScore score =
                evaluator.evaluateRagPipeline("Query", "Context", "Answer");
        System.out.println("评测结果 上下文相关度 " + score.contextRelevance + " 忠实度 " + score.groundedness + " 答案相关度 " + score.answerRelevance);

        // 模块六 验证多源知识冲突加权仲裁
        System.out.println("\n第六阶段 验证多源知识冲突加权仲裁机制");
        AdvancedRagAndEvaluationDemo.KnowledgeConflictArbitrator arbitrator =
                new AdvancedRagAndEvaluationDemo.KnowledgeConflictArbitrator();
        List<AdvancedRagAndEvaluationDemo.KnowledgeConflictArbitrator.SourcedDocument> conflictDocs = List.of(
                new AdvancedRagAndEvaluationDemo.KnowledgeConflictArbitrator.SourcedDocument("老版部门规章", 60, 1600000000000L, "差旅标准每日两百元"),
                new AdvancedRagAndEvaluationDemo.KnowledgeConflictArbitrator.SourcedDocument("最新集团红头文件", 95, 1700000000000L, "差旅标准每日四百元")
        );
        arbitrator.arbitrate(conflictDocs);

        System.out.println("\nRAG 全链路核心机制验证执行完成");
    }
}
