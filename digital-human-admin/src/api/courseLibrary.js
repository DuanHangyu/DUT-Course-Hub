import request from "@/utils/request";
// 分页查询
export function getList(params) {
  return request({
    url: "/api/school/list",
    method: "get",
    params,
  });
}

// 详情
export function getDetail(id) {
  return request({
    url: `/api/school/${id}`,
    method: "get",
  });
}

// 创建
export function create(data) {
  return request({
    url: "/api/school/create",
    method: "post",
    data,
  });
}

// 修改
export function modify(data) {
  return request({
    url: "/api/school/update",
    method: "put",
    data,
  });
}

// 删除
export function del(id) {
  return request({
    url: `/api/school/${id}`,
    method: "delete",
  });
}

// 统计
export function getStats(params) {
  return request({
    url: "/api/school/stats",
    method: "get",
    params,
  });
}

// 生成邀请码
export function generateCode(data) {
  return request({
    url: "/api/school/generateCode",
    method: "post",
    data,
  });
}

// 设置/取消精选
export function featuredApi(data) {
  return request({
    url: "/api/school/featured",
    method: "put",
    data,
  });
}

// 获取学科列表
export function getSubjectList(params) {
  return request({
    url: "/api/school/subject-list",
    method: "get",
    params,
  });
}

// 查询班级及学生列表
export function getAllWithStudents(params) {
  return request({
    url: "/api/school/class/all-with-students",
    method: "get",
    params,
  });
}

// 添加关联学生
export function addRelationStudent(data) {
  return request({
    url: "/api/school/addRelationStudent",
    method: "post",
    data,
  });
}
