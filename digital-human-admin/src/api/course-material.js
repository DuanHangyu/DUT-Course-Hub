import request from "@/utils/request";

// 获取课程资料列表
export function getCourseMasterialList(params) {
  return request({
    url: "/course-material/list",
    method: "get",
    params,
  });
}

// 创建文件夹
export function createCourseMaterial(data) {
  return request({
    url: "/course-material/folder/create",
    method: "post",
    data,
  });
}

// 重命名文件夹
export function renameCourseMaterial(data) {
  return request({
    url: "/course-material/folder/rename",
    method: "put",
    data,
  });
}

// 删除夹排序
export function delCourseMaterial(params) {
  return request({
    url: `/course-material/folder/${params.folderId}`,
    method: "delete",
    params,
  });
}

// 文件夹排序
export function sortFolder(data) {
  return request({
    url: "/course-material/folder/sort",
    method: "put",
    data,
  });
}

// 获取文件夹内文件列表
export function getCourseMasterialFileList(params) {
  return request({
    url: `/course-material/folder/${params.folderId}/files`,
    method: "get",
    params,
  });
}

// 上传文件到课程
export function createCourseFile(data) {
  return request({
    url: "/course-material/file/upload",
    method: "post",
    data,
  });
}

// 重命名文件
export function renameCourseFile(data) {
  return request({
    url: "/course-material/file/rename",
    method: "put",
    data,
  });
}

// 删除文件
export function delCourseFile(params) {
  return request({
    url: `/course-material/file/${params.fileId}`,
    method: "delete",
    params,
  });
}

// 文件排序
export function sortFile(data) {
  return request({
    url: "/course-material/file/sort",
    method: "put",
    data,
  });
}
