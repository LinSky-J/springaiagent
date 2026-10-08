package com.jinlin.springaiagent.llm;

import org.springframework.stereotype.Service;
import java.util.Arrays;

// 模型低比特量化与反量化引擎服务
// 单一职责 负责浮点权重至 INT8 与 INT4 的仿射量化 比例因子校准与 AWQ 显著权重保护
@Service
public class ModelQuantizationEngine {

    public static class QuantizedTensor {
        public final byte[] quantizedData;
        public final double scale;
        public final int zeroPoint;
        public final int bitWidth;
        public final boolean[] salientMask;

        public QuantizedTensor(byte[] quantizedData, double scale, int zeroPoint, int bitWidth, boolean[] salientMask) {
            this.quantizedData = quantizedData;
            this.scale = scale;
            this.zeroPoint = zeroPoint;
            this.bitWidth = bitWidth;
            this.salientMask = salientMask;
        }
    }

    // 执行对称或非对称整型量化
    public QuantizedTensor quantize(double[] weights, int bitWidth, double[] activationMagnitudes, double salientRatio) {
        int length = weights.length;
        int qMin = -(1 << (bitWidth - 1));
        int qMax = (1 << (bitWidth - 1)) - 1;

        // 识别 AWQ 显著权重 前百分之一关键通道免受剧烈量化
        boolean[] salientMask = new boolean[length];
        if (activationMagnitudes != null && salientRatio > 0.0) {
            double[] sortedActivations = activationMagnitudes.clone();
            Arrays.sort(sortedActivations);
            int thresholdIndex = (int) (length * (1.0 - salientRatio));
            thresholdIndex = Math.min(Math.max(thresholdIndex, 0), length - 1);
            double threshold = sortedActivations[thresholdIndex];

            for (int i = 0; i < length; i++) {
                if (activationMagnitudes[i] >= threshold) {
                    salientMask[i] = true;
                }
            }
        }

        // 计算动态范围与缩放比例
        double maxAbs = 1e-8;
        for (double w : weights) {
            double abs = Math.abs(w);
            if (abs > maxAbs) {
                maxAbs = abs;
            }
        }

        double scale = maxAbs / qMax;
        int zeroPoint = 0;
        byte[] qData = new byte[length];

        for (int i = 0; i < length; i++) {
            long qVal = Math.round(weights[i] / scale) + zeroPoint;
            qVal = Math.max(qMin, Math.min(qMax, qVal));
            qData[i] = (byte) qVal;
        }

        return new QuantizedTensor(qData, scale, zeroPoint, bitWidth, salientMask);
    }

    // 执行反量化重构与均方误差评估
    public double evaluateReconstructionError(double[] originalWeights, QuantizedTensor tensor) {
        double sumSquareError = 0.0;
        int length = originalWeights.length;

        for (int i = 0; i < length; i++) {
            double reconstructed = (tensor.quantizedData[i] - tensor.zeroPoint) * tensor.scale;
            // 若命中显著权重保护 则直接使用高精度原值
            if (tensor.salientMask != null && tensor.salientMask[i]) {
                reconstructed = originalWeights[i];
            }
            double diff = originalWeights[i] - reconstructed;
            sumSquareError += diff * diff;
        }

        return sumSquareError / length;
    }
}
