package com.jinlin.springaiagent.llm;

import org.springframework.stereotype.Service;
import java.util.*;

// 文本生成解码采样服务
// 单一职责 负责对模型输出未归一化对数几率执行温度缩放 顶部截断 核采样与分类抽样
@Service
public class TextGenerationSampler {

    public static class SamplingConfig {
        public final double temperature;
        public final double topP;
        public final int topK;

        public SamplingConfig(double temperature, double topP, int topK) {
            this.temperature = temperature;
            this.topP = topP;
            this.topK = topK;
        }
    }

    private static class TokenScore implements Comparable<TokenScore> {
        final int tokenId;
        final double logit;

        TokenScore(int tokenId, double logit) {
            this.tokenId = tokenId;
            this.logit = logit;
        }

        @Override
        public int compareTo(TokenScore o) {
            return Double.compare(o.logit, this.logit);
        }
    }

    // 执行真实解码采样计算
    public int sampleNextToken(double[] rawLogits, SamplingConfig config, Random random) {
        int vocabSize = rawLogits.length;
        double[] scaledLogits = new double[vocabSize];

        // 贪心搜索分支 温度极低或等于零时直接选取最大值
        if (config.temperature <= 1e-4) {
            int bestToken = 0;
            double maxLogit = rawLogits[0];
            for (int i = 1; i < vocabSize; i++) {
                if (rawLogits[i] > maxLogit) {
                    maxLogit = rawLogits[i];
                    bestToken = i;
                }
            }
            return bestToken;
        }

        // 应用温度缩放
        for (int i = 0; i < vocabSize; i++) {
            scaledLogits[i] = rawLogits[i] / config.temperature;
        }

        // 排序构建候选队列
        List<TokenScore> candidates = new ArrayList<>(vocabSize);
        for (int i = 0; i < vocabSize; i++) {
            candidates.add(new TokenScore(i, scaledLogits[i]));
        }
        Collections.sort(candidates);

        // 应用 Top-K 截断
        int kLimit = Math.min(config.topK > 0 ? config.topK : vocabSize, vocabSize);
        List<TokenScore> topKCandidates = candidates.subList(0, kLimit);

        // 计算 Softmax 概率分布
        double maxL = topKCandidates.get(0).logit;
        double sumExp = 0.0;
        double[] expVals = new double[topKCandidates.size()];
        for (int i = 0; i < topKCandidates.size(); i++) {
            expVals[i] = Math.exp(topKCandidates.get(i).logit - maxL);
            sumExp += expVals[i];
        }

        double[] probs = new double[topKCandidates.size()];
        for (int i = 0; i < topKCandidates.size(); i++) {
            probs[i] = expVals[i] / sumExp;
        }

        // 应用 Top-P 核采样截断
        double cumulativeProb = 0.0;
        int pLimitIndex = 0;
        for (int i = 0; i < probs.length; i++) {
            cumulativeProb += probs[i];
            pLimitIndex = i;
            if (cumulativeProb >= config.topP) {
                break;
            }
        }

        // 在核采样集合内重新归一化并抽样
        double subSum = 0.0;
        for (int i = 0; i <= pLimitIndex; i++) {
            subSum += probs[i];
        }

        double r = random.nextDouble() * subSum;
        double current = 0.0;
        for (int i = 0; i <= pLimitIndex; i++) {
            current += probs[i];
            if (r <= current) {
                return topKCandidates.get(i).tokenId;
            }
        }

        return topKCandidates.get(pLimitIndex).tokenId;
    }
}
