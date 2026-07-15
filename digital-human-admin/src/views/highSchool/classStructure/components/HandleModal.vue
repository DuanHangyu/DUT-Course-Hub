<template>
  <Modal
    v-model:open="dialogOpen"
    :width="500"
    :title="ruleForm?.id ? '编辑班级' : '新增班级'"
    destroy-on-close
    @ok="submitForm"
    :confirm-loading="loading"
  >
    <Form ref="ruleFormRef" :model="ruleForm" layout="vertical">
      <FormItem label="班级名称" name="className" required>
        <Input v-model:value="ruleForm.className" placeholder="请输入" />
      </FormItem>
      <FormItem label="班主任" name="teacherId" required>
        <Select
          v-model:value="ruleForm.teacherId"
          :options="teacherList"
          :field-names="{
            label: 'userName',
            value: 'id',
          }"
          placeholder="请输入"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
<script setup>
import { message, Modal, Form, FormItem, Input, Select } from "ant-design-vue";
import { ref } from "vue";
import * as teacherApi from "@/api/teacher";
import * as classApi from "@/api/classStructure";
import useUserStore from "@/store/modules/user";
import { cloneDeep } from "lodash-es";

const emits = defineEmits(["success"]);
const dialogOpen = ref(false);
const ruleFormRef = ref();
const ruleForm = ref({});
const loading = ref(false);
const teacherList = ref([]);
const userStore = useUserStore();
const submitForm = () => {
  ruleFormRef.value.validate().then(() => {
    if (ruleForm.value?.id) {
      loading.value = true;
      classApi
        .modify(ruleForm.value)
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
      classApi
        .create({
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

const openModal = (e = {}) => {
  try {
    ruleFormRef.value?.resetFields?.();
  } catch (error) {}
  ruleForm.value = cloneDeep(e);
  dialogOpen.value = true;
  teacherApi
    .getTeacherList({ page: 1, size: 5000, schoolId: userStore.schoolId })
    .then((res) => {
      teacherList.value = res.data?.records || [];
    });
};

defineExpose({
  openModal,
});
</script>
