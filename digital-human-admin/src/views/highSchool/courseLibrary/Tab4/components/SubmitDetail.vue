<template>
  <Drawer
    v-model:open="open"
    title="作业提交详情"
    width="1060px"
    :closable="false"
    @close="
      () => {
        $emit('success');
      }
    "
  >
    <template #extra>
      <CloseOutlined @click="open = false" />
    </template>
    <div class="overflow-y-auto">
      <Tabs v-model:active-key="tab" @change="changeTab">
        <TabPane
          :tab="`总人数（${statistics?.totalCount || 0}）`"
          key="all"
        ></TabPane>
        <TabPane
          :tab="`已提交（${statistics?.submittedCount || 0}）`"
          key="SUBMITTED"
        ></TabPane>
        <TabPane
          :tab="`未提交（${statistics?.unsubmittedCount || 0}）`"
          key="UNSUBMITTED"
        ></TabPane>
        <TabPane
          :tab="`已批改（${statistics?.gradedCount || 0}）`"
          key="GRADED"
        ></TabPane>
      </Tabs>
      <Form
        :model="params"
        layout="inline"
        class="shrink-0 mt-2"
        :colon="false"
      >
        <FormItem label="姓名" name="name">
          <Input v-model:value="params.name" placeholder="请输入" />
        </FormItem>
        <FormItem label="学号" name="studentNo">
          <Input v-model:value="params.studentNo" placeholder="请输入" />
        </FormItem>
        <FormItem label="班级" name="classId">
          <Select
            v-model:value="params.classId"
            placeholder="请选择"
            class="!w-[214px]"
            :options="schoolClass"
            :field-names="{
              label: 'className',
              value: 'id',
            }"
          />
        </FormItem>
        <FormItem>
          <Button type="primary" @click="onSubmit"> 查询 </Button>
          <Button type="default" class="ml-3" @click="onCancel"> 重置 </Button>
        </FormItem>
      </Form>
      <div class="mt-5">
        <Table :data-source="list" row-key="id" :pagination="false">
          <template #emptyText>
            <Empty />
          </template>
          <TableColumn dataIndex="studentName" title="姓名" />
          <TableColumn dataIndex="studentNo" title="学号" />
          <TableColumn dataIndex="className" title="班级" />
          <TableColumn dataIndex="status" title="状态">
            <template #default="{ record }">
              <Tag
                :bordered="false"
                color="success"
                v-if="record?.status == 'GRADED'"
              >
                已批改
              </Tag>
              <Tag
                :bordered="false"
                color="processing"
                v-if="record?.status == 'SUBMITTED'"
              >
                已提交
              </Tag>
              <Tag
                :bordered="false"
                color="error"
                v-if="record?.status == 'UNSUBMITTED'"
              >
                未提交
              </Tag>
            </template>
          </TableColumn>
          <TableColumn dataIndex="submitTime" title="提交时间">
            <template #default="{ record }">
              {{ record.submitTime || "-" }}
            </template>
          </TableColumn>
          <TableColumn dataIndex="score" title="成绩">
            <template #default="{ record }">
              {{ record.score || "-" }}
            </template>
          </TableColumn>
          <TableColumn dataIndex="action" title="操作">
            <template #default="{ record }">
              <Button
                type="link"
                v-if="record?.status == 'GRADED'"
                class="!pl-0"
                @click="gradFn(record)"
              >
                重新批改
              </Button>
              <Button
                type="link"
                class="!pl-0"
                @click="gradFn(record)"
                v-if="record?.status == 'SUBMITTED'"
              >
                批改
              </Button>
            </template>
          </TableColumn>
        </Table>
      </div>
    </div>
    <Grading
      ref="gRef"
      @success="
        () => {
          onStatistics();
          onLoad();
        }
      "
    />
  </Drawer>
</template>
<script setup>
import { CloseOutlined } from "@ant-design/icons-vue";
import {
  Drawer,
  Table,
  TableColumn,
  Empty,
  Tag,
  Button,
  Form,
  FormItem,
  Input,
  Select,
  Tabs,
  TabPane,
} from "ant-design-vue";
import { nextTick, ref } from "vue";
import Grading from "./Grading.vue";
import { getStatistics, getSubmitList } from "@/api/homeWork";
import useUserStore from "@/store/modules/user";
import { getClassList } from "@/api/homeWork";

defineEmits(["success"]);

const open = ref(false);

const tab = ref("all");
const params = ref({});
const gRef = ref();
const homeWork = ref({});
const statistics = ref({});
const list = ref([]);
const schoolClass = ref([]);
const userStore = useUserStore();
const gradFn = (r) => {
  gRef.value?.openModal(r);
};

const onStatistics = () => {
  getStatistics({
    homeworkId: homeWork.value?.id,
  }).then((res) => {
    statistics.value = res.data;
  });
};

const onLoad = () => {
  getSubmitList({
    homeworkId: homeWork.value?.id,
    status: tab.value == "all" ? undefined : tab.value,
    ...params.value,
  }).then((res) => {
    list.value = res?.data || [];
  });
};

const changeTab = () => {
  nextTick(() => {
    onLoad();
  });
};

const onSubmit = () => {
  onLoad();
};
const onCancel = () => {
  params.value = {};
  onLoad();
};

defineExpose({
  openDrawer: (r = {}) => {
    open.value = true;
    homeWork.value = r;
    onStatistics();
    onLoad();
    getClassList({ homeworkId: homeWork.value?.id }).then((res) => {
      schoolClass.value = res?.data || [];
    });
  },
});
</script>
<style scoped lang="less">
.tab1 {
  background: linear-gradient(360deg, #f1feff 0%, #dcfcff 100%);
  border-radius: 12px 12px 12px 12px;
  border: 1px solid;
  border-image: linear-gradient(
      1deg,
      rgba(255, 255, 255, 1),
      rgba(255, 255, 255, 0)
    )
    1 1;
}
.tab2 {
  background: linear-gradient(360deg, #f1f5ff 0%, #e3ebff 100%);
  border-radius: 12px 12px 12px 12px;
}
.tab3 {
  background: linear-gradient(0deg, #fff9ec 0%, #fff5e2 100%);
  border-radius: 12px 12px 12px 12px;
}

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
