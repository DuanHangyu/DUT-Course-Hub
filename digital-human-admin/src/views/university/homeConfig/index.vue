<template>
  <div class="app-container flex flex-col overflow-hidden">
    <Tabs v-model:active-key="activeName" class="shrink-0" @change="changeTab">
      <TabPane tab="Banner配置" :key="1"></TabPane>
      <TabPane tab="底图配置" :key="2"></TabPane>
    </Tabs>
    <div class="flex items-center justify-between shrink-0">
      <Button type="primary" class="searchBtn" @click="addFn"> 新增 </Button>
    </div>
    <div class="grow overflow-hidden mt-4">
      <Table
        :data-source="tableData"
        row-key="id"
        :pagination="false"
        :loading="loading"
        :scroll="{ y: '100%' }"
        @change="onLoad"
      >
        <template #empty>
          <Empty />
        </template>
        <TableColumn dataIndex="name" title="名称" />
        <TableColumn dataIndex="imageUrl" title="课程图片">
          <template #default="{ record }">
            <Image :src="record?.imageUrl" :width="72" :height="40" />
          </template>
        </TableColumn>
        <TableColumn
          dataIndex="jumpUrl"
          title="跳转链接"
          v-if="activeName == 2"
        />
        <TableColumn dataIndex="action" title="操作" :width="300">
          <template #default="{ record }">
            <Button
              type="link"
              @click="moveFn(record, 'up')"
              :disabled="tableData?.length == 1 || record?.sort == 0"
              class="!px-0"
            >
              上移
            </Button>
            <Button
              type="link"
              @click="moveFn(record, 'down')"
              :disabled="
                tableData?.length == 1 || record?.sort == tableData?.length - 1
              "
            >
              下移
            </Button>
            <Button type="link" @click="editFn(record)"> 编辑 </Button>
            <Button
              type="link"
              danger
              :disabled="
                activeName == 1
                  ? tableData?.length == 1
                  : tableData?.length <= 3
              "
              @click="delFn(record)"
            >
              删除
            </Button>
          </template>
        </TableColumn>
      </Table>
    </div>
    <HandleModal ref="hRef" :type="activeName" @success="onLoad" />
  </div>
</template>
<script setup>
import { nextTick, onMounted, ref } from "vue";
import HandleModal from "./components/HandleModal.vue";
import {
  Button,
  Tabs,
  TabPane,
  Table,
  TableColumn,
  Empty,
  Image,
  Modal,
} from "ant-design-vue";
import * as api from "@/api/homeConfig";

const activeName = ref(1);
const tableData = ref([]);
const hRef = ref(null);
const loading = ref(false);
const params = ref({ page: 1, size: 200 });
const changeTab = (e) => {
  nextTick(() => {
    onLoad();
  });
};

const addFn = () => {
  hRef.value?.openModal();
};

const editFn = (row) => {
  hRef.value?.openModal(row);
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
      if (activeName.value == 1) {
        api
          .delBanner({
            id: e?.id,
          })
          .then(() => {
            onLoad();
            message.success("已删除");
          });
      }
      if (activeName.value == 2) {
        api
          .delBackground({
            id: e?.id,
          })
          .then(() => {
            onLoad();
            message.success("已删除");
          });
      }
    },
  });
};

const moveFn = (e, direction = "up") => {
  if (activeName.value == 1) {
    api
      .moveBanner({
        id: e?.id,
        direction,
      })
      .then(() => {
        onLoad();
      });
  }
  if (activeName.value == 2) {
    api
      .moveBackground({
        id: e?.id,
        direction,
      })
      .then(() => {
        onLoad();
      });
  }
};

const onLoad = () => {
  loading.value = true;
  if (activeName.value == 1) {
    api
      .getBannerList(params.value)
      .then((res) => {
        tableData.value = res?.data?.records || [];
      })
      .finally(() => {
        loading.value = false;
      });
  }
  if (activeName.value == 2) {
    api
      .getBackgroundList(params.value)
      .then((res) => {
        tableData.value = res?.data?.records || [];
      })
      .finally(() => {
        loading.value = false;
      });
  }
};

onMounted(() => {
  onLoad();
});
</script>
