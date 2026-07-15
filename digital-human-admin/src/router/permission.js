import router, {
  teacherRoutes,
  highSchoolRoutes,
  universityRoutes,
} from "./index";
import NProgress from "nprogress";
import "nprogress/nprogress.css";
import { getToken } from "@/utils/auth";
import { isPathMatch } from "@/utils/validate";
import useUserStore from "@/store/modules/user";
import useSettingsStore from "@/store/modules/settings";
import { message } from "ant-design-vue";
import { cloneDeep } from "lodash-es";

NProgress.configure({ showSpinner: false });

const whiteList = ["/login"];

const isWhiteList = (path) => {
  return whiteList.some((pattern) => isPathMatch(pattern, path));
};

router.beforeEach((to, from, next) => {
  NProgress.start();
  if (getToken()) {
    to.meta.title && useSettingsStore().setTitle(to.meta.title);
    /* has token*/
    if (to.path === "/login") {
      next({ path: "/" });
      NProgress.done();
    } else if (isWhiteList(to.path)) {
      next();
    } else {
      const userStore = useUserStore();
      if (!userStore.id) {
        userStore
          .getInfo()
          .then((e) => {
            // 仅在已注册时移除，避免 "Cannot remove non-existent route RoutesHome" 警告
            if (router.hasRoute("RoutesHome")) {
              router.removeRoute("RoutesHome");
            }
            if (e?.data?.role == 0) {
              if (userStore.isSchoolMode == 1) {
                router.addRoute(highSchoolRoutes);
              } else {
                router.addRoute(universityRoutes);
              }
            } else if (e?.data?.role == 1) {
              const r = cloneDeep(highSchoolRoutes);

              r.children = r?.children?.filter(
                (item) =>
                  userStore.info?.authModules?.includes(item.name) ||
                  item.name == "courseLibraryDetail",
              );
              r.redirect = {
                name: userStore.info?.authModules?.[0],
              };
              router.addRoute(r);
            } else {
              router.addRoute(teacherRoutes);
            }
            // 防御：若目标路由在动态注册后仍无匹配（如刷新到无权限页），回退到首页
            // 避免用户卡在 404 页
            const resolved = router.resolve(to.fullPath);
            if (!resolved.matched || resolved.matched.length === 0) {
              next({ path: "/", replace: true });
            } else {
              next({ ...to, replace: true });
            }
          })
          .catch((err) => {
            userStore.logOut().then(() => {
              message.error(err);
              next({ path: "/" });
            });
          });
      } else {
        next();
      }
    }
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      next();
    } else {
      next(`/login?redirect=${to.fullPath}`); // 否则全部重定向到登录页
      NProgress.done();
    }
  }
});

router.afterEach(() => {
  NProgress.done();
});
