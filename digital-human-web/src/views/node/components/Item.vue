<template>
  <div class="flex space-x-4">
    <div class="avatar shrink-0">
      <Avatar />
    </div>
    <div class="grow overflow-hidden">
      <div class="hf" :style="{ background: isBg ? '#F0F6FF' : '#fff' }">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-2">
            <span class="text-base font-semibold text-[#0E172A]">{{
              record?.userName
            }}</span>
            <div :class="record?.userType == 1 ? 'xsTag' : 'lsTag'">
              {{ record?.userType == 1 ? "老师" : "学生" }}
            </div>
            <template v-if="record?.replyName">
              <span class="text-base font-medium text-[#B3B3B3] px-3">
                回复
              </span>
              <span class="text-base font-semibold text-[#0E172A]">{{
                record?.replyName
              }}</span>
              <div :class="record?.replyUserType == 1 ? 'xsTag' : 'lsTag'">
                {{ record?.replyUserType == 1 ? "老师" : "学生" }}
              </div>
            </template>
          </div>
          <span class="text-sm text-[#A8B0B9]">
            发表于{{ record?.createTime }}
          </span>
        </div>
        <div class="text-base mt-2 text-[#5D5C68]">
          {{ record?.content }}
        </div>
        <div
          class="mt-2 flex items-center space-x-3 file w-fit"
          v-for="(item2, index2) in record?.files"
          :key="index2"
          @click="openPreview(item2)"
        >
          <div class="img">
            <fileIcon class="text-[#0052CC]" />
          </div>
          <span>{{ item2?.fileName }}</span>
        </div>
        <div
          class="mt-2 flex items-center space-x-3 file w-fit"
          v-for="(item2, index2) in record?.pictures"
          :key="index2"
          @click="openPreview(item2)"
        >
          <img class="size-10 rounded-sm" :src="item2?.url" alt="" />
          <span>{{ item2?.fileName }}</span>
        </div>
        <div
          class="mt-2 text-base text-[#0052CC] cursor-pointer"
          @click="$emit('reply')"
        >
          回复
        </div>
      </div>
      <div class="flex space-x-2" v-if="isChild">
        <div class="line shrink"></div>
        <div class="mt-4 grow">
          <slot name="chart" />
        </div>
      </div>
    </div>
    <PreviewHomeWord ref="rRef" />
  </div>
</template>
<script setup>
import fileIcon from "../icons/file.vue";
import Avatar from "@/components/Avatar.vue";
import PreviewHomeWord from "./PreviewHomeWord.vue";
import { ref } from "vue";
defineEmits(["reply"]);

const props = defineProps({
  isChild: {
    type: Boolean,
    default: false,
  },
  record: {
    type: Object,
    default: () => ({}),
  },
  isBg: {
    type: Boolean,
    default: false,
  },
});

const rRef = ref(null);
const openPreview = (e) => {
  if (
    e?.url?.includes(".pdf") ||
    e?.url?.includes(".jpg") ||
    e?.url?.includes(".png") ||
    e?.url?.includes(".jpeg")
  ) {
    rRef.value.show(e?.url);
  } else {
    window.open(e?.url);
  }
};
</script>
<style scoped lang="scss">
.avatar {
  width: 41px;
  height: 41px;
  box-shadow: 2px 2px 0px 0px #0c3a6c;
  border-radius: 100%;
  border: 2px solid #0c3a6c;
  overflow: hidden;
}
.hf {
  width: 100%;
  border-radius: 0px 20px 20px 20px;
  border: 2px solid #e2e8f0;
  padding: 16px;
}
.xsTag {
  width: 52px;
  line-height: 24px;
  background: #f0f6ff;
  border-radius: 8px 8px 8px 8px;
  border: 1px solid #e3effe;
  font-weight: 600;
  font-size: 14px;
  color: #7dbffb;
  text-align: center;
}
.lsTag {
  width: 52px;
  line-height: 24px;
  background: #cbf4ff;
  border-radius: 8px 8px 8px 8px;
  border: 1px solid #d1e4fd;
  font-weight: 600;
  font-size: 14px;
  color: #4ea8c0;
  text-align: center;
}

.line {
  width: 40px;
  height: 57px;
  border-left: 2px solid #e2e8f0;
  border-bottom: 2px solid #e2e8f0;
  border-bottom-left-radius: 20px;
}

.file {
  padding: 12px;
  background: #f0f6ff;
  border-radius: 16px 16px 16px 16px;
  border: 2px solid #dbeafe;
  font-weight: 400;
  font-size: 16px;
  color: #0e172a;
  .img {
    width: 40px;
    height: 40px;
    background: #d8e5fa;
    border-radius: 8px 8px 8px 8px;
    border: 1px solid #b6cdf1;
    display: flex;
    align-items: center;
    justify-content: center;
    img {
      width: 24.62px;
      height: 24.62px;
    }
  }

  &:hover {
    color: #0052cc;
    cursor: pointer;
  }
}
</style>
