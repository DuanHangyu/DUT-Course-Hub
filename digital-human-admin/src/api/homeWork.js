import request from "@/utils/request";
// 分页查询
export function getList(params) {
  return request({
    url: "/backend/homework/list",
    method: "get",
    params,
  });
}

// 详情
export function getDetail(params) {
  return request({
    url: "/backend/homework/submit-detail",
    method: "get",
    params,
  });
}

// 创建
export function create(data) {
  return request({
    url: "/backend/homework/create",
    method: "post",
    data,
  });
}

// 修改
export function modify(data) {
  return request({
    url: "/backend/homework/modify",
    method: "post",
    data,
  });
}

// 删除
export function del(data) {
  return request({
    url: "/backend/homework/delete",
    method: "post",
    data,
  });
}

// 重新批改
export function regradeApi(data) {
  return request({
    url: "/backend/homework/submission/regrade",
    method: "put",
    data,
  });
}

// 批改作业
export function gradeApi(data) {
  return request({
    url: "/backend/homework/submission/grade",
    method: "post",
    data,
  });
}

// 提交统计
export function getStatistics(params) {
  return request({
    url: "/backend/homework/submission/statistics",
    method: "get",
    params,
  });
}

// 提交列表
export function getSubmitList(params) {
  return request({
    url: "/backend/homework/submission/list",
    method: "get",
    params,
  });
}


// 获取作业关联的班级列表
export function getClassList(params) {
  return request({
    url: "/backend/homework/submission/classes",
    method: "get",
    params,
  });
}
