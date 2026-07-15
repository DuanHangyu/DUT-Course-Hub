<template>
  <Modal
    v-model:open="dialogOpen"
    :width="520"
    destroy-on-close
    :title="ruleForm?.id ? '编辑教师' : '新增教师'"
    @ok="submitForm"
    :confirm-loading="loading"
  >
    <Form ref="ruleFormRef" :model="ruleForm" layout="vertical">
      <FormItem
        label="姓名"
        name="userName"
        :rules="[
          {
            required: true,
            message: '请输入',
            trigger: 'blur',
          },
          { min: 2, max: 10, message: '请输入2~10位', trigger: 'blur' },
        ]"
      >
        <Input v-model:value="ruleForm.userName" placeholder="请输入" />
      </FormItem>
      <FormItem label="账号" name="account" required>
        <Input v-model:value="ruleForm.account" placeholder="请输入" />
      </FormItem>
      <FormItem
        label="密码"
        name="password"
        :rules="[
          {
            required: true,
            message: '请输入',
            trigger: 'blur',
          },
          { min: 6, max: 11, message: '请输入6~11位', trigger: 'blur' },
        ]"
      >
        <InputPassword
          v-model:value="ruleForm.password"
          autocomplete="off"
          placeholder="请输入"
        />
      </FormItem>
      <FormItem label="教师头像" name="avatar" required>
        <Upload
          tipText="建议上传比例为1:1的图片"
          v-model:modelValue="ruleForm.avatar"
          @update:modelValue="validateFn"
        />
      </FormItem>
      <FormItem label="状态" name="state" required>
        <Switch
          v-model:checked="ruleForm.state"
          checked-children="启用"
          un-checked-children="禁用"
          :checked-value="true"
          :un-checked-value="false"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
<script setup>
import {
  Modal,
  message,
  Input,
  InputPassword,
  Switch,
  Form,
  FormItem,
} from "ant-design-vue";
import { ref } from "vue";
import { cloneDeep } from "lodash-es";
import { modifyTeacher, createTeacher } from "@/api/teacher";
import Upload from "@/components/Upload/index.vue";
import useUserStore from "@/store/modules/user";

const userStore = useUserStore();

const emits = defineEmits(["success"]);
const dialogOpen = ref(false);
const ruleFormRef = ref();
const ruleForm = ref({ state: true });
const loading = ref(false);

const submitForm = () => {
  ruleFormRef.value.validate().then(() => {
    if (ruleForm.value?.id) {
      loading.value = true;
      modifyTeacher(ruleForm.value)
        .then((res) => {
          dialogOpen.value = false;
          message.success("修改成功");
          emits("success");
        })
        .finally(() => {
          loading.value = false;
        });
    } else {
      loading.value = true;
      createTeacher({
        ...ruleForm.value,
        schoolId: userStore.schoolId,
      })
        .then((res) => {
          dialogOpen.value = false;
          message.success("添加成功");
          emits("success");
        })
        .finally(() => {
          loading.value = false;
        });
    }
  });
};

const openModal = (e = { state: true }) => {
  try {
    ruleFormRef.value?.resetFields?.();
  } catch (error) {}
  ruleForm.value = cloneDeep(e);
  dialogOpen.value = true;
};

const validateFn = () => {
  ruleFormRef.value?.validateFields(["avatar"]);
};
defineExpose({
  openModal,
});
</script>
