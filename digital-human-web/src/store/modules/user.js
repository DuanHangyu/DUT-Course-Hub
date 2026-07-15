import { login, getInfo, logout } from "@/api/api";
import { getToken, setToken, removeToken } from "@/utils/auth";
import { defineStore } from "pinia";

const useUserStore = defineStore("user", {
  state: () => ({
    token: getToken(),
    id: "",
    user: {},
  }),
  actions: {
    // 登录
    login(userInfo) {
      return new Promise((resolve, reject) => {
        login(userInfo)
          .then((res) => {
            setToken(res);
            this.token = res;
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
        getInfo()
          .then((res) => {
            this.id = res?.id;
            this.user = res || {};
            resolve(res);
          })
          .catch((error) => {
            reject(error);
          });
      });
    },
    logOut() {
      return new Promise((resolve, reject) => {
        logout()
          .then((res) => {
            this.token = "";
            removeToken();
            resolve();
          })
          .catch((error) => {
            reject(error);
          });
      });
    },
    localLogOut() {
      return new Promise((resolve) => {
        this.token = "";
        removeToken();
        resolve();
      });
    },
  },
});

export default useUserStore;
