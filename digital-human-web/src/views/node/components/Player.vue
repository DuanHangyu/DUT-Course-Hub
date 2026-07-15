<template>
  <div class="size-full overflow-hidden flex flex-col bg-[#091625]">
    <!--  @play="playFn"
            @pause="endStudyFn"
            @ended="playEnd" -->
    <div class="grow bg-[#000000] relative">
      <video
        :src="detail?.video?.url"
        :loop="false"
        :autoplay="false"
        :muted="false"
        x5-playsinline
        playsinline
        webkit-playsinline
        ref="videoRef"
        class="size-full"
        @timeupdate="onTimeUpdate"
        @loadedmetadata="onLoadedMetadata"
        @play="onPlay"
        @pause="onPause"
        @ended="onEnded"
      ></video>
      <img
        v-if="!isPlaying"
        src="@/assets/node/big-stop.png"
        class="absolute top-1/2 left-1/2 -translate-1/2 size-17.5 cursor-pointer"
        alt="播放"
        @click="togglePlay"
      />
    </div>
    <div class="shrink-0 px-6 pb-7.5 pt-5 w-full">
      <el-slider
        v-model="progress"
        class="w-full progress-slider"
        size="large"
        :show-tooltip="false"
        @change="seekVideo"
      />
      <div class="flex items-center justify-between mt-8">
        <section class="flex items-center space-x-6">
          <img
            :src="isPlaying ? smallPlay : smallStop"
            class="size-6 cursor-pointer"
            alt=""
            @click="togglePlay"
          />
          <span class="text-base font-medium text-[#FFFFFF]">
            {{ formatTime(currentTime) }} / {{ formatTime(duration) }}
          </span>
        </section>
        <section class="flex items-center space-x-6">
          <el-tooltip effect="dark" placement="top">
            <img
              src="@/assets/node/laba.png"
              class="w-5 h-4.5 cursor-pointer"
              alt="音量"
            />
            <template #content>
              <div class="pt-2">
                <el-slider
                  v-model="volume"
                  vertical
                  height="100px"
                  :min="0"
                  :max="1"
                  :step="0.1"
                  @change="setVolume"
                  class="volume-slider"
                  size="small"
                />
              </div>
            </template>
          </el-tooltip>
          <span
            class="text-base font-medium text-white cursor-pointer"
            @click="changePlaybackRate"
          >
            {{ playbackRate }}x
          </span>

          <img
            src="@/assets/node/full-screen.png"
            class="w-4.5 h-4.5 cursor-pointer"
            alt="全屏"
            @click="toggleFullscreen"
          />
        </section>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import smallPlay from "@/assets/node/small-play.png";
import smallStop from "@/assets/node/small-stop.png";
import { startStudy, endStudy } from "@/api/node";
import useUserStore from "@/store/modules/user";

const props = defineProps({
  detail: {
    type: Object,
    default: () => ({}),
  },
});

const userStore = useUserStore();
const isPlaying = ref(false);
const progress = ref(0); // 0-100
const currentTime = ref(0);
const duration = ref(0);
const volume = ref(1); // 0-1
const playbackRate = ref(1); // 倍速
const isFullscreen = ref(false);
const videoRef = ref(null);
let updateInterval = null;
const isStartStudy = ref(false);
// 格式化时间 (秒 → mm:ss)
const formatTime = (seconds) => {
  if (!seconds || isNaN(seconds)) return "00:00";
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
};

// 切换播放/暂停
const togglePlay = () => {
  const video = videoRef.value;
  if (!video) return;

  if (isPlaying.value) {
    video.pause();
  } else {
    video.play();
  }
};

// 播放事件
const onPlay = () => {
  isPlaying.value = true;
  isStartStudy.value = true;
  startStudy({ courseNodeId: props.detail?.id });
};

const endStudyFn = (completed = false) => {
  if (isStartStudy.value) {
    isStartStudy.value = false;
    // 使用标准 API（带 Authorization 认证头），不能用 sendBeacon（不带认证头→401）
    endStudy({
      courseNodeId: props.detail?.id,
      completed: completed,
    });
  }
};

