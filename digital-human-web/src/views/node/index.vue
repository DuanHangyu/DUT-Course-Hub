<template>
  <div
    class="w-full overflow-x-hidden overflow-y-auto dot-background relative pt-40"
  >
    <Topbar class="z-100" />
    <img
      src="@/assets/node/wl-1.png"
      class="absolute top-0 left-0 w-219.75 h-219.75"
      alt=""
    />
    <img
      src="@/assets/node/wl-3.png"
      class="w-49 absolute left-0 top-106"
      alt=""
    />
    <img
      src="@/assets/node/wl-2.png"
      class="w-157.75 h-158.75 absolute right-0 top-120 z-1"
      alt=""
    />
    <img
      src="@/assets/node/wl-4.png"
      class="w-195.25 h-92.5 absolute bottom-0 left-0"
      alt=""
    />
    <div
      class="w-56 h-56 bg-[rgba(0,7,199,0.08)] rounded-full absolute -right-20 top-160"
    ></div>
    <main class="content-container mx-auto">
      <div
        class="section1 relative overflow-hidden px-20 py-15 grid grid-cols-5 gap-x-12 z-10"
      >
        <div class="topRight absolute -top-40 -right-38"></div>
        <div class="col-span-3">
          <div
            class="px-4 py-2 bg-[#0C3A6C] rounded-[100px] text-base font-medium text-white w-fit"
          >
            课程节点学习
          </div>
          <div class="text-[70px] font-semibold text-[#0C3A6C] mt-3 truncate">
            {{ nodeDetail?.nodeName }}
          </div>
          <div class="text-2xl text-[#384A5F] line-clamp-6 h-40">
            {{ nodeDetail?.nodeIntroduce }}
          </div>
          <div class="flex space-x-4 mt-5">
            <div class="btn text-[#0C3A6C] bg-white">
              所属课程：{{ nodeDetail?.courseName }}
            </div>
            <div class="btn text-[#0C3A6C] bg-white">
              课程分类：{{ nodeDetail?.subject }}
            </div>
            <div class="btn bg-[#0C3A6C] text-white">
              学习时长：{{ nodeDetail?.studyTime }} 分
            </div>
          </div>
        </div>
        <div
          class="progress col-span-2 shrink-0 relative mt-6 px-8 py-6 flex flex-col items-center justify-center"
        >
          <div class="tag absolute -top-6 -right-13 rotate-15">
            OVERALL PROGRESS
          </div>
          <div class="flex items-center justify-between w-full">
            <span class="text-2xl font-semibold text-[#99B5EA]">总课程进度</span>
            <span class="text-[44px] font-semibold text-[#0461C1]">
              {{ nodeDetail?.studyProgress }}%
            </span>
          </div>
          <div class="out-progress mx-auto mt-3 overflow-hidden">
            <div
              class="in-progress"
              :style="{ width: nodeDetail?.studyProgress + '%' }"
            ></div>
          </div>
          <div
            class="w-full mt-2 text-base text-[#64748B] font-semibold flex mask-type-luminance justify-between"
          >
            <span>0%</span>
            <span>100%</span>
          </div>
        </div>
      </div>
      <div class="flex items-center space-x-4 title mt-16">
        <div class="icon">
          <img src="@/assets/node/kcxx.png" alt="" />
        </div>
        <span> 课程学习 </span>
      </div>
      <div class="section2 mt-4 grid grid-cols-3 gap-x-4 relative z-20">
        <section class="video col-span-2 overflow-hidden">
          <Player :detail="nodeDetail" v-if="nodeDetail?.id" />
        </section>
        <section class="chart">
          <chart :detail="nodeDetail" v-if="nodeDetail?.id" />
        </section>
      </div>
      <div class="flex items-center space-x-4 title mt-16">
        <div class="icon">
          <img src="@/assets/node/xxzl.png" alt="" />
        </div>
        <span> 学习资源 </span>
      </div>
      <div class="section3 mt-4 w-full mb-20 relative">
        <div class="tabs flex">
          <div
            class="tab space-x-2"
            :class="current == index ? 'activeTab' : ''"
            @click="changeTab(index)"
            v-for="(item, index) in tabs"
            :key="index"
          >
            <xxhdIcon v-if="index == 0" />
            <kczlIcon v-if="index == 1" />
            <tjzyIcon v-if="index == 2" />
            <span>{{ item.label }}</span>
          </div>
        </div>
        <div class="h-204 w-full overflow-hidden">
          <xxhd v-if="current == 0" :detail="nodeDetail" />
          <kczl v-if="current == 1" :detail="nodeDetail" />
          <tjzy v-if="current == 2" :detail="nodeDetail" />
        </div>
      </div>
    </main>
  </div>
