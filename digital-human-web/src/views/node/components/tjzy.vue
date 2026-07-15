<template>
  <div class="size-full py-8 px-4">
    <div
      class="size-full flex items-center justify-center"
      v-if="!homeWorkList?.length"
    >
      <el-empty />
    </div>
    <el-scrollbar height="100%" v-else>
      <main
        class="px-4 flex space-x-5 mb-4"
        v-for="(item, index) in homeWorkList"
        :key="item.id"
      >
        <img
          src="@/assets/node/zkIcon.png"
          class="size-7 cursor-pointer shrink-0"
          :class="zkList?.includes(index) ? '' : '-rotate-90'"
          alt=""
          @click="zkSq(index)"
        />
        <div
          class="text-[20px] font-semibold text-[#0E172A] grow truncate"
          v-if="!zkList?.includes(index)"
        >
          {{ item?.title }}
        </div>
        <section class="grow overflow-hidden" v-else>
          <div class="space-y-2">
            <div class="text-[20px] font-semibold text-[#0E172A]">
              {{ item?.title }}
            </div>
            <div class="text-sm text-[#0E172A]">
              {{ item?.description }}
            </div>
            <div class="flex space-x-1">
              <span class="text-sm text-[#0E172A]">附件：</span>
              <div class="space-x-0.5">
                <section
                  class="flex items-center space-x-1"
                  v-for="(item2, index2) in item?.attachments"
                  :key="index2"
                >
                  <span class="text-sm text-[#225DD9]">
                    {{ item2?.fileName }}
                  </span>
                  <div
                    class="px-2 py-0.5 rounded-[100px] bg-[#0045AC] text-[10px] text-[#FFFFFF] cursor-pointer"
                    @click="downFn(item2?.url)"
                  >
                    下载
                  </div>
                </section>
              </div>
            </div>
            <div
              class="bg-[#D8E5FA] w-fit border-solid border border-[#B6CDF1] rounded-lg py-0.5 px-1 text-sm text-[#225DD9]"
            >
              截止时间：{{ item?.deadline }}
            </div>
          </div>
          <el-upload
            class="w-full mt-5"
            name="file"
            accept=".png,.jpg,.jpeg,.pdf,.doc,.docx,.ppt,.pptx,.zip"
            :http-request="(e) => httpRequest(e, item)"
            :show-file-list="false"
            v-loading="uploadVideoLoading"
            drag
          >
            <div
              class="upload flex flex-col items-center justify-center space-y-4"
            >
              <div class="icon">
                <img src="@/assets/node/upload.png" alt="" />
              </div>
              <div class="text text-center">拖拽或点击上传文件</div>
              <div class="uploadBtn">上传文件</div>
            </div>
          </el-upload>
          <div class="my-4 text-lg text-[#0C3A6C] font-medium">已上传文件</div>
          <div
            class="h-50 w-full flex items-center justify-center"
            v-if="!item.myFiles?.length"
          >
            <el-empty :image-size="100" />
          </div>
          <div class="space-y-4" v-else>
            <section
              class="flex items-center space-x-3"
              v-for="(item3, index3) in item.myFiles"
              :key="index3"
            >
              <div class="flex items-center justify-between file grow">
                <div class="flex items-center space-x-3">
                  <div class="img">
                    <fileIcon class="text-[#0052CC]" />
                  </div>
                  <span>{{ item3?.fileName }}</span>
                </div>
                <div class="flex items-center space-x-4">
                  <img
                    src="@/assets/node/eye.png"
                    class="size-8 cursor-pointer"
                    alt=""
                    @click="viewPdf(item3.presignedUrl)"
                    v-if="
                      item3?.url?.includes('.pdf') ||
                      item3?.url?.includes('.jpg') ||
                      item3?.url?.includes('.png') ||
                      item3?.url?.includes('.jpeg')
                    "
                  />
                  <img
                    src="@/assets/node/download.png"
                    class="size-8 cursor-pointer"
                    alt=""
                    @click="downFn(item3.presignedUrl)"
                  />
                  <el-icon
                    v-if="item?.reviewStatus == 0"
                    color="red"
                    size="24px"
                    class="cursor-pointer"
                    @click="delFn(index, index3)"
                  >
                    <Delete />
                  </el-icon>
                </div>
              </div>
              <div
                class="flex items-center space-x-3 shrink-0"
                v-if="item?.reviewStatus == 1"
              >
                <div class="ypg">已批改</div>
                <span class="score">{{ item?.score }}分数</span>
              </div>
              <div class="shrink-0 dpg mr-2" v-if="item?.reviewStatus == 0">
                待批改
              </div>
            </section>
          </div>
        </section>
      </main>
    </el-scrollbar>
    <PreviewHomeWord ref="rRef" />
  </div>
</template>
<script setup>
import { onMounted, ref } from "vue";
import fileIcon from "../icons/file.vue";
import {
  getHomeWorkList,
  submitHomeWork,
  getMyHomeWork,
  getHomeWorkDetail,
  delHomeWork,
} from "@/api/node";
import { useRoute } from "vue-router";
import { generateUploadUrl } from "@/api/api";
import { ElMessage } from "element-plus";
import axios from "axios";
import PreviewHomeWord from "./PreviewHomeWord.vue";
import { Delete } from "@element-plus/icons-vue";

