<script setup>
import { Handle, Position } from "@vue-flow/core";
import { Dropdown, Menu, MenuItem } from "ant-design-vue";

const emit = defineEmits(["deleteNode", "updateNode"]);
defineProps({
  id: {
    type: String,
    required: true,
  },
  data: {
    type: Object,
    default: () => ({}),
  },
});

const clickMenuItem = (e) => {
  if (e?.key == 1) {
    emit("updateNode");
  }
  if (e?.key == 2) {
    emit("deleteNode");
  }
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
    class="card relative"
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
  >
    <Dropdown :trigger="['contextmenu']">
      <img src="@/assets/images/node.png" class="size-[50%]" alt="" />
      <template #overlay>
        <Menu @click="clickMenuItem">
          <MenuItem :key="1">修改</MenuItem>
          <MenuItem :key="2">删除</MenuItem>
        </Menu>
      </template>
    </Dropdown>

    <div class="text truncate absolute -bottom-[36px]" :title="data?.nodeName">
      {{ data?.nodeName }}
    </div>
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
<style scoped lang="less">
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
}
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
</style>
