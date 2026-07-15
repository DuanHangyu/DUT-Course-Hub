import request from "@/utils/request";

// 获取活跃度趋势（近30天）
export function getTrend(params) {
  return request({
    url: "/backend/overview/trend",
    method: "get",
    params,
  });
}

// 获取概览摘要
export function getSummary(params) {
  return request({
    url: "/backend/overview/summary",
    method: "get",
    params,
  });
}
// 获取风险预警趋势（近10天）
export function getRiskTrend(params) {
  return request({
    url: "/backend/overview/risk-trend/10d",
    method: "get",
    params,
  });
}
// 获取热门课程（Top3）
export function getPopularCourses(params) {
  return request({
    url: "/backend/overview/popular-courses",
    method: "get",
    params,
  });
}
// 获取作业提交趋势（近7天）
export function getHomeworkTrend7d(params) {
  return request({
    url: "/backend/overview/homework-trend/7d",
    method: "get",
    params,
  });
}
// 获取学习时间热力图
export function getHeatmap(params) {
  return request({
    url: "/backend/overview/heatmap",
    method: "get",
    params,
  });
}
// 获取课程完成度
export function getCourseCompletion(params) {
  return request({
    url: "/backend/overview/course-completion",
    method: "get",
    params,
  });
}
// 获取班级学习进度
export function getClassProgress(params) {
  return request({
    url: "/backend/overview/class-progress",
    method: "get",
    params,
  });
}

// 获取AI响应趋势（近7天）
export function getAiResponseTrend7d(params) {
  return request({
    url: "/backend/overview/ai-response-trend/7d",
    method: "get",
    params,
  });
}

// 获取24小时活跃趋势
export function getActiveTrend24h(params) {
  return request({
    url: "/backend/overview/active-trend/24h",
    method: "get",
    params,
  });
}
