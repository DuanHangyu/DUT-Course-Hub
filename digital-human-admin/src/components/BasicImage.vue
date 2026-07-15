<template>
  <Image class="size-full" :src="url" :preview="preview"> </Image>
</template>

<script setup>
import { ref, watch } from "vue";
import { getFileSignedUrl } from "@/api/common";
import { Image } from "ant-design-vue";

const props = defineProps({
  src: {
    type: String,
    default: "",
  },
  preview: {
    type: Boolean,
    default: false,
  },
});

const url = ref();
const loading = ref(false);

watch(
  () => props.src,
  () => {
    if (props.src) {
      if (props.src?.includes("http://") || props.src?.includes("https://")) {
        url.value = props.src;
      } else {
        loading.value = true;
        getFileSignedUrl(props.src)
          .then((res) => {
            url.value = res?.data || "";
          })
          .finally(() => {
            loading.value = false;
          });
      }
    }
  },
  {
    deep: true,
    immediate: true,
  },
);
</script>
