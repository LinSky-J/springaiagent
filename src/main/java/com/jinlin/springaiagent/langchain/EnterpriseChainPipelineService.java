package com.jinlin.springaiagent.langchain;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.function.Function;

// 企业级链式管道编排服务
// 单一职责 负责模拟 LangChain 核心概念 Chain 与 LCEL 表达式管道 在 Java 平台实现可组合链式流水线
@Service
public class EnterpriseChainPipelineService {

    // 链式执行步骤接口
    @FunctionalInterface
    public interface ChainStep<I, O> {
        O execute(I input, Map<String, Object> context);
    }

    // 复合链式管道构建器
    public static class ComposableChain<I, O> {
        private final List<ChainStep<Object, Object>> steps = new ArrayList<>();
        private Function<Throwable, O> fallbackHandler;

        @SuppressWarnings("unchecked")
        public <NEXT> ComposableChain<I, NEXT> pipe(ChainStep<O, NEXT> nextStep) {
            steps.add((input, ctx) -> nextStep.execute((O) input, ctx));
            return (ComposableChain<I, NEXT>) this;
        }

        public ComposableChain<I, O> withFallback(Function<Throwable, O> fallback) {
            this.fallbackHandler = fallback;
            return this;
        }

        @SuppressWarnings("unchecked")
        public O invoke(I initialInput, Map<String, Object> context) {
            Object current = initialInput;
            try {
                for (ChainStep<Object, Object> step : steps) {
                    current = step.execute(current, context);
                }
                return (O) current;
            } catch (Exception ex) {
                if (fallbackHandler != null) {
                    return fallbackHandler.apply(ex);
                }
                throw new RuntimeException("链式调用执行异常", ex);
            }
        }
    }

    // 创建新链实例
    public <T> ComposableChain<T, T> createPipeline() {
        return new ComposableChain<>();
    }

    // 构建一个典型的企业提示词组装加模型推理加输出解析复合链
    public ComposableChain<String, Map<String, Object>> buildRAGInferenceChain(
            Function<String, String> promptFormatter,
            Function<String, String> modelInvoker,
            Function<String, Map<String, Object>> outputParser
    ) {
        ComposableChain<String, String> startChain = createPipeline();
        return startChain
                .pipe((input, ctx) -> promptFormatter.apply(input))
                .pipe((formattedPrompt, ctx) -> modelInvoker.apply(formattedPrompt))
                .pipe((rawResponse, ctx) -> outputParser.apply(rawResponse));
    }
}