// 暂停事件
const onPause = () => {
  isPlaying.value = false;
  if (currentTime.value + 1 > duration.value) {
    endStudyFn(true);
  } else {
    endStudyFn();
  }
};

// 播放结束
const onEnded = () => {
  isPlaying.value = false;
  progress.value = 0;
  currentTime.value = 0;
  endStudyFn(true);
};

// 元数据加载完成（获取总时长）
const onLoadedMetadata = () => {
  const video = videoRef.value;
  if (video) {
    duration.value = video.duration;
  }
};

// 时间更新（同步进度）
const onTimeUpdate = () => {
  const video = videoRef.value;
  if (video) {
    currentTime.value = video.currentTime;
    // 计算进度百分比 (避免除零)
    progress.value =
      duration.value > 0 ? (video.currentTime / duration.value) * 100 : 0;
  }
};

// 跳转到指定时间
const seekVideo = (value) => {
  const video = videoRef.value;
  if (video && duration.value > 0) {
    video.currentTime = (value / 100) * duration.value;
  }
};

// 设置音量
const setVolume = (value) => {
  const video = videoRef.value;
  if (video) {
    video.volume = value;
    volume.value = value;
  }
};

// 切换倍速
const changePlaybackRate = () => {
  const rates = [0.5, 1, 1.25, 1.5, 2];
  const currentIndex = rates.indexOf(playbackRate.value);
  const nextIndex = (currentIndex + 1) % rates.length;
  playbackRate.value = rates[nextIndex];

  const video = videoRef.value;
  if (video) {
    video.playbackRate = playbackRate.value;
  }
};

const toggleFullscreen = () => {
  const videoContainer = videoRef.value?.parentElement;
  if (!videoContainer) return;

  if (!isFullscreen.value) {
    if (videoContainer.requestFullscreen) {
      videoContainer.requestFullscreen();
    } else if (videoContainer.webkitRequestFullscreen) {
      videoContainer.webkitRequestFullscreen();
    } else if (videoContainer.msRequestFullscreen) {
      videoContainer.msRequestFullscreen();
    }
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen();
    } else if (document.webkitExitFullscreen) {
      document.webkitExitFullscreen();
    } else if (document.msExitFullscreen) {
      document.msExitFullscreen();
    }
  }
};

const handleFullscreenChange = () => {
  isFullscreen.value =
    !!document.fullscreenElement || !!document.webkitFullscreenElement;
};

onMounted(() => {
  document.addEventListener("fullscreenchange", handleFullscreenChange);
  document.addEventListener("webkitfullscreenchange", handleFullscreenChange);

  if (window.addEventListener) {
    window.addEventListener("beforeunload", endStudyFn);
    window.addEventListener("pagehide", endStudyFn);
    window.addEventListener("unload", endStudyFn);
  } else if (window.attachEvent) {
    window.attachEvent("onbeforeunload", endStudyFn);
    window.attachEvent("onpagehide", endStudyFn);
    window.attachEvent("onunload", endStudyFn);
  }
});

// 组件卸载时清理
onUnmounted(() => {
  document.addEventListener("fullscreenchange", handleFullscreenChange);
  document.addEventListener("webkitfullscreenchange", handleFullscreenChange);
  if (updateInterval) {
    clearInterval(updateInterval);
  }
  endStudyFn();
});
</script>
<style scoped lang="scss">
:deep(.progress-slider) {
  .el-slider__runway {
    height: 15px;
    border-radius: 100px;
    border: 2px solid #0c3a6c;
    overflow: hidden;
    background: transparent;
    .el-slider__bar {
      background: #4effff;
      height: 100%;
    }
    .el-slider__button-wrapper {
      display: none;
    }
  }
}

video {
  outline: none;
  /* 隐藏 shadow DOM 控件（部分浏览器支持） */
  &::-webkit-media-controls {
    display: none !important;
  }
  /* 隐藏播放按钮等 */
  &::cue {
    display: none;
  }
}
</style>
