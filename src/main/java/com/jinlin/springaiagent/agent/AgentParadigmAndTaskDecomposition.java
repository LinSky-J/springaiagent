package com.jinlin.springaiagent.agent;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

// 面试题第二部分 设计范式与任务拆解
// 涵盖第四题 第六题 第七题 的代码定义与原理解析
public class AgentParadigmAndTaskDecomposition {

    // 第四题 常见的 Agent 设计范式及 Agent 与 Workflow 的区别
    // 常见设计范式包括
    // 单智能体模式 聚焦单一领域或工具的闭环交互
    // 路由分发模式 通过意图识别模型将用户输入分流到特定的专家节点
    // 编排分发模式 主控智能体统领多个专业工作者智能体并行或串行协同
    // 评估反思模式 生成器与评判器形成对抗演化机制持续打磨结果
    // Agent 与 Workflow 的区别核心在于决策权归属
    // Workflow 决策权在代码设计者 手动固定有向无环图分支 容错低 适合标准作业程序
    // Agent 决策权在大模型大脑 运行时动态规划分支路径 具备自适应探索能力

    // 第六题 ReAct 与 Plan-and-Execute 与 Reflection 三种范式核心区别及项目选型
    // ReAct 单步规划单步执行 适合工具联动强 实时依赖外部最新数据的动态环境探索
    // 缺点是长链条容易迷失方向 容易陷入局部死循环
    // Plan-and-Execute 先宏观生成计划 随后由执行器逐步完成各步骤 最后反思汇总
    // 优点是全局目标感极强 能够拆解庞大任务 避免中途跑偏
    // Reflection 包含生成模块与批评校验模块 循环打磨直至达到预设标准
    // 适合代码审查 法律文书润色 高准确率要求的严谨内容输出
    // 工业界选型实践为 复合型架构 例如外层使用 Plan-and-Execute 负责全局任务切分
    // 每个子步骤内部使用 ReAct 负责工具调用交互 最后通过 Reflection 机制进行质量验收

    // 第七题 复杂任务怎么做任务拆分 为什么要拆分 效果如何提升
    // 为什么要拆分
    // 大模型长上下文存在注意力衰减问题 复杂指令一次性输入极易产生幻觉与信息遗漏
    // 拆分能够降低单个提示词的认知负荷 将不确定的大问题转换为确定的原子步骤
    // 怎么拆分
    // 第一步 目标解构 抽取主任务的核心交付物
    // 第二步 依赖拓扑排序 构建有向无环图 区分必须串行的步骤与可并行的步骤
    // 第三步 上下文隔离 每个子步骤只传递必须的上下文 消除无关信息干扰
    // 效果如何提升
    // 步骤一 引入前置校验器与后置审查器 确保单个子步骤产出质量
    // 步骤二 支持失败重试与动态重规划 如果某个步骤返回异常 触发局部重试而非全部重来
    // 步骤三 关键决策点设置人机协同确认机制 保障工程落地安全

    // 任务单元数据结构
    public static class SubTask {
        public final int stepId;
        public final String taskName;
        public final String taskGoal;
        public String status;
        public String result;

        public SubTask(int stepId, String taskName, String taskGoal) {
            this.stepId = stepId;
            this.taskName = taskName;
            this.taskGoal = taskGoal;
            this.status = "待执行";
            this.result = "";
        }
    }

    // 计划执行器演示
    public static class PlanAndExecuteEngine {
        private final ChatLanguageModel chatModel;

        public PlanAndExecuteEngine() {
            this.chatModel = OpenAiChatModel.builder()
                    .baseUrl("https://api.xiaomimimo.com/v1")
                    .apiKey("sk-c60ttpa5pcetez3ybbjw1ihzfh9zoqwi5rt1cjaw6hwy7afb")
                    .modelName("mimo-v2.6-flash")
                    .timeout(Duration.ofSeconds(20))
                    .maxTokens(50)
                    .temperature(0.2)
                    .build();
        }

        // 任务分解规划器
        public List<SubTask> plan(String complexGoal) {
            System.out.println("规划阶段 开始对复杂任务进行分解 任务目标 " + complexGoal);
            List<SubTask> taskList = new ArrayList<>();
            taskList.add(new SubTask(1, "收集输入需求与依赖项", "明确输入参数完整度与外部数据源"));
            taskList.add(new SubTask(2, "执行核心业务逻辑与数据加工", "调用业务接口或算法处理数据"));
            taskList.add(new SubTask(3, "输出最终综合报告与质量校验", "合并所有子步骤输出并进行结构化输出"));
            return taskList;
        }

        // 执行与反馈迭代
        public String executePlan(List<SubTask> taskList) {
            StringBuilder executionLog = new StringBuilder();
            for (SubTask task : taskList) {
                System.out.println("执行阶段 正在执行步骤 " + task.stepId + " " + task.taskName);
                String subPrompt = "请用十个字以内概述如何执行 " + task.taskGoal;
                String subResult = chatModel.generate(subPrompt);
                task.status = "已完成";
                task.result = subResult.trim();
                executionLog.append("步骤 ").append(task.stepId).append(" 完成 结果 ").append(task.result).append("\n");
            }
            return executionLog.toString();
        }
    }

    // 反思评估器演示
    public static class ReflectionEngine {
        private final ChatLanguageModel chatModel;

        public ReflectionEngine() {
            this.chatModel = OpenAiChatModel.builder()
                    .baseUrl("https://api.xiaomimimo.com/v1")
                    .apiKey("sk-c60ttpa5pcetez3ybbjw1ihzfh9zoqwi5rt1cjaw6hwy7afb")
                    .modelName("mimo-v2.6-flash")
                    .timeout(Duration.ofSeconds(60))
                    .temperature(0.3)
                    .build();
        }

        // 生成 评审 修正闭环
        public String runReflectionLoop(String prompt) {
            System.out.println("反思阶段 第一轮 初始内容生成");
            String draft = chatModel.generate("请根据以下需求生成初始方案 " + prompt);

            System.out.println("反思阶段 第二轮 评估审查生成内容");
            String critique = chatModel.generate("请作为资深技术专家 严格审查以下方案并指出不足 " + draft);

            System.out.println("反思阶段 第三轮 结合改进意见进行最终打磨");
            return chatModel.generate("请根据审查建议 优化重构最终方案 原草稿 " + draft + " 改进意见 " + critique);
        }
    }
}
