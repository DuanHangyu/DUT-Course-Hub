<template>
  <div class="w-full overflow-hidden flex flex-col">
    <section class="flex items-center space-x-2 px-4 h-10 card-title shrink-0">
      <img src="@/assets/screen/arrow.png" class="w-[12px] h-[14px]" alt="" />
      <slot name="title"></slot>
    </section>
    <div class="w-full grow">
      <slot name="content"></slot>
    </div>
  </div>
</template>
<script setup></script>
<style scoped lang="less">
.card-title {
  width: 100%;
  background: linear-gradient(0deg, #1d418d 0%, rgba(29, 65, 141, 0.3) 100%);
  border: 1px solid #4163aa;
}
</style>
