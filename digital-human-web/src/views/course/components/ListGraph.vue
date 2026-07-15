<template>
  <div
    class="absolute top-0 left-0 w-full h-full flex items-center justify-center py-10 space-x-4 overflow-hidden"
  >
    <section class="left1 h-full overflow-y-auto">
      <el-scrollbar>
        <div
          class="item cursor-pointer mb-3"
          v-for="(item, index) in list"
          :key="index"
          :class="active == index ? 'active' : ''"
          @click="active = index"
        >
          路径{{ item?.pathIndex }}
        </div>
      </el-scrollbar>
    </section>
    <section class="right1 h-full overflow-y-auto p-4">
      <el-scrollbar>
        <div
          class="flex items-center p-1 space-x-2 hover:bg-[rgba(255,255,255,0.1)] rounded cursor-pointer mb-3"
          v-for="(item, index) in list[active]?.nodes"
          :key="index"
          @click="item?.nodeColour != 1 && item?.nodeColour != 2 && goToNode(item)"
        >
          <!-- 开始/结束节点不显示状态 -->
          <div
            v-if="item?.nodeColour != 1 && item?.nodeColour != 2"
            class="text-xs w-13 rounded text-center leading-5.25"
            :class="item?.state == 1 ? 'bg-[#0EC55D] text-white' : 'bg-white text-[#FF383C]'"
          >
            {{ item?.state == 1 ? "已完成" : "未完成" }}
          </div>
          <div v-else class="w-13"></div>
          <div
            class="size-5.25 bg-[#7DBFFB] border border-solid border-white rounded-full flex items-center justify-center"
          >
            <img src="@/assets/course/node.png" class="size-3" alt="" />
          </div>
          <span class="text-base font-normal text-white">
            {{ item?.nodeName }}
          </span>
        </div>
      </el-scrollbar>
    </section>
  </div>
</template>
<script setup>
import { ref } from "vue";
import { useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute();

const goToNode = (item) => {
  router.push({
    path: "/node",
    query: { courseId: route.query.courseId, nodeId: item.id },
  });
};

const props = defineProps({
  list: {
    type: Array,
    default: () => [],
  },
});

const active = ref(0);
</script>
<style scoped lang="scss">
.left1 {
  width: 146px;
  .item {
    width: 100%;
    line-height: 41px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px 8px 8px 8px;
    padding: 6px;
    font-weight: 600;
    font-size: 18px;
    color: #ffffff;
  }
  .active {
    background: #7dbffb;
  }
}
.right1 {
  width: 500px;
  height: 100%;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px 8px 8px 8px;
  overflow-y: auto;
}
</style>
