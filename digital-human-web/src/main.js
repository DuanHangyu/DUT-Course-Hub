import { createApp } from "vue";
import "./assets/style/index.css";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import "element-plus/theme-chalk/dark/css-vars.css";
import locale from "element-plus/es/locale/lang/zh-cn";

import App from "./App.vue";
import store from "./store";
import router from "./router";
import "./permission";

import "@/utils/mathjax";
import "mathjax/es5/tex-svg"; 
import '@vue-flow/core/dist/style.css';
import '@vue-flow/core/dist/theme-default.css';

const app = createApp(App);
app.use(router);
app.use(store);

app.use(ElementPlus, {
  locale: locale,
  // 支持 large、default、small
  size: "default",
  dialog: {
    alignCenter: true,
  },
});
app.mount("#app");
