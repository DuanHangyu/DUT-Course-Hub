<template>
  <Card>
    <template #title>
      <img src="@/assets/screen/kcwcd.png" class="w-[83px]" alt="" />
    </template>
    <template #content>
      <div class="h-full flex flex-col overflow-hidden relative">
        <Select
          class="!w-full flex-shrink-0"
          :field-names="{
            label: 'courseName',
            value: 'currentValue',
          }"
          :options="data"
          v-model:value="currentIndex"
          @change="updateCurrentIndex"
        />
        <div ref="chart" class="w-full grow"></div>
        <div
          class="pt-4 space-y-2 flex flex-col items-center shrink-0"
          style="border-top: 1px solid rgba(255, 255, 255, 0.1)"
        >
          <div class="flex items-center space-x-2">
            <div class="w-[10px] h-[10px] bg-[#34C759] rounded-full"></div>
            <span class="text-sm text-white">已完成</span>
            <span
              class="pl-4 text-sm text-[rgba(255,255,255,0.88)] font-medium"
            >
              {{
                data[currentIndex]?.totalCount
                  ? (
                      (data[currentIndex]?.completedCount /
                        data[currentIndex]?.totalCount) *
                      100
                    ).toFixed(2)
                  : 0
              }}%
            </span>
            <span class="text-sm text-[rgba(255,255,255,0.55)]">
              {{ data[currentIndex]?.completedCount || 0 }}
              人
            </span>
          </div>
          <div class="flex items-center space-x-2">
            <div class="w-[10px] h-[10px] bg-[#FF383C] rounded-full"></div>
            <span class="text-sm text-white">未完成</span>
            <span
              class="pl-4 text-sm text-[rgba(255,255,255,0.88)] font-medium"
            >
              {{
                data[currentIndex]?.totalCount
                  ? (
                      (data[currentIndex]?.uncompletedCount /
                        data[currentIndex]?.totalCount) *
                      100
                    ).toFixed(2)
                  : 0
              }}%
            </span>
            <span class="text-sm text-[rgba(255,255,255,0.55)]">
              {{ data[currentIndex]?.uncompletedCount || 0 }}
              人
            </span>
          </div>
        </div>
      </div>
    </template>
  </Card>
</template>
<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import Card from "./Card.vue";
import * as echarts from "echarts";
import { Select } from "ant-design-vue";
import { getCourseCompletion } from "@/api/highSchool-dataOverview";
import useUserStore from "@/store/modules/user";

const userStore = useUserStore();

const chart = ref(null);
let myChart;
const currentIndex = ref(0);

const init = () => {
  myChart = echarts.init(chart.value);

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
        center: ["50%", "80%"],
        radius: "140%",
        progress: {
          show: true,
          width: 16,
        },
        axisLine: {
          lineStyle: {
            width: 16,
          },
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
          fontSize: 10,
          color: "#fff",
        },
        detail: {
          fontSize: 25,
          fontWeight: 500,
          color: "#fff",
          offsetCenter: [0, "-45%"],
          valueAnimation: true,
          formatter: function (value) {
            return value + "%";
          },
          fontFamily: "BarlowCondensed-SemiBold",
        },
        data: [
          {
            value: data.value[currentIndex.value]?.completionRate || 0,
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

const data = ref([]);

const updateCurrentIndex = () => {
  nextTick(() => {
    init();
  });
};

onMounted(() => {
  getCourseCompletion({
    schoolId: userStore.schoolId,
  }).then((res) => {
    data.value =
      res?.data?.map((item, index) => {
        return {
          ...item,
          currentValue: index,
        };
      }) || [];
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
  .ant-select-selector {
    background: rgba(255, 255, 255, 0.05) !important;
    border-radius: 4px 4px 4px 4px;
    border: 1px solid rgba(255, 255, 255, 0.05);
  }
  .ant-select-selection-item {
    color: white;
  }
}
</style>
