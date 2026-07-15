<template>
  <el-dialog
    title="修改密码"
    v-model="open"
    width="480px"
    append-to-body
    destory-on-close
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="auto"
      label-position="top"
    >
      <el-form-item label="原密码" prop="oldPassword">
        <el-input
          type="password"
          v-model="form.oldPassword"
          placeholder="请输入"
          show-password
        />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          type="password"
          v-model="form.newPassword"
          placeholder="请输入"
          show-password
        />
      </el-form-item>
      <el-form-item label="确认新密码" prop="checkPass">
        <el-input
          v-model="form.checkPass"
          placeholder="请输入"
          type="password"
          show-password
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="cancel">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
// import * as UserApi from "@/api/user";
import useUserStore from "@/store/modules/user";
import { ref } from "vue";
const emit = defineEmits(["success"]);

const formRef = ref();
const open = ref(false);
const form = ref({});
const rules = ref({
  oldPassword: [{ required: true, message: "该项为必填项" }],
  newPassword: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error("该项为必填项"));
        } else {
          if (form.value.checkPass) {
            formRef.value?.validateField("checkPass");
          }
          callback();
        }
      },
      required: true,
    },
  ],
  checkPass: [
    {
      validator: (rule, value, callback) => {
        if (!value && !form.value.id) {
          callback(new Error("该项为必填项"));
        } else if (value !== form.value.newPassword) {
          callback(new Error("两次密码不一致!"));
        } else {
          callback();
        }
      },
      required: form.value?.id ? false : true,
    },
  ],
});

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {};
  formRef.value?.resetFields();
}
/** 提交按钮 */
function submitForm() {
  formRef.value?.validate((valid) => {
    if (valid) {
      // UserApi.changePwd({
      //   ...form.value,
      // }).then(() => {
      //   open.value = false;
      //   reset();
      //   useUserStore()
      //     .logOut()
      //     .then(() => {
      //       location.href = "/login";
      //     });
      // });
    }
  });
}

defineExpose({
  openModal: () => {
    form.value = {};
    reset();
    open.value = true;
  },
});
</script>
