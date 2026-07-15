<template>
  <div class="w-full">
    <div
      v-if="!ranking.length"
      class="h-[220px] flex items-center justify-center text-[rgba(10,8,26,0.35)] text-sm"
    >
      暂无数据
    </div>
    <div v-else class="space-y-3 py-2">
      <div
        v-for="(item, index) in ranking"
        :key="item.studentId"
        class="flex items-center space-x-3"
      >
        <!-- 排名徽章 -->
        <div
          class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold shrink-0"
          :class="rankBgClass(index)"
        >
          {{ index + 1 }}
        </div>
        <!-- 姓名+班级 -->
        <div class="w-24 shrink-0 overflow-hidden">
          <div class="text-sm font-medium text-[rgba(10,8,26,0.88)] truncate">
            {{ item.studentName }}
          </div>
          <div class="text-xs text-[rgba(10,8,26,0.35)] truncate">
            {{ item.className || "—" }}
          </div>
        </div>
        <!-- 进度条 -->
        <div class="flex-1 h-6 bg-[#F1F5F9] rounded-full overflow-hidden relative">
          <div
            class="h-full rounded-full transition-all duration-700 ease-out"
            :style="{
              width: (item.courseProgress || 0) + '%',
              background: barGradient(index, item.courseProgress || 0),
            }"
          ></div>
        </div>
        <!-- 百分比 -->
        <span
          class="w-12 text-right text-sm font-semibold shrink-0"
          :class="index < 3 ? 'text-[#0461C1]' : 'text-[rgba(10,8,26,0.55)]'"
        >
          {{ Math.round(item.courseProgress || 0) }}%
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { getProgressRanking } from "@/api/studyDetailMonitor";

const route = useRoute();
const ranking = ref([]);

const rankBgClass = (index) => {
  if (index === 0) return "bg-[#FFD700] text-white"; // 金
  if (index === 1) return "bg-[#C0C0C0] text-white"; // 银
  if (index === 2) return "bg-[#CD7F32] text-white"; // 铜
  return "bg-[#E2E8F0] text-[rgba(10,8,26,0.55)]";
};

const barGradient = (index, progress) => {
  if (index < 3)
    return "linear-gradient(90deg, #7DBFFB, #0461C1)";
  if (progress >= 80) return "linear-gradient(90deg, #4FD882, #0EC55D)";
  if (progress >= 50) return "linear-gradient(90deg, #7DBFFB, #4FD8C8)";
  return "linear-gradient(90deg, #FB837D, #FF6B6B)";
};

onMounted(async () => {
  try {
    const res = await getProgressRanking({ courseId: route.query.id });
    ranking.value = res?.data || [];
  } catch (e) {
    ranking.value = [];
  }
});
</script>
