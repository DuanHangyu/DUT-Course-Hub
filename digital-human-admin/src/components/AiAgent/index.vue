<template>
  <Tooltip
    :arrow="false"
    placement="rightTop"
    color="#F8FAFC"
    trigger="click"
    overlayClassName="overlayClassName-chat"
    v-model:open="open"
  >
    <template #title>
      <div class="chat">
        <section class="top1 flex items-center justify-between px-4">
          <div class="flex items-center space-x-2">
            <img src="@/assets/images/aiagent2.png" class="size-8" alt="" />
            <span class="text-base font-medium text-[#FFFFFF]">AI数据助手</span>
          </div>
          <CloseOutlined @click="open = false" />
        </section>
        <section class="content p-4 space-y-6 overflow-y-auto" ref="contentRef">
          <template v-for="item in list">
            <div class="line1" v-if="item?.type == 1">
              {{ item?.text }}
            </div>
            <div v-if="item?.type == 2" class="flex justify-end">
              <div class="line2">
                {{ item?.text }}
              </div>
            </div>
          </template>
        </section>
        <section class="bottom1 flex px-4 items-center justify-between">
          <Input placeholder="请输入" class="w-[189px]" v-model:value="msg" />
          <div
            class="size-10 bg-[#1966FF] rounded-full flex items-center justify-center cursor-pointer"
            @click="sendMsg"
          >
            <svg-icon icon-class="send" class="text-white" />
          </div>
        </section>
      </div>
    </template>
    <div
      class="fixed cardAi right-[42px] bottom-[48px] flex flex-col items-center"
      @click="
        () => {
          msg = '';
          open = true;
        }
      "
    >
      <div class="img">
        <img src="@/assets/images/aiAgent.png" alt="" />
      </div>
      <div class="text">AI数据助手</div>
    </div>
  </Tooltip>
</template>
<script setup>
import { CloseOutlined } from "@ant-design/icons-vue";
import { Input, Tooltip } from "ant-design-vue";
import { onMounted, onUnmounted, ref } from "vue";
import { getToken } from "@/utils/auth";
import { sendChat } from "../../api/ws";

const open = ref(false);

let socket = null;
let reconnectTimer = null;
let heartbeatTimer = null;
let isManuallyClosed = false; // 标记是否是用户主动关闭
const MAX_RECONNECT_ATTEMPTS = 5;
const RECONNECT_DELAY_BASE = 2000; // 初始延迟 2s
let reconnectAttempts = 0;
const msg = ref("");
const contentRef = ref();
const secctionId = ref(null);

const list = ref([
  {
    text: "你好!我是你的AI数据助手。你可以问我关于学生学习进度、作业提交情况等问题。",
    type: 1,
  },
]);

// 获取 WebSocket URL 和 token
const getWsUrl = () => {
  return `${import.meta.env.VITE_APP_WEBSOCKET_BASE_URL}`;
};

// 启动心跳（每5秒发一次 ping）
const startHeartbeat = () => {
  if (heartbeatTimer) clearInterval(heartbeatTimer);
  heartbeatTimer = setInterval(() => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({ action: "ping" }));
    }
  }, 5000);
};

// 停止心跳
const stopHeartbeat = () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer);
    heartbeatTimer = null;
  }
};

// 关闭连接（安全关闭）
const closeSocket = () => {
  isManuallyClosed = true;
  stopHeartbeat();
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.close();
  }
  socket = null;
};

