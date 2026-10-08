package com.jinlin.springaiagent.llm;

import org.springframework.stereotype.Service;

// 旋转位置编码与线性偏置计算服务
// 单一职责 负责执行 RoPE 旋转矩阵正交变换以及 ALiBi 线性衰减偏置矩阵生成
@Service
public class RotaryPositionEmbeddingService {

    // 计算 RoPE 旋转位置编码 将位置信息注入查询与键向量
    public double[] applyRotaryEmbedding(double[] vector, int positionIndex, int dimension, double baseTheta) {
        double[] rotated = new double[dimension];
        
        // 按照维度两两成对进行复数平面旋转变换
        for (int i = 0; i < dimension; i += 2) {
            double theta = 1.0 / Math.pow(baseTheta, (double) i / dimension);
            double angle = positionIndex * theta;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            double x0 = vector[i];
            double x1 = (i + 1 < dimension) ? vector[i + 1] : 0.0;

            rotated[i] = x0 * cos - x1 * sin;
            if (i + 1 < dimension) {
                rotated[i + 1] = x0 * sin + x1 * cos;
            }
        }
        return rotated;
    }

    // 计算 ALiBi 线性偏置矩阵 无需可学习参数 提供天然长度外推能力
    public double[][] computeAlibiBiases(int sequenceLength, int headIndex, int totalHeads) {
        // 计算当前注意力头的几何级数斜率比例
        double baseRatio = Math.pow(2.0, -8.0 / totalHeads);
        double slope = Math.pow(baseRatio, headIndex + 1);

        double[][] biasMatrix = new double[sequenceLength][sequenceLength];
        for (int i = 0; i < sequenceLength; i++) {
            for (int j = 0; j < sequenceLength; j++) {
                // 仅针对非未来词元施加距离衰减负向偏置
                if (j <= i) {
                    biasMatrix[i][j] = -slope * (i - j);
                } else {
                    biasMatrix[i][j] = Double.NEGATIVE_INFINITY;
                }
            }
        }
        return biasMatrix;
    }
}
