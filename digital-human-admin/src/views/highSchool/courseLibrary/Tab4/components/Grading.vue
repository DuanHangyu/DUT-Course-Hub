<template>
  <Modal
    v-model:open="dialogOpen"
    :width="960"
    destroy-on-close
    title="批改"
    @ok="submitForm"
    :confirm-loading="loading"
  >
    <div class="flex flex-col h-full overflow-hidden space-y-4">
      <!-- bg-[#D9D9D9] -->
      <div
        class="w-full grow overflow-y-auto flex flex-col space-y-4"
        style="max-height: 600px"
      >
        <template v-for="(item, index) in ruleForm.files">
          <VuePdfApp
            v-if="item?.url?.includes('.pdf')"
            :pdf="item?.url"
            @pages-rendered="renderedHandler"
            style="height: 530px"
          ></VuePdfApp>
          <img
            :src="item?.url"
            v-else-if="
              item?.url?.includes('.jpg') ||
              item?.url?.includes('.png') ||
              item?.url?.includes('.jpeg')
            "
            class="w-full max-h-[530px]"
            alt=""
          />
        </template>
      </div>
      <template v-for="(item, index) in ruleForm.files">
        <Button
          v-if="
            !item?.url?.includes('.pdf') &&
            !item?.url?.includes('.jpg') &&
            !item?.url?.includes('.png') &&
            !item?.url?.includes('.jpeg')
          "
          @click="downLoadFile(item)"
          type="primary"
          class="w-fit"
        >
          下载: {{ item?.fileName }}
        </Button>
      </template>
      <Form
        ref="ruleFormRef"
        :model="ruleForm"
        layout="inline"
        class="shrink-0"
      >
        <FormItem label="成绩" name="score" required>
          <InputNumber
            class="!w-[254px]"
            v-model:value="ruleForm.score"
            placeholder="请输入"
          />
        </FormItem>
        <FormItem label="批改备注" name="comment" required>
          <Input v-model:value="ruleForm.comment" placeholder="请输入" />
        </FormItem>
      </Form>
    </div>
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
  InputNumber,
  Button,
} from "ant-design-vue";
import { cloneDeep } from "lodash-es";
import VuePdfApp from "vue3-pdf-app";
import "vue3-pdf-app/dist/icons/main.css";
import { regradeApi, gradeApi } from "@/api/homeWork";

const emits = defineEmits(["success"]);
const dialogOpen = ref(false);
const ruleFormRef = ref();
const ruleForm = ref({});
const loading = ref(false);

const submitForm = () => {
  ruleFormRef.value.validate().then(async () => {
    if (ruleForm.value.status == "GRADED") {
      await regradeApi({
        submissionId: ruleForm.value.id,
        score: ruleForm.value.score,
        comment: ruleForm.value.comment,
      });
    } else {
      await gradeApi({
        submissionId: ruleForm.value.id,
        score: ruleForm.value.score,
        comment: ruleForm.value.comment,
      });
    }
    dialogOpen.value = false;
    message.success("添加成功");
    emits("success");
  });
};

const openModal = (e = {}) => {
  try {
    ruleFormRef.value?.resetFields?.();
  } catch (error) {}
  ruleForm.value = cloneDeep(e);
  dialogOpen.value = true;
};

const downLoadFile = (e) => {
  window.open(e?.url);
};

defineExpose({
  openModal,
});
</script>
