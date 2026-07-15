import request from "@/utils/request";

// 获取学生进度列表
export function getStudentProgress(params) {
  return request({
    url: "/api/study-monitor/student-progress",
    method: "get",
    params,
  });
}

// 获取学生学习详情
export function getLearningDetail(params) {
  return request({
    url: `/api/study-monitor/student/${params?.studentId}/learning-detail`,
    method: "get",
    params,
  });
}