const props = defineProps({
  detail: {
    type: Object,
    default: () => ({}),
  },
});

const rRef = ref(null);
const route = useRoute();
const homeWorkList = ref([]);
const uploadVideoLoading = ref(false);
const zkList = ref([]);

const onLoad = () => {
  getHomeWorkList({
    courseId: route.query?.courseId,
  }).then((res) => {
    homeWorkList.value = res || [];
  });
};

const allowedTypes = [
  "image/png",
  "image/jpeg",
  "application/pdf",
  "application/msword", // .doc
  "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
  "application/vnd.ms-powerpoint", // .ppt
  "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
];

const httpRequest = async (e, record) => {
  const { file } = e;
  const isValidType = allowedTypes.includes(file.type);
  if (!isValidType) {
    ElMessage.error("仅支持 PNG、JPG、JPEG、PDF、DOC、DOCX、PPT、PPTX 文件");
    return false; // 阻止上传
  }
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
      const currentIndex = homeWorkList.value.findIndex(
        (item) => item.id == record?.id,
      );
      submitHomeWork({
        homeworkId: record?.id,
        files: [res, ...(homeWorkList.value[currentIndex].myFiles || [])],
      }).then(() => {
        getHomeWorkDetail({
          homeworkId: record?.id,
        }).then((res) => {
          homeWorkList.value[currentIndex] = res;
        });
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

const zkSq = (e) => {
  if (zkList.value?.includes(e)) {
    zkList.value = zkList.value.filter((item) => item !== e);
  } else {
    getHomeWorkDetail({
      homeworkId: homeWorkList.value[e]?.id,
    }).then((res) => {
      homeWorkList.value[e] = res;
    });
    zkList.value.push(e);
  }
};

const viewPdf = (r) => {
  rRef.value.show(r);
};

const downFn = (e) => {
  window.open(e);
};

const delFn = (i, i2) => {
  if (homeWorkList.value[i]?.myFiles?.length == 1) {
    delHomeWork({
      id: homeWorkList.value[i]?.submitId,
    }).then(() => {
      getHomeWorkDetail({
        homeworkId: homeWorkList.value[i]?.id,
      }).then((res) => {
        homeWorkList.value[i] = res;
      });
    });
  } else {
    const list = homeWorkList.value[i]?.myFiles?.filter(
      (item, index) => index !== i2,
    );
    submitHomeWork({
      homeworkId: homeWorkList.value[i]?.id,
      files: list,
    }).then(() => {
      getHomeWorkDetail({
        homeworkId: homeWorkList.value[i]?.id,
      }).then((res) => {
        homeWorkList.value[i] = res;
      });
    });
  }
};

onMounted(() => {
  onLoad();
});
</script>
<style scoped lang="scss">
:deep(.el-upload) {
  width: 100%;
}
:deep(.el-upload-dragger) {
  width: 100%;
  height: 286px;
  background: #f0f6ff;
  border-radius: 16px 16px 16px 16px;
  border: 4px dashed #9ab5ea;
}
.upload {
  width: 100%;
  height: 100%;
  .icon {
    width: 72px;
    height: 72px;
    background: #ffffff;
    box-shadow: 4px 4px 0px 0px #001773;
    border-radius: 16px 16px 16px 16px;
    border: 2px solid #0c3a6c;
    display: flex;
    align-items: center;
    justify-content: center;
    img {
      width: 44px;
      height: 44px;
    }
  }
  .text {
    font-weight: 400;
    font-size: 16px;
    color: #0e172a;
  }
  .uploadBtn {
    width: 133px;
    line-height: 44px;
    background: #0045ac;
    box-shadow: 3px 3px 0px 0px #002d73;
    border-radius: 16px 16px 16px 16px;
    border: 2px solid #0c3a6c;
    font-weight: 500;
    font-size: 16px;
    color: #ffffff;
    text-align: center;
  }
}
.file {
  padding: 16px;
  background: #f0f6ff;
  border-radius: 16px 16px 16px 16px;
  border: 2px solid #dbeafe;
  font-weight: 400;
  font-size: 16px;
  color: #0e172a;
  .img {
    width: 40px;
    height: 40px;
    background: #d8e5fa;
    border-radius: 8px 8px 8px 8px;
    border: 1px solid #b6cdf1;
    display: flex;
    align-items: center;
    justify-content: center;
    img {
      width: 24.62px;
      height: 24.62px;
    }
  }
}

.ypg {
  width: 80px;
  line-height: 40px;
  background: rgba(52, 199, 89, 0.1);
  box-shadow: 4px 4px 0px 0px #34c759;
  border-radius: 100px 100px 100px 100px;
  border: 1px solid #34c759;
  font-weight: 500;
  font-size: 16px;
  color: #34c759;
  text-align: center;
}
.score {
  font-weight: 600;
  font-size: 32px;
  color: #0e172a;
}
.dpg {
  width: 80px;
  line-height: 40px;
  background: rgba(25, 119, 255, 0.1);
  box-shadow: 4px 4px 0px 0px #2a81ff;
  border-radius: 100px 100px 100px 100px;
  border: 1px solid #1977ff;
  font-weight: 500;
  font-size: 16px;
  color: #1977ff;
  text-align: center;
}
</style>
