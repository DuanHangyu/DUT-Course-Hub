<template>
  <div ref="chart" class="w-full h-[280px]"></div>
</template>
<script setup>
import { onBeforeUnmount, onMounted, ref } from "vue";
import * as echarts from "echarts";
import { getActivityHeatmap } from "@/api/studyDetailMonitor";
import { useRoute } from "vue-router";

const chart = ref(null);
let myChart;

// Y 轴数据（人名）
const yNames = ref([]);

// X 轴数据（日期）
const xNames = ref([]);

// 热力图数据 [x 索引，y 索引，值]
// 值越大颜色越深，0 表示无数据
const data = ref([]);

const init = () => {
  myChart = echarts.init(chart.value);

  const option = {
    tooltip: {
      position: "top",
      backgroundColor: "rgba(0, 0, 0, 0.8)",
      borderColor: "rgba(76, 213, 251, 0.5)",
      textStyle: {
        color: "#fff",
        fontSize: 12,
      },
      // formatter: function (params) {
      //   return `${yNames?.value[params.data[1]]} ${xNames?.value[params.data[0]]}`;
      // },
    },
    grid: {
      left: "3%",
      right: "3%",
      bottom: "3%",
      top: "3%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: xNames.value,
      offset: 5,
      axisLine: {
        show: false,
      },
      axisTick: {
        show: false,
      },
      axisLabel: {
        color: "rgba(10,8,26,0.45)",
        fontSize: 12,
        fontFamily: "PingFang SC",
      },
    },
    yAxis: {
      type: "category",
      data: yNames.value,
      offset: 2,
      axisLine: {
        show: false,
      },
      axisTick: {
        show: false,
      },
      axisLabel: {
        color: "rgba(0,0,0,0.85)",
        fontSize: 13,
        fontFamily: "PingFang SC",
      },
    },
    visualMap: {
      min: 0,
      max: 100,
      calculable: false,
      orient: "horizontal",
      left: "center",
      bottom: "0%",
      show: false,
      inRange: {
        // 蓝色渐变，从浅到深
        color: [
          "rgba(25, 102, 255, 0.1)",
          "rgba(25, 102, 255, 0.2)",
          "rgba(25, 102, 255, 0.3)",
          "rgba(25, 102, 255, 0.4)",
          "rgba(25, 102, 255, 0.5)",
          "rgba(25, 102, 255, 0.6)",
          "rgba(25, 102, 255, 0.7)",
          "rgba(25, 102, 255, 0.8)",
          "rgba(25, 102, 255, 0.9)",
          "rgba(25, 102, 255, 1)",
        ],
      },
    },
    series: [
      {
        name: "学习活力",
        type: "heatmap",
        data: data.value,
        label: {
          show: false,
        },
        zlevel: -1,
        itemStyle: {
          borderRadius: 6,
          borderColor: "#fff",
          borderWidth: 3,
        },
      },
    ],
  };

  myChart.setOption(option);
};

const handleResize = () => {
  if (myChart) {
    myChart.resize();
  }
};

const route = useRoute();

onMounted(() => {
  getActivityHeatmap({
    courseId: route.query.id,
  }).then((res) => {
    xNames.value = res?.data?.dateRange || [];
    for (let index = 0; index < res?.data?.students?.length; index++) {
      const element = res?.data?.students?.[index];
      yNames.value.push(element?.studentName);
      for (let index2 = 0; index2 < element?.dailyScores?.length; index2++) {
        const element2 = element?.dailyScores?.[index2];
        data.value.push([index2, index, element2]);
      }
    }
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
