<template>
  <div class="w-full grid grid-cols-3 gap-x-6">
    <div
      class="w-full card flex flex-col items-center justify-center"
      v-for="(item, index) in list"
      :key="index"
    >
      <div class="text-2xl font-medium text-white fontCustom">{{ item.value }}</div>
      <div class="text-sm text-[rgba(255,255,255,0.9)] mt-1">
        {{ item.label }}
      </div>
    </div>
  </div>
</template>
<script setup>
import { onMounted, ref } from "vue";
import { getRealtimeData } from "@/api/university-dataOverview";

const list = ref([
  {
    label: "当前活跃学生",
    value: 0,
  },
  {
    label: "当前活跃学校",
    value: 0,
  },
  {
    label: "当前覆盖课程",
    value: 0,
  },
]);

onMounted(() => {
  getRealtimeData().then((res) => {
    list.value[0].value = res.data?.activeStudents || 0;
    list.value[1].value = res.data?.activeSchools || 0;
    list.value[2].value = res.data?.activeCourses || 0;
  });
});
</script>
<style scoped lang="less">
.card {
  height: 65px;
  background: linear-gradient(
    0deg,
    #0c3672 0%,
    rgba(0, 10, 37, 0) 49.98%,
    rgba(12, 54, 114, 0.9) 100%
  );
  border-radius: 0px 0px 0px 0px;
  border: 1px solid;
  border-image: linear-gradient(
      90deg,
      rgba(0, 102, 255, 0.1),
      rgba(0, 102, 255, 1),
      rgba(0, 102, 255, 0.1)
    )
    1 1;
}
</style>
