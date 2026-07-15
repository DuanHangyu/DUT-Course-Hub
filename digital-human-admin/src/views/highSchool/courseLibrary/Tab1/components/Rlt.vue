<template>
  <div class="card22 w-full flex flex-col overflow-hidden">
    <div class="grow overflow-hidden p-5">
      <div class="size-full" ref="container">
        <VueFlow
          v-model:nodes="nodes"
          :edges="edges"
          :style="{ backgroundColor: 'initial' }"
          fit-view-on-init
          ref="vueFlowRef"
        >
          <template #node-start-selector="props">
            <div class="startCard">
              <img src="@/assets/images/startNode.png" class="size-7" alt="" />
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
          <template #node-node="props">
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
              class="node relative mb-6"
              :class="
                props.data?.completionRate > 60 ? 'node-success' : 'node-red'
              "
              @click="openDetail(props.data)"
            >
              {{ props.data?.completionRate }}%
              <div class="text truncate absolute -bottom-[24px]">
                {{ props.data?.nodeName }}
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
          <template #node-end-selector="props">
            <div class="endCard">
              <img src="@/assets/images/endNode.png" class="size-7" alt="" />
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
        </VueFlow>
      </div>
    </div>
    <div class="bottom2 text-center flex flex-col justify-center shrink-0">
      <div class="flex justify-center space-x-4">
        <div class="btn" @click="suoxiao">
          <img src="@/assets/images/suoxiao.png" alt="" />
        </div>
        <div class="btn" @click="fangda">
          <img src="@/assets/images/add.png" alt="" />
        </div>
      </div>
      <span class="t pt-3"> 长按鼠标拖动画布；操作鼠标滚动缩放 </span>
    </div>
    <NodeDetail ref="nodeDetailRef" />
  </div>
</template>
<script setup>
import { Handle, Position } from "@vue-flow/core";
import { VueFlow } from "@vue-flow/core";
import { onMounted, ref, nextTick } from "vue";
import { getKnowledgeGraph } from "@/api/studyDetailMonitor";
import { useRoute } from "vue-router";
import NodeDetail from "./NodeDetail.vue";
const vueFlowRef = ref();
const nodeDetailRef = ref();
const container = ref();
const nodes = ref([]);
const edges = ref([]);
const route = useRoute();
const suoxiao = () => {
  vueFlowRef.value?.zoomOut(); // 缩小
};

const fangda = () => {
  vueFlowRef.value?.zoomIn(); // 放大
};

onMounted(() => {
  getKnowledgeGraph({
    courseId: route.query?.id,
  }).then((res) => {
    vueFlowRef.value?.removeEdges(edges.value);
    vueFlowRef.value?.removeNodes(nodes.value);
    nextTick(async () => {
      nodes.value =
        res?.data?.nodes?.map((item) => {
          return {
            id: item?.nodeId,
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
            draggable: item.nodeColour != 1 && item.nodeColour != 2,
          };
        }) || [];
      edges.value = [];
      const lines = [];

      for (let index = 0; index < res?.data?.nodes?.length; index++) {
        const element = res?.data?.nodes?.[index];
        if (element?.relateNode?.length) {
          lines?.push(
            ...element?.relateNode?.map((item) => {
              return [element?.nodeId, item];
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
                  stroke: "#b4ccd4",
                  strokeDasharray: "5,5",
                  strokeWidth: 3,
                },
              });
            }
          }
        }
      }
    });
  });
});

const openDetail = (e) => {
  nodeDetailRef.value?.openModal(e);
};
</script>
<style scoped lang="less">
.card22 {
  height: 460px;
  background: linear-gradient(
    180deg,
    #f0fbff 0%,
    rgba(240, 251, 255, 0.5) 100%
  );
  border-radius: 12px 12px 12px 12px;
}
.startCard {
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
.endCard {
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
.node {
  width: 64px !important;
  height: 64px !important;
  background: #ffffff;
  border-radius: 100%;
  font-weight: 600;
  font-size: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  .text {
    font-weight: 400;
    font-size: 14px;
    color: rgba(10, 8, 26, 0.88);
  }
}

.node-success {
  box-shadow: 0px 4px 8px 0px rgba(12, 58, 108, 0.12);
  border: 2px solid #1977ff;
  color: #1977ff;
}

.node-red {
  box-shadow: 0px 4px 8px 0px rgba(108, 23, 12, 0.12);
  border: 2px solid #ff4d4f;
  color: #ff4d4f;
}

.bottom2 {
  border-top: 1px solid rgba(0, 0, 0, 0.15);
  height: 116px;
  width: calc(100% - 60px);
  margin: 0 auto;
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
  .t {
    font-weight: 400;
    font-size: 14px;
    color: rgba(10, 8, 26, 0.25);
  }
}
</style>
