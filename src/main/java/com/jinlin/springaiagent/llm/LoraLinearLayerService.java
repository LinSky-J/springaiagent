package com.jinlin.springaiagent.llm;

import org.springframework.stereotype.Service;
import java.util.Random;

// 低秩适配线性层企业级计算服务
// 单一职责 负责 LoRA 低秩矩阵分解 前向计算 权重合并与解绑
@Service
public class LoraLinearLayerService {

    public static class LoraLayerConfig {
        public final int inFeatures;
        public final int outFeatures;
        public final int rank;
        public final double alpha;
        public final double scaling;

        public LoraLayerConfig(int inFeatures, int outFeatures, int rank, double alpha) {
            this.inFeatures = inFeatures;
            this.outFeatures = outFeatures;
            this.rank = rank;
            this.alpha = alpha;
            this.scaling = alpha / rank;
        }
    }

    public static class LoraLayerState {
        public double[][] baseWeight;
        public double[][] loraA;
        public double[][] loraB;
        public boolean isMerged;
        public final LoraLayerConfig config;

        public LoraLayerState(LoraLayerConfig config) {
            this.config = config;
            this.baseWeight = new double[config.outFeatures][config.inFeatures];
            this.loraA = new double[config.rank][config.inFeatures];
            this.loraB = new double[config.outFeatures][config.rank];
            this.isMerged = false;
        }
    }

    // 初始化 LoRA 矩阵 矩阵 A 使用高斯随机初始化 矩阵 B 初始化为全零 确保微调初始状态与基座完全一致
    public LoraLayerState initializeLayer(int inFeatures, int outFeatures, int rank, double alpha) {
        LoraLayerConfig config = new LoraLayerConfig(inFeatures, outFeatures, rank, alpha);
        LoraLayerState state = new LoraLayerState(config);
        Random random = new Random(42);

        // 初始化基座权重
        for (int i = 0; i < outFeatures; i++) {
            for (int j = 0; j < inFeatures; j++) {
                state.baseWeight[i][j] = (random.nextDouble() - 0.5) * 0.1;
            }
        }

        // 初始化矩阵 A 高斯分布缩放
        for (int i = 0; i < rank; i++) {
            for (int j = 0; j < inFeatures; j++) {
                state.loraA[i][j] = random.nextGaussian() / Math.sqrt(inFeatures);
            }
        }

        // 矩阵 B 全零初始化
        for (int i = 0; i < outFeatures; i++) {
            for (int j = 0; j < rank; j++) {
                state.loraB[i][j] = 0.0;
            }
        }

        return state;
    }

    // 执行前向传播计算
    public double[] forward(LoraLayerState state, double[] input) {
        double[] output = new double[state.config.outFeatures];

        if (state.isMerged) {
            // 已合并状态 直接单次矩阵乘法计算 具备零额外推理延迟
            for (int i = 0; i < state.config.outFeatures; i++) {
                double sum = 0.0;
                for (int j = 0; j < state.config.inFeatures; j++) {
                    sum += state.baseWeight[i][j] * input[j];
                }
                output[i] = sum;
            }
            return output;
        }

        // 计算主干网络基座输出
        for (int i = 0; i < state.config.outFeatures; i++) {
            double sum = 0.0;
            for (int j = 0; j < state.config.inFeatures; j++) {
                sum += state.baseWeight[i][j] * input[j];
            }
            output[i] = sum;
        }

        // 计算低秩旁路输出 先乘 A 后乘 B 并乘以缩放系数
        double[] intermediate = new double[state.config.rank];
        for (int r = 0; r < state.config.rank; r++) {
            double sum = 0.0;
            for (int j = 0; j < state.config.inFeatures; j++) {
                sum += state.loraA[r][j] * input[j];
            }
            intermediate[r] = sum;
        }

        for (int i = 0; i < state.config.outFeatures; i++) {
            double sum = 0.0;
            for (int r = 0; r < state.config.rank; r++) {
                sum += state.loraB[i][r] * intermediate[r];
            }
            output[i] += sum * state.config.scaling;
        }

        return output;
    }

    // 线上部署时将低秩旁路增量永久合并至基座矩阵
    public void mergeWeights(LoraLayerState state) {
        if (state.isMerged) {
            return;
        }
        for (int i = 0; i < state.config.outFeatures; i++) {
            for (int j = 0; j < state.config.inFeatures; j++) {
                double delta = 0.0;
                for (int r = 0; r < state.config.rank; r++) {
                    delta += state.loraB[i][r] * state.loraA[r][j];
                }
                state.baseWeight[i][j] += delta * state.config.scaling;
            }
        }
        state.isMerged = true;
    }
}
