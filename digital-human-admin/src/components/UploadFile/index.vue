<template>
  <div class="relative upload">
    <Upload
      class="relative"
      show-upload-list
      name="file"
      :accept="accept"
      :style="{ width: width, height: height }"
      :customRequest="httpRequest"
      :file-list="modelValue"
      @remove="del"
      :max-count="maxCount"
    >
      <div
        class="flex flex-col items-center text-sm text-[rgba(0,0,0,0.88)] justify-center space-y-1 rounded-[8px] bg-[rgba(0,0,0,0.04)] relative"
        style="border: 1px dashed rgba(0, 0, 0, 0.15)"
        :style="{ width: width, height: height }"
      >
        <PlusOutlined />
        <span class="text-sm text-[rgba(10,8,26,0.88)] pl-1">{{
          uploadText
        }}</span>
        <Spin
          v-if="uploadVideoLoading"
          class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2"
        />
      </div>
    </Upload>
    <div class="text-xs text-[rgba(0,0,0,0.45)] mt-2" v-if="tipText">
      {{ tipText }}
    </div>
  </div>
</template>
<script setup>
import { ref } from "vue";
import { message, Spin, Upload } from "ant-design-vue";
import { PlusOutlined } from "@ant-design/icons-vue";
import { generateUploadUrl } from "@/api/common";
import axios from "axios";

function getVideoDuration(file) {
  return new Promise((resolve, reject) => {
    const video = document.createElement("video");
    video.preload = "metadata";
    video.onloadedmetadata = () => {
      URL.revokeObjectURL(video.src); // 释放内存
      resolve(Math.round(video.duration)); // 四舍五入取整秒，也可保留小数
    };
    video.onerror = () => {
      URL.revokeObjectURL(video.src);
      reject(new Error("无法加载视频元数据"));
    };
    video.src = URL.createObjectURL(file);
  });
}

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => [],
  },
  uploadText: {
    type: String,
    default: "上传图片",
  },
  tipText: {
    type: String,
    default: "",
  },
  accept: { type: String, default: "" },
  url: {
    type: String,
    default: "/file/upload",
  },
  width: {
    type: String,
    default: "104px",
  },
  height: {
    type: String,
    default: "104px",
  },
  maxCount: {
    type: Number,
    default: 1,
  },
  fileSize: {
    type: Number,
    default: 1024 * 1024 * 5,
  },
  fileType: {
    type: Array,
    default: () => [],
  },
});
const uploadVideoLoading = ref(false);
const emit = defineEmits(["update:modelValue"]);

const del = (e) => {
  const list = [...props.modelValue]?.filter((item) => item?.url != e?.url);
  emit("update:modelValue", list);
};

const httpRequest = async (e) => {
  const { file } = e;
  if (!props.fileType?.includes(file.type)) {
    message.error("文件格式不正确");
    return;
  }
  if (file.size > props.fileSize) {
    message.error("文件大小超过限制！");
    return;
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
      let list = [];
      if (!props.modelValue) {
        list = [];
      } else {
        list = [...props.modelValue];
      }
      const t = {
        url: res?.data?.url,
        name: res?.data?.fileName,
        status: "done",
      };
      if (file.type.startsWith("video/")) {
        let videoDuration = null;
        videoDuration = await getVideoDuration(file);
        t.videoDuration = videoDuration;
      }
      if (props.maxCount == 1) {
        list = [t];
      } else {
        list.push(t);
      }
      emit("update:modelValue", list);
    } else {
      message.error("上传失败");
    }
  } catch (error) {
  } finally {
    uploadVideoLoading.value = false;
  }
};
</script>
<style scoped lang="less">
.upload {
  .overly {
    opacity: 0;
  }

  &:hover {
    .overly {
      opacity: 1;
    }
  }
}
</style>
