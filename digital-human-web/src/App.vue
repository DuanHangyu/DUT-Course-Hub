<template>
  <div>
    <router-view />
    <div
      v-if="$route.path != '/login' && isShow"
      class="h-13 w-full bg-[#04509EFF] flex items-center justify-center text-white text-sm"
    >
      版权所有：大连理工大学  地址：中国·辽宁省大连市甘井子区凌工路2号 电话：0411-84708320 邮编：116024  辽ICP备05001357号
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const isShow = ref(false);

setTimeout(() => {
  isShow.value = true;
}, 1500);
</script>
