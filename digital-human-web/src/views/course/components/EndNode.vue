<script setup>
import { Handle, Position } from "@vue-flow/core";

const props = defineProps({
  id: {
    type: String,
    required: true,
  },
  data: {
    type: Object,
    required: true,
  },
});
</script>

<template>
  <div class="card">
    <img src="@/assets/course/endNode.png" class="size-7" alt="" />
    <span>结束</span>
    <Handle
      id="end"
      type="target"
      :position="Position.Left"
      :connectable="false"
      :style="{
        height: 0,
        width: 0,

        opacity: 0,
      }"
    />
  </div>
</template>
<style scoped lang="scss">
.card {
  width: 72px !important;
  height: 72px !important;
  background: #4fd882 !important;
  box-shadow: 0px 0px 24px 0px #4fd882 !important;
  border-radius: 100% !important;
  border: 2px solid #ffffff;
  font-weight: 600;
  font-size: 14px;
  color: #ffffff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
</style>
