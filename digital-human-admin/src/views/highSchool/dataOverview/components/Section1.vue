<template>
  <Card>
    <template #title>
      <img src="@/assets/screen/xxsdrlt.png" class="w-[227px]" alt="" />
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
import { getHeatmap } from "@/api/highSchool-dataOverview";
import useUserStore from "@/store/modules/user";

const userStore = useUserStore();

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
      axisLine: {
        show: false,
      },
      axisTick: {
        show: false,
      },
      axisLabel: {
        color: "#D5D6FF",
        fontSize: 12,
        fontFamily: "PingFang SC",
      },
    },
    yAxis: {
      type: "category",
      data: yNames.value,
      axisLine: {
        show: false,
      },
      axisTick: {
        show: false,
      },
      axisLabel: {
        color: "#FFFFFF",
        fontSize: 13,
        fontFamily: "PingFang SC",
      },
    },
    visualMap: {
      min: 0,
      max: 24,
      calculable: false,
      orient: "horizontal",
      left: "center",
      bottom: "0%",
      show: false,
      inRange: {
        // 蓝色渐变，从浅到深
        color: [
          "rgba(76,152,251,0.1)",
          "rgba(76,152,251,0.2)",
          "rgba(76,152,251,0.3)",
          "rgba(76,152,251,0.4)",
          "rgba(76,152,251,0.5)",
          "rgba(76,152,251,0.6)",
          "rgba(76,152,251,0.7)",
          "rgba(76,152,251,0.8)",
          "rgba(76,152,251,0.9)",
          "rgba(76,152,251,1)",
        ],
      },
      outOfRange: {
        color: ["rgba(0,0,0,0)"],
      },
      borderRadius: 6,
      borderColor: "transparent",
      borderWidth: 3,
    },
    series: [
      {
        name: "学习时间",
        type: "heatmap",
        data: data.value,
        label: {
          show: false,
        },
        zlevel: -1,
        itemStyle: {
          borderRadius: 6,
          borderColor: "#061e39",
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

onMounted(() => {
  getHeatmap({
    schoolId: userStore.schoolId,
  }).then((res) => {
    xNames.value = res?.data?.dates || [];
    yNames.value = res?.data?.students || [];
    for (let i = 0; i < res?.data?.values?.length; i++) {
      const element = res?.data?.values[i];
      for (let index2 = 0; index2 < element?.length; index2++) {
        const element2 = element?.[index2];
        data.value.push([index2, i, element2]);
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
