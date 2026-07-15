<template>
  <video controls :src="url" v-if="url" class="size-full"></video>
  <div
    v-else
    class="bg-[#909399] rounded-[10px] flex items-center justify-center h-full w-full"
  >
    <PlayCircleOutlined class="text-xl text-white" />
  </div>
</template>

<script setup>
import { ref, watch } from "vue";
import { getFileSignedUrl } from "@/api/common";
import { PlayCircleOutlined } from "@ant-design/icons-vue";

const props = defineProps({
  src: {
    type: String,
    default: "",
  },
});

const url = ref();
const loading = ref(false);

watch(
  () => props.src,
  () => {
    if (props.src) {
      loading.value = true;
      getFileSignedUrl(props.src)
        .then((res) => {
          url.value = res?.data || "";
        })
        .finally(() => {
          loading.value = false;
        });
    }
  },
  {
    deep: true,
    immediate: true,
  },
);
</script>
