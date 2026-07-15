<template>
  <div class="chart relative p-6 overflow-y-auto">
    <img
      src="@/assets/images/detailJianbian.png"
      class="topImg absolute top-0 left-0"
      alt=""
    />
    <img
      src="@/assets/screen/rightBottom.png"
      class="bottomImg fixed bottom-0 right-0"
      alt=""
    />
    <div class="size-full relative">
      <div class="grid grid-cols-4 gap-x-4">
        <section
          class="flex p-4 space-x-4"
          :class="`tab${index + 1}`"
          v-for="(item, index) in list"
          :key="index"
        >
          <img :src="item.img" class="size-8 shrink-0" alt="" />
          <div class="grow overflow-hidden">
            <div class="title">{{ item.title }}</div>
            <div
              class="text-[32px] text-[rgba(10,8,26,0.88)] font-semibold flex items-center justify-between mt-3"
            >
              <span>{{ item.value }}</span>
              <span class="text-xs text-[rgba(10,8,26,0.45)]">{{
                item.desc
              }}</span>
            </div>
          </div>
        </section>
      </div>
      <div class="grid grid-cols-3 gap-x-5 mt-5">
        <div class="col-span-2 space-y-5">
          <section class="card">
            <div class="flex items-center justify-between mb-4">
              <span class="text-base text-[rgba(10,8,26,0.88)] font-semibold">
                班级知识图谱热力图
              </span>
              <div class="flex items-center space-x-3">
                <Badge color="#1977FF" text="通畅" />
                <Badge color="#FF4D4F" text="阻塞" />
              </div>
            </div>
            <RltCom />
          </section>
          <section class="card">
            <div class="text-base text-[rgba(10,8,26,0.88)] font-semibold mb-4">
              班级进度分布
            </div>
            <Bjfb />
          </section>
        </div>
        <div class="w-full space-y-5">
          <section class="card">
            <div class="text-base text-[rgba(10,8,26,0.88)] font-semibold mb-4">
              学习时段分布
            </div>
            <Gpzsmq />
          </section>
          <section class="card">
            <div class="text-base text-[rgba(10,8,26,0.88)] font-semibold mb-4">
              学生进度排行榜（Top10）
            </div>
            <Hydpm />
          </section>
        </div>
      </div>
      <div class="grid grid-cols-2 gap-x-5 mt-5">
        <section class="card">
          <div class="text-base text-[rgba(10,8,26,0.88)] font-semibold mb-4">
            每日学习趋势（近14天）
          </div>
          <Mrxqsv />
        </section>
        <section class="card">
          <div class="text-base text-[rgba(10,8,26,0.88)] font-semibold mb-4">
            近7日学习活力热力图
          </div>
          <Hlrlt />
        </section>
      </div>
      <div class="grid grid-cols-2 gap-x-5 mt-5">
        <section class="card">
          <div class="text-base text-[rgba(10,8,26,0.88)] font-semibold mb-4">
            学业风险预警
          </div>
          <Xyfxyj />
        </section>
        <section class="card">
          <div class="text-base text-[rgba(10,8,26,0.88)] font-semibold mb-4">
            作业提交统计
          </div>
          <Kcqjjk />
        </section>
      </div>
    </div>
  </div>
</template>
<script setup>
import { computed, onMounted, ref } from "vue";
import ImgIcon1 from "@/assets/images/pjjd.png";
import ImgIcon2 from "@/assets/images/swhyd.png";
import ImgIcon3 from "@/assets/images/kgyj.png";
import ImgIcon4 from "@/assets/images/zdzh.png";
import { Badge } from "ant-design-vue";
import RltCom from "./components/Rlt.vue";
import Gpzsmq from "./components/Gpzsmq.vue";
import Hydpm from "./components/Hydpm.vue";
import Mrxqsv from "./components/Mrxqsv.vue";
import Bjfb from "./components/Bjfb.vue";
import Hlrlt from "./components/Hlrlt.vue";
import Kcqjjk from "./components/Kcqjjk.vue";
import Xyfxyj from "./components/Xyfxyj.vue";
import { getOverview } from "@/api/studyDetailMonitor";
import { useRoute } from "vue-router";
import { useNumberFormat } from "@/utils";

