import request from "@/utils/request";
export function login(data) {
  return request({
    url: "/login",
    method: "post",
    data,
  });
}

export function logout(data) {
  return request({
    url: "/logout",
    method: "post",
    data,
  });
}

export function getInfo(params) {
  return request({
    url: "/api/student/info",
    method: "get",
    params,
  });
}

export function updatePwd(data) {
  return request({
    url: "/api/student/modify-password",
    method: "post",
    data,
  });
}

// 获取文件签名URL
export function getFileSignedUrl(url) {
  return request({
    url: "/file/signed-url",
    method: "get",
    params: { url },
  });
}

// 生成上传URL
export function generateUploadUrl(params) {
  return request({
    url: "/file/generate-upload-url",
    method: "get",
    params,
  });
}

export function allSchool(params) {
  return request({
    url: "/allSchool",
    method: "get",
    params,
  });
}
