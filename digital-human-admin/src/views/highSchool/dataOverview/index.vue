<template>
  <div class="size-full con flex flex-col overflow-hidden" id="screenApp">
    <div class="topBar flex items-center justify-center shrink-0 relative">
      <img src="@/assets/screen/sjgl.png" class="h-6" alt="" />
      <div class="absolute right-6 top-1/2 -translate-y-1/2">
        <Avatar color="rgba(255,255,255,0.88)" />
      </div>
    </div>
    <div
      class="grow overflow-hidden p-6 space-y-5 flex flex-col"
      :key="refreKey"
    >
      <div class="grid grid-cols-4 gap-x-5 h-[20%]">
        <TCard v-for="item in [0, 1, 2, 3]" :key="item" :card-index="item" />
      </div>
      <div class="grid grid-cols-3 gap-x-5 h-[39%]">
        <Section1 class="col-span-2" />
        <Section2 />
      </div>
      <div class="flex space-x-5 grow">
        <Section3 />
        <Section4 />
        <Section5 />
      </div>
    </div>
  </div>
</template>
<script setup>
import Avatar from "@/layout/components/Avatar.vue";
import TCard from "./components/TCard.vue";
import Section1 from "./components/Section1.vue";
import Section2 from "./components/Section2.vue";
import Section3 from "./components/Section3.vue";
import Section4 from "./components/Section4.vue";
import Section5 from "./components/Section5.vue";
import { mittOn, mittOff } from "@/utils/appMitt";
import { onMounted, onUnmounted, ref } from "vue";

const refreKey = ref(Date.now());

const setRefresh = () => {
  refreKey.value = Date.now();
};

onMounted(() => {
  mittOn("refresh", setRefresh);
});

onUnmounted(() => {
  mittOff("refresh", setRefresh);
});
</script>
<style scoped lang="less">
.con {
  background: url(@/assets/screen/bg.png);
  background-size: 100% 100%;
  background-repeat: no-repeat;
}
.topBar {
  height: 50px;
  background: url(@/assets/screen/top.png);
  background-size: 100% 100%;
  background-repeat: no-repeat;
}
</style>