// 建立连接
const connect = () => {
  if (socket) return; // 防止重复连接
  try {
    socket = new WebSocket(getWsUrl(), getToken());

    socket.addEventListener("open", (event) => {
      console.log("WebSocket connected");
      reconnectAttempts = 0; // 重置重试次数
      startHeartbeat();
    });

    socket.addEventListener("message", (event) => {
      try {
        const jsonData = event.data;
        console.log("Received:", event, jsonData);
        if (
          !event?.data?.includes("content") &&
          !event?.data?.includes("type") &&
          !event?.data?.includes("pong")
        ) {
          if (list.value?.[list.value.length - 1]?.type == 2) {
            list.value.push({
              text: "",
              type: 1,
            });
          }
          list.value[list.value.length - 1].text += event.data;
          scrollToBottom();
        }
      } catch (error) {
        console.error("解析 JSON 失败:", error);
      }
    });

    socket.addEventListener("close", (event) => {
      console.log("WebSocket closed", event.code, event.reason);
      socket = null;
      stopHeartbeat();

      // 如果不是手动关闭且未达最大重试次数，则重连
      if (!isManuallyClosed && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
        const delay = RECONNECT_DELAY_BASE * Math.pow(2, reconnectAttempts); // 指数退避
        reconnectAttempts++;
        console.log(`尝试第 ${reconnectAttempts} 次重连，延迟 ${delay}ms`);
        reconnectTimer = setTimeout(() => {
          connect();
        }, delay);
      }
    });

    socket.addEventListener("error", (error) => {
      console.error("WebSocket error:", error);
      // error 通常会触发 close，所以这里不单独重连
    });
  } catch (err) {
    console.error("创建 WebSocket 失败:", err);
  }
};

const sendMsg = () => {
  if (!msg.value) return;
  list.value?.push({
    text: msg.value,
    type: 2,
  });
  scrollToBottom();
  sendChat({
    message: msg.value,
    // secctionId: secctionId.value,
  }).then((res) => {
    secctionId.value = res?.data?.secctionId;
    msg.value = "";
  });
};

let scrollTimer = null;
const scrollToBottom = () => {
  if (contentRef.value) {
    if (scrollTimer) clearTimeout(scrollTimer);
    scrollTimer = setTimeout(() => {
      contentRef.value.scrollTop = contentRef.value.scrollHeight;
    }, 50); // 延迟 50ms，避免频繁滚动
  }
};

onMounted(() => {
  isManuallyClosed = false;
  connect();
});

onUnmounted(() => {
  closeSocket();
});
</script>
<style scoped lang="less">
.cardAi {
  cursor: pointer;
  .img {
    width: 72px;
    height: 72px;
    box-shadow: 0px 4px 32px 0px rgba(0, 119, 255, 0.24);
    border-radius: 100%;
    img {
      width: 100%;
      height: 100%;
      transform: scale(2);
    }
  }
  .text {
    padding: 0px 15px;
    line-height: 26px;
    background: #ffffff;
    border-radius: 100px 100px 100px 100px;
    font-weight: 500;
    font-size: 14px;
    color: #133e7c;
    box-shadow: 0px 4px 32px 0px rgba(0, 119, 255, 0.24);
    margin-top: -10px;
    z-index: 2;
  }
}

.chat {
  width: 100%;
  background: #f8fafc;
  .top1 {
    height: 56px;
    background: linear-gradient(45deg, #0062f1 0%, #3ccbff 100%);
    box-shadow: 0px 2px 32px 0px rgba(0, 0, 0, 0.02);
  }
  .content {
    height: 380px;
  }
  .bottom1 {
    height: 72px;
    background: #ffffff;
  }
}

.line1 {
  width: fit-content;
  padding: 12px;
  background: #ffffff;
  border-radius: 0px 6px 6px 6px;
  border: 1px solid #d9d9d9;
  font-weight: 400;
  font-size: 14px;
  color: #0c3a6c;
}
.line2 {
  width: fit-content;
  padding: 12px;
  background: #1966ff;
  border-radius: 6px 0px 6px 6px;
  border: 1px solid rgba(217, 217, 217, 0);
  font-weight: 400;
  font-size: 14px;
  color: #ffffff;
}
</style>

<style lang="less">
.overlayClassName-chat {
  bottom: 160px !important;
  top: initial !important;

  .ant-tooltip-inner {
    width: 357px;
    padding: 0;
    border-radius: 12px;
    overflow: hidden;
  }
}
</style>
