<script setup>
import { Handle, Position } from "@vue-flow/core";
import { useRoute, useRouter } from "vue-router";
import { CircleCheckFilled } from "@element-plus/icons-vue";

const emit = defineEmits(["deleteNode", "updateNode"]);
const props = defineProps({
  id: {
    type: String,
    required: true,
  },
  data: {
    type: Object,
    default: () => ({}),
  },
});

const route = useRoute();
const router = useRouter();

const goDetail = () => {
  router.push(
    `/node?courseId=${route.query?.courseId}&nodeId=${props.data?.id}`,
  );
};
</script>

<template>
  <Handle
    id="left"
    type="target"
    :position="Position.Left"
    :connectable="false"
    :style="{
      height: 0,
      width: 0,
      opacity: 0,
    }"
  />
  <div
    class="relative card"
    :style="{
      width: data?.nodeSize + 'px',
      height: data?.nodeSize + 'px',
      background:
        data?.nodeColour == 3
          ? '#7DBFFB'
          : data?.nodeColour == 4
            ? '#4FD8C8'
            : '#FB837D',
    }"
    @click="goDetail"
  >
    <img src="@/assets/course/node.png" class="size-[50%]" alt="" />
    <div class="text truncate absolute -bottom-9" :title="data?.nodeName">
      {{ data?.nodeName }}
    </div>
    <img
      src="@/assets/course/study-right.png"
      class="w-[50%] h-[50%] absolute! top-1/2 left-1/2 -translate-1/2"
      v-if="data?.state != 0"
      alt=""
    />
  </div>
  <Handle
    id="right"
    type="source"
    :position="Position.Right"
    :connectable="false"
    :style="{
      height: 0,
      width: 0,
      opacity: 0,
    }"
  />
</template>
<style scoped lang="scss">
.card {
  border-radius: 100%;
  border: 2px solid #ffffff;
  font-weight: 600;
  font-size: 14px;
  color: #ffffff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  .text {
    width: 114px;
    line-height: 28px;
    padding: 0 6px;
    background: #ffffff;
    border-radius: 100px 100px 100px 100px;
    font-weight: 600;
    font-size: 14px;
    color: #0c3a6c;
    text-align: center;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}
.card-no {
  border-radius: 100%;
  border: 2px solid #2f4168;
  font-weight: 600;
  font-size: 14px;
  color: #9ac1ed;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  .text {
    width: 114px;
    line-height: 28px;
    padding: 0 6px;
    background: #2f4168;
    border-radius: 100px 100px 100px 100px;
    font-weight: 600;
    font-size: 14px;
    color: #9ac1ed;
    text-align: center;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}
</style>
