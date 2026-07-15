import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "path";
import { createSvgIconsPlugin } from "vite-plugin-svg-icons";

// https://vite.dev/config/
export default defineConfig(({ mode, command }) => {
  return {
    plugins: [
      vue(),
      createSvgIconsPlugin({
        iconDirs: [path.resolve(process.cwd(), "src/assets/icons/svg")],
        symbolId: "icon-[dir]-[name]",
        svgoOptions: command === "build",
      }),
    ],
    resolve: {
      alias: {
        // 设置路径
        "~": path.resolve(__dirname, "./"),
        // 设置别名
        "@": path.resolve(__dirname, "./src"),
      },
    },
    build: {
      sourcemap: command === "build" ? false : "inline",
      outDir: "dist",
      assetsDir: "assets",
      chunkSizeWarningLimit: 2000,
      rollupOptions: {
        output: {
          chunkFileNames: "static/js/[name]-[hash].js",
          entryFileNames: "static/js/[name]-[hash].js",
          assetFileNames: "static/[ext]/[name]-[hash].[ext]",
        },
      },
    },
    server: {
      port: 9999,
      host: true,
      // open: true,
      proxy: {
        "/dev-api": {
          target: "https://teach.deepolylink.com/digital/",
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/dev-api/, ""),
        },
      },
    },
  };
});
