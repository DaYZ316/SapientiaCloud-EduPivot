package com.dayz.sapientiacloud_edupivot.celestial_hub.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SaaLLMConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${spring.ai.dashscope.chat.options.model:qwen3-max}")
    private String chatModel;

    @Value("${spring.ai.dashscope.chat.options.temperature:0.7}")
    private Double temperature;

    @Bean(name = "QWen")
    public ChatModel qWen() {
        return DashScopeChatModel.builder()
                .dashScopeApi(DashScopeApi.builder()
                        .apiKey(apiKey)
                        .build())
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withModel(chatModel)
                                .withTemperature(temperature)
                                .build()
                )
                .build();
    }

    @Bean(name = "qWenChatClient")
    public ChatClient qWenChatClient(@Qualifier("QWen") ChatModel qwen) {
        return ChatClient.builder(qwen)
                .defaultOptions(ChatOptions.builder()
                        .model(chatModel)
                        .build())
                .build();
    }
}