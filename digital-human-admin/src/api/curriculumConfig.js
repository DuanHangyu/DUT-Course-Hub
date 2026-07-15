import request from "@/utils/request";
// 学科列表
export function getList(params) {
  return request({
    url: "/api/backend/subject/list",
    method: "get",
    params,
  });
}

// 更新学科排序
export function updateSort(data) {
  return request({
    url: "/api/backend/subject/sort",
    method: "put",
    data,
  });
}
