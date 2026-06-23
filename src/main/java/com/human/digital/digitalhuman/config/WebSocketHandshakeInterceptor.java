package com.human.digital.digitalhuman.config;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;

/**
 * @Author taoHouChao
 * @Date 14:24 2025/6/7
 */
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.info("beforeHandshake");
        String token = request.getHeaders().getFirst("Sec-WebSocket-Protocol");
        log.info("beforeHandshake token:{}", token);
        String userId = Optional.ofNullable(StpUtil.getLoginIdByToken(token)).map(Object::toString).orElse("");
        log.info("beforeHandshake userId:{}", userId);
        if (StrUtil.isNotBlank(userId)) {
            attributes.put("userId", userId);
            return true;
        }
        log.error("link websocket fail");
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        log.info("afterHandshake");
        String token = request.getHeaders().getFirst("Sec-WebSocket-Protocol");
        response.getHeaders().add("Sec-WebSocket-Protocol", token);
    }
}
