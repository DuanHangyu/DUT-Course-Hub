import request from "@/utils/request";

// 获取课程教师提问列表
export function getCommentList(params) {
  return request({
    url: "/backend/course-student-comment/list",
    method: "get",
    params,
  });
}

// 获取课程教师回复列表
export function getMoreReplyList(params) {
  return request({
    url: "/backend/course-student-comment/more-reply-list",
    method: "get",
    params,
  });
}

// 保存课程老师提问
export function saveComment(data) {
  return request({
    url: "/backend/course-student-comment/save-comment",
    method: "post",
    data,
  });
}

// 保存课程老师回复
export function saveReply(data) {
  return request({
    url: "/backend/course-student-comment/save-reply",
    method: "post",
    data,
  });
}

// 删除课程老师提问
export function deleteComment(data) {
  return request({
    url: "/backend/course-student-comment/delete-comment",
    method: "post",
    data,
  });
}
