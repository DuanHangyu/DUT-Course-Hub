<template>
  <div class="app-container flex flex-col overflow-hidden">
    <Form layout="inline" :model="params" :colon="false" @finish="onSubmit">
      <FormItem label="学校名称" name="schoolName">
        <Input v-model:value="params.schoolName" placeholder="请输入" />
      </FormItem>
      <FormItem class="float-right !mr-0 clear-both">
        <Button type="primary" html-type="submit"> 查询 </Button>
        <Button type="default" class="ml-3" @click="onCancel"> 重置 </Button>
      </FormItem>
    </Form>
    <div class="flex items-center justify-between shrink-0 divider pt-4 mt-4">
      <Button type="primary" @click="addFn"> 新增 </Button>
    </div>
    <div class="grow overflow-hidden mt-4">
      <Table
        :data-source="tableData"
        row-key="id"
        :pagination="{
          total,
          pageSize: params.size,
          current: params.page,
          pageSizeOptions: [20, 50, 100],
          showQuickJumper: true,
          showSizeChanger: true,
        }"
        :loading="loading"
        :scroll="{ y: '100%' }"
        @change="changePage"
      >
        <template #empty>
          <Empty :image="EmptyImage" />
        </template>
        <TableColumn dataIndex="schoolName" title="学校名称" />
        <TableColumn dataIndex="location" title="地址" />
        <TableColumn dataIndex="studentCount" title="学生人数" />
        <TableColumn dataIndex="teacherCount" title="教师人数" />
        <TableColumn dataIndex="principalName" title="校长" />
        <TableColumn dataIndex="phone" title="校长电话" />
        <TableColumn dataIndex="createTime" title="创建时间">
          <template #default="{ record }">
            {{
              record.createTime
                ? dayjs(record.createTime).format("YYYY-MM-DD HH:mm:ss")
                : ""
            }}
          </template>
        </TableColumn>
        <TableColumn dataIndex="adminName" title="管理员名称" />
        <TableColumn dataIndex="adminAccount" title="管理员账号" />
        <TableColumn dataIndex="action" title="操作" :width="220">
          <template #default="{ record }">
            <Button type="link" class="!px-0" @click="gohighSchool(record)">
              管理后台
            </Button>
            <Button type="link" @click="editFn(record)"> 编辑 </Button>
            <Button type="link" danger @click="delFn(record)"> 删除 </Button>
          </template>
        </TableColumn>
      </Table>
    </div>
    <HandleModal ref="hRef" @success="onLoad" />
  </div>
</template>
<script setup>
import { onMounted, ref } from "vue";
import EmptyImage from "@/assets/images/empty.png";
import HandleModal from "./components/HandleModal.vue";
import {
  Table,
  TableColumn,
  Button,
  Form,
  FormItem,
  Input,
  Empty,
  Modal,
  message,
} from "ant-design-vue";
import * as api from "@/api/middleSchoolManage";
import dayjs from "dayjs";

const params = ref({ page: 1, size: 20 });
const tableData = ref([]);
const hRef = ref(null);
const total = ref(0);
const loading = ref(false);
const onSubmit = () => {
  params.value.page = 1;
  onLoad();
};
const onCancel = () => {
  params.value.schoolName = "";
  params.value.page = 1;
  onLoad();
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
      api
        .del({
          id: e?.id,
        })
        .then(() => {
          onLoad();
          message.success("已删除");
        });
    },
  });
};

const onLoad = () => {
  api
    .getList(params.value)
    .then((res) => {
      tableData.value = res?.data?.records || [];
    })
    .finally(() => {
      loading.value = false;
    });
};

const changePage = (e) => {
  params.value.page = e?.current;
  params.value.size = e?.pageSize;
  onLoad();
};

const gohighSchool = (e) => {
  window.open(
    `${location.origin}/highSchool-dataOverview?schoolId=${e?.id}&isSchoolMode=1`,
    "_blank",
  );
};
onMounted(() => {
  onLoad();
});
</script>
<style scoped lang="less">
.divider {
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}
</style>
