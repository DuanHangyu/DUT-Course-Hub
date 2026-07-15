<template>
  <ElDialog
    v-model="dialogOpen"
    width="500"
    append-to-body
    :show-close="false"
    class="rounded-[10px]! p-0!"
  >
    <template #header="{ close }">
      <div class="header">
        <h2>修改密码</h2>
        <el-icon class="close" :size="20" @click="close">
          <Close />
        </el-icon>
      </div>
    </template>
    <el-form
      ref="ruleFormRef"
      :model="ruleForm"
      status-icon
      label-width="100px"
      label-position="right"
      require-asterisk-position="right"
      label-suffix=":"
      class="form"
    >
      <el-form-item
        label="原密码"
        prop="originalPassword"
        :rules="[
          {
            required: true,
            message: '请输入',
            trigger: 'blur',
          },
          { min: 6, max: 11, message: '请输入6~11位', trigger: 'blur' },
        ]"
      >
        <el-input
          v-model="ruleForm.originalPassword"
          type="password"
          autocomplete="off"
          placeholder="请输入原密码"
          show-password
          :minlength="6"
          :maxlength="11"
        />
      </el-form-item>
      <el-form-item
        label="新密码"
        prop="newPassword"
        :rules="[
          {
            required: true,
            message: '请输入',
            trigger: 'blur',
          },
          { min: 6, max: 11, message: '请输入6~11位', trigger: 'blur' },
        ]"
      >
        <el-input
          v-model="ruleForm.newPassword"
          type="password"
          autocomplete="off"
          placeholder="请输入新密码"
          show-password
          :minlength="6"
          :maxlength="11"
        />
      </el-form-item>
      <el-form-item
        label="确认密码"
        prop="confirmPwd"
        required
        :rules="[
          { min: 6, max: 11, message: '请输入6~11位', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (value === '') {
                callback(new Error('请输入'));
              } else if (value !== ruleForm.newPassword) {
                callback(new Error('两次输入的密码不一致'));
              } else {
                callback();
              }
            },
            trigger: 'blur',
          },
        ]"
      >
        <el-input
          v-model="ruleForm.confirmPwd"
          type="password"
          autocomplete="off"
          placeholder="请输入确认密码"
          show-password
          :minlength="6"
          :maxlength="11"
        />
      </el-form-item>
    </el-form>
    <div class="footer">
      <el-button @click="resetForm(ruleFormRef)">取消</el-button>
      <el-button
        type="primary"
        @click="submitForm(ruleFormRef)"
        :loading="loading"
        color="#524fff"
      >
        修改
      </el-button>
    </div>
  </ElDialog>
</template>
<script setup>
import { ElDialog, ElButton, ElMessage } from "element-plus";
import { Close } from "@element-plus/icons-vue";
import { ref } from "vue";
import { updatePwd, logout } from "@/api/api";
import { useRouter } from "vue-router";
import { removeToken } from "@/utils/auth";

const dialogOpen = ref(false);
const ruleFormRef = ref();
const ruleForm = ref({});
const loading = ref(false);
const router = useRouter();

const submitForm = (formEl) => {
  if (!formEl) return;
  formEl.validate((valid) => {
    if (valid) {
      updatePwd(ruleForm.value)
        .then(() => {
          dialogOpen.value = false;
          ElMessage.success("密码修改成功");
          logout().then(() => {
            removeToken();
            router.replace("/login");
          });
        })
        .finally(() => {
          loading.value = false;
        });
    } else {
      console.log("error submit!");
    }
  });
};

const resetForm = (formEl) => {
  if (!formEl) return;
  formEl.resetFields();
  dialogOpen.value = false;
};

const openModal = (e = {}) => {
  try {
    ruleFormRef.value?.resetFields?.();
  } catch (error) {}
  ruleForm.value = {
    originalPassword: "",
    newPassword: "",
    confirmPwd: "",
  };
  dialogOpen.value = true;
};

defineExpose({
  openModal,
});
</script>
<style lang="scss" scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0px 10px 15px 10px;
  h2 {
    font-weight: 500;
    font-size: 20px;
    color: #000;
  }
  .close {
    cursor: pointer;
  }
}
.footer {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-input) {
  .el-input__inner {
    &:focus {
      border: initial !important;
    }
  }
  .is-focus {
    box-shadow: 0 0 0 0.0625rem #4b84ff;
  }
}

.form {
  padding: 0 20px;
}
</style>
