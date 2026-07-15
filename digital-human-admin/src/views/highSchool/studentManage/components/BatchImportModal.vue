<template>
  <Modal
    v-model:open="dialogOpen"
    title="批量导入"
    ok-text="导入"
    @ok="submit"
    :confirm-loading="loading"
  >
    <div class="pt-3 px-8">
      <Steps direction="vertical" :current="2">
        <Step :key="1">
          <template #icon>
            <div
              class="size-8 bg-[#1677FF] rounded-full flex items-center justify-center text-white text-xs"
            >
              1
            </div>
          </template>
          <template #title>
            <div class="space-y-4 flex flex-col pb-7">
              <span class="text-base text-[rgba(0,0,0,0.88)] font-normal">
                请先下载模板，编辑后导入
              </span>
              <Button type="primary" class="w-fit" ghost @click="downFn">
                下载模板
              </Button>
            </div>
          </template>
        </Step>
        <Step :key="2" class="mt-7">
          <template #icon>
            <div
              class="size-8 bg-[#1677FF] rounded-full flex items-center justify-center text-white text-xs"
            >
              2
            </div>
          </template>
          <template #description>
            <UploadDragger
              v-model:file-list="fileList"
              name="file"
              :multiple="true"
              show-upload-list
              accept=".xlsx,.xls"
              :before-upload="
                () => {
                  return false;
                }
              "
            >
              <p class="ant-upload-drag-icon">
                <inbox-outlined></inbox-outlined>
              </p>
              <p class="ant-upload-text">点击或拖拽上传</p>
            </UploadDragger>
          </template>
        </Step>
      </Steps>
    </div>
  </Modal>
</template>
<script setup>
import { ref, unref } from "vue";
import { saveAs } from "file-saver";
import { importStudent, downloadTemplate } from "@/api/student";
import {
  Modal,
  Step,
  Steps,
  UploadDragger,
  message,
  Button,
} from "ant-design-vue";
import { InboxOutlined } from "@ant-design/icons-vue";
import useUserStore from "@/store/modules/user";

const emits = defineEmits(["success"]);
const dialogOpen = ref(false);
const fileList = ref([]);
const loading = ref(false);
const userStore = useUserStore();

defineExpose({
  openModal: () => {
    loading.value = false;
    fileList.value = [];
    dialogOpen.value = true;
  },
});

const downFn = () => {
  downloadTemplate({
    schoolId: userStore.schoolId,
  }).then((response) => {
    const { data, headers } = response;
    const contentDisposition = headers["content-disposition"];
    // 解析文件名
    let organFileName = "";
    if (contentDisposition) {
      const fileNameMatch = contentDisposition.match(/filename="?(.+)"?/);
      if (fileNameMatch && fileNameMatch.length > 1) {
        try {
          organFileName = decodeURIComponent(fileNameMatch[1]);
        } catch (error) {}
      }
    }
    const blob = new Blob([data]);
    saveAs(blob, organFileName || "学生导入模板.xlsx");
  });
};

const submit = () => {
  if (!unref(fileList)?.length) {
    message.warning("请先上传文件");
    return;
  }
  loading.value = true;
  Promise.all(
    fileList.value?.map((item) => {
      const formdata = new window.FormData();
      formdata.append("file", item?.originFileObj);
      return importStudent(formdata, { schoolId: userStore.schoolId });
    }),
  )
    .then(() => {
      emits("success");
      dialogOpen.value = false;
    })
    .finally(() => {
      loading.value = false;
    });
};
</script>
