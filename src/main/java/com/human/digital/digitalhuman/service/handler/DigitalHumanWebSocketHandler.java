package com.human.digital.digitalhuman.service.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.human.digital.digitalhuman.common.exception.AssessedException;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.utils.MarkdownCleaner;
import com.human.digital.digitalhuman.repository.po.CourseNodePO;
import com.human.digital.digitalhuman.repository.service.CourseNodeService;
import com.human.digital.digitalhuman.service.AnswerAppService;
import com.human.digital.digitalhuman.service.AssessService;
import com.human.digital.digitalhuman.service.ChatService;
import com.human.digital.digitalhuman.service.model.WebSocketResponse;
import com.human.digital.digitalhuman.service.model.dto.QuestionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import reactor.core.Disposable;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @Author taoHouChao
 * @Date 14:21 2025/6/7
 */
@Slf4j
@AllArgsConstructor
public class DigitalHumanWebSocketHandler extends AbstractWebSocketHandler {

    private static final int BUFFER_THRESHOLD = 3;

    private static final String COURSE_QUESTION = "请根据视频内容，对问题做出回答" +
            "**视频内容**:" +
            "%s" +
            "**问题**:" +
            "%s";

    private static final Map<WebSocketSession, Disposable> SESSION_DISPOSABLE_MAP = new ConcurrentHashMap<>(16);

    private final ChatService chatService;

    private final TtsHandler ttsHandler;

    private final AssessService questionService;

    private final AnswerAppService answerAppService;

