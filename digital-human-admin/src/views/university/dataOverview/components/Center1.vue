<template>
  <div class="h-full w-full" ref="chart"></div>
</template>
<script setup>
import { onBeforeUnmount, onMounted, ref } from "vue";
import * as echarts from "echarts";
import geoJson from "./map.json";

echarts.registerMap("china", geoJson);

const chart = ref(null);
let myChart;

const init = () => {
  myChart = echarts.init(chart.value);
  const option = {
    tooltip: {
      trigger: "item",
      // formatter: "{b}",
      renderMode: "html",
      className: "echarts-tooltip22",
      triggerOn: "click",
      formatter: function (params) {
        // 返回自定义 HTML 字符串
        return `
      <div class="custom-tooltip">
        <div class="t1">${params.name}</div>
      ${
        data.value?.find((item) => item?.name == params.name)
          ? `<button
          class="t2"
          onclick="window.open('${params}')"
        >
          进入后台>
        </button>`
          : ""
      }
      </div>
    `;
      },
    },
    visualMap: {
      // 图例，用于颜色映射
      min: 0,
      max: 1000,
      text: ["高", "低"],
      calculable: true,
      inRange: {
        color: ["rgba(213,255,240,1)", "#FF9D25"], // 颜色范围
      },
      show: false,
    },
    geo: {
      map: "china",
      show: true,
      roam: false,
      label: {
        show: false,
        emphasis: {
          show: false,
        },
      },
      layoutSize: "100%",
      zoom: 1.2,
      itemStyle: {
        borderColor: new echarts.graphic.LinearGradient(
          0,
          0,
          0,
          1,
          [
            {
              offset: 0,
              color: "#00F6FF",
            },
            {
              offset: 1,
              color: "#53D9FF",
            },
          ],
          false,
        ),
        borderWidth: 3,
        shadowColor: "rgba(10,76,139,1)",
        shadowOffsetY: 0,
        shadowBlur: 10,
      },
    },
    series: [
      {
        name: "数据名称",
        type: "map",
        map: "china",
        roam: false,
        label: {
          show: false,
        },
        layoutSize: "100%",
        zoom: 1.2,
        itemStyle: {
          areaColor: {
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              {
                offset: 0,
                color: "#073684",
              },
              {
                offset: 1,
                color: "#061E3D",
              },
            ],
          },
          borderColor: "#215495",
          borderWidth: 1,
        },
        data: [],
      },
      {
        type: "effectScatter", // 涟漪散点图（也可用 'scatter'）
        coordinateSystem: "geo",
        rippleEffect: {
          brushType: "stroke",
          period: 4, // 动画周期
          scale: 2, // 涟漪放大倍数
          number: 2, // 波纹数量
        },
        symbolSize: 8,
        zlevel: 2,
        itemStyle: {
          opacity: 1,
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowOffsetY: 0,
          shadowColor: "rgba(213,255,240,0.3)",
        },
        label: {
          show: false,
        },
        data: data.value,
      },
    ],
  };

  option && myChart.setOption(option);
};

const handleResize = () => {
  if (myChart) {
    myChart.resize();
  }
};
const data = ref([
  { name: "北京", value: [116.407526, 39.90403, 10000] },
  { name: "上海", value: [121.473701, 31.230416, 666] },
  { name: "广州", value: [113.264385, 23.12911, 555] },
  { name: "深圳", value: [114.057868, 22.543099, 777] },
  { name: "杭州", value: [120.15507, 30.274085, 444] },
]);

onMounted(() => {
  init();
  window.addEventListener("resize", handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
  if (myChart) {
    myChart.dispose();
    myChart = null;
  }
});
</script>
<style lang="less">
.echarts-tooltip22 {
  background: linear-gradient(0deg, #09213b 0%, rgba(9, 33, 59, 0.7) 100%);
  box-shadow: 0px 8px 12px 0px rgba(0, 0, 0, 0.25);
  border-radius: 0px 0px 0px 0px;
  display: flex;
  align-items: center;
}
.custom-tooltip {
  display: flex;
  align-items: center;
  .t1 {
    font-weight: 500;
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
  }
  .t2 {
    font-weight: 500;
    font-size: 12px;
    color: #6ca8ff;
    cursor: pointer;
    padding-left: 10px;
  }
}
</style>
