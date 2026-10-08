# SpringAIAgent - 企业级 AI Agent 与大模型工程化实战架构

本项目基于 Java 17、Spring Boot 3.2.4、Spring AI 与 LangChain4j 构建，是一套面向企业级生成式人工智能（GenAI）、智能体系统（AI Agent）、检索增强生成（RAG）、模型上下文协议（MCP）以及大模型底层工程架构的深度实战代码库。

项目严格遵循单一职责原则（SRP），抛弃空泛的概念打印，以严谨的企业级数学算法与生产架构模式实现了全部技术模块，并配有可直接运行验证的端到端测试运行套件。

---

## 核心技术栈

- **核心语言**：Java 17 (LTS)
- **核心框架**：Spring Boot 3.2.4
- **AI 框架集成**：
  - Spring AI (OpenAI 统一适配模块 `spring-ai-openai-spring-boot-starter:1.0.0-M6`)
  - LangChain4j (`dev.langchain4j:langchain4j-open-ai:0.33.0`)
- **构建工具**：Apache Maven 3.8+
- **模型接入**：小米 MiMo 旗舰大模型 (`mimo-v2.6-flash`)，兼容标准 OpenAI 协议
- **工程设计原则**：单一职责原则（Single Responsibility Principle）、管道组合模式（LCEL Pipeline）、状态图循环工作流（StateGraph）

---

## 系统架构与核心模块划分

项目按业务领域与技术专题划分为五大核心包：

```text
com.jinlin.springaiagent
├── agent         # 多智能体协同、状态检查点恢复、防死循环与工程治理
├── rag           # RAG 核心检索流水线、动态更新、图谱融合与防幻觉评测
├── tools         # Function Calling、MCP 上下文协议、Agent Skill 与通信协议
├── llm           # 大模型底层工程算法（GQA、RoPE、BPE、LoRA、DPO、量化、MoE）
└── langchain     # LangChain 与 Java 生态（LCEL、声明式工具、状态图、Deep Research）
```

### 1. 智能体工程治理模块 (`com.jinlin.springaiagent.agent`)
- **MultiAgentCollaborationSystem**：多智能体层级协同系统，实现调研专家与架构设计专家的有序交接与超时容错保护。
- **AgentStateAndCheckpointManager**：基于快照版本号的智能体对话检查点管理器，支持会话中断持久化与精确断点恢复。
- **ToolLoopAndFingerprintDefender**：工具调用指纹提取与滑动窗口频次检测器，阻断路径震荡与死循环重复调用。
- **TaskHallucinationReceiptVerifier**：任务幻觉防线，通过底层系统真实执行收据与签名机制，严惩未调用工具却宣称完成的欺骗行为。
- **DataSecurityAndPermissionInterceptor**：企业级数据安全防线，执行高危写 SQL 动态拦截以及身份证、手机号正则脱敏。
- **AgentDistributedTraceRecorder**：分布式全链路 Trace 耗时记录器，统计模型推理、网关校验与数据库耗时分布。
- **HierarchicalContextEngineeringPipeline**：五层上下文工程装配管道（系统角色、业务规则、知识切片、短期记忆、当前输入）。
- **RollingDialogueMemoryCompressor**：滚动对话记忆压缩器，触发滑动窗口提炼核心业务结论，规避 Token 爆炸。
- **AgentBenchmarkEvaluator**：智能体量化评测体系，计算工具命中率、任务完成率、步骤数与延迟。
- **AdvancedAgentInterviewRunner**：进阶智能体全链路综合执行套件。

### 2. 检索增强生成模块 (`com.jinlin.springaiagent.rag`)
- **RagCorePipelineDemo**：基础 RAG 全流程，涵盖文档加载、分块重叠滑动窗口、向量写入、检索增强与事实回答。
- **RagDynamicUpdateDemo**：知识库动态热更新机制，演示双缓冲影子索引切换与软删除逻辑过期。
- **AdvancedRagAndEvaluationDemo**：
  - **QueryRewriterAndExpander**：Query 重写扩展器，生成多视角子查询。
  - **ReciprocalRankFusionEngine**：RRF 倒数排名融合算法，多路合并稠密向量与稀疏关键词检索结果。
  - **GraphRagKnowledgeExplorer**：Graph RAG 实体关系图谱推导引擎，支持跨跳知识推理。
  - **AntiHallucinationGuard**：防幻觉相似度得分拦截器，设定严格拒绝回答阈值。
  - **RagTriadEvaluator**：RAG 三元组量化评测器（上下文相关度、答案忠实度、回答相关度）。
  - **KnowledgeConflictArbitrator**：多源知识冲突加权仲裁器，基于权威度权重与发布时间戳自动仲裁。
- **RagInterviewRunner**：RAG 核心机制端到端综合测试运行器。

### 3. 工具调用与通信协议模块 (`com.jinlin.springaiagent.tools`)
- **FunctionCallingAndMcpDemo**：原生 Function Calling 闭环与 MCP（模型上下文协议）架构。
  - 演示 MCP 三大核心原语：Resource（资源）、Prompt（提示词模板）、Tool（执行工具）。
  - 演示客户端与服务端通过 stdio / SSE 进行协议握手与工具同步。
- **SkillAndProtocolEcosystemDemo**：Agent Skill 领域业务能力包封装、A2A 智能体对等协商协议，以及 SSE / WebSocket / WebRTC 实时通信协议选型分析。
- **ToolRoutingAndFaultToleranceDemo**：
  - **ToolSemanticRouter**：海量工具语义向量路由器，动态过滤候选工具集，极大节约 Prompt Token。
  - **ToolFaultToleranceSelfHealingLoop**：格式非法捕获、必填参数校验拦截与错误回灌自我纠正循环。
