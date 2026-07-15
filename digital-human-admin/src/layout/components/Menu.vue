<template>
  <div
    class="coll flex items-center justify-center cursor-pointer fixed left-0 bottom-[65px]"
    v-if="isColl"
    @click="setColl(false)"
  >
    <img src="@/assets/screen/shouqi.png" class="size-6" alt="" />
  </div>
  <div v-else class="w-[220px] menu overflow-hidden flex flex-col">
    <div class="w-full h-[86px] flex items-center justify-center shrink-0">
      <img src="@/assets/images/logo.png" class="w-[135px] h-[61px]" alt="" />
    </div>
    <div
      class="p-4 pt-0 shrink-0"
      v-if="userStore?.isSchoolMode == 1 && userStore?.info?.role == 0"
    >
      <Tooltip
        placement="bottomLeft"
        :arrow="false"
        color="#fff"
        v-model:open="open"
        trigger="click"
      >
        <template #title>
          <div class="card22">
            <Input placeholder="搜索学校" v-model:value="schoolName">
              <template #prefix>
                <search-outlined />
              </template>
            </Input>
            <div class="mt-2">
              <section
                class="leading-8 cursor-pointer rounded-md px-2 text-sm text-[rgba(0,0,0,0.88);] hover:bg-[rgba(0,0,0,0.04)]"
                v-for="item in filteredList"
                :key="item?.id"
                :class="
                  item?.id == userStore?.schoolId ? 'bg-[rgba(0,0,0,0.04)]' : ''
                "
                @click="setSchool(item)"
              >
                {{ item?.schoolName }}
              </section>
            </div>
          </div>
        </template>
        <div class="h-[51px] rounded-lg bg-[#F0FBFF] py-1 px-3">
          <div class="text-xs text-[rgba(10,8,26,0.45)]">当前学校</div>
          <div
            class="text-sm text-[rgba(10,8,26,0.88)] flex items-center justify-between mt-1"
            @click="open = true"
          >
            <span>
              {{
                schoolList?.find((item) => item?.id == userStore?.schoolId)
                  ?.schoolName
              }}
            </span>
            <svg-icon
              icon-class="arrow"
              class="text-[#1977FF] text-xs cursor-pointer"
              :class="open ? 'rotate-180' : ''"
            />
          </div>
        </div>
      </Tooltip>

      <div class="h-[1px] w-full bg-[rgba(0,0,0,0.15)] mt-4"></div>
    </div>
    <div
      class="p-4 pt-0 shrink-0"
      v-if="userStore?.info?.role == 1 || userStore?.info?.role == 2"
    >
      <div class="h-[51px] rounded-lg bg-[#F0FBFF] py-1 px-3">
        <div class="text-xs text-[rgba(10,8,26,0.45)]">当前学校</div>
        <div class="text-sm text-[rgba(10,8,26,0.88)] mt-1">
          <span>
            {{ userStore?.info?.school?.schoolName }}
          </span>
        </div>
      </div>
      <div class="h-[1px] w-full bg-[rgba(0,0,0,0.15)] mt-4"></div>
    </div>

    <div class="space-y-[18px] px-4 overflow-y-auto grow">
      <section
        class="flex items-center space-x-3 pl-[14px] menu-item cursor-pointer"
        v-for="(item, index) in list"
        :key="index"
        :class="
          item?.key?.includes($route.meta?.activeMenu)
            ? 'active-menu-item'
            : ' '
        "
        @click="goPage(item)"
      >
        <svg-icon
          :icon-class="item.icon"
          class="text-[20px]"
          :class="
            item?.key?.includes($route.meta?.activeMenu)
              ? 'text-[#fff]'
              : 'text-[#B2B8C2]'
          "
        />
        <div class="text-lg font-[500]">
          {{ item.label }}
        </div>
      </section>
    </div>
    <div
      class="flex items-center space-x-1 shrink-0 mb-3 justify-center cursor-pointer"
      @click="setColl(true)"
      v-if="['dataOverview'].includes($route.name)"
    >
      <img src="@/assets/screen/shouqi.png" class="size-6" alt="" />
      <span class="text-sm text-[rgba(10,8,26,0.88)]">收起面板</span>
    </div>
    <div
      class="h-[54px] flex items-center justify-center text-sm text-[#1977FF] cursor-pointer bottom2 shrink-0"
      @click="swicthSchool"
      v-if="userStore?.info?.role == 0 && userStore?.isSchoolMode != 1"
    >
      <span>大学端（总部）</span>
      <svg-icon icon-class="switch" class="text-[16px]" />
    </div>
  </div>
</template>
<script setup>
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import useUserStore from "@/store/modules/user";
import { Tooltip, Input } from "ant-design-vue";
import { SearchOutlined } from "@ant-design/icons-vue";
import { highSchoolRoutes, universityRoutes } from "@/router/index";
import * as schoolApi from "@/api/middleSchoolManage";
import { mittEmit } from "../../utils/appMitt";
import qs from "qs";

const router = useRouter();
const userStore = useUserStore();
const open = ref(false);

const isColl = ref(false);
const schoolList = ref([]);
const schoolName = ref("");

const filteredList = computed(() => {
  if (!schoolName.value) {
    return schoolList.value;
  }
  return schoolList.value.filter((item) =>
    item?.schoolName?.includes(schoolName.value),
  );
});

const teacherManage = [
  {
    label: "校本课程库",
    icon: "courseConstruction",
    key: ["teacher-courseLibrary"],
    path: "/teacher-courseLibrary",
  },
];

