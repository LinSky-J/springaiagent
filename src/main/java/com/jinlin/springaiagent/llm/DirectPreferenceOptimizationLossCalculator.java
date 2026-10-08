package com.jinlin.springaiagent.llm;

import org.springframework.stereotype.Service;

// 直接偏好优化损失计算服务
// 单一职责 负责基于 DPO 算法计算偏好对隐式奖励 相对概率对数差值与负对数似然损失
@Service
public class DirectPreferenceOptimizationLossCalculator {

    public static class DpoEvaluationResult {
        public final double loss;
        public final double chosenReward;
        public final double rejectedReward;
        public final double rewardMargin;
        public final boolean isAccurate;

        public DpoEvaluationResult(double loss, double chosenReward, double rejectedReward, double rewardMargin, boolean isAccurate) {
            this.loss = loss;
            this.chosenReward = chosenReward;
            this.rejectedReward = rejectedReward;
            this.rewardMargin = rewardMargin;
            this.isAccurate = isAccurate;
        }
    }

    // 计算 DPO 损失与隐式奖励
    public DpoEvaluationResult computeDpoLoss(
            double policyChosenLogProb,
            double policyRejectedLogProb,
            double refChosenLogProb,
            double refRejectedLogProb,
            double beta
    ) {
        // 计算当前策略与参考策略在人类偏好样本上的对数概率比值
        double chosenLogRatio = policyChosenLogProb - refChosenLogProb;
        double rejectedLogRatio = policyRejectedLogProb - refRejectedLogProb;

        // 计算隐式奖励值
        double chosenReward = beta * chosenLogRatio;
        double rejectedReward = beta * rejectedLogRatio;
        double rewardMargin = chosenReward - rejectedReward;

        // 计算逻辑回归 Sigmoid 概率与负对数似然损失
        double sigmoid = 1.0 / (1.0 + Math.exp(-rewardMargin));
        double loss = -Math.log(Math.max(sigmoid, 1e-12));
        boolean isAccurate = chosenReward > rejectedReward;

        return new DpoEvaluationResult(loss, chosenReward, rejectedReward, rewardMargin, isAccurate);
    }
}