- **ToolCallingInterviewRunner**：工具调用与 MCP 核心机制综合执行套件。

### 4. 大模型底层算法与工程服务 (`com.jinlin.springaiagent.llm`)
- **GroupedQueryAttentionService**：分组查询注意力（GQA/MQA/MHA）纯数学实现，模拟 QKV 矩阵投影、键值头分组复制、缩放点积注意力与显存压缩计算。
- **RotaryPositionEmbeddingService**：旋转位置编码（RoPE）吉文斯复数平面旋转正交变换与 ALiBi 线性衰减偏置矩阵生成。
- **BytePairEncodingTokenizer**：BPE 字节对编码分词器，实现语料库子词训练、高频词对贪心合并与双向编解码还原。
- **LoraLinearLayerService**：低秩适配（LoRA）线性层，冻结主干权重矩阵，引入低秩矩阵 A（高斯分布）与 B（零初始化），实现前向传播与线上无缝权重合并。
- **DirectPreferenceOptimizationLossCalculator**：直接偏好优化（DPO）损失计算器，计算人类偏好隐式奖励、奖励裕度与负对数似然损失。
- **TextGenerationSampler**：文本生成解码采样器，实现温度缩放、Top-K 截断、Top-P 核采样（Nucleus Sampling）与 Softmax 分类抽样。
- **PagedKvCacheManager**：分页键值缓存管理服务，模拟 vLLM PagedAttention 物理块表映射机制与 Radix 前缀树缓存命中复用。
- **ModelQuantizationEngine**：低比特量化引擎，实现 INT8/INT4 仿射量化、比例因子标定、反量化均方误差评估，以及 AWQ 激活感知前 1% 关键通道保护。
- **MixtureOfExpertsRouter**：混合专家（MoE）门控路由器，实现 Top-K 专家打分、局部 Softmax 归一化激活权重、专家前向分发与共享专家输出无条件融合。
- **LlmEngineeringInterviewRunner**：大模型工程综合真实算法验证套件。

### 5. LangChain 与 Java 生态架构模块 (`com.jinlin.springaiagent.langchain`)
- **EnterpriseChainPipelineService**：Java 平台 LCEL 链式管道实现，支持函数式组合、输入输出类型推导与全局 Fallback 回退。
- **DeclarativeAgentToolRegistryService**：声明式工具注册服务，基于 `@AgentTool` 自定义注解扫描 Spring Bean 方法，动态提取反射元数据与 JSON 模式并路由执行。
- **DualTierMemoryManagerService**：双层记忆协同管理器，实现短期滑动窗口对话缓冲容量控制与长期向量知识库余弦相似度回忆。
- **StateGraphWorkflowEngine**：Java 原生状态图工作流引擎（类似 LangGraph），实现有环状态通道转移、节点动作执行、条件边路由与 ReAct 循环。
- **DeepResearchWorkflowCoordinator**：深度研究智能体协同工作流，实现宏观议题拆解、多跳迭代信息检索、反思盲区探测与万字长篇报告归纳。
- **LangChainInterviewRunner**：LangChain 与 Java 生态综合运行套件。

---

## 快速开始

### 1. 环境准备
- 安装 JDK 17 或以上版本
- 安装 Apache Maven 3.8+
- 配置环境变量 `JAVA_HOME` 指向 JDK 17

### 2. 编译项目
在项目根目录下执行 Maven 编译：
```bash
mvn clean test-compile -Dtest=none
```

### 3. 执行端到端测试运行器
项目中每个核心包均提供独立的综合运行验证类，可直接运行验证：

```bash
# 1. 验证进阶智能体工程体系（多智能体协作、状态恢复、死循环拦截、收据验真）
java -cp target/classes com.jinlin.springaiagent.agent.AdvancedAgentInterviewRunner

# 2. 验证 RAG 全链路（滑动切片、RRF 融合、Graph RAG、评测与冲突仲裁）
java -cp target/classes com.jinlin.springaiagent.rag.advanced.RagInterviewRunner

# 3. 验证工具调用与协议（Function Calling、MCP 三原语、语义路由、自愈容错）
java -cp target/classes com.jinlin.springaiagent.tools.ToolCallingInterviewRunner

# 4. 验证大模型底层算法（GQA 注意力、RoPE、BPE 分词、LoRA、DPO、量化、MoE）
java -cp target/classes com.jinlin.springaiagent.llm.LlmEngineeringInterviewRunner

# 5. 验证 LangChain 与 Java 生态（LCEL 管道、声明式工具、状态图、Deep Research）
java -cp target/classes com.jinlin.springaiagent.langchain.LangChainInterviewRunner
```

---

## 技术亮点与工程规范

1. **零概念空谈，纯真实代码**：每个业务与算法题目均编写了完整的数学运算逻辑、数据结构与流程管道，拒绝使用控制台打印代替实际实现。
2. **严格遵循单一职责原则（SRP）**：将复杂的大模型工程体系解耦为互相独立的高内聚低耦合服务类，便于在微服务与高并发生产环境下直接复用。
3. **企业级生产防御机制**：内置了包括高危 SQL 拦截、防任务幻觉执行收据校验、重复调用指纹熔断、超时降级重试与多源知识冲突加权仲裁在内的全套安全风控防线。
4. **与云端大模型无缝兼容**：支持通过 Spring AI 与 LangChain4j 标准客户端无缝对接各主流商业模型与开源模型服务。
