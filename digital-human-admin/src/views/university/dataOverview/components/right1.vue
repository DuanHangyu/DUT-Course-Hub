<template>
  <Card>
    <template #title>
      <img src="@/assets/screen/hxsjtj.png" class="w-[100px]" alt="" />
    </template>
    <template #content>
      <div class="flex items-center justify-center h-full">
        <div class="grid grid-cols-2 gap-x-[60px] gap-y-4 mt-5 px-6">
          <div
            v-for="(item, index) in list"
            :key="index"
            class="relative w-[100px] h-[100px] text-center"
          >
            <img
              src="@/assets/screen/dz.png"
              class="absolute bottom-0 left-0 w-full h-full"
              alt=""
            />
            <div class="text-[28px] font-medium text-white fontCustom">
              {{ item.value }}
            </div>
            <div class="text-white text-sm mt-2">{{ item.title }}</div>
          </div>
        </div>
      </div>
    </template>
  </Card>
</template>
<script setup>
import { onMounted, ref } from "vue";
import Card from "./Card.vue";
import { getCoreStatistics } from "@/api/university-dataOverview";

const list = ref([
  {
    title: "覆盖学校数",
    value: 0,
  },
  {
    title: "累计服务学生数",
    value: 0,
  },
  {
    title: "累计精品课程数",
    value: 0,
  },
  {
    title: "AI算力消耗总量",
    value: 0,
  },
]);
const data = ref({});
onMounted(() => {
  getCoreStatistics().then((res) => {
    data.value = res.data || {};
    list.value[0].value = res.data?.schoolCount || 0;
    list.value[1].value = res.data?.studentCount || 0;
    list.value[2].value = res.data?.courseCount || 0;
    list.value[3].value = res.data?.aiPowerCount || 0;
  });
});
</script>
