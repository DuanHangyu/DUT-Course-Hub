<template>
  <div class="h-[300px]">
    <Table
      :data-source="list"
      row-key="id"
      :scroll="{ h: '100%' }"
      :pagination="false"
      size="small"
      class="bg-white rounded-lg"
    >
      <TableColumn dataIndex="studentName" title="姓名" />
      <TableColumn dataIndex="studentNo" title="学号" />
      <TableColumn dataIndex="riskLevel" title="风险等级">
        <template #default="{ record }">
          <Tag color="error" v-if="record.riskLevel == 'high'">高风险</Tag>
          <Tag color="volcano" v-if="record.riskLevel == 'medium'">
            中等风险
          </Tag>
          <Tag color="warning" v-if="record.riskLevel == 'low'">低风险</Tag>
        </template>
      </TableColumn>
      <TableColumn dataIndex="riskReason" title="核心问题" />
      <TableColumn dataIndex="nodeName" title="课程" />
    </Table>
  </div>
</template>
<script setup>
import { Table, TableColumn, Tag } from "ant-design-vue";
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { getRiskWarning } from "@/api/studyDetailMonitor";

const list = ref([]);
const route = useRoute();

onMounted(() => {
  getRiskWarning({
    courseId: route.query.id,
  }).then((res) => {
    list.value = res?.data?.warnings || [];
  });
});
</script>
<style scoped lang="less">
:deep(.ant-table) {
  height: 100%;
  .ant-table-content {
    height: 100%;
    table {
      height: 100%;
    }
  }
}
</style>
