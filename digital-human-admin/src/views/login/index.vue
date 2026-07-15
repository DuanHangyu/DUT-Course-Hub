<template>
  <div
    class="w-screen h-screen login-container overflow-hidden flex items-center justify-center"
    :style="`background-size:${size ? '100% auto' : 'auto 100%'}`"
  >
    <div class="absolute top-20 left-[91px] flex items-center">
      <img
        src="@/assets/images/login-logo-1.png"
        class="w-[297px] h-[92px] scale-y-[3.4]"
        alt=""
      />
      <div class="w-[3px] h-[48px] bg-[#4B84FF] ml-3"></div>
      <img
        src="@/assets/images/login-logo-2.png"
        class="w-[194px] h-[65px] ml-8"
        alt=""
      />
    </div>
    <img
      src="@/assets/images/login-icon.png"
      class="size-[600px] mr-[153px] mt-20"
      alt=""
    />
    <div class="login-box flex flex-col items-center justify-center px-[54px]">
      <h1 class="mb-10 flex items-center">
        未来技术学院<span class="text-[#4B84FF] relative">
          管理
          <img
            src="@/assets/images/login-tip.png"
            class="w-[38px] h-[30px] absolute top-[-7px] right-[-10px]"
            alt=""
          />
        </span>
        系统
      </h1>
      <Form ref="formRef" :model="data" class="w-full">
        <FormItem name="account" label="">
          <Input
            v-model:value.trim="data.account"
            class="w-full"
            placeholder="请输入账号"
            @keydown.enter.native="submitLogin"
          />
        </FormItem>
        <FormItem name="password" label="">
          <Input
            type="password"
            v-model:value.trim="data.password"
            show-password
            placeholder="请输入密码"
            @keydown.enter.native="submitLogin"
          />
        </FormItem>
        <FormItem class="text-center">
          <Button
            @click="submitLogin"
            :loading="loading"
            :class="data.account && data.password ? 'active' : 'default'"
          >
            登录
          </Button>
        </FormItem>
      </Form>
      <div
        class="flex items-center space-x-2 justify-center mt-5 text-sm text-[#4B84FF] font-normal cursor-pointer"
        @click="toStudent"
      >
        <svg-icon
          icon-class="currentColor"
          class="text-lg text-[#4B84FF]"
          alt=""
        />
        <span>跳转学生端</span>
      </div>
    </div>
  </div>
</template>
<script setup>
import { Form, FormItem, Input, Button } from "ant-design-vue";
import { ref } from "vue";
import { useRouter } from "vue-router";
import useUserStore from "@/store/modules/user";
import { onMounted } from "vue";
import { onUnmounted } from "vue";

const loading = ref(false);
const data = ref({
  account: "",
  password: "",
});
const formRef = ref();
const router = useRouter();
const submitLogin = () => {
  if (!data.value.account && !data.value.password) {
    return;
  }
  formRef.value.validate()?.then(() => {
    loading.value = true;
    useUserStore()
      ?.login(data.value)
      .then((e) => {
        router.replace("/");
      })
      .finally(() => {
        loading.value = false;
      });
  });
};

const size = ref(window.innerWidth / window.innerHeight > 2880 / 1800);
const handleResize = () => {
  size.value = window.innerWidth / window.innerHeight > 2880 / 1800;
};

onMounted(() => {
  window.addEventListener("resize", handleResize);
});
onUnmounted(() => {
  window.removeEventListener("resize", handleResize);
});

const toStudent = () => {
  window.open("https://learn.deepolylink.com", "_blank");
};
</script>
<style scoped lang="less">
.login-container {
  background: url(@/assets/images/login-bg.png);
  background-size: 100% auto;
  background-repeat: no-repeat;
  background-position: center center;
}
// /* 如果容器宽 > 高，则 background-size: 100% auto */
// @media (min-aspect-ratio: 1/1) {
//   .login-container {
//     background-size: 100% auto !important;
//   }
// }

// /* 如果容器高 > 宽，则 background-size: auto 100% */
// @media (max-aspect-ratio: 1/1) {
//   .login-container {
//     background-size: auto 100% !important;
//   }
// }

.login-box {
  width: 448px;
  height: 523px;
  background: rgba(255, 255, 255, 0.5);
  box-shadow: 0px 4px 30px 0px rgba(75, 132, 255, 0.2);
  border-radius: 30px 30px 30px 30px;
  // position: absolute;
  // top: 180px;
  // right: 180px;
  h1 {
    font-weight: 500;
    font-size: 30px;
    color: #333333;
  }
}

:deep(.ant-input) {
  height: 50px;
  border-radius: 100px 100px 100px 100px;
  border: 1px solid rgba(0, 0, 0, 0.2);
  padding: 0px 18px;
  box-shadow: initial;
  font-size: 16px;
  &::placeholder {
    font-weight: 500;
    font-size: 16px;
    color: #999999;
  }
}
:deep(.ant-btn) {
  border-radius: 100px 100px 100px 100px;
  height: 50px;
  width: calc(100% - 34px);
  margin: 0 auto;
  margin-top: 30px;
  font-size: 20px;
  font-weight: bold;
  &:hover {
    background: #4b84ff;
    color: #ffffff;
  }
}

.default {
  background: #dbdbdb;
  color: #666666;
}
.active {
  background: #4b84ff;
  color: #ffffff;
}
</style>
