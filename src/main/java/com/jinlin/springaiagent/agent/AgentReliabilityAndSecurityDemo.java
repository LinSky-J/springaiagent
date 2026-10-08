package com.jinlin.springaiagent.agent;

import java.util.*;

// 面试题第五部分 可靠性治理 运行安全与观测评测
// 涵盖第十二题至第十五题 第十七题 第十九题至第二十一题 第二十三题 第二十四题 的核心实现与原理解析
public class AgentReliabilityAndSecurityDemo {

    // 第二十题 为什么会出现路径震荡 重复调用与死循环 怎么检测和治理
    // 产生原因
    // 诱因一 工具返回值未提供足够增量信息 模型无法判定当前进度
    // 诱因二 提示词指令存在逻辑冲突或边界定义模糊 模型在两个工具间摇摆震荡
    // 诱因三 历史上下文堆叠了错误尝试导致模型产生重复生成的自回归惯性
    // 检测与治理手段
    // 手段一 工具调用指纹比对机制 记录最近调用的工具名称与入参哈希 连续相同调用立即阻断
    // 手段二 最大调用步数限制与预算熔断 设定单次任务最大步骤阈值
    // 手段三 循环惩罚注入 当检测到疑似死循环时 动态注入警告提示词 强制模型更换策略或向用户求助
    public static class LoopAndOscillationDetector {
        private final List<String> callFingerprints = new ArrayList<>();
        private final int maxSteps;

        public LoopAndOscillationDetector(int maxSteps) {
            this.maxSteps = maxSteps;
        }

        // 校验并记录调用指纹
        public boolean checkAndRecord(String toolName, String params) {
            if (callFingerprints.size() >= maxSteps) {
                System.out.println("安全熔断 超过最大允许步骤阈值 " + maxSteps + " 强制终止以防死循环");
                return false;
            }

            String currentFingerprint = toolName + "#" + params;
            int repeatCount = 0;
            for (String fp : callFingerprints) {
                if (fp.equals(currentFingerprint)) {
                    repeatCount++;
                }
            }

            if (repeatCount >= 2) {
                System.out.println("死循环治理 触发重复调用阻断检测 工具 " + toolName + " 参数 " + params);
                return false;
            }

            callFingerprints.add(currentFingerprint);
            return true;
        }
    }

    // 第二十三题 任务幻觉是什么 如何避免未执行工具却声称已完成
    // 任务幻觉是指大模型在没有真正发起工具调用或工具调用失败的情况下 凭借自身概率补全虚构执行过程并谎称任务已完成
    // 防御方案
    // 方案一 执行凭证收据机制 业务系统必须由外部受控调度引擎驱动 只有收到底层工具执行器返回的合法验签收据才认可该步骤完成
    // 方案二 状态断言校验 在标记任务完成前 对物理环境或数据库真实状态进行断言查询确认
    public static class TaskExecutionGuard {
        private final Set<String> validExecutionReceipts = new HashSet<>();

        // 工具真实执行后下发验签收据
        public String executeRealTool(String toolId, Runnable action) {
            action.run();
            String receiptToken = "RECEIPT_" + toolId + "_" + System.currentTimeMillis();
            validExecutionReceipts.add(receiptToken);
            return receiptToken;
        }

        // 校验完成状态是否具备真实收据支撑
        public boolean verifyCompletion(String receiptToken) {
            if (receiptToken == null || !validExecutionReceipts.contains(receiptToken)) {
                System.out.println("任务幻觉拦截 智能体声称完成但缺乏真实工具执行收据 判定为虚假完成");
                return false;
            }
            System.out.println("任务完成验真 成功核验真实底层工具执行凭证 " + receiptToken);
            return true;
        }
    }

    // 第二十四题 数据库连接时如何防止越权 敏感数据泄漏和查询幻觉
    // 防御体系包括四道防线
    // 第一道防线 权限与只读约束 数据库账号配置为只读账号 严禁赋予写库与删表权限
    // 第二道防线 SQL 词法语法白名单校验 拦截堆叠查询 危险系统函数及跨库探测
    // 第三道防线 动态脱敏过滤 响应文本自动匹配手机号 身份证 密码等特征并脱敏替换
    // 第四道防线 Schema 真实元数据注入 消除模型凭空臆造表名与字段名的查询幻觉
    public static class DatabaseSecurityGuardrail {

        // SQL 安全审查
        public boolean validateSql(String sql) {
            String cleanSql = sql.trim().toLowerCase();
            if (!cleanSql.startsWith("select")) {
                System.out.println("越权拦截 禁止执行非查询 SQL 语句 " + sql);
                return false;
            }
            if (cleanSql.contains("drop") || cleanSql.contains("delete") || cleanSql.contains("update") || cleanSql.contains("insert")) {
                System.out.println("高危操作拦截 检测到越权修改关键字 " + sql);
                return false;
            }
            return true;
        }

        // 敏感数据动态脱敏处理
        public String maskSensitiveData(String rawContent) {
            // 手机号脱敏
            String masked = rawContent.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
            // 身份证脱敏
            masked = masked.replaceAll("(\\d{6})\\d{8}(\\w{4})", "$1********$2");
            return masked;
        }
    }

    // 第二十一题 线上 Agent 延迟升高时的 Trace 观测定位与性能优化
    // 追踪指标包含 大模型首字时延 网络通信耗时 工具IO耗时 循环步数与重试损耗
    public static class AgentTraceObserver {
        public static class TraceSpan {
            public final String spanName;
            public final long durationMs;

