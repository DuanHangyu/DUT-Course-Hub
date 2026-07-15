import router from "./router";
import NProgress from "nprogress";
import "nprogress/nprogress.css";
import { getToken } from "@/utils/auth";
import { isPathMatch } from "@/utils/validate";
import { isRelogin } from "@/utils/request";
import useUserStore from "@/store/modules/user";

NProgress.configure({ showSpinner: false });

const whiteList = ["/login"];

const isWhiteList = (path) => {
  return whiteList.some((pattern) => isPathMatch(pattern, path));
};

router.beforeEach((to, from, next) => {
  NProgress.start();
  //  next();
  //  return
  if (getToken()) {
    if (to.path == "/login") {
      next({ path: "/" });
      NProgress.done();
    } else {
      isRelogin.show = true;
      if (!useUserStore()?.id) {
        useUserStore()
          .getInfo()
          .then(() => {
            isRelogin.show = false;
            next({ ...to, replace: true });
          })
          .catch((err) => {});
      } else {
        next();
      }
    }
  } else {
    if (isWhiteList(to.path)) {
      next();
    } else {
      next(`/login?redirect=${to.fullPath}`);
      NProgress.done();
    }
  }
});

router.afterEach(() => {
  NProgress.done();
});
