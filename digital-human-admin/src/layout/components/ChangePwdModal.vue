<template>
  <Modal
    title="修改密码"
    v-model:open="open"
    width="480px"
    append-to-body
    destory-on-close
  >
    <Form ref="formRef" :model="form" :rules="rules" layout="vertical">
      <FormItem label="原密码" prop="oldPassword">
        <Input
          type="password"
          v-model="form.oldPassword"
          placeholder="请输入"
          show-password
        />
      </FormItem>
      <FormItem label="新密码" prop="newPassword">
        <Input
          type="password"
          v-model="form.newPassword"
          placeholder="请输入"
          show-password
        />
      </FormItem>
      <FormItem label="确认新密码" prop="checkPass">
        <Input
          v-model="form.checkPass"
          placeholder="请输入"
          type="password"
          show-password
        />
      </FormItem>
    </Form>
    <template #footer>
      <div class="dialog-footer">
        <Button @click="cancel">取消</Button>
        <Button type="primary" @click="submitForm">确定</Button>
      </div>
    </template>
  </Modal>
</template>

<script setup>
import * as UserApi from "@/api/user";
import useUserStore from "@/store/modules/user";
import { ref } from "vue";
import { Modal, Form, FormItem, Button, Input } from "ant-design-vue";
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
  formRef.value?.validate()?.then(() => {
    UserApi.changePwd({
      ...form.value,
    }).then(() => {
      open.value = false;
      reset();
      useUserStore()
        .logOut()
        .then(() => {
          location.href = "/login";
        });
    });
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
