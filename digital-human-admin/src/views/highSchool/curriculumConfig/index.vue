<template>
  <div class="app-container flex flex-col overflow-hidden">
    <Tabs v-model="activeName" class="shrink-0 ml-4">
      <TabPane tab="学科分类排序" key="1"></TabPane>
    </Tabs>
    <div class="grow overflow-hidden mt-4" ref="tableRef">
      <Table
        :data-source="tableData"
        row-key="id"
        :pagination="false"
        :loading="loading"
        :scroll="{ y: '100%' }"
        @change="onLoad"
      >
        <template #emptyText>
          <Empty />
        </template>
        <TableColumn dataIndex="name" title="学科名称" />
        <TableColumn dataIndex="courseCount" title="包含课程">
          <template #default="{ record }">
            <a
              class="cursor-pointer text-sm text-[#1966FF]"
              @click="viewDetail(record)"
            >
              {{ record?.courseCount }}
            </a>
          </template>
        </TableColumn>
        <TableColumn dataIndex="action" title="操作" :width="200">
          <template #default="{ record, index }">
            <Button
              type="link"
              class="!px-0"
              :disabled="index == 0"
              @click="moveFn('up', index)"
            >
              上移
            </Button>
            <Button
              type="link"
              :disabled="tableData.length - 1 == index"
              @click="moveFn('down', index)"
            >
              下移
            </Button>
          </template>
        </TableColumn>
      </Table>
    </div>
    <CourseDetail ref="cRef" />
  </div>
</template>
<script setup>
import {
  Tabs,
  TabPane,
  Button,
  Table,
  TableColumn,
  Empty,
} from "ant-design-vue";
import { onMounted, ref } from "vue";
import { getList, updateSort } from "@/api/curriculumConfig";
import CourseDetail from "./components/CourseDetail.vue";
import useUserStore from "@/store/modules/user";
const activeName = ref("1");
const tableData = ref([]);
const loading = ref(false);
const cRef = ref();
const userStore = useUserStore();
const onLoad = () => {
  loading.value = true;
  getList({
    schoolId: userStore.schoolId,
  })
    .then((res) => {
      tableData.value = res?.data || [];
    })
    .finally(() => {
      loading.value = false;
    });
};

const viewDetail = (row) => {
  cRef.value?.openModal(row);
};

const moveFn = (direction, index) => {
  const list = [...tableData.value];
  if (direction === "up") {
    if (index <= 0) return;
    [list[index], list[index - 1]] = [list[index - 1], list[index]];
  } else if (direction === "down") {
    if (index >= list.length - 1) return;
    [list[index], list[index + 1]] = [list[index + 1], list[index]];
  }

  updateSort({
    subjectIds: list?.map((item) => item?.id),
    schoolId: userStore.schoolId,
  }).then(() => {
    onLoad();
  });
};

onMounted(() => {
  onLoad();
});
</script>
