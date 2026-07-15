<template>
  <Modal
    v-model:open="dialogOpen"
    :width="740"
    destroy-on-close
    :title="`${nodeDetail?.nodeName}-${nodeDetail?.completionRate || 0}%`"
    :footer="null"
  >
    <div class="grid grid-cols-2 gap-x-4">
      <div class="c1 p-4 space-x-4 flex">
        <img src="@/assets/images/wl-1.png" class="size-8" alt="" />
        <div>
          <div class="text-sm text-[rgba(10,8,26,0.66)]">已完成人数</div>
          <div class="text-lg font-semibold text-[rgba(10,8,26,0.88)]">
            {{ nodeDetail?.completedCount || 0 }}
          </div>
        </div>
      </div>
      <div class="c2 p-4 space-x-4 flex">
        <img src="@/assets/images/wl-2.png" class="size-8" alt="" />
        <div>
          <div class="text-sm text-[rgba(10,8,26,0.66)]">未完成人数</div>
          <div class="text-lg font-semibold text-[rgba(10,8,26,0.88)]">
            {{ nodeDetail?.incompleteCount || 0 }}
          </div>
        </div>
      </div>
    </div>
    <div class="mt-6 flex space-x-2 items-center">
      <div class="w-[3px] h-[18px] bg-[#1977FF]"></div>
      <span class="text-base font-semibold text-[rgba(10,8,26,0.88)]">
        已完成学生
      </span>
    </div>
    <Empty v-if="!nodeDetail?.completedStudents?.length" />
    <div class="mt-2 flex flex-wrap gap-3" v-else>
      <div
        class="bg-[#F0F0F4] px-3 leading-[30px] text-sm text-[rgba(10,8,26,0.75)] rounded-[100px]"
        v-for="(item, index) in nodeDetail?.completedStudents"
        :key="index"
      >
        {{ item?.studentName }}
      </div>
    </div>
    <div class="mt-6 flex space-x-2 items-center">
      <div class="w-[3px] h-[18px] bg-[#1977FF]"></div>
      <span class="text-base font-semibold text-[rgba(10,8,26,0.88)]">
        未完成学生
      </span>
    </div>
    <Empty v-if="!nodeDetail?.incompleteStudents?.length" />
    <div class="mt-2 flex flex-wrap gap-3" v-else>
      <div
        class="bg-[#F0F0F4] px-3 leading-[30px] text-sm text-[rgba(10,8,26,0.75)] rounded-[100px]"
        v-for="(item, index) in nodeDetail?.incompleteStudents"
        :key="index"
      >
        {{ item?.studentName }}
      </div>
    </div>
  </Modal>
</template>
<script setup>
import { ref } from "vue";
import { Empty, Modal } from "ant-design-vue";
import { getNodeDetail } from "../../../../../api/studyDetailMonitor";
import { useRoute } from "vue-router";
const dialogOpen = ref(false);
const route = useRoute();
const nodeDetail = ref({});
const openModal = (e = {}) => {
  dialogOpen.value = true;
  getNodeDetail({
    courseId: route.query?.id,
    nodeId: e?.nodeId,
  }).then((res) => {
    nodeDetail.value = res?.data || {};
  });
};
defineExpose({
  openModal,
});
</script>
<style scoped lang="less">
.c1 {
  width: 100%;
  height: 79px;
  background: linear-gradient(180deg, #f1feff 0%, #dcfcff 100%);
  border-radius: 12px 12px 12px 12px;
}
.c2 {
  width: 100%;
  height: 79px;
  background: linear-gradient(180deg, #fff1f1 0%, #ffdfde 100%);
  border-radius: 12px 12px 12px 12px;
}
</style>
