import useSettingsStore from "@/store/modules/settings";

/**
 * 动态修改标题
 */
export function useDynamicTitle() {
  const defaultTitle = import.meta.env.VITE_APP_TITLE;
  const settingsStore = useSettingsStore();
  if (settingsStore.dynamicTitle) {
    document.title = settingsStore.title + " - " + defaultTitle;
  } else {
    document.title = defaultTitle;
  }
}
