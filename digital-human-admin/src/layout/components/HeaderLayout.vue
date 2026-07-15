<template>
  <div class="flex items-center px-6 justify-between h-[52px] box shrink-0">
    <div
      class="w-[200px] flex-shrink-0 text-base text-[#0A081A] font-medium space-x-3 flex items-center"
    >
      <svg-icon
        v-if="$route?.meta?.icon"
        :icon-class="$route?.meta?.icon"
        class="text-[#0A081A] text-[20px]"
      />
      <span>{{ $route.meta.title }}</span>
    </div>
    <Avatar />
  </div>
</template>
<script setup>
import Avatar from "./Avatar.vue";
</script>
<style scoped lang="less">
.box {
  background: #ffffff;
  border-radius: 0px 0px 0px 0px;
  border-bottom: 1px solid rgba(29, 29, 29, 0.12);
}
:focus-visible {
  outline: initial;
}
</style>
