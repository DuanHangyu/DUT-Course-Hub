<template>
  <el-drawer
    v-model="drawer"
    title="课程评论"
    direction="rtl"
    size="960px"
    class="custom-class"
  >
    <div
      class="flex items-center justify-center h-full w-full"
      v-if="!commentList?.length"
    >
      <el-empty />
    </div>
    <div class="w-full space-y-6" v-else>
      <div
        class="box flex space-x-3"
        v-for="(item, index) in commentList"
        :key="index"
      >
        <div class="borderImg shrink-0 overflow-hidden">
          <Avatar />
        </div>
        <div class="grow overflow-hidden">
          <div class="flex items-center justify-between">
            <div class="space-x-2">
              <span class="text-base text-[#08233F] font-semibold">{{
                item?.userName
              }}</span>
              <span class="text-sm text-[#A8B0B9]">
                发表于{{ item?.createTime }}
              </span>
            </div>
            <a class="text-[#FF383C] text-sm cursor-pointer" @click="del(item)"
              >删除</a
            >
          </div>
          <div class="text-base text-[#5D5C68] mt-2">
            {{ item?.content }}
          </div>
        </div>
      </div>
    </div>
  </el-drawer>
</template>
<script setup>
import { ref } from "vue";
import { getCommentList, delComment } from "@/api/node";
import { useRoute } from "vue-router";
import { ElMessageBox } from "element-plus";
import Avatar from "@/components/Avatar.vue";

const drawer = ref(false);
const route = useRoute();
const commentTotal = ref(0);
const commentList = ref([]);

const del = (e) => {
  ElMessageBox.confirm("删除后不可恢复。", "确认删除吗？", {
    confirmButtonText: "删除",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    delComment({
      id: e?.id,
    }).then((res) => {
      onLoadComment();
    });
  });
};

const onLoadComment = () => {
  getCommentList({
    courseId: route.query.courseId,
  }).then((res) => {
    commentTotal.value = res?.total || 0;
    commentList.value = res?.comments || [];
  });
};
defineExpose({
  show: () => {
    drawer.value = true;
    onLoadComment();
  },
});
</script>
<style lang="scss">
.custom-class {
  border-radius: 8px 0px 0px 8px !important;
  .el-drawer__header {
    border-bottom: 1px solid rgba(0, 0, 0, 0.06) !important;
    margin-bottom: 0px;
    padding: 16px 20px;
  }
}
</style>
<style scoped lang="scss">
.box {
  width: 100%;
  background: #ffffff;
  box-shadow: 10px 10px 0px 0px #0c3a6c;
  border-radius: 30px 30px 30px 30px;
  border: 4px solid #0c3a6c;
  padding: 20px;
  .borderImg {
    width: 41px;
    height: 41px;
    box-shadow: 2px 2px 0px 0px #0c3a6c;
    border-radius: 100%;
    border: 2px solid #0c3a6c;
  }
}
</style>
