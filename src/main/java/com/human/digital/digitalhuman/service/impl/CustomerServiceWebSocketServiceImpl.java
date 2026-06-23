package com.human.digital.digitalhuman.service.impl;

import com.human.digital.digitalhuman.service.CustomerServiceWebSocketService;
import com.human.digital.digitalhuman.service.handler.SessionManager;
import com.human.digital.digitalhuman.service.model.WebSocketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * 客服对话 WebSocket 服务实现
 *
 * @Author taoHouChao
 * @Date 10:00 2025/3/14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceWebSocketServiceImpl implements CustomerServiceWebSocketService {

    @Override
    public void sendMessage(Integer userId, String content, boolean isLast) {
        try {
            // 通过 userId 获取 WebSocketSession
            WebSocketSession session = SessionManager.getTextSession(userId);
            if (session != null && session.isOpen()) {
                WebSocketResponse response = WebSocketResponse.ofText(content, isLast);
                session.sendMessage(new TextMessage(response.getContent()));
            } else {
                log.warn("WebSocket session not found or closed, userId:{}", userId);
            }
        } catch (Exception e) {
            log.error("发送 WebSocket 消息失败, userId:{}", userId, e);
        }
    }
}
