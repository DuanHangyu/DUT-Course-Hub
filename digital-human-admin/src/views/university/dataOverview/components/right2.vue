<template>
  <Card>
    <template #title>
      <img src="@/assets/screen/fwjkjz.png" class="w-[100px]" alt="" />
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
import { getSystemHealth } from "@/api/university-dataOverview";
const chart = ref(null);
let myChart;
const data = ref({});

// 创建一个六边形的 points 数组
function createHexagon(centerX, centerY, radius) {
  const points = [];
  for (let i = 0; i < 6; i++) {
    const angle = (Math.PI / 3) * i - Math.PI / 6;
    const x = centerX + radius * Math.cos(angle);
    const y = centerY + radius * Math.sin(angle);
    points.push([x, y]);
  }
  return points;
}
function generateHexGrid(
  rows,
  cols,
  radius,
  offsetX = 0,
  offsetY = 0,
  data = [],
) {
  const hexagons = [];
  const spacing = radius * 1.5;

  for (let row = 0; row < rows; row++) {
    for (let col = 0; col < cols; col++) {
      let x = offsetX + col * spacing * 1.2;
      let y = offsetY + row * spacing * 0.85;

      // 关键修改：奇偶行交替偏移
      if (row % 2 === 0) {
        x -= spacing * 0; // 偶数行（第1、3、5...）向左偏移
      } else {
        x += spacing * 0.6; // 奇数行（第2、4、6...）向右偏移
      }

      const points = createHexagon(x, y, radius);
      const index = row * cols + col;
      const value = data[index] || 0;

      const colors = ["rgba(58,88,218,0.5)", "#ffcc00", "#ff6b6b"];
      const fill = colors[value];

      hexagons.push({
        type: "polygon",
        shape: { points },
        style: {
          fill: fill,
          stroke: "#000",
          lineWidth: 1,
        },
        z: 1,
      });
    }
  }
  return hexagons;
}

const init = () => {
  myChart = echarts.init(chart.value);

  const items = data.value?.items || [];
  const n = items.length;
  if (n === 0) {
    myChart.setOption({ graphic: [] });
    return;
  }

  const radius = 15;
  const spacingX = radius * 1.5 * 1.2; // 列间距
  const spacingY = radius * 1.5 * 0.85; // 行间距
  const offsetX = 80;
  const offsetY = 80;

  const hexagons = [];
  const positions = []; // 如果后续要加文字用

  for (let index = 0; index < n; index++) {
    const row = Math.floor(index / 8);
    const col = index % 8;

    // 计算中心点（注意奇偶行偏移）
    let x = offsetX + col * spacingX;
    let y = offsetY + row * spacingY;
    if (row % 2 === 1) {
      x += spacingX / 2; // 奇数行向右偏移半个列宽
    }

    const points = createHexagon(x, y, radius);
    const value = items[index]?.status === "red" ? 2 : 0;
    const colors = ["rgba(58,88,218,0.5)", "#ffcc00", "#ff6b6b"];
    const fill = colors[value];

    hexagons.push({
      type: "polygon",
      shape: { points },
      style: {
        fill: fill,
        stroke: "#000",
        lineWidth: 1,
      },
      z: 1,
    });

    positions.push({ x, y }); // 用于文字（如果需要）
  }

  // 动态计算图表高度（可选，确保容器能显示全部）
  const totalRows = Math.ceil(n / 8);
  const chartHeight = offsetY * 2 + totalRows * spacingY + radius * 2;
  // 注意：ECharts 容器高度由外部 div 控制，这里仅作参考

  const option = {
    grid: { left: 0, right: 0, top: 0, bottom: 0 },
    graphic: [
      {
        type: "group",
        left: "center",
        top: "middle",
        children: hexagons,
      },
    ],
    // 如果不需要文字，可以移除 series
    series: [], // 或保留用于交互
  };

  myChart.setOption(option);
};
const handleResize = () => {
  if (myChart) {
    myChart.resize();
  }
};

onMounted(() => {
  getSystemHealth().then((res) => {
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
