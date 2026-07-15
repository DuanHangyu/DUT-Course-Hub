import request from "@/utils/request";

// 处理风险预警
export function riskWarning(id) {
  return request({
    url: `/api/study-monitor/risk-warnings/${id}`,
    method: "post",
    data: { id },
  });
}

// 获取任务完成流程图
export function getTaskFlow(params) {
  return request({
    url: `/api/study-monitor/task-flow`,
    method: "get",
    params,
  });
}

// 获取风险预警列表
export function getRiskWarning(params) {
  return request({
    url: `/api/study-monitor/risk-warnings`,
    method: "get",
    params,
  });
}

// 获取进度趋势
export function getProgressTrend(params) {
  return request({
    url: `/api/study-monitor/progress-trend`,
    method: "get",
    params,
  });
}

// 获取核心指标概览
export function getOverview(params) {
  return request({
    url: `/api/study-monitor/overview`,
    method: "get",
    params,
  });
}

// 获取节点学生列表
export function getNodeStudents(params) {
  return request({
    url: `/api/study-monitor/node-students/${params?.nodeId}`,
    method: "get",
    params,
  });
}

// 获取节点详情
export function getNodeDetail(params) {
  return request({
    url: `/api/study-monitor/node-detail/${params?.nodeId}`,
    method: "get",
    params,
  });
}

// 获取知识图谱热力图
export function getKnowledgeGraph(params) {
  return request({
    url: `/api/study-monitor/knowledge-graph`,
    method: "get",
    params,
  });
}

// 获取课程全景数据
export function getCourseOverview(params) {
  return request({
    url: `/api/study-monitor/course-overview`,
    method: "get",
    params,
  });
}

// 获取章节效率分布
export function getChapterEfficiency(params) {
  return request({
    url: `/api/study-monitor/chapter-efficiency`,
    method: "get",
    params,
  });
}

// 获取高频知识盲区
export function getBlindSpots(params) {
  return request({
    url: `/api/study-monitor/blind-spots`,
    method: "get",
    params,
  });
}

// 获取问题活跃度排行
export function getActivityRanking(params) {
  return request({
    url: `/api/study-monitor/activity-ranking`,
    method: "get",
    params,
  });
}

// 获取学习活力热力图
export function getActivityHeatmap(params) {
  return request({
    url: `/api/study-monitor/activity-heatmap`,
    method: "get",
    params,
  });
}

// 获取学习时段分布（24小时玫瑰图）
export function getTimeDistribution(params) {
  return request({
    url: `/api/study-monitor/time-distribution`,
    method: "get",
    params,
  });
}

// 获取学生进度排行榜 Top10
export function getProgressRanking(params) {
  return request({
    url: `/api/study-monitor/progress-ranking`,
    method: "get",
    params,
  });
}

// 获取节点完成率排行
export function getNodeCompletionRanking(params) {
  return request({
    url: `/api/study-monitor/node-completion-ranking`,
    method: "get",
    params,
  });
}

// 获取每日学习趋势（14天）
export function getDailyStudyTrend(params) {
  return request({
    url: `/api/study-monitor/daily-study-trend`,
    method: "get",
    params,
  });
}

// 获取班级进度分布
export function getProgressDistribution(params) {
  return request({
    url: `/api/study-monitor/progress-distribution`,
    method: "get",
    params,
  });
}
