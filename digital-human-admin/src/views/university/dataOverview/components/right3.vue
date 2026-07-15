<template>
  <Card>
    <template #title>
      <img src="@/assets/screen/kcxxrstj.png" class="w-[133px]" alt="" />
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
import { getCourseLearning } from "@/api/university-dataOverview";

const colorList = ref([
  ["#537CE5", "#C8D7FF"],
  ["#FFB012", "#FFECC6"],
  ["#026FB2", "#0DD5D3"],
  ["#6DD403", "#4ED68A"],
  ["#116BFE", "#2CDAFD"],
]);

const chart = ref(null);
let myChart;
const init = () => {
  myChart = echarts.init(chart.value);
  const option = {
    grid: {
      left: "8px",
      right: "-10px",
      bottom: "-10px",
      top: "16px",
      containLabel: true,
    },
    xAxis: {
      type: "value",
      show: false,
      axisLine: {
        show: false,
      },
      splitLine: {
        show: false,
      },
    },
    yAxis: [
      {
        type: "category",
        axisLine: {
          show: false,
        },
        axisTick: {
          show: false,
        },
        axisLabel: {
          show: false,
        },
      },
      {
        triggerEvent: true,
        show: true,
        // inverse: true,
        data: data.value?.courses?.map((item) => item?.studentCount || 0),
        axisLine: {
          show: false,
        },
        splitLine: {
          show: false,
        },
        axisTick: {
          show: false,
        },
        axisLabel: {
          interval: 0,
          color: "rgba(255,255,255,0.8)",
          align: "right",
          verticalAlign: "bottom",
          lineHeight: 20,
          fontSize: 13,
          padding: [0, 10, 0, 0],
          fontFamily: "BarlowCondensed-SemiBold",
          formatter: function (value, index) {
            return value;
          },
        },
      },
    ],
    series: [
      {
        type: "pictorialBar",
        symbol:
          "image://data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAMAAABEpIrGAAAAjVBMVEUAAADz9v/r8P/s8v/n7v/y9v/9///o8//w8P/p8P/s9f/o7//v9P/m7v/n7v/o7//o9P/p9v/p8v/l7f/m7f/x9v/09f/l7f/l7f/l7v/l7f/m7f/m7f/m7v/o7v/k7P/m8P/w9P/m7v/v8//u+P/q7f/m7f/m7f/l7v/n7f/v8//t9f/r9P/m6//m7f8o67YUAAAALnRSTlMABg8cLWgKHwxfFikZqJNbJRNhvnJlYGzPtqyfjoeDfHZWw0ZEMG7x75hQTULDTK/1ngAAAXxJREFUOMutk9m2giAUhkNFQFAQRZtzLEuP7/94B3PAWq3VTf8t32YP/96bnwos+vzsYmw9hV2DrKKxFXrM1mIstPCMmHArZEhQR4sKpBH3hdDhni3qMk8gTPKqFrY3EiaeIdopJTnnRKqkouiFAJaHnAckMo2zLE4lh62jCWAShDZtIbleTscgOJ4uKYEPaocYLB8wUUISn4ODv9/7h+AcE9gJZrlzBSGqE3493/1dVBTRzr+fU6JqFM4A9kSl5CXwo+2t75tt5AcXqUrhYTBlsGlO0tNht+2f2u4Op5TklFkL4CQkPvpRMwK3yD/GPHHsFQB5FuyLflKxDzIOvwNonYIPKW7jezOkIAk1PzCay/cipSlyaLOc2mx0iVOb1dLmButBKTIN6m8clG5CDwqYUXfvoy7FM4Mx67Ey60pgS5Exa+MOdreQz3ZrM43dg8BA0CpRknDOpVKdWRhD2MKpxpUra6HfMfi6tK9arb0XWp8vw10dDvhyer/UPyWeKa64k9arAAAAAElFTkSuQmCC",
        symbolSize: [16, 16],
        symbolOffset: [8, 0],
        z: 12,
        data: data.value?.courses?.map((item) => {
          return {
            value: item?.studentCount || 0,
            symbolPosition: "end",
          };
        }),
      },
      {
        type: "bar",
        showBackground: true,
        backgroundStyle: {
          color: "#193A96",
        },
        data: data.value?.courses?.map((item, index) => {
          return {
            value: item?.studentCount || 0,
            name: item?.courseName,
            rank: index,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                {
                  offset: 0,
                  color: colorList.value[index % colorList.value.length][0],
                },
                {
                  offset: 1,
                  color: colorList.value[index % colorList.value.length][1],
                },
              ]),
              borderRadius: [0, 0, 0, 0],
            },
          };
        }),
        barWidth: "2px",
        label: {
          show: true,
          formatter: (parmas) => {
            return parmas.name;
          },
          color: "rgba(255,255,255,0.8)",
          fontSize: 10,
          fontWeight: 400,
          position: [0, -14],
        },
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

onMounted(() => {
  getCourseLearning().then((res) => {
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
