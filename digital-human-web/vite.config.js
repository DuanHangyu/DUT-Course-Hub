import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "path";
import tailwindcss from "@tailwindcss/vite";

// https://vite.dev/config/
export default defineConfig(({ mode, command }) => {
  return {
    plugins: [vue(), tailwindcss()],
    server: {
      port: "9099",
      host: true,
      proxy: {
        "/dev-api": {
          // target: "https://digital-student-test.hotsupper.top/digital/",
          // target: "https://learn.deepolylink.com/digital",
          target: "https://learn.deepolylink.com/digital",
          changeOrigin: true,
          secure: false,
          rewrite: (path) => path.replace(/^\/dev-api/, ""),
        },
      },
    },
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "src"),
      },
      extensions: [".mjs", ".js", ".ts", ".jsx", ".tsx", ".json", ".vue"],
    },
  };
});
