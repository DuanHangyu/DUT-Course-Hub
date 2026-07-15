import request from "@/utils/request";
// 分页查询
export function getList(data) {
  return request({
    url: "/course-template/page-query",
    method: "post",
    data,
  });
}

// 创建
export function create(data) {
  return request({
    url: "/course-template/create",
    method: "post",
    data,
  });
}

// 修改
export function modify(data) {
  return request({
    url: "/course-template/modify",
    method: "post",
    data,
  });
}

// 删除
export function del(params) {
  return request({
    url: "/course-template/delete",
    method: "get",
    params,
  });
}

// 课程详情
export function getDetail(params) {
  return request({
    url: "/course-template/detail",
    method: "get",
    params,
  });
}

// 分页查询
export function getNodeList(params) {
  return request({
    url: "/course-template/nodes",
    method: "get",
    params,
  });
}

// 创建
export function createNode(data) {
  return request({
    url: "/course-template/node-create",
    method: "post",
    data,
  });
}

// 修改
export function modifyNode(data) {
  return request({
    url: "/course-template/node-modify",
    method: "post",
    data,
  });
}

// 删除
export function delNode(data) {
  return request({
    url: "/course-template/node-delete",
    method: "post",
    data,
  });
}

// 获取学科列表
export function getSubjectList(params) {
  return request({
    url: "/course-template/subject-list",
    method: "get",
    params,
  });
}

// 获取课程模板状态统计
export function getCount(params) {
  return request({
    url: "/course-template/status-count",
    method: "get",
    params,
  });
}

// 移动课程节点
export function moveNode(data) {
  return request({
    url: "/course-template/node-move",
    method: "post",
    data,
  });
}
