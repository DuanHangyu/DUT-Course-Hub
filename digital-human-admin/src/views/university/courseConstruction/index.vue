<template>
  <div class="app-container flex flex-col overflow-hidden !px-0">
    <Tabs
      v-model:active-key="state"
      class="shrink-0 ml-4"
      @change="changeStatus"
    >
      <TabPane
        :tab="`已上架（${statistic?.onlineCount || 0}）`"
        :key="1"
      ></TabPane>
      <TabPane
        :tab="`已下架（${statistic?.offlineCount || 0}）`"
        :key="0"
      ></TabPane>
    </Tabs>
    <div class="flex gap-2 shrink-0 px-4">
      <div
        class="tab"
        v-for="(item, index) in subjectList"
        :key="index"
        @click="changeTab(index)"
        :class="tab == index ? 'activeTab' : ''"
      >
        {{ item }}
      </div>
    </div>
    <div class="flex items-center justify-between mt-4 shrink-0 px-4">
      <Button type="primary" class="searchBtn" @click="addFn"> 新增 </Button>
      <Form layout="inline" :model="params" :colon="false">
        <FormItem label="课程名称" name="courseName">
          <Input
            v-model:value="params.courseName"
            @keydown.enter="onLoad"
            placeholder="请输入"
          />
        </FormItem>
      </Form>
    </div>
    <div class="grow overflow-y-auto">
      <div
        class="size-full flex items-center justify-center"
        v-if="!list?.length || loading"
      >
        <Spin :spinning="loading" v-if="loading" />
        <Empty v-if="!list.length && !loading" />
      </div>
      <div class="grid grid-cols-2 gap-4 p-4" v-if="list.length">
        <div
          class="card flex space-x-5"
          v-for="(item, index) in list"
          :key="index"
        >
          <div class="shrink-0">
            <div
              class="w-[220px] h-[132px] rounded-lg overflow-hidden relative"
            >
              <img :src="item?.pictureUrlSigned" class="size-full" alt="" />
              <div
                class="flex items-center justify-between px-14 action absolute left-0 bottom-0 text-white text-sm font-medium"
              >
                <a @click="editFn(item)">编辑</a>
                <a @click="delFn(item)">删除</a>
              </div>
            </div>

            <div class="mt-2 btn" @click="goDetail(item)">进入课程</div>
          </div>
          <div class="grow overflow-hidden">
            <div class="text-lg font-medium text-[rgba(10,8,26,0.88)]">
              {{ item?.courseName }}
            </div>
            <div class="w-full truncate text-sm text-[rgba(10,8,26,0.45)]">
              {{ item?.courseIntroduce }}
            </div>
            <div class="line mt-2 flex items-center">
              <span class="label">授课老师：</span>
              <div class="content flex items-center space-x-2">
                <img
                  :src="item?.headUrlSigned"
                  class="w-[18px] h-[18px] rounded-full"
                  alt=""
                />
                <span>{{ item?.teacherName }}</span>
              </div>
            </div>
            <div class="line">
              <span class="label">所属学科：</span>
              <span class="content">{{ item?.subject }}</span>
            </div>
            <div class="line">
              <span class="label">创建人：</span>
              <span class="content">{{ item?.createName }}</span>
            </div>
            <div class="line">
              <span class="label">创建时间：</span>
              <span class="content">{{ item?.createTime }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <HandleModal ref="handleRef" @success="onLoad()" />
  </div>
</template>
<script setup>
import { nextTick, onMounted, ref } from "vue";
import HandleModal from "./components/HandleModal.vue";
import {
  Tabs,
  TabPane,
  Input,
  Form,
  FormItem,
  Button,
  Modal,
  message,
  Empty,
  Spin,
} from "ant-design-vue";
import { useRouter } from "vue-router";
import {
  getList,
  del,
  getSubjectList,
  getCount,
} from "@/api/courseConstruction";

const state = ref(1);
const tab = ref(0);
const params = ref({ page: 1, size: 2000 });
const handleRef = ref();
const router = useRouter();
const list = ref([]);
const loading = ref(false);
const statistic = ref({});
const subjectList = ref(["全部"]);
const changeTab = (index) => {
  tab.value = index;
  onLoad();
};

const changeStatus = () => {
  nextTick(() => {
    params.value.page = 1;
    onLoad();
  });
};

const addFn = () => {
  handleRef.value?.openModal();
};

const editFn = (e) => {
  handleRef.value?.openModal(e);
};

const goDetail = (e) => {
  router.push(`/university-courseConstructionDetail?id=${e?.id}`);
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
      del({
        id: e?.id,
      }).then(() => {
        onLoad();
        message.success("已删除");
      });
    },
  });
};

const onLoad = () => {
  loading.value = true;
  const p = {
    ...params.value,
    state: state.value,
    subject:
      subjectList.value[tab.value] == "全部"
        ? ""
        : subjectList.value[tab.value],
  };
  getList(p)
    .then((res) => {
      list.value = res?.data?.records || [];
    })
    .finally(() => {
      loading.value = false;
    });
  getCount().then((res) => {
    statistic.value = res?.data || {};
  });
  getSubjectList().then((res) => {
    subjectList.value = ["全部", ...(res?.data || [])];
  });
};
onMounted(() => {
  onLoad();
});
</script>
<style scoped lang="less">
.tab {
  padding: 0 16px;
  line-height: 28px;
  background: #eff1f4;
  border-radius: 100px 100px 100px 100px;
  font-weight: 400;
  font-size: 14px;
  color: rgba(10, 8, 26, 0.75);
  cursor: pointer;
}
.activeTab {
  background: #e8f1ff;
  border-radius: 100px 100px 100px 100px;
  border: 1px solid #1977ff;
  font-weight: 500;
  font-size: 14px;
  color: #1977ff;
}

.card {
  width: 100%;
  background: #ffffff;
  box-shadow: 0px 8px 16px 0px rgba(0, 0, 0, 0.06);
  border-radius: 12px 12px 12px 12px;
  border: 1px solid rgba(0, 0, 0, 0.15);
  padding: 12px;
  transition: transform 0.3s ease;
  .btn {
    width: 100%;
    line-height: 32px;
    background: #ffffff;
    border-radius: 6px 6px 6px 6px;
    border: 1px solid #1977ff;
    font-weight: 400;
    font-size: 14px;
    color: #1977ff;
    text-align: center;
    cursor: pointer;
  }
  .action {
    width: 100%;
    height: 40px;
    background: linear-gradient(
      180deg,
      rgba(0, 0, 0, 0) 0%,
      rgba(0, 0, 0, 0.7) 100%
    );
    border-radius: 0px 0px 8px 8px;
    opacity: 0;
    a {
      cursor: pointer;
    }
  }
  &:hover {
    transform: scale(1.02);
    box-shadow: 0px 8px 16px 0px rgba(0, 0, 0, 0.16);

    .btn {
      background: #1977ff;
      color: #fff;
    }

    .action {
      opacity: 1;
    }
  }

  .line {
    line-height: 24px;
    .label {
      font-weight: 400;
      font-size: 14px;
      color: rgba(10, 8, 26, 0.45);
    }
    .content {
      font-weight: 400;
      font-size: 14px;
      color: rgba(10, 8, 26, 0.88);
    }
  }
}
</style>
