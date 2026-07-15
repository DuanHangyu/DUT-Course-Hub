<template>
  <div class="app-container flex flex-col overflow-hidden !p-0">
    <section class="shrink-0 px-6 py-4 bg-white">
      <Button
        type="link"
        class="flex items-center space-x-1 text-sm text-[rgba(10,8,26,0.88)] w-fit !px-0"
        @click="$router.back()"
      >
        <LeftOutlined />
        <span>返回</span>
      </Button>
      <div class="mt-2 card flex items-center space-x-[10px]">
        <img src="@/assets/images/kcxq.png" class="size-8" alt="" />
        <span class="text-lg font-medium text-[rgba(10,8,26,0.88)]">
          当前课程：{{ detail?.courseName }}
        </span>
      </div>
    </section>
    <section class="grow p-6 bg-[#F7F8FA]">
      <div
        class="rounded-xl bg-white p-4 flex flex-col overflow-hidden size-full"
      >
        <Button type="primary" class="shrink-0 w-fit" @click="add">
          新增节点
        </Button>
        <div class="mt-4 card2 grow flex flex-col overflow-hidden px-10">
          <div class="grow overflow-hidden p-5">
            <div class="size-full" ref="container">
              <VueFlow
                :key="flowKey"
                v-model:nodes="nodes"
                :edges="edges"
                :style="{ backgroundColor: '#111827' }"
                fit-view-on-init
                ref="vueFlowRef"
                @nodes-change="onNodesChange"
              >
                <template #node-start-selector="props">
                  <StartNode :id="props.id" :data="props.data" />
                </template>
                <template #node-node="props">
                  <OutputNode
                    :id="props?.id"
                    :data="props.data"
                    @delete-node="delFn(props.data)"
                    @update-node="updateFn(props.data)"
                  />
                </template>
                <template #node-end-selector="props">
                  <EndNode :id="props.id" :data="props.data" />
                </template>
              </VueFlow>
            </div>
          </div>
          <div
            class="bottom2 text-center flex flex-col justify-center shrink-0"
          >
            <div class="flex justify-center space-x-4">
              <div class="btn" @click="suoxiao">
                <img src="@/assets/images/suoxiao.png" alt="" />
              </div>
              <div class="btn" @click="fangda">
                <img src="@/assets/images/add.png" alt="" />
              </div>
            </div>
            <span class="text-sm text-[rgba(255,255,255,0.5)] pt-3">
              长按鼠标拖动画布；操作鼠标滚动缩放
            </span>
          </div>
        </div>
      </div>
    </section>
    <AddNode ref="addRef" @success="initBall" />
  </div>
</template>
<script setup>
import { LeftOutlined } from "@ant-design/icons-vue";
import { Button, message, Modal } from "ant-design-vue";
import { VueFlow } from "@vue-flow/core";
import StartNode from "./components/StartNode.vue";
import OutputNode from "./components/OutputNode.vue";
import EndNode from "./components/EndNode.vue";
import { nextTick, onMounted, ref } from "vue";
import AddNode from "./components/AddNode.vue";
import * as api from "@/api/courseConstruction";
import { useRoute } from "vue-router";

const route = useRoute();
const detail = ref({});
const vueFlowRef = ref();
const addRef = ref();
const container = ref();
const nodes = ref();
const edges = ref([]);
const flowKey = ref(0);
const calcNum = (num) => {
  return Math.round((num + Number.EPSILON) * 100) / 100;
};
const onNodesChange = (changes) => {
  changes.forEach((change) => {
    if (change.type == "position") {
      if (change.dragging === false) {
        const node = nodes.value.find((n) => n.id === change.id);
        if (node) {
          api
            .moveNode({
              courseNodeId: change.id,
              xAxis: calcNum(node.position.x / container.value?.clientWidth),
              yAxis: calcNum(node.position.y / container.value?.clientHeight),
            })
            .then(() => {
              initBall();
            });
        }
      }
    }
  });
};

const suoxiao = () => {
  vueFlowRef.value?.zoomOut();
};

const fangda = () => {
  vueFlowRef.value?.zoomIn();
};

const add = () => {
  addRef.value?.openModal({ courseId: route.query?.id });
};

const initBall = async () => {
  nextTick(async () => {
    let res = await api.getNodeList({
      courseId: route.query?.id,
    });
    const isStartBall = res?.data?.find((item) => item?.nodeColour == 1);
    const isEndBall = res?.data?.find((item) => item?.nodeColour == 2);
    if (!isStartBall || !isEndBall) {
      if (!isStartBall) {
        await api.createNode({
          courseId: route.query?.id,
          nodeName: "开始",
          nodeColour: "1",
          nodeSize: 72,
        });
      }
      if (!isEndBall) {
        await api.createNode({
          courseId: route.query?.id,
          nodeName: "结束",
          nodeColour: "2",
          nodeSize: 72,
        });
      }
      res = await api.getNodeList({
        courseId: route.query?.id,
      });
    }
    // 数据就绪后直接响应式重赋值（v-model:nodes 会按 id 自动 diff 旧/新节点）。
    // 不要调用命令式 removeNodes/removeEdges —— 它与 v-model 存在竞态：
    // remove 异步执行时 nodes.value 已被新数组替换，于是把新节点也清掉 → 画布变空。
    nodes.value =
      res?.data?.map((item) => {
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
          draggable: item.nodeColour != 1 && item.nodeColour != 2,
        };
      }) || [];
    edges.value = [];
    const lines = [];

    for (let index = 0; index < res?.data?.length; index++) {
      const element = res?.data?.[index];
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
    // 数据与连线就绪后自增 key，强制 VueFlow 重新挂载，确保重载后节点可靠重渲染
    // （仅靠 v-model:nodes 响应式重赋值，在拖拽等交互后 vue-flow 不会可靠重渲染）
    flowKey.value++;
  });
};

const delFn = (e) => {
  Modal.confirm({
    title: "确认删除吗？",
    content: "删除后不可恢复。",
    okText: "删除",
    okButtonProps: {
      type: "primary",
      danger: true,
    },
    cancelButtonText: "取消",
    onOk: () => {
      api
        .delNode({
          id: e?.id,
        })
        .then(() => {
          initBall();
          message.success("已删除");
        });
    },
  });
};

const updateFn = (e) => {
  addRef.value?.openModal(e);
};

onMounted(() => {
  api
    .getDetail({
      id: route.query?.id,
    })
    .then((res) => {
      detail.value = res?.data || {};
    });
  initBall();
});
</script>
<style scoped lang="less">
.card {
  width: 100%;
  padding: 16px 12px;
  background: linear-gradient(90deg, rgba(209, 242, 255, 0.4) 0%, #d1f2ff 100%);
  border-radius: 12px 12px 12px 12px;
}

.card2 {
  background: #111827;
  border-radius: 12px 12px 12px 12px;
  border: 4px solid #0c3a6c;
}

.bottom2 {
  border-top: 2px solid rgba(255, 255, 255, 0.5);
  height: 116px;
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
}
</style>