</template>
<script setup>
import Topbar from "@/components/Topbar.vue";
import { onMounted, ref } from "vue";
import xxhdIcon from "./icons/xxhd.vue";
import kczlIcon from "./icons/kczl.vue";
import tjzyIcon from "./icons/tjzy.vue";
import xxhd from "./components/xxhd.vue";
import kczl from "./components/kczl.vue";
import tjzy from "./components/tjzy.vue";
import chart from "./components/chart.vue";
import Player from "./components/Player.vue";
import { useRoute } from "vue-router";
import { getNodeDetail } from "../../api/node";

const tabs = ref([
  {
    label: "学习互动",
  },
  {
    label: "课程资料",
  },
  {
    label: "提交作业",
  },
]);

const current = ref(0);
const route = useRoute();
const nodeDetail = ref({});

const changeTab = (e) => {
  current.value = e;
};

onMounted(() => {
  getNodeDetail({
    nodeId: route.query?.nodeId,
  }).then((res) => {
    nodeDetail.value = res || {};
  });
});
</script>
<style scoped lang="scss">
.dot-background {
  background-color: #f0f5ff;
  background-image:
    radial-gradient(circle, #dfe4ed 2px, transparent 2px),
    radial-gradient(circle, #dfe4ed 2px, transparent 2px);
  background-size: 20px 20px;
  background-position: 0 0;
}
.section1 {
  width: 100%;
  height: 548px;
  background: linear-gradient(76deg, #e6f0fe 0%, #feffff 100%);
  border-radius: 30px 30px 30px 30px;
  box-shadow: 6px 6px 0px 0px #0c3a6c;
  border: 4px solid #0c3a6c;
  .topRight {
    width: 400px;
    height: 389px;
    background: #e1eafa;
    border-radius: 100%;
  }
  .btn {
    line-height: 60px;
    padding: 0px 16px;
    box-shadow: 3px 3px 0px 0px #002d73;
    border-radius: 20px 20px 20px 20px;
    border: 2px solid #0c3a6c;
    font-weight: 500;
    font-size: 16px;
  }
}
.progress {
  height: 224px;
  background: #ffffff;
  border-radius: 30px 30px 30px 30px;
  border: 3px dashed #9eb8eb;
  .tag {
    line-height: 40px;
    padding: 0px 10px;
    background: #00a3cc;
    border-radius: 12px 12px 12px 12px;
    border: 2px solid #0c3a6c;
    font-weight: 500;
    font-size: 16px;
    color: #ffffff;
  }
}
.out-progress {
  width: 100%;
  height: 25px;
  background: #f1f5f9;
  border-radius: 100px;
  border: 2px solid #0c3a6c;
  position: relative;
  .in-progress {
    position: absolute;
    top: 0;
    left: 0;
    height: 100%;
    background: linear-gradient(90deg, #0051c9 0%, #013f9d 100%);
    border-radius: 100px 0px 0px 100px;
    border: 2px solid #0c3a6c;
  }
}

.title {
  font-weight: 600;
  font-size: 44px;
  color: #0c3a6c;
  .icon {
    width: 48px;
    height: 48px;
    background: #0052cc;
    box-shadow: 3px 3px 0px 0px #0c3a6c;
    border-radius: 12px 12px 12px 12px;
    border: 2px solid #0c3a6c;
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      width: 32px;
      height: 32px;
    }
  }
}

.section2 {
  .video {
    height: 808px;
    background: #000000;
    box-shadow: 6px 6px 0px 0px #0c3a6c;
    border-radius: 30px 30px 30px 30px;
    border: 4px solid #0c3a6c;
  }
  .chart {
    height: 806px;
    background: #ffffff;
    box-shadow: 6px 6px 0px 0px #0c3a6c;
    border-radius: 30px 30px 30px 30px;
    border: 4px solid #0c3a6c;
  }
}
.section3 {
  background: #ffffff;
  box-shadow: 6px 6px 0px 0px #0c3a6c;
  border-radius: 30px 30px 30px 30px;
  border: 4px solid #0c3a6c;
  overflow: hidden;
  .tabs {
    width: 100%;
    height: 64px;
    background: #f3f6fc;
    border-bottom: 4px solid #0c3a6c;
    box-sizing: border-box;
  }
  .tab {
    font-weight: 500;
    font-size: 18px;
    color: #0c3a6c;
    cursor: pointer;
    width: 230px;
    height: 64px;
    background: #f3f6fc;
    border-right: 4px solid #0c3a6c;
    border-bottom: 4px solid #0c3a6c;
    display: flex;
    align-items: center;
    justify-content: center;
    &:hover {
      background: white;
    }
  }
  .activeTab {
    color: white;
    background: linear-gradient(90deg, #0050c7 0%, #0241a2 100%);
    position: relative;
    box-shadow: inset 0px -5px 0px 0px #4effff;
    &:hover {
      background: linear-gradient(90deg, #0050c7 0%, #0241a2 100%);
    }
  }
}
</style>