const highSchool = [
  {
    label: "数据概览",
    icon: "dataOverview",
    key: ["highSchool-dataOverview"],
    path: "/highSchool-dataOverview",
    permission: "dataOverview",
  },
  {
    label: "校本课程库",
    icon: "courseConstruction",
    key: ["highSchool-courseLibrary"],
    path: "/highSchool-courseLibrary",
    permission: "courseLibrary",
  },
  {
    label: "班级架构",
    icon: "classStructure",
    key: ["highSchool-classStructure"],
    path: "/highSchool-classStructure",
    permission: "classStructure",
  },
  {
    label: "学生管理",
    icon: "studentManage",
    key: ["highSchool-studentManage"],
    path: "/highSchool-studentManage",
    permission: "studentManage",
  },
  {
    label: "教师管理",
    icon: "teacherManage",
    key: ["highSchool-teacherManage"],
    path: "/highSchool-teacherManage",
    permission: "teacherManage",
  },
  {
    label: "学科配置",
    icon: "curriculumConfig",
    key: ["highSchool-curriculumConfig"],
    path: "/highSchool-curriculumConfig",
    permission: "curriculumConfig",
  },
];

const university = [
  {
    label: "数据概览",
    icon: "dataOverview",
    key: ["university-dataOverview"],
    path: "/university-dataOverview",
  },
  {
    label: "课程建设",
    icon: "courseConstruction",
    key: ["university-courseConstruction"],
    path: "/university-courseConstruction",
  },
  {
    label: "中学管理",
    icon: "middleSchoolManage",
    key: ["university-middleSchoolManage"],
    path: "/university-middleSchoolManage",
  },
  {
    label: "首页配置",
    icon: "homeConfig",
    key: ["university-homeConfig"],
    path: "/university-homeConfig",
  },
];

const list = computed(() => {
  if (userStore.info?.role === 0) {
    if (userStore.isSchoolMode == 1) {
      return highSchool;
    } else {
      return university;
    }
  } else if (userStore.info?.role === 1) {
    return highSchool?.filter((item) =>
      userStore?.info?.authModules?.includes(item?.permission),
    );
  } else if (userStore.info?.role === 2) {
    return teacherManage;
  }
});

const goPage = (e) => {
  router.push(e?.path);
};

watch(
  () => router.currentRoute.value.path,
  () => {
    isColl.value = false;
  },
);

const swicthSchool = async () => {
  // if (userStore.isSchoolMode == 1) {
  //   userStore.setSchoolMode(0);
  //   router.removeRoute(highSchoolRoutes);
  //   router.addRoute(universityRoutes);
  // } else {
  //   userStore.setSchoolMode(1);
  //   router.removeRoute(universityRoutes);
  //   router.addRoute(highSchoolRoutes);
  // }
  await schoolApi
    .getList({ page: 1, size: 5000 })
    .then((res) => {
      schoolList.value = res.data?.records || [];
      const sid = schoolList.value?.[0]?.id;
      // userStore.setSchoolId(sid);
      // sessionStorage.setItem("schoolId", sid);
      window.open(
        `${location.origin}/highSchool-dataOverview?schoolId=${sid}&isSchoolMode=1`,
        "_blank",
      );
    })
    .finally(() => {
      router.push("/");
    });
};

const setSchool = (e) => {
  sessionStorage.setItem("schoolId", e?.id);
  userStore.setSchoolId(e?.id);
  // let url = `${location.origin}${location.pathname}`;
  // const p = router.currentRoute.value.query;
  // p.schoolId = e?.id;
  // p.isSchoolMode = 1;
  // location.replace(`${url}?${qs.stringify(p)}`);
};

const setColl = (e) => {
  isColl.value = e;
  nextTick(() => {
    mittEmit("refresh");
  });
};

onMounted(async () => {
  if (userStore.isSchoolMode == 1) {
    await schoolApi.getList({ page: 1, size: 5000 }).then((res) => {
      schoolList.value = res.data?.records || [];
      const id =
        sessionStorage.getItem("schoolId") || schoolList.value?.[0]?.id;
      userStore.setSchoolId(id);
    });
  }
});
</script>
<style scoped lang="less">
.menu {
  background: linear-gradient(180deg, #cde7fd 0%, #ffffff 6.73%);
  border-right: 1px solid rgba(29, 29, 29, 0.12);
}
.menu-item {
  height: 48px;
  border-radius: 12px 12px 12px 12px;
  font-weight: 500;
  font-size: 16px;
  color: rgba(10, 8, 26, 0.88);
}
.active-menu-item {
  background: linear-gradient(90deg, #3ccbff 0%, #1977ff 100%);
  box-shadow: 0px 2px 32px 0px rgba(0, 0, 0, 0.02);
  color: #ffffff;
}
.bottom2 {
  position: relative;
  &::after {
    content: "";
    display: block;
    position: absolute;
    top: 0;
    left: 24px;
    border-top: 1px solid rgba(0, 0, 0, 0.15);
    width: calc(100% - 48px);
  }
}

.card22 {
  background: #ffffff;
}

.coll {
  width: 30px;
  height: 30px;
  background: linear-gradient(0deg, #1d418d 0%, rgba(29, 65, 141, 0.3) 100%);
  border-radius: 0px 4px 4px 0px;
  img {
    width: 24px;
    height: 24px;
  }
}
</style>
