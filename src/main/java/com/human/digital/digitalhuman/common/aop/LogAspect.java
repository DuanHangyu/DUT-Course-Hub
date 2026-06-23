package com.human.digital.digitalhuman.common.aop;

import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Stream;

/**
 * @Author taoHouChao
 * @Date 09:47 2025/5/18
 */
@Aspect
@Component
@Slf4j
public class LogAspect {

    @Around("execution(* com.human.digital.digitalhuman.controller..*.*(..))") // 拦截 controller 包下的所有方法
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();
        List<Object> params = Stream.of(args).filter(item -> item != null
                && !(item instanceof HttpServletRequest)
                && !(item instanceof HttpServletResponse)
                && !(item instanceof MultipartFile)).toList();
        log.info("Method {}.{}() called with args: {}", className, methodName, JSONUtil.toJsonStr(params));

        long startTime = System.currentTimeMillis();
        Object result = null; // 执行目标方法
        try {
            result = joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("Method {}.{}() response {} executed in {}ms", className, methodName, result, duration);
        }
        return result;
    }
}