const route = useRoute();
const { format } = useNumberFormat();
const detail = ref({});

const list = computed(() => [
  {
    img: ImgIcon1,
    title: "平均进度",
    desc: `较上周 ${detail.value?.progress?.weekOverWeek > 0 ? "+" : detail.value?.progress?.weekOverWeek < 0 ? "-" : ""}${detail.value?.progress?.weekOverWeek || 0}%`,
    value: (detail.value?.progress?.currentProgress || 0) + "%",
  },
  {
    img: ImgIcon2,
    title: "思维活跃度",
    desc:
      "AI互动总数" +
      format(
        (detail.value?.activity?.questionCount || 0) +
          (detail.value?.activity?.commentCount || 0),
      ),
    value: detail.value?.activity?.activityIndex || 0,
  },
  {
    img: ImgIcon3,
    title: "卡关预警",
    desc: "滞留节点超过48h",
    value: detail.value?.stuck?.stuckCount || 0,
  },
  {
    img: ImgIcon4,
    title: "进度滞后",
    desc: "落后标准曲线 20%",
    value: detail.value?.lagging?.laggingCount || 0,
  },
]);

onMounted(() => {
  getOverview({
    courseId: route.query.id,
  }).then((res) => {
    console.log(res);
    detail.value = res.data || {};
  });
});
</script>
<style scoped lang="less">
.chart {
  width: 100%;
  height: 100%;
  background: url(@/assets/images/detailWenli.png);
  background-size: 100% 100%;
  .topImg {
    width: 712px;
    height: 815px;
  }
  .bottomImg {
    width: 712px;
    height: 815px;
  }
}

.tab1 {
  background: linear-gradient(180deg, #dcfcff 0%, #ffffff 100%);
  box-shadow: 0px 4px 8px 0px rgba(0, 0, 0, 0.04);
  border-radius: 12px 12px 12px 12px;

  .title {
    font-weight: 500;
    font-size: 14px;
    color: #0a91b3;
  }
}

.tab2 {
  background: linear-gradient(180deg, #e6edff 0%, #ffffff 100%);
  box-shadow: 0px 4px 8px 0px rgba(0, 0, 0, 0.04);
  border-radius: 12px 12px 12px 12px;

  .title {
    font-weight: 500;
    font-size: 14px;
    color: #6466f1;
  }
}

.tab3 {
  background: linear-gradient(180deg, #ffede9 0%, #ffffff 100%);
  box-shadow: 0px 4px 8px 0px rgba(0, 0, 0, 0.04);
  border-radius: 12px 12px 12px 12px;

  .title {
    font-weight: 500;
    font-size: 14px;
    color: #f43f5f;
  }
}

.tab4 {
  background: linear-gradient(180deg, #fff5e2 0%, #ffffff 100%);
  box-shadow: 0px 4px 8px 0px rgba(0, 0, 0, 0.04);
  border-radius: 12px 12px 12px 12px;

  .title {
    font-weight: 500;
    font-size: 14px;
    color: #f59e0c;
  }
}

.card {
  background: #ffffff;
  box-shadow: 0px 4px 8px 0px rgba(0, 0, 0, 0.04);
  border-radius: 20px 20px 20px 20px;
  padding: 20px;
}
.card2 {
  background: linear-gradient(330deg, #ffffff 0%, #e5edff 100%);
  box-shadow: 0px 4px 8px 0px rgba(0, 0, 0, 0.04);
  border-radius: 20px 20px 20px 20px;
  border: 1px solid;
  border-image: linear-gradient(
      358deg,
      rgba(255, 255, 255, 1),
      rgba(255, 255, 255, 0)
    )
    1 1;
  padding: 20px;
}
.card3 {
  background: linear-gradient(330deg, #ffffff 0%, #ffeae5 100%);
  box-shadow: 0px 4px 8px 0px rgba(0, 0, 0, 0.04);
  border-radius: 20px 20px 20px 20px;
  border: 1px solid;
  border-image: linear-gradient(
      358deg,
      rgba(255, 255, 255, 1),
      rgba(255, 255, 255, 0)
    )
    1 1;
  padding: 20px;
}
</style>
