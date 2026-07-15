import { createWebHistory, createRouter } from "vue-router";
// 公共路由
export const constantRoutes = [
  {
    path: "/:pathMatch(.*)*",
    component: () => import("@/views/error/404"),
    hidden: true,
  },
  {
    path: "/401",
    component: () => import("@/views/error/401"),
    hidden: true,
  },
  {
    path: "/login",
    component: () => import("@/views/login"),
    name: "Login",
    meta: { title: "登录" },
  },
  {
    path: "",
    redirect: "/home",
  },
  {
    path: "/home",
    component: () => import("@/views/home"),
    name: "Home",
    meta: { title: "首页" },
  },
  {
    path: "/course",
    component: () => import("@/views/course"),
    name: "Course",
    meta: { title: "课程" },
  },
  {
    path: "/node",
    component: () => import("@/views/node"),
    name: "Node",
    meta: { title: "节点" },
  },
];
// 动态路由，基于用户权限动态去加载
export const dynamicRoutes = [];

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior(to, from) {
    return { top: 0 };
  },
});

export default router;
