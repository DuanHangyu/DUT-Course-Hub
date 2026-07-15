<template>
  <div class="app-container flex flex-col overflow-hidden">
    <Form :model="params" layout="inline" :colon="false" @finish="onSubmit">
      <FormItem label="学生姓名" name="studentName">
        <Input v-model:value="params.studentName" placeholder="请输入" />
      </FormItem>
      <FormItem label="学号" name="idNumber">
        <Input v-model:value="params.idNumber" placeholder="请输入" />
      </FormItem>
      <FormItem label="班级" name="classId">
        <Select
          v-model:value="params.classId"
          :options="classList"
          :field-names="{
            label: 'className',
            value: 'id',
          }"
          placeholder="请输入"
          class="!w-[214px]"
        />
      </FormItem>
      <FormItem>
        <Button type="primary" html-type="submit"> 查询 </Button>
        <Button type="default" @click="onCancel" class="ml-3"> 重置 </Button>
      </FormItem>
    </Form>
    <div class="flex items-center shrink-0 divider mt-5 pt-5 space-x-3">
      <Button type="primary" @click="addFn"> 新增 </Button>
      <Button type="default" plain @click="openBatchImport"> 批量导入 </Button>
      <Button type="default" plain @click="exportFn"> 导出 </Button>
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
        :row-selection="{
          selectedRowKeys: multipleSelection,
          onChange: handleSelectionChange,
        }"
      >
        <template #emptyText>
          <Empty />
        </template>
        <TableColumn dataIndex="studentName" title="姓名" />
        <TableColumn dataIndex="idNumber" title="学号" />
        <TableColumn dataIndex="className" title="班级" />
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
    <BatchImportModal ref="batchImportRef" @success="onLoad()" />
  </div>
</template>
<script setup>
import { onMounted, ref } from "vue";
import HandleModal from "./components/HandleModal.vue";
import { getStudentList, deleteStudent } from "@/api/student";
import request from "@/utils/request";
import { saveAs } from "file-saver";
import BatchImportModal from "./components/BatchImportModal.vue";
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
  Select,
} from "ant-design-vue";
import useUserStore from "@/store/modules/user";
import { useRoute } from "vue-router";
import * as classApi from "@/api/classStructure";

const handleRef = ref();
const total = ref();
const params = ref({ page: 1, size: 20 });
const tableData = ref([]);
const loading = ref(false);
const multipleSelection = ref([]);
const batchImportRef = ref();
const userStore = useUserStore();
const route = useRoute();
const classList = ref([]);

const onSubmit = () => {
  params.value.page = 1;
  onLoad();
};
const onCancel = () => {
  params.value.studentName = "";
  params.value.idNumber = "";
  params.value.classId = undefined;
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
      deleteStudent(e?.id).then(() => {
        onLoad();
        message.success("已删除");
      });
    },
  });
};

const onLoad = () => {
  loading.value = true;
  getStudentList({
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

onMounted(() => {
  if (route.query?.classId) {
    params.value.classId = route.query.classId * 1;
  }
  onLoad();
  classApi
    .getList({
      schoolId: userStore.schoolId,
    })
    .then((res) => {
      classList.value = res?.data?.records || [];
    });
});

const handleSelectionChange = (val) => {
  multipleSelection.value = val;
};

const exportFn = () => {
  if (!multipleSelection.value?.length) {
    message.warning("请先选择数据");
    return;
  }
  request
    .post(
      "/api/school/student/export",
      { ids: multipleSelection.value },
      {
        responseType: "blob",
      },
    )
    .then((response) => {
      const { data, headers } = response;

      const contentDisposition = headers["content-disposition"];

      // 解析文件名
      let organFileName = "";
      if (contentDisposition) {
        const fileNameMatch = contentDisposition.match(/filename="?(.+)"?/);
        if (fileNameMatch && fileNameMatch.length > 1) {
          try {
            organFileName = decodeURIComponent(fileNameMatch[1]);
          } catch (error) {}
        }
      }
      const blob = new Blob([data]);
      saveAs(blob, organFileName || "student.xlsx");
    });
};

const openBatchImport = () => {
  batchImportRef.value?.openModal();
};
</script>
<style scoped lang="less">
.divider {
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}
</style>
