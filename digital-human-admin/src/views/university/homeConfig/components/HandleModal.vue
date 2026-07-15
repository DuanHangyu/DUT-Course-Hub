<template>
  <Modal
    v-model:open="dialogOpen"
    :width="550"
    destroy-on-close
    :title="ruleForm?.id ? '编辑图片' : '新增图片'"
    @ok="submitForm"
    :confirm-loading="loading"
  >
    <Form ref="ruleFormRef" :model="ruleForm" layout="vertical">
      <FormItem
        label="图片名称"
        name="name"
        :rules="[
          {
            required: true,
            validateTrigger: 'change',
            message: '请输入',
          },
        ]"
      >
        <Input v-model:value="ruleForm.name" placeholder="请输入" />
      </FormItem>
      <FormItem
        label="上传图片"
        name="imageUrl"
        :rules="[
          {
            required: true,
            validateTrigger: 'blur',
            message: '请上传图片',
          },
        ]"
      >
        <Upload
          uploadText="上传照片"
          accept=".png,.jpg,.jpeg,image/jpg,image/jpeg,image/png"
          tipText="建议尺寸：1920px*1036px"
          v-model="ruleForm.imageUrl"
          @update:modelValue="validateFn"
        />
      </FormItem>
      <FormItem label="跳转链接" name="jumpUrl" v-if="type == 2">
        <Input v-model:value="ruleForm.jumpUrl" placeholder="请输入" />
      </FormItem>
    </Form>
  </Modal>
</template>
<script setup>
import { ref } from "vue";
import Upload from "@/components/Upload/index.vue";
import { Modal, Form, FormItem, Input, message } from "ant-design-vue";
import { pick } from "lodash-es";
import {
  createBackground,
  modifyBackground,
  createBanner,
  modifyBanner,
} from "@/api/homeConfig";

const props = defineProps({
  type: {
    type: Number,
    default: 1,
  },
});

const emits = defineEmits(["success"]);
const dialogOpen = ref(false);
const ruleFormRef = ref();
const ruleForm = ref({});
const loading = ref(false);

const submitForm = () => {
  ruleFormRef.value.validate().then(async () => {
    if (props.type == 1) {
      if (ruleForm.value?.id) {
        await modifyBanner(ruleForm.value);
      } else {
        await createBanner(ruleForm.value);
      }
    }
    if (props.type == 2) {
      if (ruleForm.value?.id) {
        await modifyBackground(ruleForm.value);
      } else {
        await createBackground(ruleForm.value);
      }
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
  ruleForm.value = pick(e, ["id", "name", "jumpUrl"]);
  ruleForm.value.imageUrl = e?.url;
  dialogOpen.value = true;
};

const validateFn = () => {
  ruleFormRef.value?.validateFields(["imageUrl"]);
};
defineExpose({
  openModal,
});
</script>
