<template>
  <div class="app-container flex flex-col overflow-hidden">
    <Form :model="params" layout="inline" :colon="false" @finish="onSubmit">
      <FormItem label="教师姓名" name="userName">
        <Input v-model:value="params.userName" placeholder="请输入" />
      </FormItem>
      <FormItem label="登录账号" name="account">
        <Input v-model:value="params.account" placeholder="请输入" />
      </FormItem>
      <FormItem>
        <Button type="primary" html-type="submit"> 查询 </Button>
        <Button type="default" class="ml-3" @click="onCancel"> 重置 </Button>
      </FormItem>
    </Form>
    <div class="flex items-center shrink-0 divider mt-5 pt-5 space-x-3">
      <Button type="primary" @click="addFn"> 新增 </Button>
    </div>
    <div class="grow overflow-hidden mt-4" ref="tableRef">
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
        <template #emptyText>
          <Empty />
        </template>
        <TableColumn dataIndex="userName" title="教师姓名" />
        <TableColumn dataIndex="account" title="登录账号" />
        <TableColumn dataIndex="state" title="状态">
          <template #default="{ record }">
            <Switch
              :checked-value="true"
              :un-checked-value="false"
              checked-children="启用"
              un-checked-children="禁用"
              :checked="record?.state"
              @change="changeStateFn(record)"
            />
          </template>
        </TableColumn>
        <TableColumn dataIndex="createTime" title="创建时间" />
        <TableColumn dataIndex="action" title="操作">
          <template #default="{ record }">
            <Button type="link" class="!px-0" @click="editFn(record)">
              修改
            </Button>
            <Button type="link" danger @click="delFn(record)"> 删除 </Button>
          </template>
        </TableColumn>
      </Table>
    </div>
    <HandleModal ref="handleRef" @success="onLoad()" />
  </div>
</template>
<script setup>
import {
  Form,
  FormItem,
  Input,
  Button,
  Table,
  TableColumn,
  Empty,
  message,
  Modal,
  Switch,
} from "ant-design-vue";
import { h, onMounted, ref } from "vue";
import HandleModal from "./components/HandleModal.vue";
import { getTeacherList, deleteTeacher, changeState } from "@/api/teacher";
import { ExclamationCircleOutlined } from "@ant-design/icons-vue";
import useUserStore from "@/store/modules/user";

const handleRef = ref();
const total = ref();
const params = ref({ page: 1, size: 20 });
const tableData = ref([]);
const loading = ref(false);
const userStore = useUserStore();
const onSubmit = () => {
  params.value.page = 1;
  onLoad();
};
const onCancel = () => {
  params.value.userName = "";
  params.value.account = "";
  params.value.page = 1;
  onLoad();
};

const addFn = () => {
  handleRef.value?.openModal();
};
const editFn = (e) => {
  handleRef.value?.openModal(e);
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
      deleteTeacher(e?.id).then(() => {
        onLoad();
        message.success("已删除");
      });
    },
  });
};

const onLoad = () => {
  loading.value = true;
  getTeacherList({
    ...params.value,
    schoolId: userStore.schoolId,
  })
    .then((res) => {
      tableData.value = res?.data?.records || [];
      total.value = res?.data?.total || 0;
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

const changeStateFn = (e) => {
  Modal.confirm({
    icon: h(ExclamationCircleOutlined, {
      color: "#1677ff",
      class: "!text-[#1677ff]",
    }),
    title: e?.state ? "确认禁用吗？" : "确认启用吗？",
    content: e?.state
      ? "禁用后将不展示该供应商的数据。"
      : "启用后将展示该供应商的数据。",
    okText: e?.state ? "禁用" : "启用",
    onOk: () => {
      changeState({
        id: e?.id,
        state: !e?.state,
      }).then(() => {
        message.success(e?.state ? "已禁用" : "已启用");
        onLoad();
      });
    },
  });
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
