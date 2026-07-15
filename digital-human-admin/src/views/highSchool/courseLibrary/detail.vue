<template>
  <div class="app-container flex flex-col overflow-hidden !p-0">
    <section class="shrink-0 px-6 py-4 pb-0 bg-white">
      <Button
        type="link"
        class="flex items-center space-x-1 text-sm text-[rgba(10,8,26,0.88)] w-fit !px-0"
        @click="$router.back()"
      >
        <LeftOutlined />
        <span>返回</span>
      </Button>
      <div class="mt-2 card flex items-center space-x-[10px]">
        <img src="@/assets/images/kcxq.png" class="size-8" alt="" />
        <span class="text-lg font-medium text-[rgba(10,8,26,0.88)]">
          当前课程：{{ detail?.courseName }}
        </span>
      </div>
      <Tabs v-model:active-key="activeName" class="mt-2">
        <TabPane tab="学情监控看板" :key="1"></TabPane>
        <TabPane tab="课程内容管理" :key="2"></TabPane>
        <TabPane tab="学生进度追踪" :key="3"></TabPane>
        <TabPane tab="作业管理" :key="4"></TabPane>
        <TabPane tab="教师答疑" :key="5"></TabPane>
      </Tabs>
    </section>
    <section
      class="grow bg-[#F7F8FA] overflow-hidden"
      :class="activeName == 1 ? 'p-0' : 'p-4'"
    >
      <div
        class="bg-white flex flex-col overflow-hidden size-full"
        :class="activeName == 1 ? '' : 'rounded-xl'"
      >
        <Tab1 v-if="activeName == 1" />
        <Tab2 v-if="activeName == 2" />
        <Tab3 v-if="activeName == 3" />
        <Tab4 v-if="activeName == 4" />
        <Tab5 v-if="activeName == 5" />
      </div>
    </section>
  </div>
</template>
<script setup>
import { LeftOutlined } from "@ant-design/icons-vue";
import { Button, Tabs, TabPane } from "ant-design-vue";
import { onMounted, ref } from "vue";
import Tab1 from "./Tab1/index.vue";
import Tab2 from "./Tab2/index.vue";
import Tab3 from "./Tab3/index.vue";
import Tab4 from "./Tab4/index.vue";
import Tab5 from "./Tab5/index.vue";
import { getDetail } from "@/api/courseLibrary";
import { useRoute } from "vue-router";
const activeName = ref(1);
const route = useRoute();
const detail = ref({});
onMounted(() => {
  getDetail(route.query?.id).then((res) => {
    detail.value = res?.data || {};
  });
});
</script>
<style scoped lang="less">
.card {
  width: 100%;
  padding: 16px 12px;
  background: linear-gradient(90deg, rgba(209, 242, 255, 0.4) 0%, #d1f2ff 100%);
  border-radius: 12px 12px 12px 12px;
}
:deep(.ant-tabs-nav) {
  margin-bottom: 2px;
  &::before {
    display: none;
  }
}
</style>
