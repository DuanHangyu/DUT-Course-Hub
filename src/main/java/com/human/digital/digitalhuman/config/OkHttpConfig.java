package com.human.digital.digitalhuman.config;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @Author taoHouChao
 * @Date 17:37 2025/6/7
 */
@Configuration
public class OkHttpConfig {

    @Value("${bo-er.access-key}")
    private String accessKey;

    @Bean
    public OkHttpClient okHttpClient() {
        Interceptor headerInterceptor = chain -> {
            Request originalRequest = chain.request();
            Request newRequest = originalRequest.newBuilder()
                    .header("accessKey", accessKey)
                    .build();
            return chain.proceed(newRequest);
        };
        return new OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .connectTimeout(3, TimeUnit.MINUTES) // 连接超时
                .readTimeout(3, TimeUnit.MINUTES)    // 读取超时
                .writeTimeout(3, TimeUnit.MINUTES)   // 写入超时
                .retryOnConnectionFailure(true)       // 自动重连
                .build();
    }
}
