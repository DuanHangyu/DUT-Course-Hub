package com.human.digital.digitalhuman.service;

import com.human.digital.digitalhuman.service.model.enums.IntentTypeEnums;

import java.util.List;

/**
 * RAG 检索服务接口
 *
 * @Author taoHouChao
 * @Date 10:00 2025/3/13
 */
public interface RagRetrievalService {

    /**
     * 根据意图检索相关数据
     *
     * @param message   用户问题
     * @param intentType 意图类型
     * @return 检索结果列表
     */
    List<String> retrieve(String message, IntentTypeEnums intentType);

    /**
     * 获取特定类型的上下文
     *
     * @param intentType 意图类型
     * @return 上下文字符串
     */
    String getContext(IntentTypeEnums intentType);
}
