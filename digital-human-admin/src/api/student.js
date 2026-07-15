import request from "@/utils/request";

// 分页查询
export function getStudentList(data) {
  return request({
    url: "/api/school/student/list",
    method: "post",
    data,
  });
}

// 修改学生
export function modifyStudent(data) {
  return request({
    url: "/api/school/student/update",
    method: "put",
    data,
  });
}

// 删除学生
export function deleteStudent(id) {
  return request({
    url: `/api/school/student/${id}`,
    method: "delete",
  });
}

// 创建学生
export function createStudent(data) {
  return request({
    url: "/api/school/student/create",
    method: "post",
    data,
  });
}

// 下载模板
export function downloadTemplate(params) {
  return request({
    url: "/api/school/student/template",
    method: "get",
    responseType: "blob",
    params,
  });
}

// 导入学生
export function importStudent(data, params) {
  return request({
    url: "/api/school/student/import",
    method: "post",
    data,
    params,
    headers: {
      "Content-Type": "multipart/form-data;charset=UTF-8",
    },
  });
}

// 导出学生
export function exportStudent(data) {
  return request({
    url: "/api/school/student/export",
    method: "post",
    data,
    headers: {
      responseType: "blob",
    },
  });
}

// 获取所有班级
export function getAllSchoolClass() {
  return request({
    url: "/backend/student/all-school-class",
    method: "get",
  });
}
