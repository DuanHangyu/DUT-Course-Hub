import request from "@/utils/request";

// 分页查询
export function getCourseList(data) {
  return request({
    url: "/course/page-query",
    method: "post",
    data,
  });
}

// 移动课程节点
export function moveNode(data) {
  return request({
    url: "/course/node-move",
    method: "post",
    data,
  });
}

// 创建课程节点
export function createNode(data) {
  return request({
    url: "/course/node-create",
    method: "post",
    data,
  });
}

// 修改课程
export function modifyCourse(data) {
  return request({
    url: "/course/modify",
    method: "post",
    data,
  });
}

// 删除课程
export function deleteCourse(data) {
  return request({
    url: "/course/delete",
    method: "post",
    data,
  });
}

// 创建课程
export function createCourse(data) {
  return request({
    url: "/course/create",
    method: "post",
    data,
  });
}

// 查询课程节点
export function getCourseNode(params) {
  return request({
    url: "/course/course-node",
    method: "get",
    params,
  });
}

// 修改课程节点
export function modifyNode(data) {
  return request({
    url: "/course/node-modify",
    method: "post",
    data,
  });
}
// 删除课程节点
export function deleteNode(data) {
  return request({
    url: "/course/node-delete",
    method: "post",
    data,
  });
}

// 修改课程状态
export function changeState(data) {
  return request({
    url: "/course/change-state",
    method: "post",
    data,
  });
}
