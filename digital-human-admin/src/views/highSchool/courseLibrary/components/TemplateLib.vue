<template>
  <Modal
    v-model:open="dialogOpen"
    :width="900"
    destroy-on-close
    title="从模板新建"
    :footer="null"
  >
    <div class="flex gap-2 mb-4">
      <div
        class="tab"
        v-for="(item, index) in subjectList"
        :key="index"
        @click="changeTab(index)"
        :class="tab == index ? 'activeTab' : ''"
      >
        {{ item }}
      </div>
    </div>
    <div
      class="h-[500px] flex items-center justify-center"
      v-if="!list?.length"
    >
      <Spin :spinning="loading" v-if="loading" />
      <Empty v-else />
    </div>
    <div class="grid grid-cols-2 gap-5 min-h-[500px]" v-if="list.length">
      <div class="card h-fit" v-for="(item, index) in list" :key="index">
        <div class="flex gap-x-5">
          <img
            :src="item?.pictureUrlSigned"
            class="w-[220px] shrink-0 h-[132px] rounded-[8px]"
            alt=""
          />
          <div class="grow overflow-hidden">
            <div class="text-lg font-medium text-[rgba(10,8,26,0.88)]">
              {{ item?.courseName }}
            </div>
            <div class="w-full truncate text-sm text-[rgba(10,8,26,0.45)]">
              {{ item?.courseIntroduce }}
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
          </div>
        </div>
        <div class="btn mt-3" @click="add(item)">创建课程</div>
      </div>
    </div>
    <HandleModal
      ref="handleModalRef"
      @success="
        () => {
          $emit('success');
          dialogOpen = false;
        }
      "
    />
  </Modal>
</template>
<script setup>
import { ref } from "vue";
import { Empty, Modal, Spin } from "ant-design-vue";
import HandleModal from "./HandleModal.vue";
import * as temApi from "@/api/courseConstruction";

defineEmits(["success"]);

const tab = ref(0);
const subjectList = ref(["全部"]);
const dialogOpen = ref(false);
const handleModalRef = ref();
const list = ref([]);
const loading = ref(false);
const state = ref(true);

const changeTab = (index) => {
  tab.value = index;
  onLoad();
};

const onLoad = () => {
  const p = {
    state: state.value,
    subject:
      subjectList.value[tab.value] == "全部"
        ? ""
        : subjectList.value[tab.value],
  };
  loading.value = true;
  temApi
    .getList(p)
    .then((res) => {
      list.value = res?.data?.records || [];
    })
    .finally(() => {
      loading.value = false;
    });
};
const openModal = (e) => {
  dialogOpen.value = true;
  tab.value = 0;
  list.value = [];
  state.value = e?.state;
  temApi.getSubjectList().then((res) => {
    subjectList.value = ["全部", ...(res?.data || [])];
  });
  onLoad();
};

const add = (e) => {
  handleModalRef.value?.openModal({
    source: "template",
    templateId: e.id,
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
  .btn {
    width: 100%;
    line-height: 32px;
    background: #ffffff;
    border-radius: 6px 6px 6px 6px;
    border: 1px solid #1977ff;
    font-weight: 400;
    font-size: 14px;
    color: #1977ff;
    text-align: center;
    cursor: pointer;
  }
  &:hover {
    transform: scale(1.02);
    box-shadow: 0px 8px 16px 0px rgba(0, 0, 0, 0.06);

    .btn {
      background: #1977ff;
      color: #fff;
    }
  }
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

.tab {
  padding: 0 16px;
  line-height: 28px;
  background: #eff1f4;
  border-radius: 100px 100px 100px 100px;
  font-weight: 400;
  font-size: 14px;
  color: rgba(10, 8, 26, 0.75);
  cursor: pointer;
}
.activeTab {
  background: #e8f1ff;
  border-radius: 100px 100px 100px 100px;
  border: 1px solid #1977ff;
  font-weight: 500;
  font-size: 14px;
  color: #1977ff;
}
</style>