            public TraceSpan(String spanName, long durationMs) {
                this.spanName = spanName;
                this.durationMs = durationMs;
            }
        }

        private final List<TraceSpan> spans = new ArrayList<>();

        public void recordSpan(String name, long durationMs) {
            spans.add(new TraceSpan(name, durationMs));
            System.out.println("链路追踪记录 节点 " + name + " 耗时 " + durationMs + " 毫秒");
        }

        public void printTraceReport() {
            long total = 0;
            for (TraceSpan s : spans) {
                total += s.durationMs;
            }
            System.out.println("全链路追踪分析 完成 总耗时 " + total + " 毫秒 节点分布分析完毕");
        }
    }

    // 第十二题 Agent 记忆压缩通常有哪些方法
    // 方法一 滑动窗口修剪 保留最近 N 轮对话 丢弃早期历史
    // 方法二 分段分层摘要 当历史超出 Token 预算时 启动异步模型生成核心要点摘要并替换原文
    // 方法三 实体记忆图抽取 提取关键变量 用户偏好 键值对事实 丢弃无意义寒暄
    // 方法四 向量语义聚类过滤 对历史消息按意图与当前问题的相似度进行动态采样
    public static class MemoryCompressionEngine {
        public String compressHistoryBySummary(List<String> rawDialogues) {
            System.out.println("执行记忆压缩 原始历史轮数 " + rawDialogues.size() + " 启动摘要提炼");
            StringBuilder summary = new StringBuilder("对话历史核心摘要 用户咨询了核心系统架构且已确认基本方案");
            return summary.toString();
        }
    }

    // 第十三题 为什么有时候选择手搓 Agent 而不是直接用成熟框架
    // 原因一 框架黑盒抽象过深 框架为了通用性封装过多内部逻辑 难以在关键决策节点注入细粒度业务规则
    // 原因二 异常重试与死循环难以拦截 成熟框架内置的重试与兜底逻辑容易失控 导致高昂的 Token 费用消耗
    // 原因三 状态机持久化成本高 很多框架难以原生接入企业已有的分布式事务 MySQL Redis 检查点机制
    // 原因四 性能与首字延迟开销 框架内部层层切面与解析转换会放大链路耗时 工业级自研微内核更加轻量受控

    // 第十四题 如何赋予 LLM 规划能力
    // 方式一 思维链与分步引导 通过系统提示词强制要求模型先输出计划步骤再执行动作
    // 方式二 少样本提示注入 在上下文中给出多条复杂任务规划的标准范例
    // 方式三 工具图引导 提供显式的依赖关系描述 限制步骤调用的拓扑先后顺序
    // 方式四 蒙特卡洛树搜索与反思演化 借助多分支采样评分选择最优规划路径

    // 第十五题 Agent 的反思机制 为什么要用反思 具体怎么实现
    // 为什么要反思 单次推理极易出现事实性幻觉 逻辑漏洞与指令违背 反思能够引入自我审查提升准确度
    // 具体实现流程
    // 阶段一 产出阶段 生成器输出初始响应结果
    // 阶段二 审查阶段 审查器从事实准确性 格式合规性 安全规范等维度打分并指出缺陷
    // 阶段三 改进阶段 优化器结合审查意见重新生成 直到评分达到设定的终止阈值

    // 第十七题 Agent 的上下文工程怎么设计
    // 核心遵循模块化分层组装原则
    // 第一层 核心系统规则 包含角色定位 安全边界 强制输出格式
    // 第二层 少样本标杆 包含标准范例 错误反例纠偏
    // 第三层 外部可用工具定义 包含函数描述 参数校验约束
    // 第四层 长期记忆与检索背景知识 包含用户画像 与当前问题高相关的向量召回知识
    // 第五层 动态工作记忆与当前交互 包含近期会话窗口 以及上一步工具执行的真实观测反馈
    public static class ContextEngineeringPipeline {
        public String buildFullPrompt(String systemRule, String toolsDef, String memoryContext, String userQuery) {
            System.out.println("上下文工程组装 正在按照标准五层结构拼装完整提示词");
            return "系统规则\n" + systemRule + "\n\n工具定义\n" + toolsDef + "\n\n上下文背景\n" + memoryContext + "\n\n用户当前问题\n" + userQuery;
        }
    }

    // 第十九题 如何评估一个 Agent 的效果 评测集和指标怎么设计
    // 评测集设计原则
    // 包含常规典型样本 极端长尾样本 恶意越狱攻击样本 工具调用边界样本
    // 核心评测指标体系
    // 指标一 意图识别准确率 衡量能否正确理解用户真实目标
    // 指标二 工具选择准确率与参数合法率 衡量是否调对了工具以及参数是否有效
    // 指标三 任务闭环达成率 衡量能否真正端到端解决问题
    // 指标四 路径效率与 Token 消耗 衡量解决问题平均经历了多少步 是否存在冗余步骤
    // 指标五 幻觉率与安全性 衡量虚构事实比例与合规拦截率
    public static class AgentEvaluationFramework {
        public void evaluateCase(String caseId, boolean toolAccuracy, boolean taskSuccess, int totalSteps, long latencyMs) {
            System.out.println("评测用例 " + caseId + " 工具准确 " + toolAccuracy + " 任务完成 " + taskSuccess + " 执行步数 " + totalSteps + " 耗时 " + latencyMs + " 毫秒");
        }
    }
}
