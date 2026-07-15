<template>
  <Modal
    v-model:open="dialogOpen"
    :width="550"
    destroy-on-close
    :title="ruleForm?.id ? '编辑课程' : '新增课程'"
    @ok="submitForm"
    :confirm-loading="loading"
  >
    <Form ref="ruleFormRef" :model="ruleForm" layout="vertical">
      <FormItem label="课程名称" name="courseName" required>
        <Input v-model:value="ruleForm.courseName" placeholder="请输入" />
      </FormItem>
      <FormItem label="课程介绍" name="courseIntroduce" required>
        <Textarea
          v-model:value="ruleForm.courseIntroduce"
          placeholder="请输入"
        />
      </FormItem>
      <FormItem label="状态" name="state" required>
        <Select
          v-model:value="ruleForm.state"
          placeholder="请选择"
          clearable
          class="!w-full"
          :options="[
            { label: '已上架', value: true },
            { label: '未上架', value: false },
          ]"
        >
        </Select>
      </FormItem>
      <FormItem label="上传图片" name="pictureUrl" required>
        <Upload
          uploadText="上传照片"
          accept=".png,.jpg,.jpeg,image/jpg,image/jpeg,image/png"
          tipText="建议尺寸：586px*360px"
          v-model:modelValue="ruleForm.pictureUrl"
          @update:modelValue="validateFn"
        />
      </FormItem>
      <FormItem label="所属学科" name="subject" required>
        <Input v-model:value="ruleForm.subject" placeholder="请输入" />
      </FormItem>
      <FormItem label="授课教师" name="teacherName" required>
        <Input v-model:value="ruleForm.teacherName" placeholder="请输入" />
      </FormItem>
      <FormItem label="教师头像" name="headUrl" required>
        <Upload
          uploadText="上传照片"
          accept=".png,.jpg,.jpeg,image/jpg,image/jpeg,image/png"
          tipText="建议上传比例为1:1的图片"
          v-model:modelValue="ruleForm.headUrl"
          @update:modelValue="validateFn2"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
<script setup>
import { ref } from "vue";
import Upload from "@/components/Upload/index.vue";
import {
  Modal,
  Form,
  FormItem,
  Input,
  Select,
  Textarea,
  message,
} from "ant-design-vue";
import { create, modify } from "@/api/courseConstruction";
import { cloneDeep } from "lodash-es";

const emits = defineEmits(["success"]);
const dialogOpen = ref(false);
const ruleFormRef = ref();
const ruleForm = ref({});
const loading = ref(false);

const submitForm = () => {
  ruleFormRef.value.validate().then(async () => {
    if (ruleForm.value?.id) {
      await modify(ruleForm.value);
    } else {
      await create(ruleForm.value);
    }
    dialogOpen.value = false;
    message.success("成功");
    emits("success");
  });
};

const openModal = (e = {}) => {
  try {
    ruleFormRef.value?.resetFields?.();
  } catch (error) {}
  ruleForm.value = cloneDeep(e);
  dialogOpen.value = true;
};

const validateFn = () => {
  ruleFormRef.value?.validateFields(["pictureUrl"]);
};

const validateFn2 = () => {
  ruleFormRef.value?.validateFields(["headUrl"]);
};

defineExpose({
  openModal,
});
</script>
