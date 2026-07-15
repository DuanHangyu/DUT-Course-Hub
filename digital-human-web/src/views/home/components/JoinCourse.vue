<template>
  <el-dialog v-model="dialogVisible" title="加入课程" width="550px">
    <div class="flex items-center">
      <span class="shrink-0">邀请码：</span>
      <el-input v-model="inviteCode" placeholder="请输入邀请码"></el-input>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="joinCourseFn"> 加入 </el-button>
      </div>
    </template>
  </el-dialog>
</template>
<script setup>
import { ElMessage } from "element-plus";
import { ref } from "vue";
import { joinCourse } from "@/api/home";
import { useRouter } from "vue-router";

const dialogVisible = ref(false);
const detail = ref({});
const inviteCode = ref("");
const router = useRouter();

const joinCourseFn = async () => {
  if (!inviteCode.value) {
    ElMessage.error("请输入验证码");
    return;
  }
  joinCourse({
    courseId: detail.value?.id,
    inviteCode: inviteCode.value,
  }).then(() => {
    dialogVisible.value = false;
    router.push(`/course?courseId=${detail.value.id}`);
  });
};

defineExpose({
  show: function (r) {
    dialogVisible.value = true;
    detail.value = r;
    inviteCode.value = "";
  },
});
</script>
