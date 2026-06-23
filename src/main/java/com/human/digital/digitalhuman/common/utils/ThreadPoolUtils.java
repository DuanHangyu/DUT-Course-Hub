package com.human.digital.digitalhuman.common.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author taoHouChao
 * @Date 10:18 2025/6/8
 */
public class ThreadPoolUtils {

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, 50, 60L, TimeUnit.MINUTES, new ArrayBlockingQueue<>(1000));

    public static void execute(Runnable runnable) {
        EXECUTOR.execute(runnable);
    }

    public static ThreadPoolExecutor getExecutor(){
        return EXECUTOR;
    }
}
