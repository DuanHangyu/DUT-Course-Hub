package com.human.digital.digitalhuman.service;

import com.human.digital.digitalhuman.service.model.enums.IntentTypeEnums;

/**
 * 意图识别服务接口
 *
 * @Author taoHouChao
 * @Date 10:00 2025/3/13
 */
public interface IntentClassifierService {

    /**
     * 识别用户意图
     *
     * @param message 用户问题
     * @return 意图类型
     */
    IntentTypeEnums classify(String message);
}
