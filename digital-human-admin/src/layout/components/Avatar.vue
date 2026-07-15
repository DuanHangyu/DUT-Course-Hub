<template>
  <Dropdown>
    <div class="flex items-center space-x-[10px] flex-shrink-0 userInfo">
      <img
        :src="userStore?.avatar"
        class="size-8 rounded-full overflow-hidden"
        alt=""
      />
      <div class="text-sm" :style="{ color: color }">
        {{ userStore?.info?.userName }}
      </div>
      <svg-icon icon-class="arrow" class="text-[#B2B8C2] text-base" />
    </div>
    <template #overlay>
      <Menu @click="commandFn">
        <MenuItem :key="1"> 修改密码 </MenuItem>
        <MenuItem :key="2"> 退出登录 </MenuItem>
      </Menu>
    </template>
  </Dropdown>
  <ChangePwdModal ref="cRef" />
</template>
<script setup>
import useUserStore from "@/store/modules/user";
import { useRouter } from "vue-router";
import ChangePwdModal from "./ChangePwdModal.vue";
import { ref } from "vue";
import { Dropdown, Menu, MenuItem } from "ant-design-vue";

defineProps({
  color: {
    type: String,
    default: "rgba(10,8,26,0.88)",
  },
});

const userStore = useUserStore();

const router = useRouter();
const cRef = ref();
const commandFn = (e) => {
  if (e?.key == 1) {
    cRef.value?.openModal();
  }
  if (e?.key == 2) {
    userStore?.logOut().then(() => {
      router.replace("/login");
    });
  }
};
</script>
<style scoped lang="less">
:focus-visible {
  outline: initial;
}
</style>
