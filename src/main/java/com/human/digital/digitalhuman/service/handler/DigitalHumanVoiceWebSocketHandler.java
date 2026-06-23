package com.human.digital.digitalhuman.service.handler;

import cn.hutool.json.JSONUtil;
import com.human.digital.digitalhuman.service.model.WebSocketResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @USER taoHouChao
 * @DATE 15:03 2025/6/20
 */
@Slf4j
@AllArgsConstructor
public class DigitalHumanVoiceWebSocketHandler extends AbstractWebSocketHandler {

    private final ArsHandler arsHandler;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = session.getAttributes().get("userId").toString();
        Integer studentId = Integer.parseInt(userId);
        SessionManager.addVoiceSession(studentId, session);
    }

    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message) throws Exception {

        String text = message.getPayload();
        RequestMessage requestMessage = JSONUtil.toBean(text, RequestMessage.class);
        // 心跳检测
        if (requestMessage.ping()) {
            sendMessage(session, WebSocketResponse.ofPong());
            return;
        }

        // 语音识别结束标记
        if (requestMessage.arsFinish()) {
            arsHandler.stop(session);
        }
    }

    @Override
    protected void handleBinaryMessage(@NotNull WebSocketSession session, @NotNull BinaryMessage message) {
        log.info("handleBinaryMessage message:{}", JSONUtil.toJsonStr(message));
        ByteBuffer payload = message.getPayload();
        String userId = session.getAttributes().get("userId").toString();
        Integer studentId = Integer.parseInt(userId);
        WebSocketSession textSession = SessionManager.getTextSession(studentId);
        arsHandler.process(session,
                payload.array(),
                (content) -> sendMessage(textSession, WebSocketResponse.ofArsText(content, false)),
                (error) -> {
                    log.error("handlerBinaryMessage error:{}", error);
                    sendMessage(textSession, WebSocketResponse.ofArsText("语音识别异常", true));
                }, (content) -> sendMessage(textSession, WebSocketResponse.ofArsText("", true)));
    }

    private void sendMessage(WebSocketSession session, WebSocketResponse response) {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(response)));
            } catch (IOException e) {
                log.error("sendMessage error", e);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus closeStatus) {
        log.info("websocket close, status:{}", closeStatus);
        String userId = session.getAttributes().get("userId").toString();
        Integer studentId = Integer.parseInt(userId);
        SessionManager.removeVoiceSession(studentId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, @NotNull Throwable exception) {
        log.error("websocket连接出错，sessionId:{}", session.getId(), exception);
        String userId = session.getAttributes().get("userId").toString();
        Integer studentId = Integer.parseInt(userId);
        SessionManager.removeVoiceSession(studentId);
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

    @Data
    private static class RequestMessage {

        private static final String ARS_FINISH = "ars_finish";

        private static final String ARS_START = "ars_start";

        private static final String PING = "ping";

        private String action;

        public boolean ping() {
            return Objects.equals(PING, action);
        }

        public boolean arsFinish() {
            return Objects.equals(ARS_FINISH, action);
        }

        public boolean arsStart() {
            return Objects.equals(ARS_START, action);
        }
    }
}
