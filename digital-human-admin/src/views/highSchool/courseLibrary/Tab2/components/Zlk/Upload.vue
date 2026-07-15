<template>
  <Modal
    v-model:open="dialogOpen"
    :width="600"
    destroy-on-close
    title="上传文件"
    @ok="submitForm"
    :confirm-loading="loading"
  >
    <Form ref="ruleFormRef" :model="ruleForm" layout="vertical">
      <FormItem label="文件夹" name="folderId">
        <Select
          v-model:value="ruleForm.folderId"
          :options="list"
          :field-names="{ label: 'folderName', value: 'id' }"
          placeholder="请选择"
        />
      </FormItem>
      <FormItem label="上传文件" name="fileList" required>
        <UploadDragger
          :file-list="ruleForm.fileList"
          name="file"
          :multiple="true"
          :show-upload-list="false"
          accept=".ppt,.pptx,.dox,.docx,.mp4,.zip"
          :customRequest="httpRequest"
        >
          <p class="ant-upload-drag-icon">
            <inbox-outlined></inbox-outlined>
          </p>
          <p class="text-[rgba(0,0,0,0.88)]">点击或将文件拖拽到这里上传</p>
          <p class="text-xs text-[rgba(10,8,26,0.45)]">
            支持扩展名：ppt、pptx、dox、docx、mp4、zip等
          </p>
        </UploadDragger>
      </FormItem>
    </Form>
    <template v-if="ruleForm.fileList?.length">
      <div class="text-sm text-[rgba(10,8,26,0.45)]">待上传文件</div>
      <Table
        :data-source="ruleForm.fileList"
        row-key="id"
        :pagination="false"
        class="mt-4"
      >
        <template #emptyText>
          <Empty />
        </template>
        <TableColumn dataIndex="fileName" title="文件名">
          <template #default="{ record }">
            <div class="truncate" style="width: 160px;">{{ record?.fileName }}</div>
          </template>
        </TableColumn>
        <TableColumn dataIndex="fileType" title="格式" />
        <TableColumn dataIndex="fileSize" title="大小" :width="200">
          <template #default="{ record }">
            {{ record.fileSize }} byte
          </template>
        </TableColumn>
        <TableColumn dataIndex="action" title="操作">
          <template #default="{ record, index }">
            <Button
              type="link"
              danger
              class="!pl-0"
              @click="ruleForm.fileList?.splice(index, 1)"
            >
              删除
            </Button>
          </template>
        </TableColumn>
      </Table>
    </template>
  </Modal>
</template>
<script setup>
import { ref } from "vue";
import { cloneDeep } from "lodash-es";
import {
  Modal,
  Form,
  FormItem,
  message,
  Select,
  UploadDragger,
  Table,
  TableColumn,
  Button,
  Empty,
} from "ant-design-vue";
import { InboxOutlined } from "@ant-design/icons-vue";
import { generateUploadUrl } from "@/api/common";
import axios from "axios";
import {
  createCourseFile,
  getCourseMasterialList,
} from "@/api/course-material";
import { useRoute } from "vue-router";

const emits = defineEmits(["success"]);
const dialogOpen = ref(false);
const ruleFormRef = ref();
const ruleForm = ref({});
const loading = ref(false);
const uploadVideoLoading = ref(false);
const route = useRoute();
const list = ref([]);

const submitForm = () => {
  ruleFormRef.value.validate().then(() => {
    Promise.allSettled(
      ruleForm.value.fileList.map((item) => {
        const t = {
          courseId: route.query?.id,
          fileName: item.fileName,
          ossKey: item.ossKey,
          fileSize: item.fileSize,
          fileType: item.fileType,
        };
        if (ruleForm.value.folderId) {
          t.folderId = ruleForm.value.folderId;
        }
        return createCourseFile(t);
      }),
    ).then(() => {
      dialogOpen.value = false;
      message.success("添加成功");
      emits("success");
    });
  });
};
const openModal = (e = {}) => {
  try {
    ruleFormRef.value?.resetFields?.();
  } catch (error) {}
  ruleForm.value = cloneDeep(e);
  dialogOpen.value = true;
  getCourseMasterialList({
    courseId: route.query?.id,
  }).then((res) => {
    list.value = res?.data?.folders || [];
  });
};

const httpRequest = async (e) => {
  const { file } = e;
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
      const t = {
        url: res?.data?.url,
        name: res?.data?.fileName,
        status: "done",
        fileName: res?.data?.fileName,
        fileType: file.type,
        fileSize: file.size,
        ossKey: res?.data?.url,
      };
      if (!ruleForm.value?.fileList?.length) {
        ruleForm.value.fileList = [];
      }
      ruleForm.value.fileList.push(t);
      ruleFormRef.value?.validateFields(["fileList"]);
    } else {
      message.error("上传失败");
    }
  } catch (error) {
  } finally {
    uploadVideoLoading.value = false;
  }
};

defineExpose({
  openModal,
});
</script>
