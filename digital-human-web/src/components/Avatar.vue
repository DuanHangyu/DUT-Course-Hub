<template>
  <img
    :src="avatar[avatarIndex]"
    class="size-full overflow-hidden rounded-full"
    alt=""
  />
</template>
<script setup>
import A1 from "@/assets/avatar/a1.png";
import A2 from "@/assets/avatar/a2.png";
import A3 from "@/assets/avatar/a3.png";
import A4 from "@/assets/avatar/a4.png";
import A5 from "@/assets/avatar/a5.png";
import A6 from "@/assets/avatar/a6.png";
import A7 from "@/assets/avatar/a7.png";
import A8 from "@/assets/avatar/a8.png";
import { ref } from "vue";

const avatarIndex = ref(Math.floor(Math.random() * 8));
const avatar = ref([A1, A2, A3, A4, A5, A6, A7, A8]);
</script>
