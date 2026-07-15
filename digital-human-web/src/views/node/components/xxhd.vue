<template>
  <div class="size-full flex flex-col py-8 px-4">
    <div class="grow overflow-y-auto">
      <div
        class="size-full flex items-center justify-center"
        v-if="!commentList?.length"
      >
        <el-empty />
      </div>
      <el-scrollbar height="100%" v-else>
        <div class="px-4 space-y-4">
          <Item
            v-for="(item1, index1) in commentList"
            :key="item1?.id"
            :record="item1"
            @reply="addCommentFn(item1)"
            :is-child="!!item1?.replies?.replies?.length"
          >
            <template #chart v-if="item1?.replies?.replies?.length">
              <div class="space-y-4">
                <Item
                  v-for="item2 in item1?.replies?.replies"
                  :key="item2?.id"
                  :record="item2"
                  @reply="addCommentFn(item2)"
                  isBg
                />
              </div>
              <div
                class="cursor-pointer text-sm text-[#0052CC] pl-15 w-fit mt-3"
                @click="getMore(item1, index1)"
                v-if="item1?.replies?.hasMore"
              >
                展开更多
              </div>
            </template>
          </Item>
        </div>
      </el-scrollbar>
    </div>
    <div class="shrink-0 send mt-6">
      <div class="input relative">
        <div class="border1 relative top-0 left-0 z-2 p-2">
          <el-input v-model="msg" type="textarea" placeholder="输入你的问题" />

          <el-upload
            :show-file-list="false"
            name="file"
            accept=".png,.jpg,.jpeg,.pdf,.doc,.docx,.ppt,.pptx"
            :http-request="httpRequest"
            class="w-10 h-10 absolute bottom-2 right-15"
          >
            <el-tooltip
              placement="top"
              v-bind="fileList?.length ? {} : { visible: false }"
            >
              <el-badge :value="fileList?.length" :max="99" :show-zero="false">
                <div class="uploadBtn">
                  <el-icon>
                    <Upload />
                  </el-icon>
                </div>
              </el-badge>
              <template #content>
                <div class="w-50">
                  <section
                    class="py-1 w-full border-solid border-b border-b-gray-200 flex items-center justify-between overflow-hidden space-x-2"
                    v-for="(item, index) in fileList"
                    :key="index"
                  >
                    <div class="grow truncate">{{ item?.fileName }}</div>
                    <el-icon
                      class="shrink-0 cursor-pointer hover:text-red"
                      @click="removeFile(item)"
                      ><Delete
                    /></el-icon>
                  </section>
                </div>
              </template>
            </el-tooltip>
          </el-upload>

          <img
            src="@/assets/node/send.png"
            class="w-10 h-10 absolute bottom-2 right-2"
            alt=""
            v-if="!msg && !fileList?.length"
          />
          <img
            src="@/assets/node/send2.png"
            class="w-10 h-10 absolute bottom-2 right-2 cursor-pointer"
            alt=""
            v-else
            @click="sendFn"
          />
        </div>
        <div class="border2 absolute -bottom-3 -right-2 z-1"></div>
      </div>
    </div>
    <addComment ref="addCommentRef" @success="onLoad" />
  </div>
</template>
<script setup>
import { onMounted, ref } from "vue";
import Item from "./Item.vue";
import { saveComment, getReplyList, getCommentList } from "@/api/node";
import { useRoute } from "vue-router";
import addComment from "./addComment.vue";
import { Upload, Delete } from "@element-plus/icons-vue";
import { generateUploadUrl } from "@/api/api";
import { ElMessage } from "element-plus";
import axios from "axios";

const props = defineProps({
  detail: {
    type: Object,
    default: () => ({}),
  },
});

const msg = ref();
const route = useRoute();
const commentTotal = ref(0);
const commentList = ref([]);
const addCommentRef = ref();
const fileList = ref([]);
const uploadVideoLoading = ref(false);
const onLoad = () => {
  getCommentList({
    courseId: route.query.courseId,
    courseNodeId: route.query?.nodeId,
  }).then((res) => {
    commentTotal.value = res?.total || 0;
    commentList.value = res?.comments || [];
  });
};

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
  saveComment({
    courseId: route.query.courseId,
    courseNodeId: route.query?.nodeId,
    content: msg.value,
    files: files,
    pictures: imgs,
  }).then((res) => {
    msg.value = "";
    fileList.value = [];
    onLoad();
  });
};

const addCommentFn = (r) => {
  addCommentRef.value.openModal({
    commentId: r?.id,
  });
};

const getMore = (e, index1) => {
  getReplyList({
    rootId: e?.id,
  }).then((res) => {
    commentList.value[index1].replies = res || {};
  });
};

onMounted(() => {
  onLoad();
});

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
};
</script>
<style scoped lang="scss">
.send {
  border-top: 2px solid #e2e8f0;
  padding-top: 20px;
  .input {
    :deep(.el-textarea) {
      height: 100%;
      .el-textarea__inner {
        height: 100%;
        background: initial;
        border: initial;
        box-shadow: initial;
        resize: none;
        font-weight: 400;
        font-size: 14px;
        color: #0c3a6c;
      }
    }
  }
  .uploadBtn {
    width: 40px;
    height: 40px;
    border: 2px solid #0c3a6c;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 15px;
  }
  .border1 {
    width: calc(100% - 6px);
    height: 111px;
    background: #f9fafc;
    border-radius: 20px 20px 20px 20px;
    border: 2px solid #0c3a6c;
  }
  .border2 {
    width: 100%;
    height: 114px;
    background: #f9fafc;
    border-radius: 20px 20px 20px 20px;
    border: 2px solid #0c3a6c;
  }
}
</style>
