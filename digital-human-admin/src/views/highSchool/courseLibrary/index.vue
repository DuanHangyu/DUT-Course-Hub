<template>
  <div class="app-container flex flex-col overflow-hidden !px-0">
    <Tabs
      v-model:active-key="state"
      class="shrink-0 ml-4"
      @change="changeStatus"
    >
      <TabPane
        :tab="`已上架（${statistic?.publishedCount || 0}）`"
        :key="1"
      ></TabPane>
      <TabPane
        :tab="`已下架（${statistic?.unpublishedCount || 0}）`"
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
      <Dropdown>
        <Button type="primary" class="flex items-center justify-center">
          <span>新增</span>
          <DownOutlined />
        </Button>
        <template #overlay>
          <Menu @click="commandFn">
            <MenuItem :key="1"> 从模板新建 </MenuItem>
            <MenuItem :key="2"> 全新创建 </MenuItem>
          </Menu>
        </template>
      </Dropdown>
      <Form
        layout="inline"
        :model="params"
        class="flex flex-wrap"
        :colon="false"
      >
        <FormItem label="课程名称">
          <Input
            v-model:value="params.courseName"
            placeholder="请输入"
            @keydown.enter="onLoad"
          />
        </FormItem>
      </Form>
    </div>
    <div class="grow overflow-y-auto relative">
      <div
        class="size-full flex items-center justify-center"
        v-if="!list?.length || loading"
      >
        <Spin :spinning="loading" v-if="loading" />
        <Empty v-if="!list.length && !loading" />
      </div>
      <div class="grid grid-cols-2 gap-4 p-4" v-if="list.length">
        <div
          class="card flex gap-x-5 relative"
          v-for="item in list"
          :key="item?.id"
        >
          <div
            class="absolute top-3 right-3 w-10 leading-[22px] bg-[#FFF7E6] rounded-md border border-solid border-[#FFD591] text-xs text-[#FA8C16] text-center"
            v-if="item?.isFeatured == 1"
          >
            精选
          </div>
          <div class="w-[255px] shrink-0">
            <div class="relative">
              <img
                :src="item?.pictureUrlSigned"
                class="w-full h-[153px] rounded-[8px]"
                alt=""
              />
              <div
                class="flex items-center justify-between px-6 action absolute left-0 bottom-0 text-white text-sm font-medium"
              >
                <a @click="updateFn(item)">编辑</a>
                <a @click="setJx(item)">{{
                  item?.isFeatured == 1 ? "取消精选" : "设为精选"
                }}</a>
                <a @click="delFn(item)">删除</a>
              </div>
            </div>
            <div class="grid grid-cols-2 gap-x-2 mt-2">
              <div class="btn" @click="goDetail(item)">进入课程</div>
              <div class="btn2" @click="addStudentOrClass(item)">
                添加学生/班级
              </div>
            </div>
          </div>
          <div class="grow overflow-hidden space-y-1">
            <div class="text-lg font-medium text-[rgba(10,8,26,0.88)]">
              {{ item?.courseName }}
            </div>
            <div class="w-full truncate text-sm text-[rgba(10,8,26,0.45)]">
              {{ item?.courseIntroduce }}
            </div>
            <div class="line !mt-2">
              <span class="label">授课老师：</span>
              <span class="content">{{ item?.teacherName }}</span>
            </div>
            <div class="line">
              <span class="label">学生人数：</span>
              <span class="content">{{ item?.studentCount }}</span>
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
            <div class="line mt-2">
              <span class="label">邀请码：</span>
              <span class="content">{{ item?.inviteCode }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <HandleModal ref="handleRef" @success="onLoad()" />
    <TemplateLib ref="temRef" @success="onLoad()" />
    <AddStudent ref="addStuRef" @success="onLoad()" />
  </div>
</template>
<script setup>
import { nextTick, onMounted, ref } from "vue";
import {
  Tabs,
  TabPane,
  Button,
  Form,
  FormItem,
  Input,
  Dropdown,
  Menu,
  MenuItem,
  Empty,
  Spin,
  message,
  Modal,
} from "ant-design-vue";
import HandleModal from "./components/HandleModal.vue";
import { DownOutlined } from "@ant-design/icons-vue";
import TemplateLib from "./components/TemplateLib.vue";
import { useRouter } from "vue-router";
import * as api from "@/api/courseLibrary";
import useUserStore from "@/store/modules/user";
import AddStudent from "./components/AddStudent.vue";

const state = ref(1);
const tab = ref(0);
const list = ref([]);
const params = ref({ page: 1, size: 2000 });
const handleRef = ref();
const temRef = ref();
const router = useRouter();
const loading = ref(false);
const statistic = ref({});
const subjectList = ref(["全部"]);
const userStore = useUserStore();
const addStuRef = ref();
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

const onLoad = () => {
  loading.value = true;
  const p = {
    ...params.value,
    state: state.value,
    subject:
      subjectList.value[tab.value] == "全部"
        ? ""
        : subjectList.value[tab.value],
    schoolId: userStore.schoolId,
  };
  api
    .getList(p)
    .then((res) => {
      list.value = res?.data?.records || [];
    })
    .finally(() => {
      loading.value = false;
    });
  api.getStats({ schoolId: userStore.schoolId }).then((res) => {
    statistic.value = res?.data || {};
  });
  api.getSubjectList({ schoolId: userStore.schoolId }).then((res) => {
    subjectList.value = ["全部", ...(res?.data || [])];
  });
};

const commandFn = (e) => {
  if (e.key == 1) {
    temRef.value?.openModal({ state: state.value == 1 ? true : false });
  }
  if (e.key == 2) {
    handleRef.value?.openModal({ state: state.value == 1 ? true : false });
  }
};

const goDetail = (e) => {
  router.push(`/courseLibraryDetail?id=${e?.id}`);
};

const updateFn = (e) => {
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
      api.del(e?.id).then(() => {
        onLoad();
        message.success("已删除");
      });
    },
  });
};

const setJx = (e) => {
  api
    .featuredApi({
      courseId: e?.id,
      isFeatured: e?.isFeatured == 1 ? 0 : 1,
    })
    .then(() => {
      onLoad();
      message.success(e?.isFeatured == 1 ? "取消精选" : "已设为精选");
    });
};

const addStudentOrClass = (e) => {
  addStuRef.value.openModal(e);
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
  .btn2 {
    width: 100%;
    line-height: 32px;
    background: #ffffff;
    border-radius: 6px 6px 6px 6px;
    border: 1px solid rgba(0, 0, 0, 0.15);
    font-weight: 400;
    font-size: 14px;
    color: rgba(10, 8, 26, 0.88);
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

    .btn2 {
      border: 1px solid #1977ff;
      color: #1977ff;
    }

    .action {
      opacity: 1;
    }
  }

  .line {
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
