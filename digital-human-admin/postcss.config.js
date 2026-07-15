export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
    "postcss-pxtorem": {
      rootValue: 16, // 1rem = 16px
      propList: ["*"], // 转换所有属性
      selectorBlackList: [], // 不排除任何选择器
      replace: true, // 替换原有值
      mediaQuery: false, // 不转换媒体查询中的px
      minPixelValue: 2, // 最小转换像素值
    },
  },
};
