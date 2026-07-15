<template>
  <div ref="chartRef" class="w-full h-[280px]"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from "vue";
import * as echarts from "echarts";
import { useRoute } from "vue-router";
import { getTimeDistribution } from "@/api/studyDetailMonitor";

const route = useRoute();
const chartRef = ref(null);
let chartInstance = null;

const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`);

const initChart = (data) => {
  if (!chartRef.value) return;
  chartInstance = echarts.init(chartRef.value);

  const values = hours.map((_, h) => data[h] || 0);
  const maxVal = Math.max(...values, 1);

  chartInstance.setOption({
    tooltip: {
      trigger: "item",
      formatter: (p) => `${p.name}<br/>学习次数: ${p.value}`,
    },
    polar: { radius: ["18%", "80%"] },
    angleAxis: {
      type: "category",
      data: hours,
      startAngle: 90,
      axisLabel: { color: "rgba(10,8,26,0.45)", fontSize: 10 },
    },
    radiusAxis: {
      axisLine: { show: false },
      axisLabel: { show: false },
      splitLine: { lineStyle: { color: "rgba(10,8,26,0.06)" } },
    },
    series: [
      {
        type: "bar",
        data: values.map((v) => ({
          value: v,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: "rgba(25,119,255,0.3)" },
              { offset: 1, color: v / maxVal > 0.6 ? "#1977FF" : "#7DBFFB" },
            ]),
          },
        })),
        coordinateSystem: "polar",
        barWidth: 6,
        name: "学习次数",
      },
    ],
    animationDuration: 1200,
    animationEasing: "cubicOut",
  });
};

onMounted(async () => {
  await nextTick();
  try {
    const res = await getTimeDistribution({ courseId: route.query.id });
    initChart(res?.data || {});
  } catch (e) {
    initChart({});
  }
});

onUnmounted(() => chartInstance?.dispose());
</script>
