<template>
  <div class="card flex-col flex overflow-hidden">
    <section class="flex justify-between shrink-0">
      <div>
        <div
          class="text-sm font-semibold"
          :style="{
            color: list[cardIndex].titleColor,
          }"
        >
          {{ list[cardIndex].title }}
        </div>
        <div class="text-[34px] font-semibold text-[#FFFFFF] mt-1">
          <CountTo
            :startVal="0"
            :endVal="list[cardIndex].value"
            :suffix="cardIndex == 1 ? '%' : cardIndex == 2 ? '个' : ''"
          />
        </div>
      </div>
      <div class="size-8 rounded-lg flex items-center justify-center">
        <img :src="list[cardIndex].img" alt="" />
      </div>
    </section>
    <section class="mt-2 w-full grow">
      <div class="size-full" ref="chart"></div>
    </section>
    <section class="text-[10px] text-[#94A3B8] mt-2 shrink-0">
      {{ list[cardIndex].desc }}
    </section>
  </div>
</template>
<script setup>
import Card1Img from "@/assets/screen/card1.png";
import Card2Img from "@/assets/screen/card2.png";
import Card3Img from "@/assets/screen/card3.png";
import Card4Img from "@/assets/screen/card4.png";
import { onMounted, ref } from "vue";
import * as echarts from "echarts";
import {
  getActiveTrend24h,
  getAiResponseTrend7d,
  getHomeworkTrend7d,
  getRiskTrend,
  getSummary,
} from "@/api/highSchool-dataOverview";
import useUserStore from "@/store/modules/user";
import CountTo from "@/components/CountTo/index.vue";

const userStore = useUserStore();
const props = defineProps({
  cardIndex: {
    type: Number,
  },
});

const list = ref([
  {
    title: "实时活跃人数",
    titleColor: "#22D3EE",
    img: Card1Img,
    imgBgColor: "rgba(6,182,212,0.1)",
    desc: "24小时变化趋势",
    area: ["rgba(6,182,212,0.2)", "rgba(6,182,212,0)"],
    value: 0,
  },
  {
    title: "作业平均提交率",
    titleColor: "#C084FC",
    img: Card2Img,
    imgBgColor: "rgba(168,85,247,0.1)",
    desc: "近7天变化趋势",
    area: ["rgba(168,85,247,0.2)", "rgba(168,85,247,0)"],
    value: 0,
  },
  {
    title: "AI 助教响应数",
    titleColor: "#60A5FA",
    img: Card3Img,
    imgBgColor: "rgba(59,130,246,0.1)",
    desc: "近7天变化趋势",
    area: ["rgba(59,130,246,0.2)", "rgba(59,130,246,0)"],
    value: 0,
  },
  {
    title: "风险预警班级",
    titleColor: "#F87171",
    img: Card4Img,
    imgBgColor: "rgba(239,68,68,0.1)",
    desc: "近10天变化趋势",
    area: ["rgba(244,63,94,0.2)", "rgba(244,63,94,0)"],
    value: 0,
  },
]);

const chart = ref(null);

const init = () => {
  const myChart = echarts.init(chart.value);
  const option = {
    tooltip: {
      trigger: "axis",
    },
    legend: {
      show: false,
    },
    grid: {
      left: 0,
      right: 0,
      bottom: 0,
      top: 0,
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: data.value?.dates || [],
      axisLine: {
        show: false,
      },
      axisTick: {
        show: false,
      },
      axisLabel: {
        show: false,
      },
      boundaryGap: false,
    },
    yAxis: {
      type: "value",
      axisLine: {
        show: false,
      },
      splitLine: {
        show: false,
      },
      axisLabel: {
        show: false,
      },
    },
    series: [
      {
        data: data.value?.values || [],
        type: "line",
        showSymbol: false,
        lineStyle: {
          color: list.value[props.cardIndex]?.titleColor,
          width: 1,
        },
        smooth: props.cardIndex == 3 ? false : true,
        areaStyle: {
          opacity: 0.8,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: list.value[props.cardIndex]?.area?.[0],
            },
            {
              offset: 1,
              color: list.value[props.cardIndex]?.area?.[1],
            },
          ]),
        },
      },
    ],
  };

  option && myChart.setOption(option);
};

const data = ref({});

onMounted(() => {
  if (props.cardIndex == 0) {
    getActiveTrend24h({
      schoolId: userStore.schoolId,
    }).then((res) => {
      data.value = res?.data || {};
      init();
    });
  }
  if (props.cardIndex == 1) {
    getHomeworkTrend7d({
      schoolId: userStore.schoolId,
    }).then((res) => {
      data.value = res?.data || {};
      init();
    });
  }
  if (props.cardIndex == 2) {
    getAiResponseTrend7d({
      schoolId: userStore.schoolId,
    }).then((res) => {
      data.value = res?.data || {};
      init();
    });
  }
  if (props.cardIndex == 3) {
    getRiskTrend({
      schoolId: userStore.schoolId,
    }).then((res) => {
      data.value = res?.data || {};
      init();
    });
  }
  getSummary({
    schoolId: userStore.schoolId,
  }).then((res) => {
    list.value[0].value = res?.data?.activeCount || 0;
    list.value[1].value = res?.data?.homeworkRate || 0;
    list.value[2].value = res?.data?.aiResponseCount || 0;
    list.value[3].value = res?.data?.riskClassCount || 0;
  });
});
</script>
<style scoped lang="less">
.card {
  width: 100%;
  padding: 21px;
  height: 100%;
  background: linear-gradient(
    54deg,
    rgba(30, 41, 59, 0.7) 0%,
    rgba(15, 23, 42, 0.8) 100%
  );
  box-shadow:
    0px 2px 4px -1px rgba(0, 0, 0, 0.15),
    0px 4px 6px -1px rgba(0, 0, 0, 0.3);
  border-radius: 4px 4px 4px 4px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}
</style>
