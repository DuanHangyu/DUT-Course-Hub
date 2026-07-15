<template>
  <Modal
    v-model:open="dialogOpen"
    :width="540"
    destroy-on-close
    title="课程明细"
    :footer="null"
  >
    <div
      class="h-[300px] flex items-center justify-center"
      v-if="!list.length || loading"
    >
      <Empty v-if="!list.length && !loading" />
      <Spin spinning v-if="loading" />
    </div>
    <div class="card flex gap-x-5 mb-5" v-for="item in list" :key="item?.id">
      <div class="w-[255px] shrink-0">
        <img
          :src="item?.pictureUrlSigned"
          class="w-full h-[153px] rounded-[8px]"
          alt=""
        />
      </div>
      <div class="grow overflow-hidden">
        <div class="text-lg font-medium text-[rgba(10,8,26,0.88)]">
          {{ item?.courseName }}
        </div>
        <div class="w-full truncate text-sm text-[rgba(10,8,26,0.45)]">
          {{ item?.courseIntroduce }}
        </div>
        <div class="line !mt-2">
          <span class="label">授课老师：</span>
          <span class="content">{{ item?.teacherName }}</span>
        </div>
        <div class="line">
          <span class="label">学生人数：</span>
          <span class="content">{{ item?.studentCount }}</span>
        </div>
        <div class="line">
          <span class="label">所属学科：</span>
          <span class="content">{{ item?.subject }}</span>
        </div>
        <div class="line">
          <span class="label">创建人：</span>
          <span class="content">{{ item?.createName }}</span>
        </div>
        <div class="line">
          <span class="label">创建时间：</span>
          <span class="content">{{ item?.createTime }}</span>
        </div>
        <div class="line">
          <span class="label">邀请码：</span>
          <span class="content">{{ item?.inviteCode }}</span>
        </div>
      </div>
    </div>
  </Modal>
</template>
<script setup>
import { ref } from "vue";
import { Empty, Modal, Spin } from "ant-design-vue";
import * as api from "@/api/courseLibrary";
import useUserStore from "@/store/modules/user";

const dialogOpen = ref(false);
const userStore = useUserStore();
const loading = ref(false);
const list = ref([]);
const openModal = (e = {}) => {
  dialogOpen.value = true;
  loading.value = true;
  list.value = [];
  api
    .getList({
      subject: e?.name,
      schoolId: userStore.schoolId,
    })
    .then((res) => {
      list.value = res?.data?.records || [];
    })
    .finally(() => {
      loading.value = false;
    });
};

defineExpose({
  openModal,
});
</script>
<style scoped lang="less">
.card {
  width: 100%;
  background: #ffffff;
  border-radius: 12px 12px 12px 12px;
  border: 1px solid rgba(0, 0, 0, 0.15);
  padding: 12px;
  .line {
    line-height: 18px;
    .label {
      font-weight: 400;
      font-size: 14px;
      color: rgba(10, 8, 26, 0.45);
    }
    .content {
      font-weight: 400;
      font-size: 14px;
      color: rgba(10, 8, 26, 0.88);
    }
  }
}
</style>
