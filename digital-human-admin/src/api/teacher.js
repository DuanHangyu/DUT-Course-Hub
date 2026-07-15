import request from "@/utils/request";

// 分页查询
export function getTeacherList(data) {
  return request({
    url: "/api/school/teacher/list",
    method: "post",
    data,
  });
}

// 修改教师
export function modifyTeacher(data) {
  return request({
    url: "/api/school/teacher/update",
    method: "put",
    data,
  });
}

// 删除教师
export function deleteTeacher() {
  return request({
    url: `/api/school/teacher/${id}`,
    method: "delete",
  });
}

// 创建教师
export function createTeacher(data) {
  return request({
    url: "/api/school/teacher/create",
    method: "post",
    data,
  });
}

// 修改教师状态
export function changeState(data) {
  return request({
    url: "/api/school/teacher/changeState",
    method: "put",
    data,
  });
}
