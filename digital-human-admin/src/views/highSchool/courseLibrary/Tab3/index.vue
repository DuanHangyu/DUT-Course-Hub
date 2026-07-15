<template>
  <div class="size-full flex flex-col overflow-hidden p-4">
    <div class="tabs flex w-fit mb-4">
      <div
        class="tab"
        @click="changeTab(1)"
        :class="tab == 1 ? 'activeTab' : ''"
      >
        全部学生（{{ total }}）
      </div>
      <div
        class="tab"
        @click="changeTab(2)"
        :class="tab == 2 ? 'activeTab' : ''"
      >
        进度滞后/预警（{{ warningCount }}）
      </div>
    </div>
    <Form
      :model="params"
      layout="inline"
      class="shrink-0"
      :colon="false"
      @finish="onSubmit"
    >
      <FormItem label="姓名" name="studentName">
        <Input v-model:value="params.studentName" placeholder="请输入" />
      </FormItem>
      <FormItem label="学号" name="studentNo">
        <Input v-model:value="params.studentNo" placeholder="请输入" />
      </FormItem>
      <FormItem label="班级" name="className">
        <Input v-model:value="params.className" placeholder="请输入" />
      </FormItem>
      <FormItem>
        <Button type="primary" html-type="submit"> 查询 </Button>
        <Button type="default" @click="onCancel" class="ml-3"> 重置 </Button>
      </FormItem>
    </Form>
    <div class="grow overflow-hidden mt-4" ref="tableRef">
      <Table
        :data-source="tableData"
        row-key="id"
        :pagination="{
          total,
          pageSize: params.pageSize,
          current: params.pageNum,
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
        <TableColumn dataIndex="studentName" title="学生姓名" />
        <TableColumn dataIndex="studentNo" title="学号" />
        <TableColumn dataIndex="className" title="班级" />
        <TableColumn dataIndex="courseProgress" title="课程综合进度">
          <template #default="{ record }">
            <Progress :percent="record?.courseProgress || 0" />
          </template>
        </TableColumn>
        <TableColumn dataIndex="submittedCount" title="作业提交情况">
          <template #default="{ record }">
            <div class="flex items-center space-x-2">
              <span class="text-black text-sm">
                {{ record?.submittedCount }}/{{ record?.totalAssignmentCount }}
              </span>
              <Tag
                v-if="record?.totalAssignmentCount - record?.submittedCount > 0"
                :bordered="false"
                color="error"
              >
                缺{{ record?.totalAssignmentCount - record?.submittedCount }}份
              </Tag>
            </div>
          </template>
        </TableColumn>
        <TableColumn dataIndex="currentNodeName" title="当前学习章节" />
        <TableColumn dataIndex="lastLoginTime" title="最近登录时间">
          <template #default="{ record }">
            {{
              record?.lastLoginTime
                ? dayjs(record?.lastLoginTime).format("YYYY-MM-DD HH:mm:ss")
                : ""
            }}
          </template>
        </TableColumn>
        <TableColumn dataIndex="action" title="操作">
          <template #default="{ record }">
            <Button type="link" class="!px-0" @click="openDetail(record)">
              学习详情
            </Button>
          </template>
        </TableColumn>
      </Table>
    </div>
    <Detail ref="dRef" />
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
  Progress,
  Tag,
} from "ant-design-vue";
import { onMounted, ref } from "vue";
import Detail from "./components/Detail.vue";
import { getStudentProgress } from "@/api/progress";
import { useRoute } from "vue-router";
import dayjs from "dayjs";

const total = ref(0);
const params = ref({ pageNum: 1, pageSize: 20 });
const tableData = ref([]);
const loading = ref(false);
const tab = ref(1);
const dRef = ref(null);
const warningCount = ref(0);
const route = useRoute();

const changeTab = (key) => {
  tab.value = key;
  onCancel();
};
const onSubmit = () => {
  params.value.pageNum = 1;
  onLoad();
};
const onCancel = () => {
  params.value = {};
  params.value.pageNum = 1;
  onLoad();
};

const onLoad = () => {
  loading.value = true;
  getStudentProgress({
    ...params.value,
    courseId: route.query?.id,
    filterType: tab.value == 2 ? "warning" : undefined,
  })
    .then((res) => {
      tableData.value = res?.data?.list || [];
      total.value = res?.data?.total || 0;
      warningCount.value = res?.data?.warningCount || 0;
    })
    .finally(() => {
      loading.value = false;
    });
};
const changePage = (e) => {
  params.value.pageNum = e?.current;
  params.value.pageSize = e?.pageSize;
  onLoad();
};
onMounted(() => {
  onLoad();
});

const openDetail = (item) => {
  dRef.value.openDrawer(item);
};
</script>
<style scoped lang="less">
.tabs {
  background: rgba(0, 0, 0, 0.06);
  border-radius: 8px 8px 8px 8px;
  padding: 2px;
  .tab {
    padding: 0 12px;
    font-weight: 400;
    font-size: 14px;
    color: rgba(0, 0, 0, 0.65);
    line-height: 26px;
    cursor: pointer;
  }
  .activeTab {
    background: #ffffff;
    box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.05);
    border-radius: 8px 8px 8px 8px;
    font-weight: 500;
    font-size: 14px;
    color: #1977ff;
  }
}
</style>
