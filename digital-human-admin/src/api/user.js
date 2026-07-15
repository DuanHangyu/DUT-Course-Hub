import request from "@/utils/request";

// 登录
export function login(data) {
  return request({
    url: "/login",
    headers: {
      isToken: false,
      repeatSubmit: false,
    },
    method: "post",
    data,
  });
}

// 退出登录
export function logout() {
  return request({
    url: "/logout",
    method: "post",
  });
}

// 用户详情
export function getUserInfo() {
  return request({
    url: "/user/detail",
    method: "get",
  });
}
