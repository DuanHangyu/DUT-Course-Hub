import axios from "axios";
import { getToken } from "@/utils/auth";
import errorCode from "@/utils/errorCode";

import useUserStore from "@/store/modules/user";
import { message, Modal, notification } from "ant-design-vue";
// 创建axios实例
const service = axios.create({
  // axios中请求配置有baseURL选项，表示请求URL公共部分
  baseURL: import.meta.env.VITE_APP_BASE_API,
  // 超时
  timeout: 0,
  headers: {
    "Content-Type": "application/json;charset=utf-8",
  },
});

// request拦截器
service.interceptors.request.use(
  (config) => {
    // 是否需要设置 token
    const isToken = (config.headers || {}).isToken === false;
    if (getToken() && !isToken) {
      config.headers["Authorization"] = `Bearer ${getToken()}`;
    }
    return config;
  },
  (error) => {
    console.log(error);
    Promise.reject(error);
  },
);

// 响应拦截器
service.interceptors.response.use(
  (res) => {
    // 未设置状态码则默认成功状态
    const code = res.data.code || 200;
    // 获取错误信息
    const msg = res.data.message || errorCode[code] || errorCode["default"];
    // 二进制数据则直接返回
    if (
      res.request.responseType === "blob" ||
      res.request.responseType === "arraybuffer"
    ) {
      return res;
    }
    if (code === 401) {
      Modal.confirm({
        title: "系统提示",
        content: "登录状态已过期，请重新登录",
        okText: "重新登录",
        onOk() {
          useUserStore()
            .logOut()
            .then(() => {
              location.href = "/login";
            });
        },
      });
      return Promise.reject("无效的会话，或者会话已过期，请重新登录。");
    } else if (code === 500 || code == 400) {
      message.error(msg);
      return Promise.reject(new Error(msg));
    } else if (code === 601) {
      message.warning(msg);
      return Promise.reject(new Error(msg));
    } else if (code !== 200) {
      notification.error({ message: msg });
      return Promise.reject("error");
    } else {
      return Promise.resolve(res.data);
    }
  },
  (error) => {
    console.log("err" + error);
    let { message: msg } = error;
    if (msg == "Network Error") {
      msg = "后端接口连接异常";
    } else if (msg.includes("timeout")) {
      msg = "系统接口请求超时";
    } else if (msg.includes("Request failed with status code")) {
      msg = "系统接口" + msg.substr(msg.length - 3) + "异常";
    }
    message.error(msg, 5 * 1000);
    return Promise.reject(error);
  },
);

export default service;
