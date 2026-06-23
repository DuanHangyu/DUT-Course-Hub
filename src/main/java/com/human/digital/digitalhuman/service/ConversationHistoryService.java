package com.human.digital.digitalhuman.service;

import com.human.digital.digitalhuman.service.model.dto.CustomerServiceDTO;
import java.util.List;

/**
 * 对话历史服务接口
 *
 * @Author taoHouChao
 * @Date 10:00 2025/3/13
 */
public interface ConversationHistoryService {

    /**
     * 保存对话消息
     *
     * @param sessionId 会话ID
     * @param role     角色 (user/assistant)
     * @param content  内容
     */
    void saveMessage(String sessionId, String role, String content);

    /**
     * 获取对话历史
     *
     * @param sessionId 会话ID
     * @return 对话历史列表
     */
    List<CustomerServiceDTO> getHistory(String sessionId);

    /**
     * 清除对话历史
     *
     * @param sessionId 会话ID
     */
    void clearHistory(String sessionId);
}
