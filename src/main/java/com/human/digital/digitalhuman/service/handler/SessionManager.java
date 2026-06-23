package com.human.digital.digitalhuman.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @USER taoHouChao
 * @DATE 14:57 2025/6/20
 */
@Slf4j
public class SessionManager {

    private static final Map<Integer, WebSocketSession> VOICE_SESSION = new ConcurrentHashMap<>(100);

    private static final Map<WebSocketSession, ReentrantLock> SESSION_LOCK_MAP = new ConcurrentHashMap<>(16);

    private static final Map<Integer, WebSocketSession> TEXT_SESSION = new ConcurrentHashMap<>(16);

    // 客服对话 sessionId -> WebSocketSession
    private static final Map<String, WebSocketSession> CUSTOMER_SERVICE_SESSION = new ConcurrentHashMap<>(16);

    public static void addVoiceSession(Integer studentId, WebSocketSession session) {
        VOICE_SESSION.put(studentId, session);
    }

    public static void addTextSession(Integer studentId, WebSocketSession session) {
        TEXT_SESSION.put(studentId, session);
    }

    public static WebSocketSession getVoiceSession(Integer studentId) {
        return VOICE_SESSION.get(studentId);
    }

    public static WebSocketSession getTextSession(Integer studentId) {
        return TEXT_SESSION.get(studentId);
    }

    public static void removeVoiceSession(Integer studentId) {
        WebSocketSession webSocketSession = VOICE_SESSION.get(studentId);
        if (webSocketSession != null) {
            SESSION_LOCK_MAP.remove(webSocketSession);
        }
        WebSocketSession remove = VOICE_SESSION.remove(studentId);
        try {
            remove.close();
        } catch (IOException e) {
            log.error("关闭语音socket失败", e);
        }
    }

    public static void removeTextSession(Integer studentId) {
        WebSocketSession remove = TEXT_SESSION.remove(studentId);
        try {
            remove.close();
        } catch (IOException e) {
            log.error("关闭文本socket失败", e);
        }
    }

    /**
     * 添加客服对话 session
     */
    public static void addCustomerServiceSession(String sessionId, WebSocketSession session) {
        CUSTOMER_SERVICE_SESSION.put(sessionId, session);
    }

    /**
     * 获取客服对话 session
     */
    public static WebSocketSession getCustomerServiceSession(String sessionId) {
        return CUSTOMER_SERVICE_SESSION.get(sessionId);
    }

    /**
     * 移除客服对话 session
     */
    public static void removeCustomerServiceSession(String sessionId) {
        WebSocketSession remove = CUSTOMER_SERVICE_SESSION.remove(sessionId);
        if (remove != null) {
            try {
                remove.close();
            } catch (IOException e) {
                log.error("关闭客服对话socket失败", e);
            }
        }
    }

//    public static Lock getSessionLock(WebSocketSession session) {
//        return SESSION_LOCK_MAP.get(session);
//    }
}
