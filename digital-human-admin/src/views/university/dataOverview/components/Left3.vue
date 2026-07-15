<template>
  <Card>
    <template #title>
      <img src="@/assets/screen/rzxxqyfb.png" class="w-[133px]" alt="" />
    </template>
    <template #content>
      <div class="relative h-full w-full overflow-hidden">
        <div ref="chart" class="w-full h-full relative"></div>
      </div>
    </template>
  </Card>
</template>
<script setup>
import { onBeforeUnmount, onMounted, ref } from "vue";
import Card from "./Card.vue";
import * as echarts from "echarts";
import { getSchoolRegion } from "@/api/university-dataOverview";

const chart = ref(null);
let myChart;

const init = () => {
  myChart = echarts.init(chart.value);
  const option = {
    title: {
      text: count.value,
      top: "44%",
      textAlign: "center",
      left: "28%",
      textStyle: {
        color: "#fff",
        fontSize: 20,
        fontWeight: "500",
        fontFamily: "BarlowCondensed-SemiBold",
      },
    },
    legend: {
      top: "center",
      right: "10%",
      orient: "vertical",
      icon: "circle",
      itemWidth: 8,
      itemHeight: 8,
      width: 100,
      textStyle: {
        color: "rgba(255,255,255,0.6)",
        fontSize: 10,
        overflow: "truncate",
        width: 100,
      },
    },
    series: [
      {
        name: "Access From",
        type: "pie",
        backgroundColor: "red",
        radius: ["45%", "60%"],
        center: ["30%", "50%"],
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: "center",
        },
        labelLine: {
          show: false,
        },
        data: data.value?.regions?.map((item) => {
          return {
            value: item?.count || 0,
            name: item?.region || "",
          };
        }),
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
const count = ref(0);

onMounted(() => {
  getSchoolRegion().then((res) => {
    data.value = res.data || {};
    for (let index = 0; index < data.value?.regions?.length; index++) {
      const element = data.value?.regions?.[index];
      count.value += element?.count;
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
<style scoped lang="less">
.bg100 {
  background: url(@/assets/screen/bd.png);
  background-size: 100% 100%;
  background-repeat: no-repeat;
}
</style>
