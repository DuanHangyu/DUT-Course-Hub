import request from "@/utils/request";
export function getBanner() {
  return request({
    url: "/api/home/banner/list",
    method: "get",
  });
}

export function getBackground() {
  return request({
    url: "/api/home/background/list",
    method: "get",
  });
}

export function getFeaturedCourse() {
  return request({
    url: "/api/course/featured-course",
    method: "get",
  });
}

export function getSubjectList(params) {
  return request({
    url: "/api/course/subject-list",
    method: "get",
    params,
  });
}

export function getOptionalCourse(params) {
  return request({
    url: "/api/course/optional-course",
    method: "get",
    params,
  });
}
// 通过邀请码学习
export function joinCourse(data) {
  return request({
    url: "/api/course/invite-code-study",
    method: "post",
    data,
  });
}
// 检查课程权限
export function checkCoursePermission(params) {
  return request({
    url: "/api/course/check-course-permission",
    method: "get",
    params,
  });
}
