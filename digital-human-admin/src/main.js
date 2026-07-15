import { createApp, h } from "vue";
import "./style.css";
import "./assets/style/index.less";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import "./router/permission";
import "@/utils/rem.js";
import Antd from "ant-design-vue";
import "ant-design-vue/dist/reset.css";

import "virtual:svg-icons-register";
import SvgIcon from "@/components/SvgIcon/index.vue";

import "@vue-flow/core/dist/style.css";
import "@vue-flow/core/dist/theme-default.css";
import useUserStore from "@/store/modules/user";
import qs from "qs";

const app = createApp({
  render: () => h(App),
  mounted: async () => {
    try {
      const routeSearchParams = window.location.href?.split("?")?.[1];
      const sid =
        qs.parse(routeSearchParams)?.schoolId ||
        sessionStorage.getItem("schoolId");
      if (sid) {
        sessionStorage.setItem("schoolId", sid);
        const userStore = useUserStore();
        await userStore.setSchoolId(sid);
      }
      const isSchoolMode =
        qs.parse(routeSearchParams)?.isSchoolMode ||
        sessionStorage.getItem("isSchoolMode");
      if (isSchoolMode) {
        sessionStorage.setItem("isSchoolMode", isSchoolMode);
        await useUserStore().setSchoolMode(isSchoolMode);
      }
    } catch (error) {}
  },
});
app.use(Antd);
app.use(store);
app.use(router);
app.component("svg-icon", SvgIcon);
app.mount("#app");
