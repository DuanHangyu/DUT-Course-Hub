<template>
  <el-dialog
    v-model="dialogVisible"
    modal-class="commentModal"
    title="添加回复"
    width="860px"
    @close="$emit('success')"
  >
    <div class="input flex flex-col p-4">
      <el-input v-model="msg" type="textarea" placeholder="输入你的问题" />
      <div class="flex items-center justify-between action shrink-0 mt-4">
        <el-upload
          :file-list="fileList"
          name="file"
          accept=".png,.jpg,.jpeg,.pdf,.doc,.docx,.ppt,.pptx"
          :http-request="httpRequest"
          :before-remove="removeFile"
        >
          <el-button class="flex items-center justify-center">
            <el-icon>
              <Upload />
            </el-icon>
            <span>添加图片或文件</span>
          </el-button>
        </el-upload>
        <div class="btn" @click="sendFn">发送</div>
      </div>
    </div>
  </el-dialog>
</template>
<script setup>
import { ref } from "vue";
import { Upload } from "@element-plus/icons-vue";
import { useRoute } from "vue-router";
import { saveReply } from "@/api/node";
import { generateUploadUrl } from "@/api/api";
import { ElMessage } from "element-plus";
import axios from "axios";

defineEmits(["success"]);

const dialogVisible = ref(false);

const msg = ref("");
const detail = ref({});
const route = useRoute();
const uploadVideoLoading = ref(false);
const fileList = ref([]);

const sendFn = () => {
  if (!msg.value && !fileList.value.length) {
    return;
  }

  const imgs = [];
  const files = [];
  for (let index = 0; index < fileList.value?.length; index++) {
    const element = fileList.value?.[index];
    if (
      element?.fileName?.includes(".png") ||
      element?.fileName?.includes(".jpg") ||
      element?.fileName?.includes(".jpeg")
    ) {
      imgs.push(element);
    } else {
      files.push(element);
    }
  }
  const data = {
    ...detail.value,
    content: msg.value,
    courseId: route.query.courseId,
    courseNodeId: route.query.nodeId,
    files: files,
    pictures: imgs,
  };
  saveReply(data).then(() => {
    dialogVisible.value = false;
  });
};

const httpRequest = async (e) => {
  const { file } = e;
  try {
    uploadVideoLoading.value = true;
    e.onProgress({ percent: 0 });
    const res = await generateUploadUrl({
      fileName: file?.name,
      contentType: file?.type,
    });
    const presignedUrl = res?.presignedUrl;
    const res2 = await axios.put(presignedUrl, file, {
      headers: {
        "Content-Type": file.type || "application/octet-stream",
      },
      onUploadProgress: function (progressEvent) {
        if (progressEvent.lengthComputable) {
          const percentCompleted = Math.round(
            (progressEvent.loaded * 100) / progressEvent.total,
          );
          e.onProgress({ percent: percentCompleted });
        }
      },
    });
    if (res2.status == 200) {
      // e.onSuccess({
      //   url: res.data?.url,
      //   name: res.data?.fileName,
      // });
      fileList.value.push({
        url: res?.url,
        fileName: res?.fileName,
        uid: file.uid,
      });
    } else {
      ElMessage.error("上传失败");
      e.onError("上传失败");
    }
  } catch (error) {
    e.onError(error);
  } finally {
    uploadVideoLoading.value = false;
  }
};

const removeFile = (e) => {
  fileList.value = fileList.value.filter((item) => item.uid != e.uid);
  return false;
};

defineExpose({
  openModal: (r) => {
    detail.value = r;
    msg.value = "";
    fileList.value = [];
    dialogVisible.value = true;
  },
});
</script>
<style scoped lang="scss">
.input {
  width: 800px;
  background: #ffffff;
  box-shadow: 10px 10px 0px 0px #0c3a6c;
  border-radius: 12px 12px 12px 12px;
  border: 4px solid #0c3a6c;

  :deep(.el-textarea) {
    height: 100%;

    .el-textarea__inner {
      height: 200px;
      background: initial;
      border: initial;
      box-shadow: initial;
      resize: none;
      font-weight: 400;
      font-size: 14px;
      color: #0c3a6c;
    }
  }

  .action {
    border-top: 1px solid #d9d9d9;
    padding-top: 12px;

    .btn {
      width: 72px;
      line-height: 36px;
      background: #0c3a6c;
      border-radius: 100px 100px 100px 100px;
      font-weight: 400;
      font-size: 16px;
      color: #ffffff;
      text-align: center;
      cursor: pointer;
    }
  }
}
</style>
<style lang="scss">
.commentModal {
  .el-dialog {
    border-radius: 20px 20px 20px 20px;

    .el-dialog__header {
      position: relative;
      height: 20px;

      &::before {
        display: none;
      }

      .el-dialog__title {
        display: block;
        width: 100%;
        text-align: center;
        font-weight: 600;
        font-size: 18px;
        color: #0c3a6c;
      }

      .el-dialog__headerbtn {
        top: -10px;
        right: -10px;

        .el-icon {
          color: black;
        }
      }
    }
  }
}
</style>
