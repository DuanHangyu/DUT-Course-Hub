<template>
  <Modal
    v-model:open="dialogOpen"
    :width="550"
    destroy-on-close
    :title="ruleForm?.id ? '编辑作业' : '新增作业'"
    @ok="submitForm"
    :confirm-loading="loading"
  >
    <Form ref="ruleFormRef" :model="ruleForm" layout="vertical">
      <FormItem label="作业标题" name="title" required>
        <Input v-model:value="ruleForm.title" placeholder="请输入" />
      </FormItem>
      <FormItem label="作业描述" name="description" required>
        <Textarea v-model:value="ruleForm.description" placeholder="请输入" />
      </FormItem>
      <FormItem label="截止时间" name="deadline" required>
        <DatePicker
          v-model:value="ruleForm.deadline"
          class="!w-full"
          placeholder="请选择"
          show-time
        />
      </FormItem>
      <FormItem label="附件" name="attachments" required>
        <Upload
          show-upload-list
          name="file"
          accept=".png,.jpg,.jpeg,.pdf,.doc,.docx,.ppt,.pptx,.zip"
          :customRequest="httpRequest"
          :file-list="ruleForm.attachments"
          @remove="del"
        >
          <Button class="flex items-center">
            <upload-outlined></upload-outlined>
            <span> 上传附件</span>
          </Button>
        </Upload>
      </FormItem>
    </Form>
  </Modal>
</template>
<script setup>
import { ref } from "vue";
import {
  Modal,
  Form,
  FormItem,
  Input,
  Textarea,
  Button,
  DatePicker,
  Upload,
  message,
} from "ant-design-vue";
import { UploadOutlined } from "@ant-design/icons-vue";
import { generateUploadUrl } from "@/api/common";
import axios from "axios";
import { create, modify } from "@/api/homeWork";
import { cloneDeep } from "lodash-es";
import dayjs from "dayjs";

const emits = defineEmits(["success"]);
const dialogOpen = ref(false);
const ruleFormRef = ref();
const ruleForm = ref({});
const loading = ref(false);
const uploadVideoLoading = ref(false);

const submitForm = () => {
  ruleFormRef.value.validate().then(async () => {
    if (ruleForm.value?.id) {
      loading.value = true;
      modify({
        ...ruleForm.value,
        attachments: ruleForm.value?.attachments?.map((item) => ({
          url: item?.url,
          fileName: item?.name,
        })),
      })
        .then((res) => {
          dialogOpen.value = false;
          emits("success");
        })
        .finally(() => {
          loading.value = false;
        });
    } else {
      loading.value = true;
      create({
        ...ruleForm.value,
        attachments: ruleForm.value?.attachments?.map((item) => ({
          url: item?.url,
          fileName: item?.name,
        })),
      })
        .then((res) => {
          dialogOpen.value = false;
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
  if (e?.id) {
    ruleForm.value.attachments = e?.attachments?.map((item) => ({
      url: item?.url,
      name: item?.fileName,
    }));
    ruleForm.value.deadline = dayjs(e.deadline, "YYYY/MM/DD HH:mm");
  }
  dialogOpen.value = true;
};

const del = (e) => {
  ruleForm.value.attachments = [...ruleForm.value.attachments]?.filter(
    (item) => item?.url != e?.url,
  );
  validateFn();
};

const allowedTypes = [
  "image/png",
  "image/jpeg",
  "application/pdf",
  "application/msword", // .doc
  "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
  "application/vnd.ms-powerpoint", // .ppt
  "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
];

const httpRequest = async (e) => {
  const { file } = e;
  const isValidType = allowedTypes.includes(file.type);
  if (!isValidType) {
    message.error("仅支持 PNG、JPG、JPEG、PDF、DOC、DOCX、PPT、PPTX 文件");
    return false; // 阻止上传
  }
  try {
    uploadVideoLoading.value = true;
    const res = await generateUploadUrl({
      fileName: file?.name,
      contentType: file?.type,
    });
    const presignedUrl = res?.data?.presignedUrl;
    const res2 = await axios.put(presignedUrl, file, {
      headers: {
        "Content-Type": file.type || "application/octet-stream",
      },
    });
    if (res2.status == 200) {
      if (!ruleForm.value.attachments?.length) {
        ruleForm.value.attachments = [];
      }
      ruleForm.value.attachments.push({
        presignedUrl: res?.data?.presignedUrl,
        url: res?.data?.url,
        name: res?.data?.fileName,
        status: "done",
      });
      validateFn();
    } else {
      message.error("上传失败");
    }
  } catch (error) {
  } finally {
    uploadVideoLoading.value = false;
  }
};

const validateFn = () => {
  ruleFormRef.value?.validateFields(["attachments"]);
};

defineExpose({
  openModal,
});
</script>
