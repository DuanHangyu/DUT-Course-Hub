<template>
  <Modal
    v-model:open="dialogOpen"
    :width="550"
    destroy-on-close
    :title="
      type == 'renameFile'
        ? '编辑文件'
        : type == 'renameFolder'
          ? '编辑文件夹'
          : '新增文件夹'
    "
    @ok="submitForm"
    :confirm-loading="loading"
  >
    <Form ref="ruleFormRef" :model="ruleForm" layout="vertical">
      <FormItem
        :label="type == 'renameFile' ? '文件名称' : '文件夹名称'"
        name="name"
        required
      >
        <Input v-model:value="ruleForm.name" placeholder="请输入" />
      </FormItem>
    </Form>
  </Modal>
</template>
<script setup>
import { ref } from "vue";
import { Modal, Form, FormItem, Input, message } from "ant-design-vue";
import {
  renameCourseMaterial,
  createCourseMaterial,
  renameCourseFile,
} from "@/api/course-material";
import { useRoute } from "vue-router";

const emits = defineEmits(["success"]);
const dialogOpen = ref(false);
const ruleFormRef = ref();
const ruleForm = ref({});
const loading = ref(false);
const route = useRoute();
const type = ref("addFolder");

const submitForm = () => {
  ruleFormRef.value.validate().then(async () => {
    const params = {
      courseId: route.query.id,
    };
    if (type.value == "renameFile") {
      params.id = ruleForm.value.id;
      params.fileName = ruleForm.value.name;
      await renameCourseFile(params);
    }
    if (type.value == "renameFolder") {
      params.id = ruleForm.value.id;
      params.folderName = ruleForm.value.name;
      await renameCourseMaterial(params);
    }
    if (type.value == "addFolder") {
      params.folderName = ruleForm.value.name;
      await createCourseMaterial(params);
    }
    dialogOpen.value = false;
    message.success("操作成功");
    emits("success");
  });
};
const openModal = (e = {}, t) => {
  try {
    ruleFormRef.value?.resetFields?.();
  } catch (error) {}
  type.value = t;
  if (t == "renameFolder") {
    ruleForm.value = {
      id: e.id,
      name: e.name,
    };
  }
  if (t == "renameFile") {
    ruleForm.value = {
      id: e.id,
      name: e.fileName,
    };
  }
  dialogOpen.value = true;
};

defineExpose({
  openModal,
});
</script>
