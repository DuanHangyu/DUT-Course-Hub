<template>
  <div
    class="topBanner h-28 absolute top-0 left-0 w-full flex items-center justify-between px-6"
  >
    <div class="w-59 h-16"></div>
    <img
      src="@/assets/home/logo.png"
      class="w-auto h-15.25 absolute left-1/2 top-1/2 -translate-1/2"
      alt=""
    />
    <el-dropdown
      @command="commandFn"
      :show-arrow="false"
      trigger="click"
      popper-class="logoutBox"
      :popper-options="{
        offset: 0,
      }"
    >
      <div class="flex items-center space-x-2.5 shrink-0 cursor-pointer">
        <Avatar class="size-8!" />
        <div class="text-base text-[rgba(255,255,255,0.88)]">
          {{ userStore?.user?.studentName }}
        </div>
        <el-icon size="12px" color="#fff"><ArrowDownBold /></el-icon>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item :command="1">修改密码</el-dropdown-item>
        </el-dropdown-menu>
        <el-dropdown-menu>
          <el-dropdown-item :command="2">退出登录</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
    <ChangePwdModal ref="cRef" />
  </div>
</template>
<script setup>
import useUserStore from "@/store/modules/user";
import { useRouter } from "vue-router";
import ChangePwdModal from "./ChangePwdModal.vue";
import { ref } from "vue";
import { ArrowDownBold } from "@element-plus/icons-vue";
import Avatar from "./Avatar.vue";

const userStore = useUserStore();

const router = useRouter();
const cRef = ref();
const commandFn = (e) => {
  if (e == 1) {
    cRef.value?.openModal();
  }
  if (e == 2) {
    userStore?.logOut().then(() => {
      router.replace("/login");
    });
  }
};
</script>
<style scoped lang="scss">
.topBanner {
  background: linear-gradient(180deg, #172f49 0%, rgba(111, 136, 164, 0) 100%);
}
:focus-visible {
  outline: initial;
}
</style>
<style lang="scss">
.logoutBox {
  background: #ffffff;
  box-shadow: 0px 4px 4px 0px rgba(0, 0, 0, 0.12);
  border-radius: 8px;
  overflow: hidden !important;
  padding: 4px !important;
  .el-dropdown-menu {
    margin: 0;
    padding: 0;
  }
  .el-dropdown-menu__item {
    &:hover {
      background: rgba(0, 0, 0, 0.04);
      border-radius: 6px 6px 6px 6px;
      font-weight: 400;
      font-size: 14px;
      color: rgba(0, 0, 0, 0.88);
    }
    &:focus {
      background: rgba(0, 0, 0, 0.04);
      color: rgba(0, 0, 0, 0.88);
    }
  }
}
</style>
