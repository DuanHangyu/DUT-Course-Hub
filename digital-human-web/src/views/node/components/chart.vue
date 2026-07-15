<template>
  <!-- element-loading-background="rgba(0, 0, 0, 0.7)" -->
  <div
    class="size-full flex flex-col overflow-hidden"
    v-loading="loading"
    element-loading-text="加载中，请稍后..."
    element-loading-background="rgba(0, 0, 0, 0.7)"
    element-loading-custom-class="custom-loading"
  >
    <section class="top2 shrink-0 h-20 flex items-center justify-between px-5">
      <section class="flex items-center space-x-3">
        <img
          src="@/assets/node/dsbx.png"
          class="size-12"
          alt=""
          v-if="hideHuman"
        />
        <img src="@/assets/node/zn.png" class="size-12" alt="" v-else />
        <div class="text-base font-medium text-[#08233F]" v-if="hideHuman">
          AI实时助教
        </div>
        <div v-else>
          <div class="text-base font-medium text-[#08233F]">导师伴学</div>
          <div class="flex items-center space-x-2">
            <div class="w-2.5 h-2.5 bg-[#00A3CC] rounded-full"></div>
            <span class="text-sm text-[#0355CD]">在线</span>
          </div>
        </div>
      </section>
      <img
        src="@/assets/node/qh.png"
        class="size-7 cursor-pointer!"
        @click="hide"
        alt=""
      />
    </section>
    <section class="grow overflow-hidden py-5 relative">
      <div class="relative w-full h-full -top-26" v-show="!hideHuman">
        <DH_live
          :hide-human="hideHuman"
          @answer="answer"
          @translate="translate"
          @micFail="micFail"
          @micReady="micReady"
          @ready="ready"
          ref="digitalHumanComponentRef"
          :height="'700px'"
        ></DH_live>
      </div>

      <main
        class="overflow-y-auto absolute z-10 top-0 left-0 w-full h-full"
        :class="hideHuman ? 'pt-4' : 'pt-50'"
      >
        <el-scrollbar height="100%" ref="msgBox">
          <template v-for="(item, index) in feedback" :key="index">
            <div class="flex space-x-3 px-5 mb-5" v-if="item.type == 1">
              <img src="@/assets/node/zn.png" class="size-10" alt="" />
              <div class="space-y-2">
                <div class="text-[#64748B] text-sm font-medium">AI实时助教</div>
                <div class="znBox">
                  <div v-html="renderedMarkdown(item.content)"></div>
                </div>
              </div>
            </div>
            <div
              class="flex space-x-3 justify-end px-5 mb-5"
              v-if="item.type == 2"
            >
              <div class="space-y-2">
                <div class="text-[#64748B] text-sm font-medium text-right">
                  你
                </div>
                <div class="myBox">
                  {{ item.content }}
                </div>
              </div>
              <img src="@/assets/node/avatar.png" class="size-10" alt="" />
            </div>
          </template>
        </el-scrollbar>
      </main>
    </section>
    <section class="bottom2 shrink-0 p-5">
      <div class="relative input">
        <el-input
          v-model="question"
          type="textarea"
          :placeholder="placeholder"
          :disabled="disabled"
        />
        <div class="flex items-center justify-between px-2 w-full">
          <div
            class="w-27 h-8 rounded-4xl border border-solid border-[#524fff] text-sm text-[#524fff] flex items-center justify-center space-x-1 cursor-pointer"
            @click="isKeyboard = !isKeyboard"
          >
            <img
              src="@/assets/node/voice.png"
              v-if="isKeyboard"
              class="w-6"
              alt=""
            />
            <img src="@/assets/node/key.png" v-else class="w-6" alt="" />
            <span>{{ isKeyboard ? "切换语音" : "切换键盘" }}</span>
          </div>
          <div
            class="startBtn cursor-pointer"
            v-if="!isKeyboard && isStartRecord"
            @click="endAudioAsk"
          >
            结束对话
          </div>
          <div
            class="startBtn cursor-pointer"
            v-if="!isKeyboard && !isStartRecord && !startAnswer"
            @click="switchInputMode"
          >
            开始对话
          </div>
          <img
            src="@/assets/node/send.png"
            class="w-8 h-8"
            alt=""
            v-if="isKeyboard && !startAnswer && !question"
            @click="sendText"
          />
          <img
            src="@/assets/node/send2.png"
            class="w-8 h-8 cursor-pointer"
            alt=""
            v-if="isKeyboard && !startAnswer && question"
            @click="sendText"
          />
          <img
            src="@/assets/node/stop.png"
            class="w-8 h-8 cursor-pointer"
            v-if="startAnswer"
            @click="closeAsk(false)"
          />
        </div>
      </div>
    </section>
  </div>
</template>
<script setup>
import { nextTick, ref, computed, watch } from "vue";
import DH_live from "@/components/DH_live.vue";
// import { marked } from "marked";
import "github-markdown-css";
import MarkdownIt from "markdown-it";
import DOMPurify from "dompurify";

const props = defineProps({
  detail: {
    type: Object,
    default: () => ({}),
  },
});

// Markdown 内容
const mdi = new MarkdownIt({
  html: false,
  linkify: true,
});

