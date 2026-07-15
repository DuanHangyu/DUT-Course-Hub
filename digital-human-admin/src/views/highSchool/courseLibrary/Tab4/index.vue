<template>
  <div class="size-full p-4 flex flex-col overflow-hidden">
    <Button type="primary" class="shrink w-fit" @click="add">新增</Button>
    <div
      class="h-full flex items-center justify-center grow"
      v-if="!list.length"
    >
      <Empty />
    </div>
    <div class="grid grid-cols-3 gap-4 mt-4 grow" v-else>
      <section class="p-4 card h-fit" v-for="item in list" :key="item?.id">
        <div class="text-base font-medium text-[rgba(10,8,26,0.88)]">
          {{ item?.title }}
        </div>
        <div class="text-sm text-[rgba(10,8,26,0.45)] truncate">
          {{ item?.description }}
        </div>
        <div class="flex items-center justify-between mt-3">
          <span class="text-sm">
            <span class="text-[rgba(10,8,26,0.45)]">截止时间：</span>
            <span class="text-[rgba(10,8,26,0.88)]">{{ item?.deadline }} </span>
          </span>
          <span>
            <span class="text-xl font-medium text-[rgba(10,8,26,0.88)]">
              {{ item?.submittedCount }}/{{ item?.totalCount }}
            </span>
            <span class="text-sm text-[rgba(10,8,26,0.45)] pl-2">已提交</span>
          </span>
        </div>
        <Progress
          :percent="(item?.submittedCount / item?.totalCount) * 100"
          :show-info="false"
        />
        <div class="bottom2 grid grid-cols-3 pt-2 mt-1">
          <Button type="link" class="btn" @click="openDetail(item)">
            提交详情
          </Button>
          <Button type="link" class="btn" @click="updateFn(item)">编辑</Button>
          <Button type="link" danger @click="delFn(item)">删除</Button>
        </div>
      </section>
    </div>
    <HandleModal ref="hRef" @success="onLoad" />
    <SubmitDetail ref="sRef" @success="onLoad" />
    <AiAgent />
  </div>
</template>
<script setup>
import { Button, Empty, message, Modal, Progress } from "ant-design-vue";
import HandleModal from "./components/HandleModal.vue";
import { onMounted, ref } from "vue";
import SubmitDetail from "./components/SubmitDetail.vue";
import AiAgent from "@/components/AiAgent/index.vue";
import { getList, del } from "@/api/homeWork";
import { useRoute } from "vue-router";

const hRef = ref();
const sRef = ref();
const route = useRoute();
const list = ref([]);

const add = () => {
  hRef.value.openModal({ courseId: route.query?.id });
};

const updateFn = (data) => {
  hRef.value.openModal(data);
};

const onLoad = () => {
  getList({
    courseId: route.query?.id,
  }).then((res) => {
    list.value = res?.data || [];
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
      del({ id: e?.id }).then(() => {
        onLoad();
        message.success("已删除");
      });
    },
  });
};

const openDetail = (row) => {
  sRef.value.openDrawer(row);
};

onMounted(() => {
  onLoad();
});
</script>
<style scoped lang="less">
.card {
  background: #ffffff;
  box-shadow: 0px 8px 16px 0px rgba(0, 0, 0, 0.06);
  border-radius: 12px 12px 12px 12px;
  border: 1px solid rgba(0, 0, 0, 0.15);
  &:hover {
    box-shadow: 0px 8px 16px 0px rgba(0, 0, 0, 0.16);
  }
}
.bottom2 {
  border-top: 1px solid rgba(0, 0, 0, 0.08);
}
:deep(.ant-bth) {
  position: relative;
  &::after {
    width: 1px;
    height: 16px;
    content: "";
    display: block;
    position: absolute;
    right: 0;
    top: 50%;
    transform: translateY(-50%);
    background: rgba(0, 0, 0, 0.08);
  }
}
</style>
