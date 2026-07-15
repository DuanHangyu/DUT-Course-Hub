<template>
  <div class="size-full py-8 px-4">
    <div
      class="size-full flex items-center justify-center"
      v-if="!detail?.files?.length"
    >
      <el-empty />
    </div>
    <el-scrollbar height="100%" v-else>
      <div class="px-4 space-y-4">
        <section
          class="flex items-center justify-between file"
          v-for="(item, index) in detail?.files"
          :key="index"
        >
          <div class="flex items-center space-x-3">
            <div class="img">
              <fileIcon class="text-[#0052CC]" />
            </div>
            <span>{{ item?.fileName }}</span>
          </div>
          <div class="flex items-center space-x-4">
            <img
              src="@/assets/node/eye.png"
              class="size-8 cursor-pointer"
              alt=""
              @click="viewPdf(item)"
            />
            <img
              src="@/assets/node/download.png"
              class="size-8 cursor-pointer"
              alt=""
              @click="downPdf(item)"
            />
          </div>
        </section>
      </div>
    </el-scrollbar>
    <PreviewPdf ref="rRef" />
  </div>
</template>
<script setup>
import { ref } from "vue";
import fileIcon from "../icons/file.vue";
import PreviewPdf from "./PreviewPdf.vue";
const props = defineProps({
  detail: {
    type: Object,
    default: () => ({}),
  },
});

const rRef = ref(null);

const viewPdf = (r) => {
  rRef.value.show(r?.url);
};

const downPdf = (r) => {
  window.open(r?.url);
};
</script>
<style scoped lang="scss">
.file {
  padding: 16px;
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
}
</style>
