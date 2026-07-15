import { login, getUserInfo, logout } from "@/api/user";
import { getToken, setToken, removeToken } from "@/utils/auth";
import defAva from "@/assets/images/profile.png";
import { defineStore } from "pinia";
import router from "../../router";

const useUserStore = defineStore("user", {
  state: () => ({
    token: getToken(),
    id: "",
    avatar: "",
    info: {},
    isSchoolMode: sessionStorage.getItem("isSchoolMode"),
    schoolId: sessionStorage.getItem("schoolId") || undefined,
  }),
  actions: {
    // 登录
    login(userInfo) {
      const account = userInfo.account.trim();
      const password = userInfo.password;
      return new Promise((resolve, reject) => {
        login({
          account,
          password,
        })
          .then((res) => {
            setToken(res.data);
            this.token = res.data;
            resolve();
          })
          .catch((error) => {
            reject(error);
          });
      });
    },
    // 获取用户信息
    getInfo() {
      return new Promise((resolve, reject) => {
        getUserInfo()
          .then((res) => {
            const user = res.data;
            this.id = user.id;
            this.avatar = defAva;
            this.info = user;
            if (user?.role == 1) {
              this.schoolId = user?.school?.id;
            }
            if (user?.role == 2) {
              this.schoolId = user?.school?.id;
            }
            resolve(res);
          })
          .catch((error) => {
            reject(error);
          });
      });
    },
    // 退出系统
    logOut() {
      return new Promise((resolve, reject) => {
        logout()
          .then(() => {
            this.id = undefined;
            this.token = undefined;
            this.avatar = "";
            this.info = {};
            this.isSchoolMode = undefined;
            this.schoolId = undefined;
            removeToken();
            localStorage.clear();
            sessionStorage.clear();
            if (router.hasRoute("RoutesHome")) {
              router.removeRoute("RoutesHome");
            }
            resolve();
          })
          .catch(() => {
            this.id = undefined;
            this.token = undefined;
            this.avatar = "";
            this.info = {};
            this.isSchoolMode = undefined;
            this.schoolId = undefined;
            removeToken();
            localStorage.clear();
            sessionStorage.clear();
            if (router.hasRoute("RoutesHome")) {
              router.removeRoute("RoutesHome");
            }
            resolve();
            location.href = "/login";
          });
      });
    },
    setSchoolMode(e) {
      sessionStorage.setItem("isSchoolMode", e);
      this.isSchoolMode = e;
    },
    setSchoolId(e) {
      this.schoolId = e;
    },
  },
});

export default useUserStore;
