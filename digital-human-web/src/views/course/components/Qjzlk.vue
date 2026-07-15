<template>
  <div
    class="absolute top-0 right-0 w-75 h-full border-l-solid border-l-2 border-[#0C3A6C] bg-[#111827] rounded-tr-[30px] rounded-br-[30px] flex flex-col overflow-hidden"
  >
    <header
      class="flex items-center pl-4 border-b-solid border-b-2 border-[#0C3A6C] py-4 shrink-0"
    >
      <div class="flex items-center cursor-pointer space-x-2">
        <el-icon color="#fff" @click="$emit('close')"><Back /></el-icon>
        <span
          class="text-base font-bold text-[#FFFFFF]"
          @click="$emit('close')"
        >
          全局资料库
        </span>
      </div>
    </header>
    <main class="grow overflow-y-auto w-full p-4">
      <el-tree
        :data="list"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
        :indent="10"
        :current-node-key="0"
        :load="loadNode"
        lazy
      >
        <template #default="{ node, data }">
          <div class="flex items-center justify-between px-2 flex-1 truncate space-x-2">
            <div
              class="truncate text-[rgba(255,255,255,0.8)] text-sm grow flex items-center space-x-1"
            >
              <img
                src="@/assets/node/folder.png"
                class="w-4.25 h-3.5 shrink-0"
                alt=""
                v-if="data?.type == 'folder'"
              />
              <span class="truncate" :class="node.level == 1 ? '' : 'pl-3'">{{
                data.name
              }}</span>
            </div>
            <el-icon
              color="rgba(255,255,255,0.8)"
              class="downloadIcon shrink-0"
              @click="downloadFile(data)"
              v-if="data.type == 'file'"
              ><Download
            /></el-icon>
          </div>
        </template>
      </el-tree>
    </main>
  </div>
</template>
<script setup>
import { Back, Download } from "@element-plus/icons-vue";
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { getCourseMaterialList, getFolderFileList } from "@/api/node";

const emits = defineEmits(["close"]);

const route = useRoute();
const list = ref([]);

const loadNode = (node, resolve) => {
  if (node.level === 0) {
    getCourseMaterialList({
      courseId: route.query.courseId,
    })
      .then((res) => {
        const folders = (res?.folders || []).map((item) => ({
          ...item,
          name: item.folderName,
          type: "folder",
          hasChildren: true,
        }));
        const files = (res?.files || []).map((item) => ({
          ...item,
          name: item.fileName,
          type: "file",
          leaf: true,
        }));
        resolve([...folders, ...files]);
      })
      .catch(() => resolve([]));
  } else if (node.data.type === "folder") {
    getFolderFileList({
      folderId: node.data.id,
      courseId: route.query.courseId,
    })
      .then((res) => {
        const files = (res || []).map((item) => ({
          ...item,
          name: item.fileName,
          type: "file",
          leaf: true,
        }));
        resolve(files);
      })
      .catch(() => resolve([]));
  } else {
    resolve([]);
  }
};

const downloadFile = (e) => {
  window.open(e?.ossUrl);
};
</script>
<style lang="scss">
.custom-qjzlk-class {
  border-radius: 8px 0px 0px 8px !important;
  .el-drawer__header {
    border-bottom: 1px solid rgba(0, 0, 0, 0.06) !important;
    margin-bottom: 0px;
    padding: 16px 20px;
  }
}
</style>
<style scoped lang="scss">
:deep(.el-tree) {
  background: initial;
  .downloadIcon {
    opacity: 0;
  }
  .el-tree-node,
  .el-tree-node__content {
    background: initial;
    border-radius: 4px 4px 4px 4px;
  }
  .el-tree-node__content:hover {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 4px 4px 4px 4px;
    .downloadIcon {
      opacity: 1;
    }
  }
}
</style>
