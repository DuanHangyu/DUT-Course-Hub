package com.human.digital.digitalhuman.service;

/**
 * 客服对话 WebSocket 服务接口
 *
 * @Author taoHouChao
 * @Date 10:00 2025/3/14
 */
public interface CustomerServiceWebSocketService {

    /**
     * 发送消息到指定用户的客户端
     *
     * @param userId  用户ID
     * @param content 内容
     * @param isLast  是否最后一条
     */
    void sendMessage(Integer userId, String content, boolean isLast);
}
