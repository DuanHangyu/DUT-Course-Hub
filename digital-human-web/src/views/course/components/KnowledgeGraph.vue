<template>
  <div class="max-w-275 h-full mx-auto">
    <div class="h-125 px-5 py-3">
      <div class="size-full" ref="container">
        <VueFlow
          v-model:nodes="nodes"
          :edges="edges"
          :style="{ backgroundColor: '#111827' }"
          fit-view-on-init
          ref="vueFlowRef"
        >
          <template #node-start-selector="props">
            <StartNode :id="props.id" :data="props.data" />
          </template>
          <template #node-node="props">
            <OutputNode :id="props?.id" :data="props.data" />
          </template>
          <template #node-end-selector="props">
            <EndNode :id="props.id" :data="props.data" />
          </template>
        </VueFlow>
      </div>
    </div>
    <div class="bottom2 text-center flex flex-col justify-center">
      <div class="flex justify-center space-x-4">
        <div class="btn" @click="suoxiao">
          <img src="@/assets/course/suoxiao.png" alt="" />
        </div>
        <div class="btn" @click="fangda">
          <img src="@/assets/course/add.png" alt="" />
        </div>
      </div>
      <span class="text-sm text-[rgba(255,255,255,0.5)] pt-3">
        长按鼠标拖动画布；操作鼠标滚动缩放
      </span>
    </div>
  </div>
</template>
<script setup>
import { VueFlow } from "@vue-flow/core";
import StartNode from "./StartNode.vue";
import OutputNode from "./OutputNode.vue";
import EndNode from "./EndNode.vue";
import { nextTick, onMounted, ref } from "vue";
import { useRoute } from "vue-router";

const props = defineProps({
  detail: {
    type: Object,
    required: true,
  },
});

const route = useRoute();
const vueFlowRef = ref();
const container = ref();
const nodes = ref();
const edges = ref([]);
const suoxiao = () => {
  vueFlowRef.value?.zoomOut();
};

const fangda = () => {
  vueFlowRef.value?.zoomIn();
};

const initBall = async () => {
  vueFlowRef.value?.removeEdges(edges.value);
  vueFlowRef.value?.removeNodes(nodes.value);
  nextTick(async () => {
    nodes.value =
      props.detail?.nodes?.map((item) => {
        return {
          id: item?.id,
          data: item,
          type:
            item.nodeColour == 1
              ? "start-selector"
              : item.nodeColour == 2
                ? "end-selector"
                : "node",
          position:
            item.nodeColour == 1
              ? { x: 0, y: container.value.clientHeight / 2 }
              : item.nodeColour == 2
                ? {
                    x: container.value.clientWidth,
                    y: container.value.clientHeight / 2,
                  }
                : {
                    x: Number(item.xaxis) * container.value?.clientWidth,
                    y: Number(item.yaxis) * container.value?.clientHeight,
                  },
          draggable: false,
        };
      }) || [];
    edges.value = [];
    const lines = [];

    for (let index = 0; index < props.detail?.nodes?.length; index++) {
      const element = props.detail?.nodes?.[index];
      if (element?.relateNode?.length) {
        lines?.push(
          ...element?.relateNode?.map((item) => {
            return [element?.id, item];
          }),
        );
      }
    }
    for (let index = 0; index < lines.length; index++) {
      const element = lines[index];
      if (element?.[0] != element?.[1]) {
        const exists = edges.value.some(
          (conn) =>
            (conn.source == element?.[0] && conn.target == element?.[1]) ||
            (conn.source == element?.[1] && conn.target == element?.[0]),
        );
        if (!exists) {
          const node1 = nodes.value.find((item) => item.id == element?.[0]);
          const node2 = nodes.value.find((item) => item.id == element?.[1]);
          if (node1?.id && node2?.id) {
            let sourceId, targetId;
            if (node1?.data?.nodeColour == 1) {
              sourceId = node1.id;
              targetId = node2.id;
            } else if (node2?.data?.nodeColour == 1) {
              sourceId = node2.id;
              targetId = node1.id;
            } else if (node1?.data?.nodeColour == 2) {
              targetId = node1.id;
              sourceId = node2.id;
            } else if (node2?.data?.nodeColour == 2) {
              targetId = node2.id;
              sourceId = node1.id;
            } else if (node1?.data?.xaxis < node2?.data?.xaxis) {
              sourceId = node1.id;
              targetId = node2.id;
            } else {
              sourceId = node2.id;
              targetId = node1.id;
            }
            edges.value.push({
              id: edges.value?.length,
              source: sourceId + "",
              target: targetId + "",
              // animated: true,
              style: {
                stroke: "#2c75c8",
                strokeDasharray: "5,5",
                strokeWidth: 3,
              },
            });
          }
        }
      }
    }
  });
};

onMounted(() => {
  initBall();
});
</script>
<style scoped lang="scss">
.bottom2 {
  border-top: 2px solid rgba(255, 255, 255, 0.5);
  height: 116px;
}
.btn {
  width: 44px;
  height: 44px;
  background: #ffffff;
  box-shadow: 0px 4px 24px 8px rgba(0, 0, 0, 0.04);
  border-radius: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  img {
    width: 20px;
    height: 20px;
  }
}
</style>
