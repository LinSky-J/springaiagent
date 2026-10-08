package com.jinlin.springaiagent.llm;

import org.springframework.stereotype.Service;
import java.util.*;

// 混合专家门控路由企业级服务
// 单一职责 负责 MoE 门控路由器打分 Top-K 专家动态激活与共享专家输出加权融合
@Service
public class MixtureOfExpertsRouter {

    public static class MoERoutingResult {
        public final double[] outputVector;
        public final int[] selectedExpertIndices;
        public final double[] routingWeights;

        public MoERoutingResult(double[] outputVector, int[] selectedExpertIndices, double[] routingWeights) {
            this.outputVector = outputVector;
            this.selectedExpertIndices = selectedExpertIndices;
            this.routingWeights = routingWeights;
        }
    }

    private final int totalExperts;
    private final int activeTopK;
    private final int hiddenDim;
    private final double[][] routerWeight;
    private final boolean hasSharedExpert;

    public MixtureOfExpertsRouter() {
        this(64, 8, 128, true);
    }

    public MixtureOfExpertsRouter(int totalExperts, int activeTopK, int hiddenDim, boolean hasSharedExpert) {
        this.totalExperts = totalExperts;
        this.activeTopK = activeTopK;
        this.hiddenDim = hiddenDim;
        this.hasSharedExpert = hasSharedExpert;
        this.routerWeight = new double[totalExperts][hiddenDim];

        Random random = new Random(42);
        for (int e = 0; e < totalExperts; e++) {
            for (int d = 0; d < hiddenDim; d++) {
                routerWeight[e][d] = (random.nextDouble() - 0.5) / Math.sqrt(hiddenDim);
            }
        }
    }

    // 执行门控路由打分与专家前向分发
    public MoERoutingResult routeAndForward(double[] tokenEmbedding) {
        double[] logits = new double[totalExperts];

        // 门控线性映射计算对数几率
        for (int e = 0; e < totalExperts; e++) {
            double dot = 0.0;
            for (int d = 0; d < hiddenDim; d++) {
                dot += routerWeight[e][d] * tokenEmbedding[d];
            }
            logits[e] = dot;
        }

        // 选取 Top-K 专家
        Integer[] expertIndices = new Integer[totalExperts];
        for (int i = 0; i < totalExperts; i++) {
            expertIndices[i] = i;
        }
        Arrays.sort(expertIndices, (a, b) -> Double.compare(logits[b], logits[a]));

        int[] selected = new int[activeTopK];
        double[] selectedLogits = new double[activeTopK];
        double maxL = logits[expertIndices[0]];

        for (int k = 0; k < activeTopK; k++) {
            selected[k] = expertIndices[k];
            selectedLogits[k] = logits[expertIndices[k]];
        }

        // 对激活的 Top-K 专家应用 Softmax 归一化权重
        double sumExp = 0.0;
        double[] weights = new double[activeTopK];
        for (int k = 0; k < activeTopK; k++) {
            weights[k] = Math.exp(selectedLogits[k] - maxL);
            sumExp += weights[k];
        }
        for (int k = 0; k < activeTopK; k++) {
            weights[k] /= sumExp;
        }

        // 加权融合专家输出向量
        double[] output = new double[hiddenDim];
        for (int k = 0; k < activeTopK; k++) {
            double[] expertOut = simulateExpertFfn(tokenEmbedding, selected[k]);
            for (int d = 0; d < hiddenDim; d++) {
                output[d] += weights[k] * expertOut[d];
            }
        }

        // 若配置共享专家 则无条件叠加共享专家输出 保持通识表征稳定
        if (hasSharedExpert) {
            double[] sharedOut = simulateExpertFfn(tokenEmbedding, 999);
            for (int d = 0; d < hiddenDim; d++) {
                output[d] += sharedOut[d];
            }
        }

        return new MoERoutingResult(output, selected, weights);
    }

    private double[] simulateExpertFfn(double[] input, int expertId) {
        double[] out = new double[hiddenDim];
        double factor = 1.0 + (expertId % 7) * 0.05;
        for (int d = 0; d < hiddenDim; d++) {
            out[d] = Math.max(0.0, input[d] * factor);
        }
        return out;
    }
}
