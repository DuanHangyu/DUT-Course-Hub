import request from "@/utils/request";

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
