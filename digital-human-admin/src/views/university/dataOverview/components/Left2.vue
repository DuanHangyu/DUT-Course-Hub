<template>
  <Card>
    <template #title>
      <img src="@/assets/screen/top5zdxxxxqs.png" class="w-[209px]" alt="" />
    </template>
    <template #content>
      <div class="h-full relative">
        <div class="chart absolute top-0 left-0 w-full"></div>
        <div ref="chart" class="w-full h-full"></div>
      </div>
    </template>
  </Card>
</template>
<script setup>
import { onBeforeUnmount, onMounted, ref } from "vue";
import Card from "./Card.vue";
import * as echarts from "echarts";
import { getLearningTrend } from "@/api/university-dataOverview";

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
      left: "0%",
      right: "0%",
      bottom: "0%",
      top: "20px",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: data.value?.dates || [],
      axisLine: {
        lineStyle: {
          color: "rgba(0,166,209,0.8)",
        },
      },
      axisTick: {
        show: false,
      },
      axisLabel: {
        color: "rgba(255,255,255,0.8)",
        fontSize: 14,
      },
    },
    yAxis: {
      type: "value",
      axisLine: {
        show: false,
      },
      splitLine: {
        lineStyle: {
          color: "rgba(255,255,255,0.08)",
        },
      },
      axisLabel: {
        show: false,
      },
    },
    series: data.value?.lines?.map((item) => {
      return {
        name: item?.schoolName,
        type: "line",
        data: item?.values || [],
        lineStyle: {
          // color: "#F6BD16",
          width: 1.5,
        },
        smooth: true,
        showSymbol: false,
      };
    }),
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
  getLearningTrend().then((res) => {
    data.value = res.data || {};
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
<style scoped lang="less">
.chart {
  background: linear-gradient(
    180deg,
    rgba(76, 213, 251, 0) 0%,
    rgba(0, 98, 125, 0.3) 100%
  );
  height: calc(100% - 24px);
}
</style>
