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
    <img src="@/assets/course/startNode.png" class="size-7" alt="" />
    <span>开始</span>
    <Handle
      id="start"
      type="source"
      :position="Position.Right"
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
  background: #0461c1 !important;
  box-shadow: 0px 0px 24px 0px #51a5ff !important;
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