    private final CourseNodeService courseNodeService;

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) {
        String userId = session.getAttributes().get("userId").toString();
        Integer studentId = Integer.parseInt(userId);
        SessionManager.addTextSession(studentId, session);
    }

    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) {
        String text = message.getPayload();
        RequestMessage requestMessage = JSONUtil.toBean(text, RequestMessage.class);
        // 停止输出
        if (requestMessage.stop()) {
            Disposable disposable = SESSION_DISPOSABLE_MAP.get(session);
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
            // 停止发送
            ttsHandler.stop(session);
            return;
        }
        // 心跳检测
        if (requestMessage.ping()) {
            sendMessage(session, WebSocketResponse.ofPong(), null);
            return;
        }
        String userId = session.getAttributes().get("userId").toString();
        Integer studentId = Integer.parseInt(userId);
        WebSocketSession voiceSession = SessionManager.getVoiceSession(studentId);
        log.info("requestMessage:{}", requestMessage);
        // 问答，返回文本和语音
        if (requestMessage.generateVoice()) {
            String question = requestMessage.getText();
            // 根据课程内容进行问答
            Integer courseId = 0;
            Integer courseNodeId = 0;
            if (StrUtil.isNotBlank(requestMessage.getId())) {
                CourseNodePO courseNode = courseNodeService.getById(Long.parseLong(requestMessage.getId()));
                question = String.format(COURSE_QUESTION, courseNode.getContent(), question);
                courseId = courseNode.getCourseId();
                courseNodeId = courseNode.getId();
            }
            Integer logId = answerAppService.saveLog(studentId, requestMessage.getText(), session.getId(), courseId, courseNodeId);
            StringBuilder voiceResponse = new StringBuilder();
            StringBuilder textResponse = new StringBuilder();
            StringBuilder answer = new StringBuilder();
            AtomicLong lastSendTime = new AtomicLong(System.currentTimeMillis());
            boolean successTts = false;
            // 发送语音的session

            try {
                ttsHandler.start(session,
                        (consumer) -> sendVoiceMessage(voiceSession, WebSocketResponse.ofVoice(consumer, false)),
                        (finish) -> sendVoiceMessage(voiceSession, WebSocketResponse.ofVoice(new byte[0], true)),
                        (error) -> log.info("tts error:{}", error));
                successTts = true;
            } catch (Exception e) {
                log.error("get tts error", e);
            }
            boolean finalSuccessTts = successTts;
            Disposable disposable = chatService.chatStream(question).subscribe(content -> {
                log.info("content:{}", content);
                textResponse.append(content);
                answer.append(content);
                if (textResponse.length() > BUFFER_THRESHOLD || (System.currentTimeMillis() - lastSendTime.get()) > 100) {
                    // 发送文本
                    sendMessage(session, WebSocketResponse.ofText(textResponse.toString(), false), s -> SESSION_DISPOSABLE_MAP.get(session).dispose());
                    textResponse.setLength(0);
                    lastSendTime.set(System.currentTimeMillis());
                }

                // 发送语音
                if (StrUtil.isNotBlank(content)) {
                    String chaunk = MarkdownCleaner.cleanMarkdown(content);
                    if (StrUtil.isNotBlank(chaunk)) {
                        voiceResponse.append(chaunk);
                        if (shouldSynthesize(voiceResponse.toString())) {
                            ttsHandler.streamProcess(session, voiceResponse.toString());
                            voiceResponse.setLength(0);
                        }
                    }
                }
            }, error -> {
                log.error("tts error", error);
                // 发送结束消息
                sendMessage(session, WebSocketResponse.ofText("AI服务错误", true), null);
            }, () -> {
                log.info("send last info");
                // 发送结束消息
                if (!textResponse.isEmpty()) {
                    log.info("textResponse last:{}", textResponse);
                    sendMessage(session, WebSocketResponse.ofText(textResponse.toString(), false), null);
                }
                sendMessage(session, WebSocketResponse.ofText("", true), null);
                if (!voiceResponse.isEmpty()) {
                    log.info("voiceResponse last:{}", voiceResponse);
                    if (finalSuccessTts) {
                        ttsHandler.streamProcess(session, voiceResponse.toString());
                    }
                }
                // 停止发送
                ttsHandler.stop(session);
                // 记录问答日志
                answerAppService.updateLogAnswer(logId, answer.toString());
            });
            SESSION_DISPOSABLE_MAP.put(session, disposable);
        } else if (requestMessage.nodeAssess()) {
            handlerAssess(session, studentId, requestMessage.text, ttsHandler, questionService::generateQuestion);
        } else if (requestMessage.finalAssess()) {
            handlerAssess(session, studentId, requestMessage.text, ttsHandler, questionService::generateFinalQuestion);
        }
    }


    private String getQuestion(RequestMessage requestMessage) {
        String question = requestMessage.getText();
        // 根据课程内容进行问答
        if (StrUtil.isNotBlank(requestMessage.getId())) {
            CourseNodePO courseNode = courseNodeService.getById(Long.parseLong(requestMessage.getId()));
            question = String.format(COURSE_QUESTION, courseNode.getContent(), question);
        }
        return question;
    }

    private void handlerAssess(WebSocketSession session,
                               Integer studentId,
                               String text,
                               TtsHandler ttsService,
                               GenerateQuestionHandler handler) {
        try {
            Integer id = Integer.parseInt(text);
            QuestionDTO question = handler.handle(id, studentId);
            sendMessage(session, WebSocketResponse.of(JSONUtil.toJsonStr(question), question.getLast() ? "last-question" : "question", true), null);
            ttsService.process(question.getTitle(), (speech) -> sendMessage(SessionManager.getVoiceSession(studentId), WebSocketResponse.ofVoice(speech, false), null));
        } catch (NumberFormatException e) {
            log.info("studentId is not number");
            // 发送结束消息
            sendMessage(session, WebSocketResponse.ofError("参数错误"), null);
        } catch (AssessedException e) {
            log.info("assess error", e);
            sendMessage(session, WebSocketResponse.ofAssessedFinal(e.getMessage()), null);
        } catch (BusinessException e) {
            log.info("business error", e);
            sendMessage(session, WebSocketResponse.ofError(e.getMessage()), null);
        } catch (Exception e) {
            log.info("unknown error", e);
            sendMessage(session, WebSocketResponse.ofError("未知错误"), null);
        }
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, @NotNull PongMessage message) throws Exception {
        log.info("handlePongMessage");
        session.sendMessage(new TextMessage(JSONUtil.toJsonStr(WebSocketResponse.ofPong())));
    }

    @Override
    public void handleTransportError(WebSocketSession session, @NotNull Throwable exception) {
        log.error("websocket连接出错，sessionId:{}", session.getId(), exception);
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) {
        log.info("websocket close");
        String userId = session.getAttributes().get("userId").toString();
        Integer studentId = Integer.parseInt(userId);
        SessionManager.removeTextSession(studentId);
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

    /**
     * 判断是否应该合成语音
     */
    private boolean shouldSynthesize(String text) {
        // 简单逻辑：积累到一定长度或遇到标点
        boolean maxLength = text.length() > 20;
        boolean condition = text.length() > 8 && (text.endsWith("，") ||
                text.endsWith("。") ||
                text.endsWith("？") ||
                text.endsWith("！"));
        return maxLength || condition;
    }

    private void sendMessage(WebSocketSession session, WebSocketResponse response, Consumer<String> errorConsumer) {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(response)));
            } catch (IOException e) {
                log.error("sendMessage error", e);
                if (errorConsumer != null) {
                    errorConsumer.accept("AI服务错误");
                }
            }
        }
    }

    private void sendVoiceMessage(WebSocketSession session, WebSocketResponse response) {
        sendMessage(session, response, null);
    }

    @Data
    private static class RequestMessage {

        private static final String GENERATE_VOICE = "generate_voice";
        private static final String STOP = "stop";
        private static final String GENERATE_QUESTION = "generate_question";
        private static final String GENERATE_QUESTION_FINAL = "generate_question_final";
        private static final String PING = "ping";
        private static final String ARS_FINISH = "ars_finish";

        private String action;

        private String text;

        private String id;

        public boolean ping() {
            return Objects.equals(PING, action);
        }

        public boolean generateVoice() {
            return Objects.equals(GENERATE_VOICE, action);
        }

        public boolean stop() {
            return Objects.equals(STOP, action);
        }

        public boolean nodeAssess() {
            return Objects.equals(GENERATE_QUESTION, action);
        }

        public boolean finalAssess() {
            return Objects.equals(GENERATE_QUESTION_FINAL, action);
        }
    }

    @FunctionalInterface
    private interface GenerateQuestionHandler {
        QuestionDTO handle(Integer id, Integer studentId);
    }
}
