<template>
  <Modal
    v-model:open="dialogOpen"
    :width="550"
    destroy-on-close
    title="添加学生/班级"
    @ok="submitForm"
    :confirm-loading="loading"
  >
    <Form ref="ruleFormRef" :model="ruleForm" layout="vertical">
      <FormItem label="学生/班级" name="ids" required>
        <TreeSelect
          multiple
          :maxTagCount="3"
          :tree-data="list"
          v-model:value="ruleForm.ids"
          placeholder="请输入"
          tree-checkable
          tree-check-strictly
        />
      </FormItem>
    </Form>
  </Modal>
</template>
<script setup>
import { ref } from "vue";
import {
  Modal,
  Form,
  FormItem,
  Input,
  message,
  Cascader,
  TreeSelect,
} from "ant-design-vue";
import * as api from "@/api/courseLibrary";
import { cloneDeep } from "lodash-es";
import useUserStore from "@/store/modules/user";
import { getDetail } from "../../../../api/courseLibrary";

const emits = defineEmits(["success"]);
const dialogOpen = ref(false);
const ruleFormRef = ref();
const ruleForm = ref({});
const loading = ref(false);
const userStore = useUserStore();
const list = ref([]);
const submitForm = () => {
  ruleFormRef.value.validate().then(() => {
    loading.value = true;

    const classIds = [];
    const studentIds = [];

    for (let index = 0; index < ruleForm.value?.ids?.length; index++) {
      const element = ruleForm.value?.ids?.[index];
      if (element?.value?.includes("**student**__")) {
        const id = element?.value?.split("**student**__")?.[1];
        studentIds.push(id);
      }
      if (element?.value?.includes("**class**__")) {
        const id = element?.value?.split("**class**__")?.[1];
        classIds.push(id);
      }
    }
    api
      .addRelationStudent({
        courseId: ruleForm.value.id,
        studentIds,
        classIds,
      })
      .then((res) => {
        dialogOpen.value = false;
        message.success("添加成功");
        emits("success");
      })
      .finally(() => {
        loading.value = false;
      });
  });
};

const openModal = (e = {}) => {
  try {
    ruleFormRef.value?.resetFields?.();
  } catch (error) {}
  ruleForm.value = cloneDeep(e);
  dialogOpen.value = true;
  api.getAllWithStudents({ schoolId: userStore.schoolId }).then((res) => {
    list.value = res?.data || [];
    for (let index = 0; index < list.value.length; index++) {
      const element = list.value[index];
      element.label = element?.className;
      element.value = `**class**__${element?.id}`;
      if (element.students?.length) {
        element.children = element.students.map((item) => {
          return {
            label: item?.studentName,
            value: `**student**__${item?.id}`,
          };
        });
      }
    }
  });
  ruleForm.value.ids = [];
  getDetail(e?.id).then((res) => {
    if (res?.data?.students?.length) {
      ruleForm.value.ids = [
        ...ruleForm.value.ids,
        ...res?.data?.students.map((item) => {
          return {
            label: item?.studentName,
            value: `**student**__${item?.id}`,
          };
        }),
      ];
    }
    if (res?.data?.schoolClasses?.length) {
      ruleForm.value.ids = [
        ...ruleForm.value.ids,
        ...res?.data?.schoolClasses.map((item) => {
          return {
            label: item?.schoolClassName,
            value: `**class**__${item?.id}`,
          };
        }),
      ];
    }
  });
};

defineExpose({
  openModal,
});
</script>
