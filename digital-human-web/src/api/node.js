import request from "@/utils/request";
export function startStudy(data) {
  return request({
    url: "/api/course/start-study",
    method: "post",
    data,
  });
}

export function endStudy(params) {
  return request({
    url: "/api/course/end-study",
    method: "post",
    params,
  });
}

// 保存课程学生回复
export function saveReply(data) {
  return request({
    url: "/api/course-student-comment/save-reply",
    method: "post",
    data,
  });
}

// 保存课程学生提问
export function saveComment(data) {
  return request({
    url: "/api/course-student-comment/save-comment",
    method: "post",
    data,
  });
}
// 删除课程学生提问
export function delComment(data) {
  return request({
    url: "/api/course-student-comment/delete-comment",
    method: "post",
    data,
  });
}

// 获取课程教师回复列表
export function getReplyList(params) {
  return request({
    url: "/api/course-student-comment/more-reply-list",
    method: "get",
    params,
  });
}

// 获取课程学生提问列表
export function getCommentList(params) {
  return request({
    url: "/api/course-student-comment/list",
    method: "get",
    params,
  });
}

// 获取课程节详情
export function getNodeDetail(params) {
  return request({
    url: "/api/course/course-node-detail",
    method: "get",
    params,
  });
}

// 提交作业
export function submitHomeWork(data) {
  return request({
    url: "/api/homework/submit",
    method: "post",
    data,
  });
}

// 我的提交
export function getMyHomeWork(params) {
  return request({
    url: "/api/homework/my-submit",
    method: "get",
    params,
  });
}

// 作业列表
export function getHomeWorkList(params) {
  return request({
    url: "/api/homework/list",
    method: "get",
    params,
  });
}

// 作业详情
export function getHomeWorkDetail(params) {
  return request({
    url: "/api/homework/detail",
    method: "get",
    params,
  });
}

// 删除提交
export function delHomeWork(data) {
  return request({
    url: "/api/homework/delete",
    method: "post",
    data,
  });
}

// 获取课程资料列表
export function getCourseMaterialList(params) {
  return request({
    url: "/api/course-material/list",
    method: "get",
    params,
  });
}

// 获取文件夹内文件列表
export function getFolderFileList(params) {
  return request({
    url: `/api/course-material/folder/${params.folderId}/files`,
    method: "get",
    params,
  });
}
