package com.human.digital.digitalhuman.common.aop;

import cn.dev33.satoken.stp.StpUtil;
import com.human.digital.digitalhuman.common.annotation.OperateLog;
import com.human.digital.digitalhuman.repository.mapper.OperateLogMapper;
import com.human.digital.digitalhuman.repository.po.OperateLogPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 拦截带有 @OperateLog 注解的方法，记录用户操作
 *
 * @Author taoHouChao
 * @Date 10:40 2026/3/13
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class OperateLogAspect {

    private final OperateLogMapper operateLogMapper;

    @Around("@annotation(com.human.digital.digitalhuman.common.annotation.OperateLog)")
    public Object logOperate(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperateLog operateLog = method.getAnnotation(OperateLog.class);
        String operation = operateLog.value();

        // 执行目标方法
        Object result = joinPoint.proceed();

        // 记录操作日志
        try {
            Integer userId = null;
            try {
                userId = StpUtil.getLoginIdAsInt();
            } catch (Exception e) {
                log.warn("获取登录用户ID失败，可能未登录");
            }

            if (userId != null) {
                OperateLogPO logPO = new OperateLogPO();
                logPO.setUserId(userId);
                logPO.setOperation(operation);
                logPO.setOperateTime(LocalDateTime.now());
                operateLogMapper.insert(logPO);
            }
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }

        return result;
    }
}
