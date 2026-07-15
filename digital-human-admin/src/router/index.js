import { createWebHistory, createRouter } from "vue-router";
/* Layout */
import Layout from "@/layout/index.vue";
// 公共路由
export const constantRoutes = [
  {
    path: "/login",
    component: () => import("@/views/login/index.vue"),
    hidden: true,
  },
  {
    path: "/:pathMatch(.*)*",
    component: () => import("@/views/error/404.vue"),
    hidden: true,
  },
];
export const universityRoutes = {
  path: "/",
  component: Layout,
  redirect: "/university-dataOverview",
  name: "RoutesHome",
  children: [
    {
      path: "university-dataOverview",
      component: () => import("@/views/university/dataOverview/index.vue"),
      name: "dataOverview",
      meta: {
        title: "数据概览",
        activeMenu: "university-dataOverview",
        icon: "dataOverview",
      },
    },
    {
      path: "university-courseConstruction",
      component: () =>
        import("@/views/university/courseConstruction/index.vue"),
      name: "courseConstruction",
      meta: {
        title: "课程建设",
        activeMenu: "university-courseConstruction",
        icon: "courseConstruction",
      },
    },
    {
      path: "university-courseConstructionDetail",
      component: () =>
        import("@/views/university/courseConstruction/detail.vue"),
      name: "university-courseConstructionDetail",
      meta: {
        title: "课程详情",
        activeMenu: "university-courseConstruction",
        icon: "courseConstruction",
      },
    },
    {
      path: "university-middleSchoolManage",
      component: () =>
        import("@/views/university/middleSchoolManage/index.vue"),
      name: "middleSchoolManage",
      meta: {
        title: "学习记录",
        activeMenu: "university-middleSchoolManage",
        icon: "middleSchoolManage",
      },
    },
    {
      path: "university-homeConfig",
      component: () => import("@/views/university/homeConfig/index.vue"),
      name: "homeConfig",
      meta: {
        title: "首页配置",
        activeMenu: "university-homeConfig",
        icon: "homeConfig",
      },
    },
  ],
};

export const highSchoolRoutes = {
  path: "/",
  component: Layout,
  redirect: "/highSchool-dataOverview",
  name: "RoutesHome",
  children: [
    {
      path: "highSchool-dataOverview",
      component: () => import("@/views/highSchool/dataOverview/index.vue"),
      name: "dataOverview",
      meta: {
        title: "数据概览",
        activeMenu: "highSchool-dataOverview",
        icon: "dataOverview",
      },
    },
    {
      path: "highSchool-courseLibrary",
      component: () => import("@/views/highSchool/courseLibrary/index.vue"),
      name: "courseLibrary",
      meta: {
        title: "校本课程库",
        activeMenu: "highSchool-courseLibrary",
        icon: "courseConstruction",
      },
    },
    {
      path: "courseLibraryDetail",
      component: () => import("@/views/highSchool/courseLibrary/detail.vue"),
      name: "courseLibraryDetail",
      meta: {
        title: "课程详情",
        activeMenu: "courseLibrary",
        icon: "courseConstruction",
      },
    },
    {
      path: "highSchool-classStructure",
      component: () => import("@/views/highSchool/classStructure/index.vue"),
      name: "classStructure",
      meta: {
        title: "班级架构",
        activeMenu: "highSchool-classStructure",
        icon: "classStructure",
      },
    },
    {
      path: "highSchool-studentManage",
      component: () => import("@/views/highSchool/studentManage/index.vue"),
      name: "studentManage",
      meta: {
        title: "学生管理",
        activeMenu: "highSchool-studentManage",
        icon: "studentManage",
      },
    },
    {
      path: "highSchool-teacherManage",
      component: () => import("@/views/highSchool/teacherManage/index.vue"),
      name: "teacherManage",
      meta: {
        title: "教师管理",
        activeMenu: "highSchool-teacherManage",
        icon: "teacherManage",
      },
    },
    {
      path: "highSchool-curriculumConfig",
      component: () => import("@/views/highSchool/curriculumConfig/index.vue"),
      name: "curriculumConfig",
      meta: {
        title: "学科配置",
        activeMenu: "highSchool-curriculumConfig",
        icon: "curriculumConfig",
      },
    },
  ],
};

export const teacherRoutes = {
  path: "/",
  component: Layout,
  redirect: "/teacher-courseLibrary",
  name: "RoutesHome",
  children: [
    {
      path: "teacher-courseLibrary",
      component: () => import("@/views/highSchool/courseLibrary/index.vue"),
      name: "courseLibrary",
      meta: {
        title: "校本课程库",
        activeMenu: "teacher-courseLibrary",
        icon: "courseConstruction",
      },
    },
    {
      path: "courseLibraryDetail",
      component: () => import("@/views/highSchool/courseLibrary/detail.vue"),
      name: "courseLibraryDetail",
      meta: {
        title: "课程详情",
        activeMenu: "courseLibrary",
        icon: "courseConstruction",
      },
    },
  ],
};
const router = createRouter({
  history: createWebHistory(import.meta.env.VITE_BASE_URL),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    }
    return { top: 0 };
  },
});

export default router;
