<template>
  <div class="askInput space-y-3" :class="error ? 'errorAskBorder' : ''">
    <div class="flex gap-x-3 gap-y-2" v-if="imageList?.length">
      <div
        class="size-12 relative"
        v-for="(item, index) in imageList"
        :key="index"
      >
        <BasicImage :src="item?.url" class="size-full" preview alt="" />
        <DeleteOutlined
          size="14px"
          color="rgba(0,0,0,0.45)"
          class="cursor-pointer !absolute top-[-7px] right-[-7px]"
          @click="deleteImage(index)"
        />
      </div>
    </div>
    <div class="grid grid-cols-3 gap-x-3 gap-y-2" v-if="fileList?.length">
      <div
        class="h-[22px] bg-[rgba(0,0,0,0.04)] px-2 flex items-center space-x-2"
        v-for="(item, index) in fileList"
        :key="index"
      >
        <LinkOutlined
          color="rgba(0,0,0,0.45)"
          size="16px"
          class="flex-shrink-0"
        />
        <div
          class="flex-grow truncate text-[#1677FF] text-sm font-normal text-left"
        >
          {{ item?.fileName }}
        </div>
        <DeleteOutlined
          size="14px"
          color="rgba(0,0,0,0.45)"
          class="flex-shrink-0 cursor-pointer"
          @click="deleteFile(index)"
        />
      </div>
    </div>
    <Input.TextArea
      v-model:value.trim="text"
      :placeholder="placeholder"
      class="no-resize"
    />
    <div class="flex items-center justify-between sendContent">
      <Upload
        class="flex items-center"
        :action="action"
        name="file"
        :headers="headers"
        :show-upload-list="false"
        @change="uploadSuccess"
        :before-upload="beforeUpload"
        accept=".png,.jpg,.jpeg,image/jpg,image/jpeg,image/png,.pdf"
      >
        <Button class="flex items-center">
          <UploadOutlined />
          <span class="ml-2">添加图片或文件</span>
        </Button>
      </Upload>
      <Button type="primary" @click="sendMsg"> 发送 </Button>
    </div>
  </div>
  <div class="mt-1 text-red-500 text-sm text-left" v-if="error">请输入内容</div>
</template>
<script setup>
import { ref, watch } from "vue";
import BasicImage from "@/components/BasicImage.vue";
import { saveComment, saveReply } from "@/api/askQuestion";
import { getToken } from "@/utils/auth";
import { Button, Input, message, Upload } from "ant-design-vue";
import {
  DeleteOutlined,
  LinkOutlined,
  UploadOutlined,
} from "@ant-design/icons-vue";
const action = import.meta.env.VITE_APP_BASE_API + "/file/upload";
const headers = ref({
  Authorization: `Bearer ${getToken()}`,
});

const props = defineProps({
  tyep: {
    type: String,
    default: "comment",
  },
  courseId: {
    type: Number,
  },
  courseNodeId: {
    type: Number,
  },
  commentId: {
    type: Number,
  },
  placeholder: {
    type: String,
    default: "请输入",
  },
});

const emits = defineEmits(["success"]);
const imageList = ref([]);
const fileList = ref([]);
const text = ref("");
const error = ref(false);

const beforeUpload = (rawFile) => {
  if (
    rawFile.type !== "image/jpeg" &&
    rawFile.type !== "image/jpg" &&
    rawFile.type !== "image/png" &&
    rawFile.type !== "image/webp" &&
    rawFile.type !== "application/pdf"
  ) {
    message.error("只允许上传图片和pdf文件");
    return false;
  }
  return true;
};

const uploadSuccess = (e) => {
  if (e?.file?.status == "done") {
    if (e?.file?.response?.code == 200) {
      if (e?.file?.response?.data?.url?.includes(".pdf")) {
        fileList.value?.push({
          url: e?.file?.response?.data?.url,
          fileName: e?.file?.response?.data?.fileName,
        });
      } else {
        imageList.value?.push({
          url: e?.file?.response?.data?.url,
          fileName: e?.file?.response?.data?.fileName,
        });
      }
    } else {
      message.error(e?.file?.esponse?.message || "上传失败");
    }
  }
};

const deleteImage = (e) => {
  imageList.value?.splice(e, 1);
};
const deleteFile = (e) => {
  fileList.value?.splice(e, 1);
};
const sendMsg = () => {
  if (!text.value && !imageList.value?.length && !fileList.value?.length) {
    error.value = true;
    return;
  }
  if (props.tyep == "comment") {
    saveComment({
      courseId: props.courseId,
      courseNodeId: props.courseNodeId,
      content: text.value,
      files: fileList.value,
      pictures: imageList.value,
    }).then(() => {
      emits("success");
    });
  } else {
    saveReply({
      courseId: props.courseId,
      courseNodeId: props.courseNodeId,
      commentId: props.commentId,
      content: text.value,
      files: fileList.value,
      pictures: imageList.value,
    }).then(() => {
      emits("success");
    });
  }
};

watch(
  () => [text.value, imageList.value, fileList.value],
  () => {
    if (text.value || imageList.value?.length || fileList.value?.length) {
      error.value = false;
    }
  },
  {
    deep: true,
  },
);
</script>
<style scoped lang="less">
.askInput {
  width: 100%;
  background: #ffffff;
  border-radius: 6px 6px 6px 6px;
  border: 1px solid rgba(0, 0, 0, 0.15);
  padding: 12px;
  .sendContent {
    border-top: 1px solid #d9d9d9;
    padding-top: 12px;
  }
  .askBtn {
    width: 138px;
    height: 24px;
    background: #ffffff;
    border-radius: 6px 6px 6px 6px;
    border: 1px solid rgba(0, 0, 0, 0.15);
    font-family:
      Source Han Sans,
      Source Han Sans;
    font-weight: 350;
    font-size: 14px;
    color: rgba(0, 0, 0, 0.88);
  }
  .sendBtn {
    width: 84px;
    height: 24px;
    background: #1677ff;
    border-radius: 4px 4px 4px 4px;
  }
}

.errorAskBorder {
  border: 1px solid red;
}

:deep(.el-textarea__inner) {
  border: initial;
  box-shadow: initial;
  outline: initial;
  padding: 0;
  border-radius: initial;
  &:hover {
    border: initial;
    box-shadow: initial;
    outline: initial;
  }
  &:focus {
    border: initial;
    box-shadow: initial;
    outline: initial;
  }
}
:deep(.no-resize) {
  height: 68px;
  textarea {
    height: 100%;
    resize: none;
  }
}
</style>
