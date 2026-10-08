package com.jinlin.springaiagent.llm;

import org.springframework.stereotype.Service;
import java.util.Arrays;

// 分组查询注意力企业级计算服务
// 单一职责 负责多头注意力 多查询注意力与分组查询注意力的矩阵投影计算与头复制
@Service
public class GroupedQueryAttentionService {

    // 核心注意力计算结果封装
    public static class AttentionOutput {
        public final double[][][] contextTensor;
        public final int queryHeads;
        public final int kvHeads;
        public final long kvCacheMemoryBytes;

        public AttentionOutput(double[][][] contextTensor, int queryHeads, int kvHeads, long kvCacheMemoryBytes) {
            this.contextTensor = contextTensor;
            this.queryHeads = queryHeads;
            this.kvHeads = kvHeads;
            this.kvCacheMemoryBytes = kvCacheMemoryBytes;
        }
    }

    // 执行分组查询注意力计算
    public AttentionOutput computeAttention(
            double[][][] query,
            double[][][] key,
            double[][][] value,
            int numQueryHeads,
            int numKvHeads,
            int headDim,
            int seqLen
    ) {
        int groupSize = numQueryHeads / numKvHeads;
        double[][][] output = new double[numQueryHeads][seqLen][headDim];

        // 针对每个查询头 映射对应键值头并执行缩放点积注意力
        for (int qHead = 0; qHead < numQueryHeads; qHead++) {
            int kvHeadIndex = qHead / groupSize;
            double[][] qMatrix = query[qHead];
            double[][] kMatrix = key[kvHeadIndex];
            double[][] vMatrix = value[kvHeadIndex];

            // 计算注意力得分矩阵与缩放系数
            double scale = 1.0 / Math.sqrt(headDim);
            for (int i = 0; i < seqLen; i++) {
                double[] scores = new double[seqLen];
                double maxScore = Double.NEGATIVE_INFINITY;

                for (int j = 0; j < seqLen; j++) {
                    double dot = 0.0;
                    for (int d = 0; d < headDim; d++) {
                        dot += qMatrix[i][d] * kMatrix[j][d];
                    }
                    scores[j] = dot * scale;
                    if (scores[j] > maxScore) {
                        maxScore = scores[j];
                    }
                }

                // 数值稳定性 Softmax 计算
                double sumExp = 0.0;
                for (int j = 0; j < seqLen; j++) {
                    scores[j] = Math.exp(scores[j] - maxScore);
                    sumExp += scores[j];
                }
                for (int j = 0; j < seqLen; j++) {
                    scores[j] /= sumExp;
                }

                // 聚合值向量
                for (int d = 0; d < headDim; d++) {
                    double weightedVal = 0.0;
                    for (int j = 0; j < seqLen; j++) {
                        weightedVal += scores[j] * vMatrix[j][d];
                    }
                    output[qHead][i][d] = weightedVal;
                }
            }
        }

        // 计算当前键值缓存显存字节数 假设为十六位浮点数两字节
        long kvMemory = (long) 2 * numKvHeads * seqLen * headDim * 2;
        return new AttentionOutput(output, numQueryHeads, numKvHeads, kvMemory);
    }
}
