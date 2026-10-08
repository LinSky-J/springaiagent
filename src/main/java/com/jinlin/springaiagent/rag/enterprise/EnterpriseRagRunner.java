package com.jinlin.springaiagent.rag.enterprise;

import java.util.List;

// 企业级 RAG 生产流程运行验证入口
public class EnterpriseRagRunner {

    public static void main(String[] args) {
        System.out.println("启动企业级 RAG 生产架构端到端实战验证");

        EnterpriseProductionRagPipeline.ProductionRagService service =
                new EnterpriseProductionRagPipeline.ProductionRagService();

        // 初始化模拟企业级知识库
        service.initMockKnowledge();

        // 场景一 具备权限的合法用户查询
        System.out.println("\n测试场景一 财务部员工合法请求");
        String reply1 = service.executeEnterpriseRag(
                "TENANT_ALIBABA",
                List.of("ROLE_EMPLOYEE", "ROLE_FINANCE"),
                "请问差旅报销标准是多少"
        );
        System.out.println("系统响应 \n" + reply1);

        // 场景二 越权未授权用户请求 验证安全拦截
        System.out.println("\n测试场景二 外包员工越权请求敏感知识");
        String reply2 = service.executeEnterpriseRag(
                "TENANT_ALIBABA",
                List.of("ROLE_GUEST"),
                "请问差旅报销标准是多少"
        );
        System.out.println("系统响应 \n" + reply2);

        System.out.println("\n企业级 RAG 实战验证全流程执行完毕");
    }
}
