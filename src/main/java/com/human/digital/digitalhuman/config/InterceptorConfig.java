package com.human.digital.digitalhuman.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 仅在 dev profile 时考虑注册模拟登录拦截器。
 *
 * <p>双重保护：
 * <ol>
 *   <li>类级 {@code @Profile("dev")} 限定 dev profile（兼容 {@code dev,local} 等多 profile 列表）</li>
 *   <li>类级 {@code @ConditionalOnProperty} 要求 {@code app.mock-login.enabled=true} 才真注册</li>
 * </ol>
 *
 * <p>路径限定为 {@code /api/**}，避免误伤其他端点。
 *
 * @USER taoHouChao
 * @DATE 13:57 2025/12/19
 */
@Configuration
@Profile("dev")
@ConditionalOnProperty(prefix = "app.mock-login", name = "enabled", havingValue = "true")
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {

    @Value("${app.mock-login.user-id:25}")
    private int mockUserId;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.warn("InterceptorConfig: 模拟登录已启用 (app.mock-login.enabled=true, userId={}) — 仅限本地开发, 切勿用于生产",
                mockUserId);
        registry.addInterceptor(new MockLoginInterceptor(mockUserId))
                .addPathPatterns("/api/**");
    }
}
