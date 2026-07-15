<template>
  <div class="w-full overflow-y-auto overflow-x-hidden">
    <motion.div
      class="w-full aspect-1440/700 relative"
      :style="{
        opacity: heroOpacity,
        scale: heroScale,
        y: heroY,
      }"
    >
      <TopBar class="z-100" />
      <el-carousel
        :interval="4000"
        height="100%"
        width="100%"
        class="topZmd"
        motion-blur
      >
        <el-carousel-item v-for="(item, index) in banner" :key="index">
          <img :src="item?.imageUrl" class="size-full" alt="" />
        </el-carousel-item>
      </el-carousel>
    </motion.div>
    <section
      class="w-full min-h-137 relative flex flex-col items-center justify-center bg-white"
    >
      <img
        src="@/assets/home/wl-7.png"
        class="w-full h-15.75 absolute left-0 -top-15.75"
        alt=""
      />
      <img
        src="@/assets/home/wl-11.png"
        class="absolute w-full h-full bottom-0 left-0 z-2"
        alt=""
      />
      <img
        src="@/assets/home/wl-6.png"
        class="w-13.25 h-24 absolute top-0 left-0"
        alt=""
      />
      <img
        src="@/assets/home/wl-2.png"
        class="w-25.5 h-74 absolute -bottom-16 left-0 z-10"
        alt=""
      />
      <img
        src="@/assets/home/wl-5.png"
        class="w-47.75 h-166.5 absolute -bottom-107 right-0 z-10"
        alt=""
      />
      <img
        src="@/assets/home/wl-3.png"
        class="w-100 aspect-428/607 absolute bottom-0 right-0 z-1"
        alt=""
      />
      <img
        src="@/assets/home/kczx.png"
        class="w-lg h-17.5 block mx-auto"
        alt=""
      />
      <div class="text-[#0B3A6D] text-[32px] font-semibold text-center mt-6">
        精选课程
      </div>
      <main class="w-294 relative z-20">
        <!-- @click="carousel.prev()" -->
        <img
          src="@/assets/home/left-arrow.png"
          class="w-10 h-10 absolute top-1/2 left-0 -translate-y-1/2 cursor-pointer! z-10 arrow"
          alt=""
          @click="goLeft"
        />
        <!-- @click="carousel.next()" -->
        <img
          src="@/assets/home/right-arrow.png"
          class="w-10 h-10 absolute top-1/2 right-0 -translate-y-1/2 cursor-pointer! z-10 arrow"
          alt=""
          @click="goRight"
        />
        <div class="h-90 overflow-x-auto px-13">
          <!-- <el-carousel
            :interval="4000"
            height="369px"
            width="100%"
            indicator-position="none"
            arrow="never"
            ref="carousel"
          >
            <el-carousel-item v-for="item in 3" :key="item"> -->

          <el-scrollbar :always="false" ref="scrollbarRef" @scroll="scroll">
            <div class="w-full h-full flex items-center space-x-4 px-2 mt-4">
              <div
                class="card relative shrink-0 p-3.5 w-70.25 h-80.5"
                v-for="(item, index) in featuredList"
                :key="index"
              >
                <img
                  src="@/assets/home/kc-bg.png"
                  class="absolute top-0 left-0 w-full h-full"
                  alt=""
                />
                <div class="w-full h-36.75 relative">
                  <div class="hexagon-card">
                    <img :src="item?.pictureUrl" class="size-full" alt="" />
                  </div>
                </div>
                <div
                  class="text-[19px] font-semibold text-[#08233F] truncate relative mt-4"
                >
                  {{ item?.courseName }}
                </div>
                <div
                  class="text-base text-[#969CA3] font-normal mt-2 relative line-clamp-2 h-12.5"
                >
                  {{ item?.courseIntroduce }}
                </div>
                <div class="flex items-center space-x-2 relative mt-3 avatar">
                  <img
                    :src="item?.headUrl"
                    class="w-8 h-8 rounded-full overflow-hidden"
                    alt=""
                  />
                  <span class="text-sm text-[#094584] font-normal">{{
                    item?.teacherName
                  }}</span>
                </div>
                <div class="action flex items-center justify-center p-3">
                  <img
                    src="@/assets/home/jrkc.png"
                    class="w-full h-10 cursor-pointer"
                    alt=""
                    @click="goCourse(item)"
                  />
                </div>
              </div>
            </div>
          </el-scrollbar>
          <!-- </el-carousel-item>
          </el-carousel> -->
        </div>
      </main>
    </section>
    <section
      class="w-full h-133.5 relative bg-[#EBF5FCFF] pt-4 flex flex-col items-center justify-center"
    >
      <img
        src="@/assets/home/wl-9.png"
        class="absolute w-full h-84.5 bottom-0 left-0"
        alt=""
      />
      <img
        src="@/assets/home/wl-4.png"
        class="w-57.25 h-23.25 absolute -bottom-8.5 left-0 z-10"
        alt=""
      />
      <div class="text-[#0B3A6D] text-[32px] font-semibold text-center mt-6">
        全部课程
      </div>
      <div
        class="flex items-center space-x-8 w-294 px-15 mt-4 overflow-hidden relative z-10"
      >
        <el-tabs
          v-model="activeName"
          class="w-full"
          @tab-change="changeSubject"
        >
          <el-tab-pane
            :label="item"
            v-for="(item, index) in subjectList"
            :name="index"
          />
        </el-tabs>
      </div>
      <main class="w-294 relative z-20 flex items-center">
        <img
          src="@/assets/home/wl-14.png"
          class="w-86.25 h-88 absolute top-10 right-0"
          alt=""
        />
        <img
          src="@/assets/home/left-arrow.png"
          class="w-10 h-10 cursor-pointer! z-10 arrow shrink-0"
          alt=""
          @click="carousel2.prev()"
        />
        <div class="w-full">
          <el-carousel
            :interval="4000"
            height="330px"
            width="100%"
            indicator-position="none"
            arrow="never"
            ref="carousel2"
          >
            <el-carousel-item
              v-for="(item, index) in optionalList"
              :key="index"
            >
              <div class="pt-2 grid grid-cols-4 gap-x-4 px-6">
                <div
                  class="card relative shrink-0 p-3.5 h-71.25 w-full"
                  v-for="(item2, index2) in item"
                  :key="index2"
                >
                  <img
                    src="@/assets/home/kc-bg.png"
                    class="absolute top-0 left-0 w-full h-full"
                    alt=""
                  />
                  <div class="w-full h-30.75 relative">
                    <div class="hexagon-card">
                      <img :src="item2?.pictureUrl" class="size-full" alt="" />
                    </div>
                  </div>
                  <div
                    class="text-base font-semibold text-[#094584] truncate relative mt-3"
                  >
                    {{ item2?.courseName }}
                  </div>
                  <div
                    class="text-sm text-[#969CA3] font-normal mt-2 relative line-clamp-2"
                  >
                    {{ item2?.courseIntroduce }}
                  </div>
                  <div class="flex items-center space-x-2 relative mt-3 avatar">
                    <img
                      :src="item2?.headUrl"
                      class="w-8 h-8 rounded-full overflow-hidden"
                      alt=""
                    />
                    <span class="text-sm text-[#094584] font-normal">{{
                      item2?.teacherName
                    }}</span>
                  </div>
                  <div class="action flex items-center justify-center p-3">
                    <img
                      src="@/assets/home/jrkc.png"
                      class="w-full h-10 cursor-pointer"
                      alt=""
                      @click="goCourse(item2)"
                    />
                  </div>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
        <img
          src="@/assets/home/right-arrow.png"
          class="w-10 h-10 cursor-pointer! z-10 arrow shrink-0"
          alt=""
          @click="carousel2.next()"
        />
      </main>
    </section>
    <section
      class="w-full h-137.5 relative flex flex-col items-center justify-center pt-3"
    >
      <img
        src="@/assets/home/wl-8.png"
        class="absolute w-35.25 h-38.25 bottom-11.5 left-0"
        alt=""
      />
      <img
        src="@/assets/home/wl-13.png"
        class="absolute w-222.5 h-41 top-0 right-0"
        alt=""
      />
      <div class="w-294 h-100 relative py-4 px-10">
        <div
          class="bg-[#094584FF] absolute top-0 left-0 w-164.75 h-94.75"
        ></div>
        <div
          class="bg-[#094584FF] absolute bottom-0 right-0 w-159 h-8.25"
        ></div>
        <img
          src="@/assets/home/wl-12.png"
          class="absolute w-10 h-10 top-0 right-0"
          alt=""
        />
        <el-carousel
          :interval="4000"
          type="card"
          height="369px"
          width="100%"
          indicator-position="none"
        >
          <el-carousel-item v-for="(item, index) in background" :key="index">
            <div class="w-full h-full">
              <img
                :src="item?.imageUrl"
                class="w-full h-full img4"
                style=""
                alt=""
                @click="openPage(item)"
              />
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
      <img src="@/assets/home/wl-10.png" class="w-195.5 h-19.5 mt-1" alt="" />
    </section>
    <JoinCourse ref="joinRef" />
  </div>
