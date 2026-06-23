package com.human.digital.digitalhuman.config;

import cn.dev33.satoken.exception.NotLoginException;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

/**
 * @Author taoHouChao
 * @Date 12:27 2025/6/7
 */
@RestControllerAdvice
@Slf4j
public class GlobalException {

    @ExceptionHandler(BusinessException.class)
    public BaseResult<Void> handleException(BusinessException e){
        log.error("e", e);
        return BaseResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public BaseResult<Void> handleException(NotLoginException e){
        log.error("e", e);
        return BaseResult.error(401, e.getMessage());
    }

    /**
     * 参数校验失败（@Valid 触发的 @NotNull/@NotBlank 等）。
     * 返回第一条字段错误的提示信息，避免被兜底处理器吞成"系统异常"。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResult<Void> handleValidException(MethodArgumentNotValidException e){
        String msg = Optional.ofNullable(e.getBindingResult())
                .flatMap(r -> r.getFieldErrors().stream().map(FieldError::getDefaultMessage).findFirst())
                .orElse("参数校验失败");
        log.warn("param validation failed: {}", msg);
        return BaseResult.error(400, msg);
    }

    @ExceptionHandler(Exception.class)
    public BaseResult<Void> handleException(Exception e){
        log.error("e", e);
        return BaseResult.error(500, "系统异常");
    }
}
