package com.human.digital.digitalhuman.config;

import com.human.digital.digitalhuman.common.enums.AccessPolicyEnums;
import com.human.digital.digitalhuman.repository.po.SchoolPO;
import com.human.digital.digitalhuman.repository.service.SchoolService;
import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * IP 白名单过滤器
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/4
 */
@Slf4j
@Configuration
public class IpWhitelistFilterConfig {

    @Resource
    private SchoolService schoolService;

    @Bean
    public FilterRegistrationBean<Filter> ipWhitelistFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                HttpServletResponse httpResponse = (HttpServletResponse) response;

                // 获取请求域名
                String host = httpRequest.getHeader("Host");
                if (StringUtils.isBlank(host)) {
                    host = httpRequest.getServerName();
                }

                // 查询学校配置
                SchoolPO school = schoolService.getByDomain(host);
                if (school == null) {
                    // 未匹配到学校配置，放行
                    chain.doFilter(request, response);
                    return;
                }

                // 检查学校是否启用
                if (school.getState() == null || school.getState() != 1) {
                    httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
                    httpResponse.getWriter().write("{\"code\":403,\"message\":\"学校已禁用\"}");
                    return;
                }

                // 检查访问策略
                if (school.getAccessPolicy() != null && school.getAccessPolicy() == AccessPolicyEnums.IP_WHITELIST.getCode()) {
                    // IP 白名单模式，检查客户端 IP
                    String clientIp = getClientIp(httpRequest);
                    if (StringUtils.isNotBlank(school.getIpWhitelist())) {
                        List<String> whitelist = Arrays.stream(school.getIpWhitelist().split(","))
                                .map(String::trim)
                                .toList();
                        if (!whitelist.contains(clientIp)) {
                            log.warn("IP not in whitelist, clientIp:{}, whitelist:{}", clientIp, school.getIpWhitelist());
                            httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
                            httpResponse.getWriter().write("{\"code\":403,\"message\":\"IP不在白名单中\"}");
                            return;
                        }
                    }
                }

                chain.doFilter(request, response);
            }

            private String getClientIp(HttpServletRequest request) {
                String ip = request.getHeader("X-Forwarded-For");
                if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                // 多个代理时取第一个
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        });
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("ipWhitelistRegistrationBean");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
