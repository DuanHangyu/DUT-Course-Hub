package com.human.digital.digitalhuman.config;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author taoHouChao
 * @Date 20:34 2025/6/7
 */
@Configuration
public class ChatConfig {

    @Resource
    private ChatModel chatModel;

    @Bean
    public ChatClient client() {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}
