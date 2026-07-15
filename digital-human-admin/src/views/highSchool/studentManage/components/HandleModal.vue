<template>
  <Modal
    v-model:open="dialogOpen"
    :width="550"
    destroy-on-close
    :title="ruleForm?.id ? '编辑学生' : '新增学生'"
    @ok="submitForm"
    :confirm-loading="loading"
  >
    <Form ref="ruleFormRef" :model="ruleForm" layout="vertical">
      <FormItem label="学号" name="idNumber" required>
        <Input v-model:value="ruleForm.idNumber" placeholder="请输入" />
      </FormItem>
      <FormItem label="学生姓名" name="studentName" required>
        <Input
          v-model:value="ruleForm.studentName"
          :minlength="2"
          :maxlength="10"
          placeholder="请输入"
        />
      </FormItem>
      <FormItem
        label="登录密码"
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
      <FormItem label="班级" name="classId" required>
        <Select
          v-model:value="ruleForm.classId"
          placeholder="请选择"
          :options="schoolClass"
          :field-names="{
            label: 'className',
            value: 'id',
          }"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
<script setup>
import { ref } from "vue";
import { cloneDeep } from "lodash-es";
import {
  Modal,
  Form,
  FormItem,
  Input,
  InputPassword,
  message,
  Select,
} from "ant-design-vue";
import { modifyStudent, createStudent } from "@/api/student";
import useUserStore from "@/store/modules/user";
import * as classApi from "@/api/classStructure";

const emits = defineEmits(["success"]);
const dialogOpen = ref(false);
const ruleFormRef = ref();
const ruleForm = ref({});
const loading = ref(false);
const schoolClass = ref([]);
const userStore = useUserStore();

const submitForm = () => {
  ruleFormRef.value.validate().then(() => {
    if (ruleForm.value?.id) {
      loading.value = true;
      modifyStudent(ruleForm.value)
        .then(() => {
          dialogOpen.value = false;
          message.success("修改成功");
          emits("success");
        })
        .finally(() => {
          loading.value = false;
        });
    } else {
      loading.value = true;
      createStudent({
        ...ruleForm.value,
        schoolId: userStore.schoolId,
      })
        .then(() => {
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
const openModal = (e = {}) => {
  try {
    ruleFormRef.value?.resetFields?.();
  } catch (error) {}
  ruleForm.value = cloneDeep(e);
  dialogOpen.value = true;
  classApi
    .getList({ page: 1, size: 5000, schoolId: userStore.schoolId })
    .then((res) => {
      schoolClass.value = res?.data?.records || [];
    });
};

defineExpose({
  openModal,
});
</script>
