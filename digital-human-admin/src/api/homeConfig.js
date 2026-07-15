import request from "@/utils/request";
// 分页查询
export function getBannerList(data) {
  return request({
    url: "/home/banner/page-query",
    method: "post",
    data,
  });
}

// 移动
export function moveBanner(data) {
  return request({
    url: "/home/banner/move",
    method: "post",
    data,
  });
}

// 创建
export function createBanner(data) {
  return request({
    url: "/home/banner/create",
    method: "post",
    data,
  });
}

// 修改
export function modifyBanner(data) {
  return request({
    url: "/home/banner/modify",
    method: "post",
    data,
  });
}

// 删除
export function delBanner(params) {
  return request({
    url: "/home/banner/delete",
    method: "post",
    params,
  });
}


// 分页查询
export function getBackgroundList(data) {
  return request({
    url: "/home/background/page-query",
    method: "post",
    data,
  });
}

// 移动
export function moveBackground(data) {
  return request({
    url: "/home/background/move",
    method: "post",
    data,
  });
}

// 创建
export function createBackground(data) {
  return request({
    url: "/home/background/create",
    method: "post",
    data,
  });
}

// 修改
export function modifyBackground(data) {
  return request({
    url: "/home/background/modify",
    method: "post",
    data,
  });
}

// 删除
export function delBackground(params) {
  return request({
    url: "/home/background/delete",
    method: "post",
    params,
  });
}
