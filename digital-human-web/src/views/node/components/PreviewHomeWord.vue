<template>
  <el-dialog v-model="dialogVisible" title="" width="55%" top="5vh">
    <div v-loading="showLoading">
      <VuePdfApp
        style="height: 60vh"
        :pdf="url"
        @pages-rendered="renderedHandler"
        v-if="url?.includes('.pdf')"
      ></VuePdfApp>
      <img :src="url" v-else class="w-full h-[60vh]" alt="" />
    </div>
  </el-dialog>
</template>
<script setup>
import { ref } from "vue";
import VuePdfApp from "vue3-pdf-app";
import "vue3-pdf-app/dist/icons/main.css";

const dialogVisible = ref(false);
const showLoading = ref(false);
const url = ref(null);

function renderedHandler(params) {
  showLoading.value = false;
}

defineExpose({
  show: function (r) {
    dialogVisible.value = true;
    url.value = r;
    showLoading.value = false;
    if (url.value?.includes(".pdf")) {
      showLoading.value = true;
    }
  },
});
</script>
