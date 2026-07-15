<template>
  <div ref="chartRef" class="w-full" style="height: 320px"></div>
</template>
<script setup>
import { ref, onMounted, onUnmounted, nextTick } from "vue";
import * as echarts from "echarts";
import { useRoute } from "vue-router";
import { getNodeCompletionRanking } from "@/api/studyDetailMonitor";

const route = useRoute();
const chartRef = ref(null);
let chartInstance = null;

const initChart = (data) => {
  if (!chartRef.value) return;
  chartInstance = echarts.init(chartRef.value);
  const names = data.map((d) => d.nodeName);
  const rates = data.map((d) => d.completionRate);

  chartInstance.setOption({
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "shadow" },
      formatter: (p) => {
        const idx = p[0].dataIndex;
        const d = data[idx];
        return `${d.nodeName}<br/>完成率: ${d.completionRate}%<br/>${d.completedCount}/${d.totalCount} 人`;
      },
    },
    grid: { left: 100, right: 50, top: 10, bottom: 10 },
    xAxis: {
      type: "value",
      max: 100,
      axisLabel: { formatter: "{value}%", color: "rgba(10,8,26,0.45)", fontSize: 11 },
      splitLine: { lineStyle: { color: "rgba(10,8,26,0.06)" } },
    },
    yAxis: {
      type: "category",
      data: names,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: "rgba(10,8,26,0.65)", fontSize: 11, width: 90, overflow: "truncate" },
    },
    series: [
      {
        type: "bar",
        data: rates.map((v) => ({
          value: v,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: v < 30 ? "#FF4D4F" : v < 60 ? "#FA8C16" : v < 80 ? "#FAAD14" : "#52C41A" },
              { offset: 1, color: v < 30 ? "#FF7875" : v < 60 ? "#FFA940" : v < 80 ? "#FFC53D" : "#73D13D" },
            ]),
            borderRadius: [0, 4, 4, 0],
          },
        })),
        barWidth: 14,
        label: { show: true, position: "right", formatter: "{c}%", fontSize: 11, color: "rgba(10,8,26,0.55)" },
      },
    ],
    animationDuration: 800,
    animationDelay: (idx) => idx * 50,
  });
};

onMounted(async () => {
  await nextTick();
  try {
    const res = await getNodeCompletionRanking({ courseId: route.query.id });
    initChart(res?.data || []);
  } catch (e) { initChart([]); }
});
onUnmounted(() => chartInstance?.dispose());
</script>
