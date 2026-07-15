<template>
  <div class="size-full overflow-hidden p-4">
    <div class="size-full overflow-hidden" ref="tableRef">
      <Table
        :data-source="tableData"
        row-key="id"
        :pagination="false"
        :loading="loading"
        :scroll="{ y: '100%' }"
      >
        <template #emptyText>
          <Empty />
        </template>
        <TableColumn dataIndex="nodeName" title="节点名称" />
        <TableColumn dataIndex="action" title="操作" width="150px">
          <template #default="{ record }">
            <Button type="link" class="!px-0" @click="openAsk(record)">
              答疑
            </Button>
          </template>
        </TableColumn>
      </Table>
    </div>
    <AskQuestionModal ref="askQuestionRef" />
  </div>
</template>
<script setup>
import { Button, Table, TableColumn, Empty } from "ant-design-vue";
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import * as api from "@/api/courseConstruction";
import AskQuestionModal from "./AskQuestionModal.vue";

const tableData = ref([]);
const loading = ref(false);
const route = useRoute();
const askQuestionRef = ref();
const onLoad = () => {
  loading.value = true;
  api
    .getNodeList({
      courseId: route.query?.id,
    })
    .then((res) => {
      tableData.value =
        res?.data?.filter(
          (item) => item?.nodeColour != 1 && item?.nodeColour != 2,
        ) || [];
    })
    .finally(() => {
      loading.value = false;
    });
};
const openAsk = (row) => {
  askQuestionRef.value?.openModal(row);
};

onMounted(() => {
  onLoad();
});
</script>
