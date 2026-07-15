import request from "@/utils/request";
export function getCourseDetail(params) {
  return request({
    url: "/api/course/course-detail",
    method: "get",
    params,
  });
}
