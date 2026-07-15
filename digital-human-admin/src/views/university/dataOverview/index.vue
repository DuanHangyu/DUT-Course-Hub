<template>
  <div class="size-full con flex flex-col overflow-hidden">
    <div class="topBar flex items-center justify-center shrink-0 relative">
      <img src="@/assets/screen/sjgl.png" class="h-6" alt="" />
      <div class="absolute right-6 top-1/2 -translate-y-1/2">
        <Avatar color="rgba(255,255,255,0.88)" />
      </div>
    </div>
    <div class="grow overflow-hidden flex p-6 space-x-[50px]" :key="refreKey">
      <section
        class="w-[26%] shrink-0 space-y-5 scale-y-100 h-full overflow-hidden"
      >
        <Left1 class="h-[33%]" />
        <Left2 class="h-[33%]" />
        <Left3 class="h-[30%]" />
      </section>
      <section class="grow space-y-[30px] flex flex-col">
        <Center1 class="h-[60%]" />
        <Center2 class="shrink-0" />
        <Center3 class="h-[33%] shrink-0" />
      </section>
      <section class="w-[26%] shrink-0 space-y-[30px]">
        <right1 class="h-[33%]" />
        <right2 class="h-[30%]" />
        <right3 class="h-[30%]" />
      </section>
    </div>
  </div>
</template>
<script setup>
import Left1 from "./components/Left1.vue";
import Left2 from "./components/Left2.vue";
import Left3 from "./components/Left3.vue";
import right1 from "./components/right1.vue";
import right2 from "./components/right2.vue";
import right3 from "./components/right3.vue";
import Center1 from "./components/Center1.vue";
import Center2 from "./components/Center2.vue";
import Center3 from "./components/Center3.vue";
import Avatar from "@/layout/components/Avatar.vue";
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
