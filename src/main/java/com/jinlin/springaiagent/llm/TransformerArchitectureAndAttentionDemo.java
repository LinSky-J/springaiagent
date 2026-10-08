package com.jinlin.springaiagent.llm;

import java.util.*;

// 第一题至第五题 大模型底层架构与注意力机制工程模拟
// 演示大模型与传统模型区别 注意力演进 位置编码以及分词机制
public class TransformerArchitectureAndAttentionDemo {

    // 第一题 大语言模型与传统 NLP 模型区别对比模型
    public static class ModelEvolutionComparison {
        public void compareArchitectures() {
            System.out.println("传统 NLP 模型 依赖词袋模型 循环神经网络与长短期记忆网络 参数量小 难并行 存在长期依赖遗忘");
            System.out.println("现代大语言模型 基于 Transformer 架构 数百亿参数 具备通用无监督预训练与多任务泛化及涌现能力");
        }
    }

    // 第二题与第三题 注意力机制模拟 包括多头注意力 多查询注意力 分组查询注意力以及显存优化
    public static class AttentionMechanismEngine {
        private final int hiddenDimension;
        private final int headCount;

        public AttentionMechanismEngine(int hiddenDimension, int headCount) {
            this.hiddenDimension = hiddenDimension;
            this.headCount = headCount;
        }

        // 模拟多头注意力 MHA 每个头独立维护 Q K V 权重矩阵
        public void simulateMha(int sequenceLength) {
            long kvMemoryBytes = (long) sequenceLength * headCount * (hiddenDimension / headCount) * 2 * 2;
            System.out.println("标准多头注意力 MHA 查询头数等于键值头数 序列长度 " + sequenceLength + " 显存占用高");
        }

        // 模拟多查询注意力 MQA 所有查询头共享同一对键值投影矩阵
        public void simulateMqa(int sequenceLength) {
            long kvMemoryBytes = (long) sequenceLength * 1 * (hiddenDimension / headCount) * 2 * 2;
            System.out.println("多查询注意力 MQA 所有查询头共享单一组 KV 矩阵 显存占用大幅压缩 但表征容量略有折损");
        }

        // 模拟分组查询注意力 GQA 查询头分组共享少量的键值头
        public void simulateGqa(int sequenceLength, int groupCount) {
            int kvHeads = headCount / groupCount;
            System.out.println("分组查询注意力 GQA 将查询头分为 " + groupCount + " 组 每组共享键值头 兼顾显存速度与模型表达容量");
        }

        // 模拟 FlashAttention 显存优化计算原理
        public void simulateFlashAttention() {
            System.out.println("FlashAttention 核心利用分块平铺 Tiling 与在线 Softmax 规避写入显存 高带宽显存吞吐显著提速");
        }
    }

    // 第四题 位置编码演进模拟 包含绝对正弦位置编码 旋转位置编码与线性偏置注意力
    public static class PositionalEncodingSimulator {
        public void demonstrateEncodings() {
            System.out.println("绝对正弦编码 早期 Transformer 采用 固定周期函数 外推能力弱 无法良好泛化到更长序列");
            System.out.println("旋转位置编码 RoPE 将相对位置通过正交旋转矩阵注入点积 注意力直接感知相对距离 支持外推扩展");
            System.out.println("线性偏置注意力 ALiBi 在自注意力矩阵上直接施加与距离成正比的衰减偏置 无需可学习参数 具备天然长度外推性");
        }
    }

    // 第五题 分词器原理解析与词表匹配模拟
    public static class TokenizerSimulator {
        private final Map<String, Integer> vocabulary = new HashMap<>();

        public TokenizerSimulator() {
            vocabulary.put("大模型", 101);
            vocabulary.put("工程", 102);
            vocabulary.put("架构", 103);
            vocabulary.put("推理", 104);
        }

        public List<Integer> tokenize(String input) {
            List<Integer> tokenIds = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : vocabulary.entrySet()) {
                if (input.contains(entry.getKey())) {
                    tokenIds.add(entry.getValue());
                }
            }
            System.out.println("分词器模拟 基于 BPE 字节对编码或 WordPiece 将文本切分为子词 避免未登录词且兼顾压缩效率");
            return tokenIds;
        }
    }
}
