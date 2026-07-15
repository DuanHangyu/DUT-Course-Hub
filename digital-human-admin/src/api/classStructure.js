import request from "@/utils/request";
// 分页查询
export function getList(data) {
  return request({
    url: "/api/school/class/list",
    method: "post",
    data,
  });
}

// 创建
export function create(data) {
  return request({
    url: "/api/school/class/create",
    method: "post",
    data,
  });
}

// 修改
export function modify(data) {
  return request({
    url: "/api/school/class/update",
    method: "put",
    data,
  });
}

// 删除
export function del(id) {
  return request({
    url: `/api/school/class/${id}`,
    method: "delete",
  });
}
