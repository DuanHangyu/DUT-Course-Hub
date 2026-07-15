<template>
  <div class="overflow-hidden relative h-[300px]">
    <div ref="chart" class="w-full h-[260px]"></div>
    <Select
      class="absolute top-[160px] z-10 left-1/2 -translate-x-1/2"
      v-model:value="currentIndex"
      :options="list"
    />
    <div
      class="h-[1px] w-full bg-[rgba(0,0,0,0.08)] absolute top-[220px]"
    ></div>
    <div class="grid grid-cols-2 absolute w-full top-[240px] z-10">
      <div class="flex flex-col items-center">
        <div class="text-sm text-[rgba(10,8,26,0.55)]">已提交人数</div>
        <div class="text-2xl font-medium text-[rgba(10,8,26,0.88)]">
          {{ list[currentIndex]?.submittedCount }}
        </div>
      </div>
      <div class="flex flex-col items-center">
        <div class="text-sm text-[rgba(10,8,26,0.55)]">未提交人数</div>
        <div class="text-2xl font-medium text-[rgba(10,8,26,0.88)]">
          {{ list[currentIndex]?.unsubmittedCount }}
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { onBeforeUnmount, onMounted, ref } from "vue";
import * as echarts from "echarts";
import { Select } from "ant-design-vue";
import { getCourseOverview } from "@/api/studyDetailMonitor";
import { useRoute } from "vue-router";

const chart = ref(null);
let myChart;
const route = useRoute();
const list = ref([]);
const currentIndex = ref(0);

const init = () => {
  myChart = echarts.init(chart.value);
  const current = list.value?.[currentIndex.value];
  const option = {
    grid: {
      left: "0",
      right: "0",
      bottom: "0%",
      top: "0",
      containLabel: true,
    },
    series: [
      {
        type: "gauge",
        startAngle: 180,
        endAngle: 0,
        radius: "100%",
        progress: {
          show: true,
          width: 16,
          roundCap: true,
        },
        axisLine: {
          lineStyle: {
            width: 16,
          },
          roundCap: true,
        },
        axisTick: {
          show: false,
        },
        splitLine: {
          show: false,
        },
        axisLabel: {
          show: false,
        },
        anchor: {
          show: false,
        },
        pointer: {
          show: false,
        },
        title: {
          offsetCenter: [0, "-10%"],
          fontSize: 14,
          color: "rgba(10,8,26,0.55)",
        },
        detail: {
          fontSize: 32,
          fontWeight: 600,
          color: "rgba(10,8,26,0.88)",
          offsetCenter: [0, "-45%"],
          valueAnimation: true,
          formatter: function (value) {
            return value + "%";
          },
        },
        data: [
          {
            value: current?.submitRate || 0,
            name: "课程平均完成度",
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {
                  offset: 0,
                  color: "rgba(25, 102, 255, 1)",
                },
                {
                  offset: 1,
                  color: "rgba(89, 255, 188, 1)",
                },
              ]),
            },
          },
        ],
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
  getCourseOverview({
    courseId: route.query?.id,
  }).then((res) => {
    list.value =
      res?.data?.map((item, index) => {
        return {
          label: item?.homeworkName,
          value: index,
          ...item,
        };
      }) || {};
    currentIndex.value = 0;
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
:deep(.ant-select) {
  width: 300px !important;
  .ant-select-selector {
    border: 1px solid rgba(0, 0, 0, 0.15);
  }
}
</style>
