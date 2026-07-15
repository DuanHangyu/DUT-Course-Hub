<template>
  <Card>
    <template #title>
      <img src="@/assets/screen/qxhydqs.png" class="w-[212px]" alt="" />
    </template>
    <template #content>
      <div ref="chart" class="w-full h-full"></div>
    </template>
  </Card>
</template>
<script setup>
import { onBeforeUnmount, onMounted, ref } from "vue";
import Card from "./Card.vue";
import * as echarts from "echarts";
import { getTrend } from "@/api/highSchool-dataOverview";
import useUserStore from "@/store/modules/user";

const userStore = useUserStore();
const chart = ref(null);
let myChart;
const init = () => {
  myChart = echarts.init(chart.value);
  const option = {
    tooltip: {
      trigger: "axis",
    },
    legend: {
      show: false,
    },
    grid: {
      left: "15px",
      right: "15px",
      bottom: "0%",
      top: "20px",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: data.value?.dates || [],
      axisLine: {
        lineStyle: {
          color: "rgba(58,206,245,0.4)",
        },
      },
      axisTick: {
        show: false,
      },
      axisLabel: {
        color: "rgba(213,214,255,0.6)",
        fontSize: 12,
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: "rgba(192, 246, 255, 0.1)",
        },
      },
      boundaryGap: false,
    },
    yAxis: {
      type: "value",
      axisLine: {
        show: true,
        lineStyle: {
          color: "rgba(58,206,245,0.4)",
        },
      },
      axisTick: {
        show: false,
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: "rgba(192, 246, 255, 0.1)",
        },
      },
      axisLabel: {
        color: "rgba(213,214,255,0.6)",
        fontSize: 12,
      },
    },
    series: [
      {
        data: data.value?.values || [],
        type: "line",
        showSymbol: false,
        lineStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: "rgba(220, 214, 255, 1)",
            },
            {
              offset: 1,
              color: "rgba(145, 125, 255, 1)",
            },
          ]),
          width: 2,
        },
        smooth: true,
        areaStyle: {
          opacity: 0.8,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: "rgba(168,85,247,0.2)",
            },
            {
              offset: 1,
              color: "rgba(168,85,247,0)",
            },
          ]),
        },
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
const data = ref({});

onMounted(() => {
  getTrend({
    schoolId: userStore.schoolId,
  }).then((res) => {
    data.value = res?.data || {};
    init();
  });
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
