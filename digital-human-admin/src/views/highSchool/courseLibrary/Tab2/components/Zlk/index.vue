<template>
  <Drawer
    v-model:open="open"
    title="全局资料库"
    width="1060px"
    :closable="false"
    @close="
      () => {
        $emit('success');
      }
    "
    destroy-on-close
  >
    <template #extra>
      <CloseOutlined @click="open = false" />
    </template>
    <div class="flex items-center justify-between">
      <div class="flex space-x-3">
        <Button type="primary" class="shrink-0 w-fit" @click="addFolder">
          新增文件夹
        </Button>
        <Button type="primary" ghost class="shrink-0 w-fit" @click="uploadFile">
          上传文件
        </Button>
      </div>
      <Form
        :model="params"
        layout="inline"
        class="shrink-0"
        :colon="false"
        @finish="onSubmit"
      >
        <FormItem label="文件/文件夹" name="keyword">
          <Input v-model:value="params.keyword" placeholder="请输入" />
        </FormItem>
      </Form>
    </div>
    <div class="overflow-y-auto">
      <div class="mt-5">
        <Table
          :data-source="list"
          row-key="id"
          :pagination="false"
          @expand="expandedRowsChange"
        >
          <template #emptyText>
            <Empty />
          </template>
          <TableColumn dataIndex="name" title="文件名" />
          <TableColumn dataIndex="size" title="大小" />
          <TableColumn dataIndex="createTime" title="上传时间">
            <template #default="{ record }">
              {{
                record.createTime
                  ? dayjs(record.createTime).format("YYYY-MM-DD HH:mm:ss")
                  : ""
              }}
            </template>
          </TableColumn>
          <TableColumn dataIndex="action" title="操作" :width="230">
            <template #default="{ record }">
              <Button type="link" class="!pl-0" @click="update(record)">
                编辑
              </Button>
              <Button
                type="link"
                @click="uploadFile(record)"
                v-if="record?.type == 'folder'"
              >
                上传文件
              </Button>
              <Button type="link" danger @click="delFn(record)"> 删除 </Button>
            </template>
          </TableColumn>
        </Table>
      </div>
    </div>
    <HandleFolder ref="handleFolderRef" @success="onLoad" />
    <Upload ref="uploadRef" @success="onLoad" />
  </Drawer>
</template>
<script setup>
import { CloseOutlined } from "@ant-design/icons-vue";
import {
  Drawer,
  Table,
  TableColumn,
  Empty,
  Button,
  Form,
  FormItem,
  Input,
  Modal,
  message,
} from "ant-design-vue";
import { ref } from "vue";
import {
  getCourseMasterialList,
  delCourseMaterial,
  delCourseFile,
  getCourseMasterialFileList,
} from "@/api/course-material";
import HandleFolder from "./HandleFolder.vue";
import Upload from "./Upload.vue";
import dayjs from "dayjs";
import { useRoute } from "vue-router";

defineEmits(["success"]);

const open = ref(false);
const route = useRoute();
const params = ref({});
const list = ref([]);
const handleFolderRef = ref();
const uploadRef = ref();
const onLoad = () => {
  getCourseMasterialList({
    courseId: route.query?.id,
    ...params.value,
  }).then((res) => {
    list.value = [
      ...(res?.data?.folders?.map((item) => {
        return {
          ...item,
          name: item?.folderName,
          type: "folder",
          children: [],
          size: item?.totalFileSize,
          createTime: item?.latestUploadTime,
        };
      }) || []),
      ...(res?.data?.files?.map((item) => {
        return {
          ...item,
          name: item?.fileName,
          size: item?.fileSize,
          type: "file",
        };
      }) || []),
    ];
  });
};

const onSubmit = () => {
  onLoad();
};

const delFn = (e) => {
  Modal.confirm({
    title: "确认删除吗？",
    content: "删除后不可恢复。",
    okText: "删除",
    okButtonProps: {
      type: "primary",
      danger: true,
    },
    cancelButtonText: "取消",
    onOk: async () => {
      if (e?.type == "folder") {
        await delCourseMaterial({ folderId: e?.id, courseId: route.query.id });
      } else {
        await delCourseFile({ fileId: e?.id, courseId: route.query.id });
      }
      onLoad();
      message.success("已删除");
    },
  });
};

const addFolder = () => {
  handleFolderRef.value?.openModal({}, "addFolder");
};

const update = (e) => {
  handleFolderRef.value?.openModal(
    e,
    e?.type == "folder" ? "renameFolder" : "renameFile",
  );
};

const uploadFile = (e) => {
  uploadRef.value?.openModal(e?.id ? { folderId: e?.id } : {});
};

const expandedRowsChange = (bool, e) => {
  if (bool) {
    getCourseMasterialFileList({
      folderId: e.id,
      courseId: route.query?.id,
    }).then((res) => {
      e.children =
        res?.data?.map((item) => {
          return {
            ...item,
            name: item?.fileName,
            size: item?.fileSize,
            type: "file",
          };
        }) || [];
    });
  }
};

defineExpose({
  openDrawer: (r = {}) => {
    open.value = true;
    onLoad();
  },
});
</script>
