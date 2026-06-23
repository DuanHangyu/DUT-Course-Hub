package com.human.digital.digitalhuman.service.impl;

import com.human.digital.digitalhuman.service.*;
import com.human.digital.digitalhuman.service.model.dto.CustomerServiceDTO;
import com.human.digital.digitalhuman.service.model.enums.IntentTypeEnums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

/**
 * 客服服务实现
 *
 * @Author taoHouChao
 * @Date 10:00 2025/3/13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceAppServiceImpl implements CustomerServiceAppService {

    private static final String SYSTEM_PROMPT = """
            你是一个大学教务智能助手，专门为老师提供教学管理支持。请根据以下背景信息回答用户问题。

            背景信息：
            {context}

            角色定位：
            - 你是一位专业的教务助手
            - 你需要帮助老师了解课程情况、学生学习状态、作业提交情况等

            回答要求：
            1. 只根据提供的背景信息回答，不要编造不存在的数据
            2. 回答要简洁明了，使用清晰的结构
            3. 如果数据库中没有相关信息，请明确告知老师
            4. 对于统计数据，尽量给出具体数字
            5. 如果用户问题不明确，可以询问具体信息
            """;

    private final ConversationHistoryService conversationHistoryService;
    private final IntentClassifierService intentClassifierService;
    private final RagRetrievalService ragRetrievalService;
    private final ChatClient chatClient;
    private final CustomerServiceWebSocketService webSocketService;

    @Override
    public CustomerServiceDTO submitChat(CustomerServiceDTO dto) {
        final String message = dto.getMessage();
        final Integer userId = dto.getUserId();
        final String sessionId;

        // 生成 sessionId
        if (dto.getSessionId() == null || dto.getSessionId().isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        } else {
            sessionId = dto.getSessionId();
        }

        dto.setSessionId(sessionId);

        // 异步执行对话，通过 WebSocket 推送结果
        Thread.ofVirtual().start(() -> {
            try {
                processChatAsync(userId, sessionId, message);
            } catch (Exception e) {
                log.error("异步对话处理失败, userId:{}, sessionId:{}", userId, sessionId, e);
                if (userId != null) {
                    webSocketService.sendMessage(userId, "处理对话时发生错误: " + e.getMessage(), true);
                }
            }
        });

        return dto;
    }

    /**
     * 异步处理对话，流式推送结果
     */
    private void processChatAsync(Integer userId, String sessionId, String message) {
        try {
            // 1. 识别意图
            IntentTypeEnums intentType = intentClassifierService.classify(message);

            // 2. 检索相关数据
            List<String> retrievedData = ragRetrievalService.retrieve(message, intentType);
            String context = ragRetrievalService.getContext(intentType);

            // 添加检索结果到上下文
            if (!retrievedData.isEmpty()) {
                context += "\n\n相关数据：\n" + String.join("\n", retrievedData);
            }

            // 3. 构建 Prompt
            String prompt = SYSTEM_PROMPT.replace("{context}", context) + "\n\n用户问题：" + message;

            // 4. 流式调用 LLM，通过 WebSocket 推送
            StringBuilder fullAnswer = new StringBuilder();

            Flux<String> flux = chatClient.prompt()
                    .user(prompt)
                    .stream()
                    .content();

            // 订阅流式响应并推送
            flux.doOnComplete(() -> {
                // 发送完成标记
                if (userId != null) {
                    webSocketService.sendMessage(userId, "", true);
                }
                // 保存对话历史
                saveHistory(sessionId, message, fullAnswer.toString());
            }).doOnError(e -> {
                if (userId != null) {
                    webSocketService.sendMessage(userId, "生成答案失败: " + e.getMessage(), true);
                }
            }).subscribe(content -> {
                if (content != null && !content.isEmpty()) {
                    fullAnswer.append(content);
                    if (userId != null) {
                        webSocketService.sendMessage(userId, content, false);
                    }
                }
            });

        } catch (Exception e) {
            log.error("对话处理异常, sessionId:{}", sessionId, e);
            if (userId != null) {
                webSocketService.sendMessage(userId, "处理失败: " + e.getMessage(), true);
            }
        }
    }

    /**
     * 保存对话历史
     */
    private void saveHistory(String sessionId, String userMessage, String assistantMessage) {
        try {
            conversationHistoryService.saveMessage(sessionId, "user", userMessage);
            conversationHistoryService.saveMessage(sessionId, "assistant", assistantMessage);
        } catch (Exception e) {
            log.error("保存对话历史失败", e);
        }
    }

    @Override
    public CustomerServiceDTO chat(CustomerServiceDTO dto) {
        // 同步版本，保留兼容
        final String message = dto.getMessage();
        final String sessionId;

        // 生成 sessionId
        if (dto.getSessionId() == null || dto.getSessionId().isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        } else {
            sessionId = dto.getSessionId();
        }

        // 获取对话历史
        List<CustomerServiceDTO> history = conversationHistoryService.getHistory(sessionId);

        // 识别意图
        IntentTypeEnums intentType = intentClassifierService.classify(message);
        dto.setIntentType(intentType.getCode());

        // 检索相关数据
        List<String> retrievedData = ragRetrievalService.retrieve(message, intentType);
        String context = ragRetrievalService.getContext(intentType);

        if (!retrievedData.isEmpty()) {
            context += "\n\n相关数据：\n" + String.join("\n", retrievedData);
        }

        String prompt = SYSTEM_PROMPT.replace("{context}", context) + "\n\n用户问题：" + message;

        String answer = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        dto.setSessionId(sessionId);
        dto.setAnswer(answer);

        conversationHistoryService.saveMessage(sessionId, "user", message);
        conversationHistoryService.saveMessage(sessionId, "assistant", answer);

        return dto;
    }

    @Override
    public CustomerServiceDTO getHistory(String sessionId) {
        CustomerServiceDTO dto = new CustomerServiceDTO();
        dto.setSessionId(sessionId);
        dto.setMessage(String.valueOf(conversationHistoryService.getHistory(sessionId)));
        return dto;
    }
}
