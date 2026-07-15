import { useDynamicTitle } from "@/utils/dynamicTitle";
import { defineStore } from "pinia";

const dynamicTitle = import.meta.env.VITE_APP_TITLE;

const storageSetting = JSON.parse(localStorage.getItem("layout-setting")) || "";

const useSettingsStore = defineStore("settings", {
  state: () => ({
    title: "",
    dynamicTitle:
      storageSetting.dynamicTitle === undefined
        ? dynamicTitle
        : storageSetting.dynamicTitle,
  }),
  actions: {
    // 修改布局设置
    changeSetting(data) {
      const { key, value } = data;
      if (this.hasOwnProperty(key)) {
        this[key] = value;
      }
    },
    // 设置网页标题
    setTitle(title) {
      this.title = title;
      useDynamicTitle();
    },
  },
});

export default useSettingsStore;
