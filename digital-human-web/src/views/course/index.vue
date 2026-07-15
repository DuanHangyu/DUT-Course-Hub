<template>
  <div class="w-full overflow-y-auto overflow-x-hidden">
    <section class="h-190 relative z-30 bg1 flex items-center">
      <Topbar class="z-100" />
      <img
        src="@/assets/course/wl-13.png"
        class="w-33.75 h-64.5 absolute left-0 top-118"
        alt=""
      />
      <img
        src="@/assets/course/wl-12.png"
        class="absolute -top-10 left-0 w-70 h-120"
        alt=""
      />
      <img
        src="@/assets/course/wl-11.png"
        class="absolute top-6 right-0 w-42.25 h-82.25"
        alt=""
      />
      <div
        class="relative content-container mx-auto flex items-center overflow-hidden"
      >
        <section class="grow">
          <img
            src="@/assets/course/courseDetail.png"
            class="w-26 h-12"
            alt=""
          />
          <div class="qhxx mt-6">{{ courseDetail?.courseName }}</div>
          <div class="text mt-8">
            {{ courseDetail?.courseIntroduce }}
          </div>
        </section>
        <section class="relative shrink-0 ml-25 w-180 h-110 mt-20">
          <div class="border1 absolute rotate-6 z-2 right-6"></div>
          <div
            class="border2 absolute z-10 right-18 hover:-rotate-6 transition-all duration-300"
          >
            <img
              :src="courseDetail?.pictureUrl"
              class="size-full rounded-3xl"
              alt=""
            />
          </div>
        </section>
        <section
          class="circle1 absolute top-0 left-1/2 -translate-x-1/2 ml-20 z-1"
        ></section>
      </div>
      <div
        class="absolute content-container left-1/2 bottom-0 translate-y-1/2 -translate-x-1/2 h-26.5 info grid grid-cols-4"
      >
        <div
          class="flex items-center p-4 space-x-4 z-20"
          :style="index != 3 ? 'border-right: 3px solid #0c3a6c' : ''"
          v-for="(item, index) in list"
          :key="index"
        >
          <img
            :src="item.img"
            class="w-12 h-12 rounded-full"
            :class="index == 0 ? 'border-2 border-solid border-[#0C3A6C]' : ''"
            alt=""
          />
          <div class="space-y-1">
            <div class="text-xl text-[#7E8287]">{{ item.label }}</div>
            <div class="text-xl text-[#08233F] font-medium">
              {{ item.value }}
            </div>
          </div>
        </div>
      </div>
    </section>
    <section class="w-full relative h-237.5 pt-26 z-20 bg-white">
      <img
        src="@/assets/course/wl-9.png"
        class="absolute w-full h-full top-0 left-0"
        alt=""
      />
      <div class="zstp mx-auto rotate-2">Knowledge Graphs</div>
      <div
        class="text-center text-[#0C3A6C] text-[44px] font-semibold mt-4 relative"
      >
        知识图谱
      </div>
      <div class="text-2xl text-[#374960] text-center mt-2 relative">
        可视化知识结构，助力高效学习
      </div>

      <div class="zstp-box content-container mx-auto mt-4 relative h-154">
        <div class="absolute right-0 -top-16 switchBtn grid grid-cols-2">
          <div
            class="simpleBtn"
            :class="activeBtn == 1 ? 'activeSimpleBtn' : ''"
            @click="changeTab(1)"
          >
            图谱视图
          </div>
          <div
            class="simpleBtn"
            :class="activeBtn == 2 ? 'activeSimpleBtn' : ''"
            @click="changeTab(2)"
          >
            列表视图
          </div>
        </div>
        <KnowledgeGraph
          :detail="courseDetail"
          v-if="courseDetail.id && activeBtn == 1"
        />
        <ListGraph v-if="activeBtn == 2" :list="courseDetail?.nodes?.length ? [{ pathIndex: 1, nodes: courseDetail.nodes }] : []" />
        <div
          class="qjzlk absolute right-5 top-4 flex items-center justify-center cursor-pointer"
          @click="show_qjzlk = true"
        >
          <span>全局资料库</span>
          <img src="@/assets/course/arrow.png" class="h-6" alt="" />
        </div>
        <Qjzlk v-if="show_qjzlk" @close="show_qjzlk = false" />
      </div>
    </section>
    <section class="w-full relative">
      <img
        src="@/assets/course/wl-8.png"
        class="w-34.75 h-50.25 absolute top-0 -translate-y-1/2 right-0"
        alt=""
      />
      <div class="content-container mx-auto grid grid-cols-3 gap-6 py-20">
        <div
          class="box3 relative overflow-hidden"
          v-for="(item, index) in listBox"
          :key="index"
          :class="`box_${index}`"
        >
          <img
            :src="item.img"
            class="w-16.75 h-16.75 absolute top-0 right-0 rightIcon"
            alt=""
          />
          <div class="flex items-center space-x-4">
            <div
              class="no flex items-center justify-center"
              :style="`background:${item.color}`"
            >
              <img :src="item.no" alt="" />
            </div>
            <span class="text-[28px] font-semibold text-[#0C3A6C]">
              {{ item.title }}
            </span>
          </div>
          <div class="text-[18px] text-[#0C3A6C] mt-2 line-clamp-5">
            {{ item.desc }}
          </div>
        </div>
      </div>
    </section>
    <section class="w-full relative bg4">
      <div class="relative content-container mx-auto pt-26 pb-20">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-6">
            <div class="relative">
              <div class="kcpl -rotate-2 relative z-10">课程评论</div>
              <div
                class="w-15.75 h-15.75 border-solid border-[#0C3A6C] border-4 rotate-45 absolute -top-13 left-5 bg-white"
              ></div>
            </div>
            <div
              class="leading-10 bg-white px-6 border-solid border-[#0C3A6C] border-2 text-base font-medium text-[#0C3A6C] rounded-[100px]"
            >
              共 {{ commentTotal }} 条评论
            </div>
          </div>
          <div class="flex items-center space-x-5">
            <div class="addCommentBtn" @click="startComment">我要评论</div>
            <div class="lookpl cursor-pointer space-x-2" @click="openComment">
              <span>查看全部评论</span>
              <img src="@/assets/course/arrow.png" alt="" />
            </div>
          </div>
        </div>
        <div class="mt-8 grid grid-cols-3 gap-x-8">
          <div
            class="box3-2 flex space-x-3 transition-all duration-300 ease-in-out"
            v-for="(item, index) in commentList.slice(0, 3)"
            :key="index"
            :class="
              index == 1
                ? 'rotate-1 hover:-rotate-3'
                : '-rotate-1 hover:rotate-3'
            "
          >
            <div class="borderImg shrink-0 overflow-hidden">
              <Avatar />
            </div>
            <div class="grow overflow-hidden">
              <div class="space-x-2">
                <span class="text-base text-[#08233F] font-semibold">{{
                  item?.userName
                }}</span>
                <span class="text-sm text-[#A8B0B9]">
                  发表于{{ item?.createTime }}
                </span>
              </div>
              <div class="text-base text-[#5D5C68] mt-2 line-clamp-4">
                {{ item?.content }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
    <div class="w-full h-4.25 bg-[#FF6B6B]"></div>
    <div class="w-full h-4.25 bg-[#4ADE80]"></div>
    <div class="w-full h-4.25 bg-[#2A6EBB]"></div>
    <Comment ref="cRef" />
    <el-dialog
      v-model="dialogVisible"
      title="添加评论"
      width="860px"
      class="commentDialog"
    >
      <div class="commentBox">
        <el-input v-model="commentText" type="textarea" placeholder="请输入" />
        <div class="sendBox flex justify-end">
          <div class="btn cursor-pointer" @click="sendComment">发送</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
<script setup>
import Topbar from "@/components/Topbar.vue";
import { getCurrentInstance, onMounted, ref } from "vue";
import wl4 from "@/assets/course/wl-4.png";
import wl5 from "@/assets/course/wl-5.png";
import wl6 from "@/assets/course/wl-6.png";
import wl1 from "@/assets/course/wl-1.png";
import wl2 from "@/assets/course/wl-2.png";
import wl3 from "@/assets/course/wl-3.png";
import no1 from "@/assets/course/no1.png";
import no2 from "@/assets/course/no2.png";
import no3 from "@/assets/course/no3.png";
import Comment from "./components/Comment.vue";
import KnowledgeGraph from "./components/KnowledgeGraph.vue";
import { getCourseDetail } from "@/api/course";
import { useRoute } from "vue-router";
import { getCommentList, saveComment } from "@/api/node";
import Avatar from "@/components/Avatar.vue";
import Qjzlk from "./components/Qjzlk.vue";
import ListGraph from "./components/ListGraph.vue";

const list = ref([
  {
    label: "授课老师：",
    value: "",
    img: undefined,
  },
  {
    label: "课程节点：",
    value: "",
    img: wl4,
  },
  {
    label: "发布时间：",
    value: "",
    img: wl5,
  },
  {
    label: "课程分类：",
    value: "",
    img: wl6,
  },
]);

const listBox = ref([
  {
    img: wl1,
    no: no1,
    color: "#FB837D",
    title: "模块化学习",
    desc: "知识图谱拆解内容为模块，以闯关形式学习，可自主选择模块，提升效率与知识留存。 ",
  },
  {
    img: wl2,
    no: no2,
    color: "#4FD8C8",
    title: "AI数字人导师",
    desc: "AI 交互塑造数字人，依算法推送内容、答疑，助学习者培养自主学习与批判性思维。 ",
  },
  {
    img: wl3,
    no: no3,
    color: "#2A6EBB",
    title: "动态路径解锁",
    desc: " 三大引擎结合，根据评测动态调整学习路径，打造以学习者为中心的沉浸式体验。 ",
  },
]);

const { proxy } = getCurrentInstance();

const route = useRoute();
const courseDetail = ref({});
const commentList = ref([]);
const commentTotal = ref(0);
const dialogVisible = ref(false);
const commentText = ref("");
const show_qjzlk = ref(false);
const activeBtn = ref(1);

const openComment = () => {
  proxy.$refs.cRef?.show();
};

const startComment = () => {
  commentText.value = "";
  dialogVisible.value = true;
};

const sendComment = () => {
  saveComment({
    courseId: route.query.courseId,
    content: commentText.value,
  }).then((res) => {
    dialogVisible.value = false;
    commentText.value = "";
    getCommentList({
      courseId: route.query.courseId,
    }).then((res) => {
      commentTotal.value = res?.total || 0;
      commentList.value = res?.comments || [];
    });
  });
};

const changeTab = (e) => {
  activeBtn.value = e;
};

onMounted(() => {
  getCourseDetail({ id: route.query.courseId }).then((res) => {
    courseDetail.value = res || {};
    list.value[0].value = courseDetail.value.teacherName || "-";
    list.value[0].img = courseDetail.value.headUrl;
    list.value[1].value = courseDetail.value.nodeSize || 0;
    list.value[2].value = courseDetail.value.publishTime || "-";
    list.value[3].value = courseDetail.value.subject || "-";
  });
  getCommentList({
    courseId: route.query.courseId,
  }).then((res) => {
    commentTotal.value = res?.total || 0;
    commentList.value = res?.comments || [];
  });
});
</script>
<style scoped lang="scss">
.qhxx {
  // width: 268px;
  line-height: 77px;
  background: #2a6ebb;
  box-shadow: 8px 8px 0px 0px #0c3a6c;
  border-radius: 0px 0px 0px 0px;
  border: 2px solid #0c3a6c;
  font-weight: 600;
  font-size: 38px;
  color: #ffffff;
  text-align: center;
}
.text {
  border-left: 4px solid #2a6ebb;
  padding-left: 16px;
  width: 100%;
  font-weight: 400;
  font-size: 24px;
  color: #384a5f;
}
.border1 {
  width: 609px;
  height: 391px;
  background: #ffffff;
  border-radius: 30px 30px 30px 30px;
  border: 4px solid #0c3a6c;
}
.border2 {
  width: 586px;
  height: 360px;
  background: #ffffff;
  box-shadow: 10px 10px 0px 0px #0c3a6c;
  border-radius: 30px 30px 30px 30px;
  border: 4px solid #0c3a6c;
  padding: 6px;
}
.circle1 {
  width: 424px;
  height: 424px;
  border: 3px dashed #d1e0f0;
  border-radius: 100%;
}
.info {
  height: 118px;
  background: #ffffff;
  box-shadow: 10px 10px 0px 0px #0c3a6c;
  border-radius: 30px 30px 30px 30px;
  border: 4px solid #0c3a6c;
}
.zstp {
  width: 171px;
  line-height: 32px;
  background: #2a6ebb;
  box-shadow: 3px 3px 0px 0px #000000;
  border-radius: 100px 100px 100px 100px;
  border: 1px solid #000000;
  font-weight: 500;
  font-size: 14px;
  color: #ffffff;
  text-align: center;
}

.zstp-box {
  background: #111827;
  box-shadow: 10px 10px 0px 0px #0c3a6c;
  border-radius: 30px 30px 30px 30px;
  border: 4px solid #0c3a6c;
}

.box3 {
  width: 100%;
  height: 237px;
  background: #ffffff;
  border-radius: 30px 30px 30px 30px;
  border: 4px solid #0c3a6c;
  padding: 18px;
  transition: all 0.3s;
  .no {
    width: 54px;
    height: 54px;
    box-shadow: 2px 2px 0px 0px #0c3a6c;
    border-radius: 100px 100px 100px 100px;
    border: 2px solid #0c3a6c;
    img {
      width: 26px;
      height: 20px;
    }
  }
  &:hover {
    transform: translateY(-10px);
    .rightIcon {
      transform: scale(1.4);
    }
  }
}
.box_0 {
  &:hover {
    box-shadow: 6px 6px 0px 0px #fb837d;
  }
}
.box_1 {
  &:hover {
    box-shadow: 6px 6px 0px 0px #4fd8c8;
  }
}
.box_2 {
  &:hover {
    box-shadow: 6px 6px 0px 0px #2a6ebb;
  }
}

.kcpl {
  width: 220px;
  height: 72px;
  line-height: 72px;
  background: #ffffff;
  box-shadow: 10px 10px 0px 0px #0c3a6c;
  border-radius: 0px 0px 0px 0px;
  border: 4px solid #0c3a6c;
  font-weight: 600;
  font-size: 36px;
  color: #0c3a6c;
  text-align: center;
}

.addCommentBtn {
  width: 96px;
  line-height: 48px;
  background: #ffffff;
  box-shadow: 4px 4px 0px 0px #0c3a6c;
  border-radius: 100px 100px 100px 100px;
  border: 2px solid #0c3a6c;
  font-weight: 500;
  font-size: 16px;
  color: #0c3a6c;
  text-align: center;
  cursor: pointer;
}

.lookpl {
  padding: 0px 16px;
  height: 48px;
  background: #ffffff;
  box-shadow: 4px 4px 0px 0px #0c3a6c;
  border-radius: 100px 100px 100px 100px;
  border: 2px solid #0c3a6c;
  font-weight: 500;
  font-size: 16px;
  color: #0c3a6c;
  display: flex;
  align-items: center;
  justify-content: center;
  img {
    width: 24px;
    height: 24px;
  }
}

.box3-2 {
  width: 100%;
  height: 162px;
  background: #ffffff;
  box-shadow: 10px 10px 0px 0px #0c3a6c;
  border-radius: 30px 30px 30px 30px;
  border: 4px solid #0c3a6c;
  padding: 14px;
  .borderImg {
    width: 41px;
    height: 41px;
    box-shadow: 2px 2px 0px 0px #0c3a6c;
    border-radius: 100%;
    border: 2px solid #0c3a6c;
  }
}
.bg1 {
  background-color: #fff;
  background-image:
    radial-gradient(circle, #dfe4ed 2px, transparent 2px),
    radial-gradient(circle, #dfe4ed 2px, transparent 2px);
  background-size: 20px 20px;
  background-position: 0 0;
}
.bg4 {
  background: #2a6ebb;
  background-image:
    radial-gradient(circle, #437fc2 2px, transparent 2px),
    radial-gradient(circle, #437fc2 2px, transparent 2px);
  background-size: 20px 20px;
  background-position: 0 0;
  // clip-path: polygon(0 0, 100% 0, 100% 100%, 0 90%);
  clip-path: polygon(0 8%, 100% 0, 100% 100%, 0 100%);
}

.commentBox {
  width: 800px;
  height: 250px;
  background: #ffffff;
  box-shadow: 10px 10px 0px 0px #0c3a6c;
  border-radius: 12px 12px 12px 12px;
  border: 4px solid #0c3a6c;
  padding: 16px;
  display: flex;
  flex-direction: column;
  :deep(.el-textarea) {
    flex-grow: 1;
    border: initial;
    .el-textarea__inner {
      height: 100%;
      border: initial;
      box-shadow: initial;
      resize: none;
    }
  }
  .sendBox {
    margin-top: 16px;
    flex-shrink: 0;
    border-top: 1px solid #d9d9d9;
    padding-top: 16px;
    .btn {
      width: 72px;
      line-height: 36px;
      background: #0c3a6c;
      border-radius: 100px 100px 100px 100px;
      font-weight: 400;
      font-size: 16px;
      color: #ffffff;
      text-align: center;
    }
  }
}
.qjzlk {
  width: 138px;
  height: 48px;
  background: #ffffff;
  box-shadow: 4px 4px 0px 0px #0c3a6c;
  border-radius: 100px 100px 100px 100px;
  border: 2px solid #0c3a6c;
  font-weight: 500;
  font-size: 16px;
  color: #0c3a6c;
}
.switchBtn {
  width: 192px;
  height: 48px;
  border-radius: 100px;
  border: 2px solid #0c3a6c;
  background: #fff;
  overflow: hidden;
  .simpleBtn {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 500;
    font-size: 16px;
    color: #0c3a6c;
    cursor: pointer;
  }
  .activeSimpleBtn {
    background: #0c3a6c;
    color: #fff;
  }
}
</style>
<style lang="scss">
.commentDialog {
  border-radius: 20px;
  .el-dialog__title {
    font-weight: 600;
    font-size: 18px;
    color: #0c3a6c;
  }
  .el-dialog__header {
    text-align: center;

    &::before {
      display: none;
    }
  }
  .el-dialog__headerbtn {
    .el-icon {
      color: black;
    }
  }
  .el-dialog__body {
    padding-top: 6px;
  }
}
</style>
