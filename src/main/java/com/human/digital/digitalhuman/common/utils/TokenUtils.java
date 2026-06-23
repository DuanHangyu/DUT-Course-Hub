package com.human.digital.digitalhuman.common.utils;

import com.alibaba.nls.client.AccessToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Author taoHouChao
 * @Date 12:21 2025/6/7
 */
@Component
@Slf4j
public class TokenUtils {

    @Value("${aliyun.voice-interaction.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.voice-interaction.accessKeySecret}")
    private String accessKeySecret;

    public String accessToken() {
        try {
            AccessToken accessToken = new AccessToken(accessKeyId, accessKeySecret);
            accessToken.apply();
            String token = accessToken.getToken();
            long expireTime = accessToken.getExpireTime();
            log.info("token: {}, expireTime: {}", token, expireTime);
            return token;
        } catch (IOException e) {
            log.error("get token error", e);
        }
        return "";
    }
}
