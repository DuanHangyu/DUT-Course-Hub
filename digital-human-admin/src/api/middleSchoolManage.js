import request from "@/utils/request";
// 分页查询
export function getList(data) {
  return request({
    url: "/school/page-query",
    method: "post",
    data,
  });
}

// 创建
export function create(data) {
  return request({
    url: "/school/create",
    method: "post",
    data,
  });
}

// 修改
export function modify(data) {
  return request({
    url: "/school/modify",
    method: "post",
    data,
  });
}

// 删除
export function del(params) {
  return request({
    url: "/school/delete",
    method: "post",
    params,
  });
}
