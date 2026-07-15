<template>
  <Modal v-model:open="dialogOpen" :width="740" title="问答详情">
    <div
      class="px-6 py-2 space-y-8 max-h-[600px] overflow-y-auto min-h-[200px]"
    >
      <AskItem
        v-if="commentList.length"
        v-for="(item, index) in commentList"
        :key="index"
        :record="item"
        :course-id="detail?.courseId"
        :courseNodeId="detail?.id"
        @success="onLoad_comment"
      >
        <template #children v-if="item?.replies?.replies?.length">
          <div class="mt-4 space-y-4">
            <AskItem
              v-for="(ite, ind) in item?.replies?.replies"
              :key="ind"
              :course-id="detail?.courseId"
              :courseNodeId="detail?.id"
              :record="ite"
              @success="expandChildren(item, true)"
            />
          </div>
          <a
            class="space-x-1 flex items-center text-[#1677ff] text-sm cursor-pointer pl-10 mt-3"
            @click="expandChildren(item, false)"
            v-if="item?.replies?.hasMore"
          >
            <span class="text"> 展开 </span>
            <CaretDownOutlined />
          </a>
        </template>
      </AskItem>
      <Empty v-if="!commentList.length && !loading" description="暂无提问" />
    </div>
    <div class="px-6">
      <div
        class="mt-8 w-full h-8 rounded-md flex items-center justify-center cursor-pointer text-sm text-[#1677ff] border-solid border-[1px] border-[#1677ff]"
        @click="expandFn"
        v-if="commentList?.length != total"
      >
        <span class="text"> 展开 </span>
        <CaretDownOutlined />
      </div>
    </div>
    <template #footer>
      <div class="footer">
        <Button @click="isShowAsk = true" class="w-full" v-if="!isShowAsk">
          我来提问
        </Button>
        <AskInput
          v-else
          type="comment"
          :course-id="detail?.courseId"
          :course-node-id="detail?.id"
          @success="onLoad_comment"
        />
      </div>
    </template>
  </Modal>
</template>
<script setup>
import { ref } from "vue";
import AskInput from "./AskInput.vue";
import AskItem from "./AskItem.vue";
import { getCommentList, getMoreReplyList } from "@/api/askQuestion";
import { Button, Empty, Modal } from "ant-design-vue";
import { CaretDownOutlined } from "@ant-design/icons-vue";

const emits = defineEmits(["success"]);
const dialogOpen = ref(false);
const detail = ref({});
const commentList = ref([]);
const isShowAsk = ref(false);
const loading = ref(false);
const total = ref(0);

const onLoad_comment = (after) => {
  loading.value = true;
  isShowAsk.value = false;
  console.log(detail.value);

  getCommentList({
    courseId: detail?.value?.courseId,
    courseNodeId: detail?.value?.id,
    after,
  })
    .then((res) => {
      if (after) {
        commentList.value = [
          ...commentList.value,
          ...(res?.data?.comments || []),
        ];
      } else {
        commentList.value = res?.data?.comments || [];
      }
      total.value = res?.data?.total || 0;
    })
    .finally(() => {
      loading.value = false;
    });
};

const openModal = (e = {}) => {
  detail.value = e;
  dialogOpen.value = true;
  onLoad_comment();
};

const expandFn = () => {
  const afterId = commentList.value?.[commentList.value?.length - 1]?.id;
  onLoad_comment(afterId);
};

const expandChildren = (record, reset) => {
  const lastNode =
    record?.replies?.replies?.[record?.replies?.replies?.length - 1];
  getMoreReplyList({
    rootId: record?.id,
    after: reset ? null : lastNode?.id,
  }).then((res) => {
    record.replies.hasMore = res?.data?.hasMore;
    if (reset) {
      record.replies.replies = res?.data?.replies;
    } else {
      record.replies.replies = [
        ...record.replies.replies,
        ...(res?.data?.replies || []),
      ];
    }
  });
};

defineExpose({
  openModal,
});
</script>
<style scoped lang="less">
.footer {
  background: #ffffff;
  box-shadow:
    0px 9px 28px 8px rgba(0, 0, 0, 0.05),
    0px 3px 6px -4px rgba(0, 0, 0, 0.12),
    0px 6px 16px 0px rgba(0, 0, 0, 0.08);
  border-radius: 0px 0px 0px 0px;
  border: 0px solid #d9d9d9;
  padding: 16px 20px;
  .askBtn {
    width: 100%;
    height: 32px;
    background: #ffffff;
    border-radius: 8px 8px 8px 8px;
    border: 1px solid #d9d9d9;
  }
}
</style>