</template>
<script setup>
import { nextTick, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import TopBar from "@/components/TopBar.vue";
import {
  getBanner,
  getBackground,
  getFeaturedCourse,
  getSubjectList,
  getOptionalCourse,
  checkCoursePermission,
} from "@/api/home";
import useUserStore from "@/store/modules/user";
import JoinCourse from "./components/JoinCourse.vue";
import { motion, useTransform, useScroll } from "motion-v";

const { scrollY } = useScroll();

// Smooth parallax and fade effects
const heroOpacity = useTransform(scrollY, [0, 700], [1, 0]);
const heroScale = useTransform(scrollY, [0, 700], [1, 1.08]);
const heroY = useTransform(scrollY, [0, 700], [0, 150]);
const navOpacity = useTransform(scrollY, [0, 300], [1, 0]);

const activeName = ref(0);
const carousel = ref(null);
const carousel2 = ref(null);
const router = useRouter();
const featuredList = ref([]);
const scrollbarRef = ref();
const scrollValue = ref(0);
const subjectList = ref([]);
const userStore = useUserStore();
const optionalList = ref([]);
const joinRef = ref(null);

function goCourse(item) {
  checkCoursePermission({
    courseId: item.id,
  }).then((res) => {
    if (!res) {
      joinRef.value.show(item);
    } else {
      router.push(`/course?courseId=${item.id}`);
    }
  });
}

const goLeft = () => {
  scrollValue.value =
    scrollbarRef.value?.wrapRef?.scrollLeft > 290
      ? scrollbarRef.value?.wrapRef?.scrollLeft - 290
      : 0;
  scrollbarRef.value?.setScrollLeft(scrollValue.value);
};
const goRight = () => {
  const v =
    scrollbarRef.value?.wrapRef?.scrollWidth -
    scrollbarRef.value?.wrapRef?.clientWidth;
  scrollValue.value =
    scrollbarRef.value?.wrapRef?.scrollLeft >= v
      ? v
      : scrollbarRef.value?.wrapRef?.scrollLeft + 290;
  scrollbarRef.value?.setScrollLeft(scrollValue.value);
};

const scroll = (e) => {
  scrollValue.value = e?.scrollTop;
};

const banner = ref([]);
const background = ref([]);

const changeSubject = (e) => {
  nextTick(() => {
    getOptionalCourse(
      subjectList.value[activeName.value] == "全部"
        ? {}
        : {
            subject: subjectList.value[activeName.value],
          },
    ).then((res) => {
      const result = [];
      for (let i = 0; i < res?.length; i += 4) {
        result.push(res?.slice(i, i + 4));
      }
      optionalList.value = result;
    });
  });
};

const openPage = (e) => {
  if (!e?.jumpUrl) {
    return;
  }
  window.open(e?.jumpUrl, "_blank");
};

onMounted(() => {
  getBanner().then((res) => {
    banner.value = res || [];
  });
  getBackground().then((res) => {
    background.value = res || [];
  });
  getFeaturedCourse().then((res) => {
    featuredList.value = res || [];
  });
  getSubjectList({ schoolId: userStore.user?.schoolId }).then((res) => {
    subjectList.value = ["全部", ...(res || [])];
  });
  changeSubject();
});
</script>
<style scoped lang="scss">
.hexagon-card {
  width: 100%;
  height: 100%;
  background-color: #f0f0f0;
  clip-path: polygon(
    0 14px,
    14px 0,
    100% 0,
    100% calc(100% - 14px),
    calc(100% - 14px) 100%,
    0 100%
  );
}
:deep(.topZmd) {
  height: 100%;
  .el-carousel__indicators--horizontal {
    bottom: 128px;
    .el-carousel__button {
      transform: skewX(-40deg);
      transform-origin: left center;
      width: 10px;
      height: 12px;
      background: rgba(8, 35, 63, 0.3);
    }
    .is-active {
      .el-carousel__button {
        transform: skewX(-40deg);
        transform-origin: left center;
        width: 36px;
        height: 12px;
        background: rgba(8, 35, 63, 0.6);
      }
    }
  }
}
.card {
  transition:
    transform 0.3s ease-in-out,
    box-shadow 0.3s ease-in-out;
  filter: drop-shadow(0 8px 4px rgba(0, 0, 0, 0.1));
  .action {
    opacity: 0;
    width: 100%;
    position: absolute;
    bottom: 0;
    left: 0;
    background: url(@/assets/home/wl-16.png);
    background-size: cover;
    transition: opacity 0.3s ease-in-out;
    clip-path: polygon(
      0 13px,
      13px 0,
      100% 0,
      100% calc(100% - 13px),
      calc(100% - 13px) 100%,
      0 100%
    );
  }
  &:hover {
    transform: scale(1.04);
    .action {
      opacity: 1;
    }
    .avatar {
      filter: blur(2px);
    }
  }
}
:deep(.el-carousel__mask) {
  background: initial;
}
.img4 {
  box-shadow: 0px 12px 32px 0px rgba(0, 0, 0, 0.3);
}
:deep(.el-tabs__nav-wrap) {
  &::after {
    display: none;
  }
}

:deep(.el-tabs__item) {
  font-weight: 400;
  font-size: 18px;
  color: rgba(10, 8, 26, 0.75);
  line-height: 38px;
  background: #ffffff;
  border-radius: 100px 100px 100px 100px;
  padding: 0px 24px !important;
  margin: 0 8px;
  &:hover {
    color: #1977ff;
  }
}
:deep(.el-tabs__item.is-active) {
  border: 1px solid #1977ff;
  background: #e8f1ff;
  font-weight: 500;
  font-size: 18px;
  color: #1977ff;
}
:deep(.el-tabs__active-bar) {
  display: none;
}
:deep(.el-tabs__nav-prev) {
  font-size: 20px;
  color: #6585a6;
}
:deep(.el-tabs__nav-next) {
  font-size: 20px;
  color: #6585a6;
}

.arrow {
  border-radius: 100%;
  overflow: hidden;
  box-shadow: 0px 4px 12px 0px rgba(12, 18, 56, 0.08);
  &:hover {
    box-shadow: 0px 4px 12px 0px rgba(12, 18, 56, 0.25);
  }
}
</style>
