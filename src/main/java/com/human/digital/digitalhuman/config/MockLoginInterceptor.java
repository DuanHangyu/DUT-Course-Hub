package com.human.digital.digitalhuman.config;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 仅用于本地开发联调的模拟登录拦截器。
 *
 * <p>由 {@link InterceptorConfig} 在 dev profile 且 {@code app.mock-login.enabled=true} 时构造并注册，
 * 缺省 userId=25，可由 {@code app.mock-login.user-id} 覆盖。
 *
 * @USER taoHouChao
 * @DATE 13:59 2025/12/19
 */
@Slf4j
public class MockLoginInterceptor implements HandlerInterceptor {

    private final int mockUserId;

    public MockLoginInterceptor(int mockUserId) {
        this.mockUserId = mockUserId;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            StpUtil.checkLogin();
        } catch (Exception e) {
            StpUtil.login(mockUserId);
            log.warn("MockLoginInterceptor preHandle: 模拟登录激活, userId={}, token={} — 仅限本地开发, 切勿用于生产",
                    mockUserId, StpUtil.getTokenInfo().getTokenValue());
        }
        return true;
    }
}
