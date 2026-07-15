<template>
  <div class="flex space-x-10">
    <section
      class="shrink-0 w-[231px] flex flex-col items-center space-y-2 pt-3"
    >
      <Progress
        stroke-linecap="square"
        :percent="data.courseCompletion || 0"
        type="circle"
        :stroke-color="{
          '0%': '#7CB1FF',
          '100%': '#1966FF',
        }"
        :stroke-width="10"
        :size="[160, 160]"
      >
        <template #format="percent">
          <span class="text-2xl text-[rgba(10,8,26,0.88)] font-medium">
            {{ percent }}%
          </span>
          <div class="text-xs text-[rgba(10,8,26,0.55)]">课程平均完成度</div>
        </template>
      </Progress>
      <div
        class="pt-4 space-y-2 flex flex-col items-center flex-shrink-0"
        style="border-top: 1px solid rgba(255, 255, 255, 0.1)"
      >
        <div class="flex items-center space-x-2">
          <div class="w-[10px] h-[10px] bg-[#34C759] rounded-full"></div>
          <span class="text-sm text-[rgba(10,8,26,0.55)]">最快进度</span>
          <span class="pl-2 text-sm text-[rgba(10,8,26,0.88)] font-medium">
            {{ data?.fastestProgress || 0 }}%
          </span>
          <span class="text-sm text-[rgba(10,8,26,0.55)]">
            {{ data?.fastestProgressCount || 0 }}人
          </span>
        </div>
        <div class="flex items-center space-x-2">
          <div class="w-[10px] h-[10px] bg-[#FF383C] rounded-full"></div>
          <span class="text-sm text-[rgba(10,8,26,0.55)]">最慢进度</span>
          <span class="pl-2 text-sm text-[rgba(10,8,26,0.88)] font-medium">
            {{ data?.slowestProgress || 0 }}%
          </span>
          <span class="text-sm text-[rgba(10,8,26,0.55)]">
            {{ data?.slowestProgressCount || 0 }}
            人
          </span>
        </div>
      </div>
    </section>
    <section class="grow h-[270px]">
      <div ref="chart" class="size-full"></div>
    </section>
  </div>
</template>
<script setup>
import { Progress } from "ant-design-vue";
import { onBeforeUnmount, onMounted, ref } from "vue";
import * as echarts from "echarts";
import { getChapterEfficiency } from "@/api/studyDetailMonitor";
import { useRoute } from "vue-router";

const chart = ref(null);
let myChart;

const route = useRoute();
const data = ref({});
const init = () => {
  myChart = echarts.init(chart.value);
  const option = {
    grid: {
      left: "5%",
      right: "5%",
      bottom: 0,
      top: "24px",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: data.value?.progressList || [],
      splitLine: {
        show: false,
      },
      axisLine: {
        lineStyle: {
          color: "rgba(0,0,0,0.08)",
          type: "dashed",
        },
      },
      axisLabel: {
        color: "rgba(10,8,26,0.45)",
        fontSize: 12,
        rotate: 50,
      },
    },
    yAxis: {
      type: "value",
      axisLabel: {
        show: false,
      },
      splitLine: {
        show: false,
      },
    },
    series: [
      {
        data: data.value?.progressCountList || [],
        type: "bar",
        stack: "a",
        label: {
          show: true,
          position: "top", // 柱顶显示数字
          distance: 10, // 距离柱顶距离
          color: "rgba(10,8,26,1)",
          fontWeight: 500,
          fontSize: 14,
          formatter: "{c} 人", // 格式: 数值 + 人
        },
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: "rgba(25,102,255,0.5)",
            },
            {
              offset: 1,
              color: "rgba(25,102,255,0.1)",
            },
          ]),
        },
      },
      // {
      //   name: "单选题",
      //   type: "bar",
      //   stack: "a",
      //   itemStyle: {
      //     color: "#1966FF",
      //   },
      //   data: data.value?.progressCountList?.map((item) => 1),
      // },
      {
        data: data.value?.progressCountList,
        type: "pictorialBar",
        symbolPosition: "end",
        symbol: "rect",
        symbolOffset: [0, "-50%"],
        symbolSize: ["100%", 2],
        zlevel: 2,
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

onMounted(() => {
  getChapterEfficiency({
    courseId: route.query?.id,
  }).then((res) => {
    data.value = res?.data || {};
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
