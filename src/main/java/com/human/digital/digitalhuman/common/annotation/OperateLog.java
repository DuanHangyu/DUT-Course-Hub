package com.human.digital.digitalhuman.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标记在方法上，记录用户操作
 *
 * @Author taoHouChao
 * @Date 10:30 2026/3/13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {

    /**
     * 操作描述
     */
    String value() default "";

}
