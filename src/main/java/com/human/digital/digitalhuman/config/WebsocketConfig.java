package com.human.digital.digitalhuman.config;

import com.alibaba.nls.client.protocol.NlsClient;
import com.human.digital.digitalhuman.common.utils.TokenUtils;
import com.human.digital.digitalhuman.repository.service.CourseNodeService;
import com.human.digital.digitalhuman.service.AnswerAppService;
import com.human.digital.digitalhuman.service.AssessService;
import com.human.digital.digitalhuman.service.ChatService;
import com.human.digital.digitalhuman.service.handler.ArsHandler;
import com.human.digital.digitalhuman.service.handler.DigitalHumanVoiceWebSocketHandler;
import com.human.digital.digitalhuman.service.handler.DigitalHumanWebSocketHandler;
import com.human.digital.digitalhuman.service.handler.TtsHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @Author taoHouChao
 * @Date 13:56 2025/6/7
 */
@Slf4j
@Configuration
@EnableWebSocket
public class WebsocketConfig implements WebSocketConfigurer {

    @Resource
    private ChatService chatService;

    @Value("${aliyun.voice-interaction.appKey}")
    private String appKey;

    @Resource
    private NlsClient nlsClient;

    @Resource
    private TokenUtils tokenUtils;

    @Resource
    private ArsHandler arsHandler;

    @Resource
    private TtsHandler ttsHandler;

    @Resource
    private AssessService questionService;

    @Resource
    private AnswerAppService answerAppService;

    @Resource
    private CourseNodeService courseNodeService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateNlsToken() {
        nlsClient.setToken(tokenUtils.accessToken());
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new DigitalHumanWebSocketHandler(
                                chatService,
                                ttsHandler,
                                questionService,
                                answerAppService,
                                courseNodeService),
                        "/digital-human")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new WebSocketHandshakeInterceptor());
        registry.addHandler(new DigitalHumanVoiceWebSocketHandler(arsHandler), "/digital-human-voice")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new WebSocketHandshakeInterceptor());
    }
}
