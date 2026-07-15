<template>
  <div ref="chartRef" class="w-full" style="height: 260px"></div>
</template>
<script setup>
import { ref, onMounted, onUnmounted, nextTick } from "vue";
import * as echarts from "echarts";
import { useRoute } from "vue-router";
import { getProgressDistribution } from "@/api/studyDetailMonitor";

const route = useRoute();
const chartRef = ref(null);
let chartInstance = null;

const colors = ["#E0E0E0", "#FF7875", "#FFA940", "#69C0FF", "#52C41A"];

const initChart = (data) => {
  if (!chartRef.value) return;
  chartInstance = echarts.init(chartRef.value);
  const keys = Object.keys(data);
  const values = Object.values(data);
  const total = values.reduce((a, b) => a + b, 0);

  chartInstance.setOption({
    tooltip: { trigger: "item", formatter: "{b}: {c}人 ({d}%)" },
    legend: {
      orient: "vertical",
      right: 10,
      top: "center",
      textStyle: { fontSize: 11, color: "rgba(10,8,26,0.55)" },
      itemWidth: 10,
      itemHeight: 10,
    },
    series: [
      {
        type: "pie",
        radius: ["45%", "70%"],
        center: ["35%", "50%"],
        avoidLabelOverlap: false,
        label: {
          show: true,
          position: "center",
          formatter: () => `{a|${total}}\n{b|总人数}`,
          rich: {
            a: { fontSize: 28, fontWeight: "bold", color: "rgba(10,8,26,0.85)", lineHeight: 36 },
            b: { fontSize: 12, color: "rgba(10,8,26,0.45)" },
          },
        },
        data: keys.map((k, i) => ({ name: k, value: data[k], itemStyle: { color: colors[i] } })),
        itemStyle: { borderColor: "#fff", borderWidth: 2 },
      },
    ],
    animationDuration: 1000,
    animationType: "expansion",
  });
};

onMounted(async () => {
  await nextTick();
  try {
    const res = await getProgressDistribution({ courseId: route.query.id });
    initChart(res?.data || {});
  } catch (e) { initChart({}); }
});
onUnmounted(() => chartInstance?.dispose());
</script>
