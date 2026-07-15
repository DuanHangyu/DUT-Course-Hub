import request from "@/utils/request";

// 获取系统健康状态
export function getSystemHealth(params) {
  return request({
    url: "/backend/university-overview/system-health",
    method: "get",
    params,
  });
}

// 获取学校区域分布
export function getSchoolRegion(params) {
  return request({
    url: "/backend/university-overview/school-region",
    method: "get",
    params,
  });
}
// 获取学校活跃榜
export function getSchoolRanking(params) {
  return request({
    url: "/backend/university-overview/school-ranking",
    method: "get",
    params,
  });
}
// 获取实时活跃数据
export function getRealtimeData(params) {
  return request({
    url: "/backend/university-overview/realtime-data",
    method: "get",
    params,
  });
}
// 获取学习趋势
export function getLearningTrend(params) {
  return request({
    url: "/backend/university-overview/learning-trend",
    method: "get",
    params,
  });
}
// 获取课程学习统计
export function getCourseLearning(params) {
  return request({
    url: "/backend/university-overview/course-learning",
    method: "get",
    params,
  });
}
// 获取核心统计数据
export function getCoreStatistics(params) {
  return request({
    url: "/backend/university-overview/core-statistics",
    method: "get",
    params,
  });
}
// 获取24小时流量趋势
export function getTraffic(params) {
  return request({
    url: "/backend/university-overview/24h-traffic",
    method: "get",
    params,
  });
}
