<template>
  <div ref="chartRef" class="w-full" style="height: 260px"></div>
</template>
<script setup>
import { ref, onMounted, onUnmounted, nextTick } from "vue";
import * as echarts from "echarts";
import { useRoute } from "vue-router";
import { getDailyStudyTrend } from "@/api/studyDetailMonitor";

const route = useRoute();
const chartRef = ref(null);
let chartInstance = null;

const initChart = (data) => {
  if (!chartRef.value) return;
  chartInstance = echarts.init(chartRef.value);
  const dates = data.map((d) => {
    const parts = d.date.split("-");
    return `${parts[1]}/${parts[2]}`;
  });
  const counts = data.map((d) => d.count);

  chartInstance.setOption({
    tooltip: { trigger: "axis", formatter: (p) => `${p[0].name}<br/>学习次数: ${p[0].value}` },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: "category",
      data: dates,
      boundaryGap: false,
      axisLabel: { color: "rgba(10,8,26,0.45)", fontSize: 10, interval: 1 },
      axisLine: { lineStyle: { color: "rgba(10,8,26,0.1)" } },
    },
    yAxis: {
      type: "value",
      axisLabel: { color: "rgba(10,8,26,0.45)", fontSize: 10 },
      splitLine: { lineStyle: { color: "rgba(10,8,26,0.06)" } },
    },
    series: [
      {
        type: "line",
        data: counts,
        smooth: true,
        symbol: "circle",
        symbolSize: 5,
        lineStyle: { width: 2, color: "#1977FF" },
        itemStyle: { color: "#1977FF" },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(25,119,255,0.35)" },
            { offset: 1, color: "rgba(25,119,255,0.02)" },
          ]),
        },
      },
    ],
    animationDuration: 1200,
    animationEasing: "cubicOut",
  });
};

onMounted(async () => {
  await nextTick();
  try {
    const res = await getDailyStudyTrend({ courseId: route.query.id });
    initChart(res?.data || []);
  } catch (e) { initChart([]); }
});
onUnmounted(() => chartInstance?.dispose());
</script>
