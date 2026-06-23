package com.human.digital.digitalhuman.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.human.digital.digitalhuman.service.ConversationHistoryService;
import com.human.digital.digitalhuman.service.model.dto.CustomerServiceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 对话历史服务 Redis 实现
 *
 * @Author taoHouChao
 * @Date 10:00 2025/3/13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationHistoryServiceImpl implements ConversationHistoryService {

    private static final String KEY_PREFIX = "customer:chat:";
    private static final int MAX_HISTORY_SIZE = 10;
    private static final long EXPIRE_DAYS = 7;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void saveMessage(String sessionId, String role, String content) {
        String key = KEY_PREFIX + sessionId;
        try {
            List<CustomerServiceDTO> history = getHistory(sessionId);

            CustomerServiceDTO message = new CustomerServiceDTO();
            message.setMessage(content);
            // 根据 role 设置 answer 或 message
            if ("assistant".equals(role)) {
                message.setAnswer(content);
            } else {
                message.setMessage(content);
            }

            history.add(message);

            // 只保留最近 N 条
            if (history.size() > MAX_HISTORY_SIZE) {
                history = history.subList(history.size() - MAX_HISTORY_SIZE, history.size());
            }

            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(history),
                    EXPIRE_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("保存对话历史失败, sessionId:{}", sessionId, e);
        }
    }

    @Override
    public List<CustomerServiceDTO> getHistory(String sessionId) {
        String key = KEY_PREFIX + sessionId;
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.error("获取对话历史失败, sessionId:{}", sessionId, e);
        }
        return new ArrayList<>();
    }

    @Override
    public void clearHistory(String sessionId) {
        String key = KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
    }
}