// 处理并渲染Markdown内容（DOMPurify 消毒防 XSS）
const renderedMarkdown = computed(() => (text) => {
  if (!text) return "";
  const rawHtml = mdi.render(text);
  return DOMPurify.sanitize(rawHtml, {
    ALLOWED_TAGS: [
      "h1", "h2", "h3", "h4", "h5", "h6",
      "p", "br", "hr", "blockquote", "pre", "code",
      "ul", "ol", "li", "table", "thead", "tbody", "tr", "th", "td",
      "a", "strong", "em", "del", "s", "sup", "sub",
      "img", "span", "div", "figure", "figcaption",
    ],
    ALLOWED_ATTR: ["href", "src", "alt", "title", "class", "target", "rel"],
    ALLOW_DATA_ATTR: false,
  });
});

const isKeyboard = ref(true);
const isStartRecord = ref(false);
const startAnswer = ref(false);
const loading = ref(true);
const question = ref("");
const placeholder = ref("请输入你的问题");
const hideHuman = ref(true);
const digitalHumanComponentRef = ref(null);
const msgBox = ref(null);
const disabled = ref(false);
const feedback = ref([
  {
    content: "同学你好，我是小郭老师，视频中有任何问题都可以来问我哦！",
    type: 1,
  },
]);
const micFail = () => {
  isStartRecord.value = false;
};
const micReady = () => {
  isStartRecord.value = true;
};
const ready = () => {
  loading.value = false;
};
const switchInputMode = async () => {
  question.value = "";
  digitalHumanComponentRef.value.switchInputMode(true);
  isStartRecord.value = true;
};

const endAudioAsk = () => {
  isStartRecord.value = false;
  isKeyboard.value = true;
  digitalHumanComponentRef.value.stopMic();
};

const sendText = () => {
  if (
    digitalHumanComponentRef.value &&
    digitalHumanComponentRef.value.sendTextForVoiceGeneration &&
    question.value
  ) {
    digitalHumanComponentRef.value.sendTextForVoiceGeneration(
      question.value,
      props.detail?.id,
    );
    feedback.value.push({
      content: question.value,
      type: 2,
    });
    question.value = "";
    nextTick(() => {
      scrollToBottom();
    });
  } else {
    console.error("DH_live ready but method not found!");
  }
};

const answer = (textContent) => {
  if (textContent.last) {
    startAnswer.value = false;
  } else {
    startAnswer.value = true;
    if (feedback.value?.[feedback.value.length - 1]?.type == 2) {
      feedback.value.push({
        content: "",
        type: 1,
      });
    }
    feedback.value[feedback.value.length - 1].content += textContent.content;
    nextTick(() => {
      scrollToBottom();
    });
  }
};

let textTranslate = "";
// 语音提问转文字
const translate = (textContent) => {
  if (textContent.last) {
    question.value = textTranslate;
    textTranslate = "";
  } else {
    textTranslate += textContent.content;
    question.value = textTranslate;
    nextTick(() => {
      scrollToBottom();
    });
  }
};
const scrollToBottom = () => {
  msgBox.value?.setScrollTop(
    msgBox.value?.wrapRef?.scrollHeight - msgBox.value?.wrapRef?.clientHeight,
  );
};

function hide() {
  digitalHumanComponentRef.value.hideAudio();
  hideHuman.value = !hideHuman.value;
}

function closeAsk(state) {
  // 终止语音
  digitalHumanComponentRef.value.stopAudio();
  params.value.answer = textAnswer;
  textAnswer = "";
  startAnswer.value = false;
  isKeyboard.value = true;
  // save();
}

watch(
  () => isKeyboard.value,
  (newVal) => {
    if (!newVal) {
      placeholder.value = "请点击按钮说出你的问题";
      disabled.value = true;
    } else {
      placeholder.value = "请输入你的问题";
      disabled.value = false;
    }
  },
  { immediate: true, deep: true },
);
// 开始说话
watch(
  () => isStartRecord.value,
  (newVal) => {
    if (newVal && !isKeyboard.value) {
      placeholder.value = "语音回答中......";
      disabled.value = true;
    } else if (!isKeyboard.value) {
      placeholder.value = "请点击按钮说出你的问题";
    }
  },
  { immediate: true, deep: true },
);
</script>
<style scoped lang="scss">
.top2 {
  border-bottom: 4px solid #0c3a6c;
}
.bottom2 {
  border-top: 4px solid #0c3a6c;
  .input {
    width: 100%;
    height: 143px;
    background: #f9fafc;
    border-radius: 20px 20px 20px 20px;
    border: 2px solid #0c3a6c;
  }
  :deep(.el-textarea) {
    height: 100px;
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
.znBox {
  background: #ffffff;
  box-shadow: 3px 3px 0px 0px #002d73;
  border-radius: 0px 20px 20px 20px;
  border: 2px solid #0c3a6c;
  font-weight: 400;
  font-size: 14px;
  color: #0c3a6c;
  padding: 12px;
}
.myBox {
  background: linear-gradient(90deg, #004fc6 0%, #0041a4 100%);
  box-shadow: 3px 3px 0px 0px #002d73;
  border-radius: 20px 0px 20px 20px;
  border: 2px solid #0c3a6c;
  font-weight: 400;
  font-size: 14px;
  color: #ffffff;
  padding: 12px;
}

.startBtn {
  width: 80px;
  height: 32px;
  background: #524fffff;
  border-radius: 100px 100px 100px 100px;

  font-family:
    PingFang SC,
    PingFang SC;
  font-weight: bold;
  font-size: 12px;
  color: #ffffff;
  line-height: 32px;
  text-align: center;
}
</style>
<style lang="scss">
.custom-loading {
  border-radius: 20px;
}
</style>
