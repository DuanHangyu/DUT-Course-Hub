<template>
  <Drawer v-model:open="open" title="学习详情" width="740px" :closable="false">
    <template #extra>
      <CloseOutlined @click="open = false" />
    </template>
    <div class="overflow-y-auto">
      <div class="grid grid-cols-3 gap-4">
        <section
          v-for="(item, index) in cards"
          :key="index"
          :class="item.color"
          class="h-20 flex space-x-4 p-4"
        >
          <img :src="item.img" class="size-8" alt="" />
          <div class="">
            <div class="text-sm text-[rgba(10,8,26,0.66)]">
              {{ item.label }}
            </div>
            <div class="text-base font-semibold text-[rgba(10,8,26,0.88)] mt-1">
              {{ item.value }}
            </div>
          </div>
        </section>
      </div>
      <div class="tabs flex w-fit mt-6">
        <div
          class="tab"
          @click="changeTab(1)"
          :class="tab == 1 ? 'activeTab' : ''"
        >
          课程学习进度
        </div>
        <div
          class="tab"
          @click="changeTab(2)"
          :class="tab == 2 ? 'activeTab' : ''"
        >
          作业提交记录
        </div>
      </div>
      <div class="mt-6 space-y-4">
        <section
          class="rounded-lg bg-[#F8FAFC] px-3 py-4 flex space-x-3"
          v-for="(item, index) in learningDetail?.nodeProgressList"
          :key="index"
          v-if="tab == 1"
        >
          <img
            src="@/assets/images/wancheng.png"
            class="size-10 shrink-0"
            alt=""
            v-if="item?.status == 2"
          />
          <div
            v-else-if="item?.nodeColour == 1"
            class="size-10 bg-[#0461c1] rounded-full flex items-center justify-center"
          >
            <img src="@/assets/images/startNode.png" class="size-7" alt="" />
          </div>
          <div
            v-else-if="item?.nodeColour == 2"
            class="size-10 bg-[#4fd882] rounded-full flex items-center justify-center"
          >
            <img src="@/assets/images/endNode.png" class="size-7" alt="" />
          </div>
          <div
            v-else
            class="rounded-full size-10 flex items-center justify-center"
            :style="{
              background:
                item?.nodeColour == 3
                  ? '#7DBFFB'
                  : item?.nodeColour == 4
                    ? '#4FD8C8'
                    : '#FB837D',
            }"
          >
            <img
              src="@/assets/images/node.png"
              class="h-[20px] w-[20px]"
              alt=""
            />
          </div>
          <div class="grow overflow-hidden">
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium text-[rgba(10,8,26,0.88)]">
                {{ item?.nodeName }}
              </span>
              <span class="text-[rgba(10,8,26,0.45)] text-sm">
                {{ item?.duration || 0 }}分
              </span>
            </div>
            <div class="flex items-center justify-between mt-1">
              <Progress :percent="item?.progress || 0" class="grow" />
              <span
                class="ml-3 text-xs font-medium shrink-0"
                :class="item?.status == 2
                  ? 'text-[#0EC55D]'
                  : item?.status == 1
                    ? 'text-[#0461C1]'
                    : 'text-[rgba(10,8,26,0.35)]'"
              >
                {{ item?.status == 2 ? '已完成' : item?.status == 1 ? '学习中' : '未开始' }}
              </span>
            </div>
          </div>
        </section>
        <Table
          :data-source="learningDetail?.assignmentRecords"
          :pagination="false"
          v-if="tab == 2"
        >
          <template #emptyText>
            <Empty />
          </template>
          <TableColumn dataIndex="title" title="作业标题" />
          <TableColumn dataIndex="deadline" title="截止时间">
            <template #default="{ record }">
              {{
                record?.deadline
                  ? dayjs(record?.deadline).format("YYYY/MM/DD")
                  : "-"
              }}
            </template>
          </TableColumn>
          <TableColumn dataIndex="submitTime" title="提交时间">
            <template #default="{ record }">
              {{
                record?.submitTime
                  ? dayjs(record?.submitTime).format("YYYY/MM/DD HH:mm")
                  : "-"
              }}
            </template>
          </TableColumn>
          <TableColumn dataIndex="status" title="状态">
            <template #default="{ record }">
              <Tag :bordered="false" color="success" v-if="record?.status == 2">
                已批改
              </Tag>
              <Tag
                :bordered="false"
                color="processing"
                v-if="record?.status == 1"
              >
                已提交
              </Tag>
              <Tag :bordered="false" color="error" v-if="record?.status == 0">
                未提交
              </Tag>
            </template>
          </TableColumn>
          <TableColumn dataIndex="score" title="成绩">
            <template #default="{ record }">
              {{ record?.score ?? "-" }}
            </template>
          </TableColumn>
        </Table>
      </div>
    </div>
  </Drawer>
</template>
<script setup>
import { CloseOutlined } from "@ant-design/icons-vue";
import {
  Drawer,
  Progress,
  Table,
  TableColumn,
  Empty,
  Tag,
} from "ant-design-vue";
import { ref } from "vue";
import kczhjdIcon from "@/assets/images/kczhjd.png";
import zytjlIcon from "@/assets/images/zytjl.png";
import swhysjIcon from "@/assets/images/swhysj.png";
import { getLearningDetail } from "@/api/progress";
import { useRoute } from "vue-router";
import dayjs from "dayjs";

const open = ref(false);
const route = useRoute();
const learningDetail = ref({});

const cards = ref([
  {
    img: kczhjdIcon,
    label: "课程综合进度",
    value: "0%",
    color: "tab1",
  },
  {
    img: zytjlIcon,
    label: "作业提交率",
    value: 0,
    color: "tab2",
  },
  {
    img: swhysjIcon,
    label: "思维活跃时间",
    value: "-",
    color: "tab3",
  },
]);

const tab = ref(1);

const changeTab = (key) => {
  tab.value = key;
};

defineExpose({
  openDrawer: (e = {}) => {
    open.value = true;
    getLearningDetail({
      courseId: route.query?.id,
      studentId: e.studentId,
    }).then((res) => {
      learningDetail.value = res?.data;
      cards.value[0].value = (res?.data?.courseProgress || 0) + "%";
      cards.value[1].value = res?.data?.assignmentSubmitRate || 0;
      cards.value[2].value = res?.data?.lastActiveTime || "-";
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
