<template>
  <div class="flex items-start space-x-2">
    <img
      src="@/assets/images/profile.png"
      class="size-8 rounded-full overflow-hidden flex-shrink-0"
      alt=""
    />
    <div class="flex-grow">
      <div class="flex items-center space-x-4">
        <div class="flex items-center space-x-4">
          <div
            class="text-[14px] font-semibold text-[rgba(0,0,0,0.88)] space-x-2"
          >
            <span>{{ record?.userName }}</span>
            <span class="text-[rgba(0,0,0,0.45)]" v-if="record?.replyName">
              回复
            </span>
            <span v-if="record?.replyName">{{ record?.userName }}</span>
          </div>
          <span class="text-[14px] font-normal text-[rgba(0,0,0,0.25)]">
            {{ record?.createTime }}
          </span>
        </div>
        <a class="cursor-pointer text-red-500" @click="delFn">删除</a>
      </div>
      <div class="mt-2 space-y-3">
        <div
          class="grid gap-x-3 gap-y-8"
          v-for="(item, index) in record?.pictures"
          :key="index"
        >
          <div class="bg-[#F5F5F5] size-12">
            <BasicImage :src="item?.url" preview class="!size-full" alt="" />
          </div>
        </div>
        <div
          class="grid grid-cols-3 gap-x-3 gap-y-2"
          v-for="(item, index) in record?.files"
          :key="index"
        >
          <div
            class="w-[371px] h-[22px] bg-[rgba(0,0,0,0.04)] px-2 flex items-center space-x-2 cursor-pointer"
            @click="openView(item.url)"
          >
            <LinkOutlined color="rgba(0,0,0,0.45)" size="16px" />
            <div class="flex-grow truncate text-[#1677FF] text-sm font-normal">
              {{ item?.fileName }}
            </div>
          </div>
        </div>
        <div
          class="text-[14px] font-normal text-[rgba(0,0,0,0.88)]"
          v-if="record?.content"
        >
          {{ record?.content }}
        </div>
      </div>
      <div
        class="mt-2 text-[#1677FF] text-sm font-normal cursor-pointer"
        @click="isReply = !isReply"
      >
        回复
      </div>
      <div class="mt-2" v-if="isReply">
        <AskInput
          tyep="reply"
          :course-id="courseId"
          :comment-id="record?.id"
          :course-node-id="courseNodeId"
          :placeholder="`回复 ${record?.userName}`"
          @success="success"
        />
      </div>
      <slot name="children" />
    </div>
  </div>
</template>
<script setup>
import { ref } from "vue";
import AskInput from "./AskInput.vue";
import BasicImage from "@/components/BasicImage.vue";
import { deleteComment } from "@/api/askQuestion";
import { LinkOutlined } from "@ant-design/icons-vue";

const props = defineProps({
  record: {
    type: Object,
    default: () => ({}),
  },
  courseId: {
    type: Number,
  },
  courseNodeId: {
    type: Number,
  },
});

const emits = defineEmits(["success"]);

const isReply = ref(false);

const success = () => {
  isReply.value = false;
  emits("success");
};

const openView = (url) => {
  window.open(url, "_blank");
};

const delFn = async () => {
  await deleteComment({ id: props.record?.id });
  emits("success");
};
</script>
