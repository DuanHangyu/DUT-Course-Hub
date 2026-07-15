<template>
  <div class="relative upload">
    <div
      class="absolute top-0 left-0 bg-[rgba(0,0,0,0.2)] overly rounded-[8px] flex items-center justify-center z-10"
      :style="{ width: width, height: height }"
      v-if="modelValue"
    >
      <DeleteOutlined
        @click="delFn"
        class="cursor-pointer !text-2xl !text-red-500"
      />
    </div>
    <Upload
      class="bg-[rgba(0,0,0,0.04)] rounded-[8px] flex flex-col justify-center items-center overflow-hidden relative"
      style="border: 1px dashed rgba(0, 0, 0, 0.15)"
      :show-upload-list="false"
      name="file"
      :accept="accept"
      :style="{ width: width, height: height }"
      :customRequest="httpRequest"
    >
      <Spin
        v-if="uploadVideoLoading"
        class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2"
      />
      <BasicImage
        v-if="modelValue"
        :src="modelValue"
        :style="{ width: width, height: height }"
        class="overflow-hidden object-contain"
      />
      <div
        class="flex flex-col items-center text-sm text-[rgba(0,0,0,0.88)] justify-center space-y-1"
        v-else
        :style="{ width: width, height: height }"
      >
        <PlusOutlined />
        <span class="text-sm text-[rgba(10,8,26,0.88)] pl-1">{{
          uploadText
        }}</span>
      </div>
    </Upload>
    <div class="text-xs text-[rgba(0,0,0,0.45)] mt-2" v-if="tipText">
      {{ tipText }}
    </div>
  </div>
</template>
<script setup>
import { ref } from "vue";
import BasicImage from "@/components/BasicImage.vue";
import { message, Spin, Upload } from "ant-design-vue";
import { PlusOutlined, DeleteOutlined } from "@ant-design/icons-vue";
import { generateUploadUrl } from "@/api/common";
import axios from "axios";

const props = defineProps({
  modelValue: {
    type: String,
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
});
const uploadVideoLoading = ref(false);
const emit = defineEmits(["update:modelValue"]);

const delFn = () => {
  emit("update:modelValue", null);
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
      emit("update:modelValue", res?.data?.url);
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
