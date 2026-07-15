import request from "@/utils/request";

// 对话接口（异步，WebSocket接收结果）
export function sendChat(data) {
  return request({
    url: "/backend/customer-service/chat",
    method: "post",
    data,
  });
}

// 获取对话历史
export function getChatHistory(params) {
  return request({
    url: "/backend/customer-service/history",
    method: "get",
    params,
  });
}
