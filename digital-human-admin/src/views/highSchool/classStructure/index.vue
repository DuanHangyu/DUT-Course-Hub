<template>
  <div class="app-container flex flex-col overflow-hidden !px-0">
    <Form
      :model="params"
      :colon="false"
      class="px-6 flex items-center justify-between"
      @finish="onSubmit"
    >
      <FormItem label="班级名称" name="className">
        <Input v-model:value="params.className" placeholder="请输入" />
      </FormItem>
      <FormItem>
        <Button type="primary" html-type="submit"> 查询 </Button>
        <Button type="default" @click="onCancel" class="ml-3"> 重置 </Button>
      </FormItem>
    </Form>
    <div class="px-6 shrink-0">
      <div class="divider pt-4">
        <Button type="primary" class="searchBtn" @click="addFn"> 新增 </Button>
      </div>
    </div>

    <div class="grow overflow-y-auto relative">
      <Spin
        spinning
        class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2"
        v-if="loading"
      />
      <div
        class="size-full flex items-center justify-center"
        v-if="!list.length && !loading"
      >
        <Empty />
      </div>
      <div v-if="list.length" class="grid grid-cols-3 gap-4 py-4 px-6">
        <div class="card" v-for="(item, index) in list" :key="index">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-lg font-medium text-[rgba(10,8,26,0.88)]">
                {{ item?.className }}
              </div>
              <div class="text-sm text-[rgba(10,8,26,0.45)]">
                班主任：{{ item?.teacherName }}
              </div>
            </div>
            <svg-icon
              icon-class="edit"
              class="text-[#1977FF] text-2xl cursor-pointer edit"
              @click="updateFn(item)"
            ></svg-icon>
          </div>
          <div class="grid grid-cols-2 gap-x-3 mt-4">
            <div
              class="h-[86px] bg-[#F7F8FB] rounded-lg flex flex-col items-center justify-center"
            >
              <div class="flex items-center justify-center space-x-[6px]">
                <svg-icon
                  icon-class="xsrs"
                  class="text-[rgba(10,8,26,0.45)] text-sm"
                ></svg-icon>
                <span class="text-sm text-[rgba(10,8,26,0.45)]">
                  学生人数
                </span>
              </div>
              <span class="text-2xl font-medium text-[rgba(10,8,26,0.88)]">
                {{ item?.studentCount }}
              </span>
            </div>
            <div
              class="h-[86px] bg-[#F7F8FB] rounded-lg flex flex-col items-center justify-center"
            >
              <div class="flex items-center justify-center space-x-[6px]">
                <svg-icon
                  icon-class="xxjd"
                  class="text-[rgba(10,8,26,0.45)] text-sm"
                ></svg-icon>
                <span class="text-sm text-[rgba(10,8,26,0.45)]">
                  学习进度
                </span>
              </div>
              <span class="text-2xl font-medium text-[rgba(10,8,26,0.88)]">
                {{ item?.learningProgress }}%
              </span>
            </div>
          </div>
          <div class="mt-4 btn" @click="goDetail(item)">进入学生管理</div>
        </div>
      </div>
    </div>
    <HandleModal ref="handleRef" @success="onLoad()" />
  </div>
</template>
<script setup>
import { onMounted, ref } from "vue";
import HandleModal from "./components/HandleModal.vue";
import {
  Form,
  Button,
  FormItem,
  Input,
  Empty,
  Spin,
  message,
} from "ant-design-vue";
import * as api from "@/api/classStructure";
import useUserStore from "@/store/modules/user";
import { useRouter } from "vue-router";

const params = ref({ page: 1, size: 5000 });
const handleRef = ref();
const loading = ref(false);
const list = ref([]);
const userStore = useUserStore();
const router = useRouter();
const addFn = () => {
  handleRef.value?.openModal();
};

const updateFn = (r) => {
  handleRef.value?.openModal(r);
};

const onLoad = () => {
  loading.value = true;
  api
    .getList({
      ...params.value,
      schoolId: userStore.schoolId,
    })
    .then((res) => {
      list.value = res?.data?.records || [];
    })
    .finally(() => {
      loading.value = false;
    });
};

const onSubmit = () => {
  onLoad();
};

const onCancel = () => {
  params.value = { page: 1, size: 5000 };
  onLoad();
};

onMounted(() => {
  onLoad();
});

const goDetail = (r) => {
  if (
    !userStore?.info?.authModules?.includes("studentManage") &&
    userStore?.info?.role == 1
  ) {
    message.error("暂无权限");
    return;
  }
  router.push({
    path: "/highSchool-studentManage",
    query: {
      classId: r.id,
    },
  });
};
</script>
<style scoped lang="less">
.divider {
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}
.card {
  width: 100%;
  background: #ffffff;
  box-shadow: 0px 8px 16px 0px rgba(0, 0, 0, 0.16);
  border-radius: 12px 12px 12px 12px;
  border: 1px solid rgba(0, 0, 0, 0.15);
  padding: 16px;
  transition: transform 0.3s ease;
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
  .edit {
    opacity: 0;
  }
  &:hover {
    transform: scale(1.02);
    box-shadow: 0px 8px 16px 0px rgba(0, 0, 0, 0.16);

    .btn {
      background: #1977ff;
      color: #fff;
    }

    .edit {
      opacity: 1;
    }
  }
}
</style>
