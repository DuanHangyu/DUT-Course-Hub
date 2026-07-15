import request from "@/utils/request";

// 考核结果列表
export function getAssessmentResultsList(data) {
  return request({
    url: "/learn-record/list",
    method: "post",
    data,
  });
}

// 学习记录列表
export function getLearnRecord(data) {
  return request({
    url: "/learn-record/learn-record-list",
    method: "post",
    data,
  });
}

// 考核结果详情查询
export function getLearnRecordDetail(params) {
  return request({
    url: "/learn-record/detail",
    method: "get",
    params,
  });
}

// 学生课程详情
export function getStudentCourseDetail(params) {
  return request({
    url: "/learn-record/student-course-detail",
    method: "get",
    params,
  });
}

// 修改审核分数
export function modifyCheckScore(data) {
  return request({
    url: "/learn-record/modify-check-score",
    method: "post",
    data,
  });
}
