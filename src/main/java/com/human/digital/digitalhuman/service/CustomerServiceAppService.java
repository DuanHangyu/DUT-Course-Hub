package com.human.digital.digitalhuman.service;

import com.human.digital.digitalhuman.service.model.dto.CustomerServiceDTO;

/**
 * 客服服务接口
 *
 * @Author taoHouChao
 * @Date 10:00 2025/3/13
 */
public interface CustomerServiceAppService {

    /**
     * 提交对话任务（异步，通过 WebSocket 返回结果）
     *
     * @param dto 请求参数
     * @return 包含 sessionId
     */
    CustomerServiceDTO submitChat(CustomerServiceDTO dto);

    /**
     * 对话（同步，返回完整答案 - 保留兼容）
     *
     * @param dto 请求参数
     * @return 响应
     */
    CustomerServiceDTO chat(CustomerServiceDTO dto);

    /**
     * 获取对话历史
     *
     * @param sessionId 会话ID
     * @return 历史记录
     */
    CustomerServiceDTO getHistory(String sessionId);
}
