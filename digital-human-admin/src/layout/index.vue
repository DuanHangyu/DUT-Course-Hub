<template>
  <div class="w-screen h-screen layout flex">
    <Menu class="shrink-0" />
    <div class="flex flex-col overflow-hidden grow">
      <HeaderLayout v-if="!['dataOverview'].includes($route.name)" />
      <div
        class="flex-grow overflow-hidden"
        :class="
          [
            'university-courseConstructionDetail',
            'courseLibraryDetail',
            'dataOverview',
          ].includes($route.name)
            ? 'p-0'
            : 'p-6'
        "
        :key="userStore?.schoolId"
      >
        <RouterView />
      </div>
    </div>
  </div>
</template>
<script setup>
import { RouterView } from "vue-router";
import HeaderLayout from "./components/HeaderLayout.vue";
import Menu from "./components/Menu.vue";
import useUserStore from "@/store/modules/user";
const userStore = useUserStore();
</script>
<style scoped lang="less">
.layout {
  background: #f7f8fa;
}
</style>
