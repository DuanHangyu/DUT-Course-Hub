<template>
  <Modal
    v-model:open="dialogOpen"
    :width="550"
    destroy-on-close
    :title="ruleForm?.id ? '编辑节点' : '新增节点'"
    @ok="submitForm"
    :confirm-loading="loading"
  >
    <Form ref="ruleFormRef" :model="ruleForm" layout="vertical">
      <FormItem label="节点名称" name="nodeName" required>
        <Input v-model:value="ruleForm.nodeName" placeholder="请输入" />
      </FormItem>
      <FormItem label="节点介绍" name="nodeIntroduce" required>
        <Textarea v-model:value="ruleForm.nodeIntroduce" placeholder="请输入" />
      </FormItem>
      <FormItem label="关联节点" name="relateNode" required>
        <Select
          v-model:value="ruleForm.relateNode"
          placeholder="请选择"
          :max-tag-count="5"
          allow-clear
          :options="relateNodeList"
          :field-names="{ label: 'nodeName', value: 'id' }"
          mode="multiple"
        />
      </FormItem>
      <FormItem label="节点大小" name="nodeSize" required>
        <InputNumber
          v-model:value="ruleForm.nodeSize"
          :controls="false"
          placeholder="请输入节点大小，30-100"
          :precision="0"
          :min="30"
          :max="100"
          class="w-full"
        />
      </FormItem>
      <FormItem label="节点颜色" name="nodeColour" required>
        <Select
          v-model:value="ruleForm.nodeColour"
          placeholder="请选择"
          clearable
          class="!w-full"
        >
          <SelectOption
            v-for="item in ['3', '4', '5']"
            :key="item"
            :value="item"
          >
            <div
              class="rounded-full size-[30px] flex items-center justify-center"
              :style="{
                background:
                  item == 3 ? '#7DBFFB' : item == 4 ? '#4FD8C8' : '#FB837D',
              }"
            >
              <img
                src="@/assets/images/node.png"
                class="h-[20px] w-[20px]"
                alt=""
              />
            </div>
          </SelectOption>
        </Select>
      </FormItem>
      <FormItem label="上传视频" name="video" required>
        <UploadFile
          tipText="建议格式：MP4，文件最大可上传：3 GB"
          v-model="ruleForm.video"
          accept=".mp4"
          :fileType="['video/mp4']"
          :fileSize="1024 * 1024 * 1024 * 3"
          @update:modelValue="validateFn"
        />
      </FormItem>
      <FormItem label="上传文件" name="files" required>
        <UploadFile
          tipText="建议格式：PDF，文件最大可上传：1 GB"
          v-model="ruleForm.files"
          :max-count="1000"
          accept=".pdf,.zip"
          :fileType="['application/pdf']"
          :fileSize="1024 * 1024 * 1024 * 1"
          @update:modelValue="validateFn2"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
<script setup>
import { ref } from "vue";
import { createNode, getNodeList, modifyNode } from "@/api/courseConstruction";
import { useRoute } from "vue-router";
import { cloneDeep } from "lodash-es";
import {
  Form,
  FormItem,
  Select,
  Input,
  Modal,
  Textarea,
  InputNumber,
  SelectOption,
} from "ant-design-vue";
import UploadFile from "@/components/UploadFile/index.vue";

const emits = defineEmits(["success"]);
const route = useRoute();
const dialogOpen = ref(false);
const ruleFormRef = ref();
const ruleForm = ref({});
const relateNodeList = ref([]);
const loading = ref(false);

const submitForm = () => {
  ruleFormRef.value.validate().then(async () => {
    if (ruleForm.value?.id) {
      loading.value = true;
      modifyNode({
        courseId: ruleForm.value?.courseId,
        ...ruleForm.value,
        video: {
          url: ruleForm.value?.video?.[0]?.url,
          fileName: ruleForm.value?.video?.[0]?.name,
        },
        files: ruleForm.value?.files?.map((item) => ({
          url: item?.url,
          fileName: item?.name,
        })),
        videoDuration: ruleForm.value?.video?.[0]?.videoDuration,
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
      createNode({
        courseId: ruleForm.value?.courseId,
        ...ruleForm.value,
        video: {
          url: ruleForm.value?.video?.[0]?.url,
          fileName: ruleForm.value?.video?.[0]?.name,
        },
        files: ruleForm.value?.files?.map((item) => ({
          url: item?.url,
          fileName: item?.name,
        })),
        videoDuration: ruleForm.value?.video?.[0]?.videoDuration,
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
  dialogOpen.value = true;
  try {
    ruleFormRef.value?.resetFields?.();
  } catch (error) {}
  ruleForm.value = cloneDeep(e);
  if (ruleForm.value?.id) {
    ruleForm.value.video = ruleForm.value?.video?.url
      ? [
          {
            url: ruleForm.value?.video?.url,
            name: ruleForm.value?.video?.fileName,
            status: "done",
            videoDuration: ruleForm.value?.videoDuration,
          },
        ]
      : [];
    ruleForm.value.files =
      ruleForm.value?.files?.map((item) => ({
        url: item?.url,
        name: item?.fileName,
        status: "done",
      })) || [];
  }
  getNodeList({
    courseId: route.query?.id,
  }).then((res) => {
    relateNodeList.value = [
      ...(res?.data?.filter((item) => item?.id != ruleForm.value?.id) || []),
    ];
  });
};

const validateFn = () => {
  ruleFormRef.value?.validateFields(["video"]);
};
const validateFn2 = () => {
  ruleFormRef.value?.validateFields(["files"]);
};
defineExpose({
  openModal,
});
</script>
<style lang="less" scoped>
:deep(.Input-number) {
  .Input__inner {
    text-align: left;
  }
}
:deep(.no-resize) {
  textarea {
    resize: none;
  }
}
:deep(.ant-select-selection-item) {
  display: flex;
  align-items: center;
}
</style>
