<template>
  <Card>
    <template #title>
      <img src="@/assets/screen/top3.png" class="w-[126px]" alt="" />
    </template>
    <template #content>
      <div class="h-full flex flex-col items-center justify-center">
        <section class="flex justify-center space-y-px-[0.5vh]">
          <div class="card w-[9.8vw] h-[12vh] flex flex-col items-center">
            <img
              src="@/assets/screen/p1.png"
              class="w-[3.8vw] h-[3.2vh]"
              alt=""
            />
            <span class="text-base font-medium text-white pt-1 text-center">
              {{ data?.[0]?.courseName }}
            </span>
            <span class="text-xl font-medium text-white">
              <CountTo :startVal="0" :endVal="data?.[0]?.studentCount || 0" />
            </span>
          </div>
        </section>
        <section class="flex justify-center space-x-[4vw] mt-3">
          <div
            class="card w-[7.8vw] h-[10vh] flex flex-col items-center space-y-[0.5vh]"
          >
            <img
              src="@/assets/screen/p2.png"
              class="w-[3vw] h-[2.5vh]"
              alt=""
            />
            <span class="text-sm font-medium text-white pt-1 text-center">
              {{ data?.[1]?.courseName }}
            </span>
            <span class="text-base font-medium text-white">
              <CountTo :startVal="0" :endVal="data?.[1]?.studentCount || 0" />
            </span>
          </div>
          <div
            class="card w-[7.8vw] h-[10vh] flex flex-col items-center space-y-[0.5vh]"
          >
            <img
              src="@/assets/screen/p3.png"
              class="w-[3vw] h-[2.5vh]"
              alt=""
            />
            <span class="text-sm font-medium text-white pt-1 text-center">
              {{ data?.[2]?.courseName }}
            </span>
            <span class="text-base font-medium text-white">
              <CountTo :startVal="0" :endVal="data?.[2]?.studentCount || 0" />
            </span>
          </div>
        </section>
      </div>
    </template>
  </Card>
</template>
<script setup>
import { onMounted, ref } from "vue";
import Card from "./Card.vue";
import { getPopularCourses } from "@/api/highSchool-dataOverview";
import useUserStore from "@/store/modules/user";
import CountTo from "@/components/CountTo/index.vue";

const userStore = useUserStore();

const data = ref([]);

onMounted(() => {
  getPopularCourses({
    schoolId: userStore.schoolId,
  }).then((res) => {
    data.value = res?.data || [];
  });
});
</script>
<style scoped lang="less">
.card {
  background: url(@/assets/screen/dz.png);
  background-size: 100% 100%;
  background-repeat: no-repeat;
}
</style>
