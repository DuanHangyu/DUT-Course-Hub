<template>
  <div class="login relative bg-red flex items-center justify-center">
    <img
      src="@/assets/images/logo-1.png"
      class="w-120 h-15.5 absolute top-10 left-10"
    />
    <div class="flex justify-center min-w-305 m-auto">
      <div class="mt-10">
        <img src="@/assets/images/logo-2.png" alt="" class="w-125.25 h-16.75" />
      </div>
      <div class="right">
        <div
          class="font-medium text-[26px] text-[#333] leading-12.5 mb-10 relative"
        >
          账号<span style="color: #524fffff">登录</span>
          <img
            src="@/assets/images/log-1.png"
            alt=""
            class="absolute w-9.5 h-7.5 -right-11.25 bottom-7.5"
          />
        </div>
        <el-select
          placeholder="请选择学校"
          :options="schoolList"
          v-model="schoolId"
          :props="{
            label: 'schoolName',
            value: 'id',
          }"
          clearable
        />
        <input
          v-model="account"
          type="text"
          placeholder="请输入账号"
          class="input-style"
          @keydown.enter="toPath"
        />

        <div class="eye">
          <input
            v-if="!isSecret"
            v-model="password"
            type="text"
            placeholder="请输入密码"
            class="input-style"
            @keydown.enter="toPath"
          />
          <input
            v-else
            v-model="password"
            type="password"
            placeholder="请输入密码"
            class="input-style"
            @keydown.enter="toPath"
          />
          <img
            v-if="!isSecret"
            src="@/assets/images/eye.png"
            alt=""
            @click="isSecret = !isSecret"
          />
          <img
            v-else
            src="@/assets/images/uneye.png"
            alt=""
            @click="isSecret = !isSecret"
          />
        </div>
        <el-button
          v-if="showLoading"
          class="btn loading-btn"
          type="primary"
          loading
        >
          登录中
        </el-button>
        <div v-else class="btn" :class="{ active: showLog }" @click="toPath">
          登录
        </div>
        <div class="protocol">
          登录即代表阅读并同意<span>《服务协议与隐私政策》</span>
        </div>
        <div class="toTeacher" @click="toTeacher">
          <img src="@/assets/images/switch.svg" alt="" />
          <span>跳转教师端</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { ref, computed, onMounted } from "vue";
import useUserStore from "@/store/modules/user";
import { allSchool } from "../../api/api";

const userStore = useUserStore();
const router = useRouter();
const schoolId = ref("");
const account = ref("");
const password = ref("");
const isSecret = ref(true);
const showLoading = ref(false);
let showLog = computed(() => {
  let flag = account.value != "" && password.value != "" && schoolId.value;
  return flag;
});
const schoolList = ref([]);

const toPath = () => {
  if (!showLog.value || showLoading.value) {
    return;
  }
  showLoading.value = true;
  userStore
    .login({
      account: account.value,
      password: password.value,
      student: true,
      schoolId: schoolId.value,
    })
    .then(() => {
      router.push({ path: "/", query: {} });
      showLoading.value = false;
    })
    .catch(() => {
      // 登录失败：错误提示由 request 拦截器统一弹出（ElMessage/ElNotification），
      // 此处无需额外处理，仅保留 catch 避免 unhandled rejection。
    })
    .finally(() => {
      showLoading.value = false;
    });
};

const toTeacher = () => {
  window.open("https://teach.deepolylink.com", "_blank");
};

onMounted(() => {
  allSchool().then((res) => {
    schoolList.value = res || [];
  });
});
</script>

<style lang="scss" scoped>
.login {
  background-image: url("@/assets/images/login-bg.png");
  width: 100vw;
  height: 100vh;
  background-size: cover;
  background-repeat: no-repeat;
  overflow: auto;

  .text {
    width: 583px;
    font-family:
      PingFang SC,
      PingFang SC;
    font-weight: 500;
    font-size: 30px;
    color: #333333;
    line-height: 60px;
    margin: 67px 0 0 0;
  }
  :deep(.el-select) {
    width: 340px;
    height: 43px;
    margin-bottom: 20px;

    .el-select__wrapper {
      width: 340px;
      height: 43px;
      border-radius: 100px 100px 100px 100px;
      border: 1px solid rgba(0, 0, 0, 0.1);
      box-shadow: initial;
      background: initial;
      padding: 0 18px;
    }
    .el-select__placeholder {
      color: #68777f !important;
      font-size: 15px;
    }
  }
  .input-style {
    width: 340px;
    height: 43px;
    border-radius: 100px 100px 100px 100px;
    border: 1px solid rgba(0, 0, 0, 0.1);
    padding: 0 18px;
    margin-bottom: 20px;
    font-size: 14px;
  }

  .right {
    width: 420px;
    height: 530px;
    background: #ffffff60;
    box-shadow: 0px 4px 30px 0px rgba(75, 132, 255, 0.2);
    border-radius: 30px 30px 30px 30px;
    backdrop-filter: blur(15px);
    margin-left: 178px;
    display: flex;
    align-items: center;
    flex-direction: column;
    padding: 45px 30px;
  }

  .title {
    font-family: Abhaya Libre SemiBold-Regular;
    font-weight: 500;
    font-size: 26px;
    color: #333333;
    line-height: 50px;
    margin-bottom: 40px;
    position: relative;
  }

  .eye {
    position: relative;

    img {
      width: 24px;
      height: 24px;
      position: absolute;
      right: 21px;
      top: 10px;
      cursor: pointer;
    }
  }

  .btn {
    width: 300px;
    height: 50px;
    background: #dbdbdb;
    border-radius: 100px 100px 100px 100px;

    font-family:
      PingFang SC,
      PingFang SC;
    font-weight: 500;
    font-size: 20px;
    color: #666666;
    line-height: 50px;
    text-align: center;
    margin-top: 20px;
    cursor: pointer;
    transition: background-color 0.3s ease;
    border: none;
  }
  .loading-btn {
    background: #4b84ff60 !important;
    color: #ffffff !important;
    cursor: not-allowed;
  }

  .active {
    color: #ffffff !important;
    background: #4b84ff !important;
  }

  .protocol {
    font-family:
      PingFang SC,
      PingFang SC;
    font-weight: 500;
    font-size: 14px;
    color: #999999;
    line-height: 50px;
    span {
      color: rgba(75, 132, 255, 1);
      cursor: pointer;
    }
  }
}

.toTeacher {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-family:
    Source Han Sans,
    Source Han Sans;
  font-weight: 400;
  font-size: 14px;
  color: #524fff;
  margin-top: 20px;
  img {
    width: 16px;
    height: 16px;
    margin-right: 8px;
  }
}
</style>
