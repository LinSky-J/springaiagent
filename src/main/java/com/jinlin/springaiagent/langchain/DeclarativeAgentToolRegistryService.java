package com.jinlin.springaiagent.langchain;

import org.springframework.stereotype.Service;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

// 声明式智能体工具注册与分发服务
// 单一职责 负责在 Java 平台通过注解元数据驱动扫描工具类 提取参数模式规范并路由执行
@Service
public class DeclarativeAgentToolRegistryService {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface AgentTool {
        String name() default "";
        String description();
    }

    public static class ToolDefinition {
        public final String name;
        public final String description;
        public final Map<String, String> parameterTypes;
        public final Object targetInstance;
        public final Method method;

        public ToolDefinition(String name, String description, Map<String, String> parameterTypes, Object targetInstance, Method method) {
            this.name = name;
            this.description = description;
            this.parameterTypes = parameterTypes;
            this.targetInstance = targetInstance;
            this.method = method;
        }
    }

    private final Map<String, ToolDefinition> registeredTools = new HashMap<>();

    // 扫描注册工具实例中带有注解的方法
    public synchronized void registerToolsFromInstance(Object toolBean) {
        Class<?> clazz = toolBean.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(AgentTool.class)) {
                AgentTool annotation = method.getAnnotation(AgentTool.class);
                String toolName = annotation.name().isEmpty() ? method.getName() : annotation.name();
                Map<String, String> paramTypes = new HashMap<>();

                for (Parameter param : method.getParameters()) {
                    paramTypes.put(param.getName(), param.getType().getSimpleName());
                }

                method.setAccessible(true);
                ToolDefinition def = new ToolDefinition(toolName, annotation.description(), paramTypes, toolBean, method);
                registeredTools.put(toolName, def);
            }
        }
    }

    // 根据工具名称与参数列表动态分发执行
    public Object executeTool(String toolName, Object... args) {
        ToolDefinition def = registeredTools.get(toolName);
        if (def == null) {
            throw new IllegalArgumentException("未找到已注册的工具定义 " + toolName);
        }
        try {
            return def.method.invoke(def.targetInstance, args);
        } catch (Exception e) {
            throw new RuntimeException("工具执行失败 " + toolName, e);
        }
    }

    public List<ToolDefinition> getAllToolDefinitions() {
        return new ArrayList<>(registeredTools.values());
    }
}
