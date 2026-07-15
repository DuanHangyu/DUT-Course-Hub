<template>
  <Modal
    v-model:open="dialogOpen"
    :width="550"
    destroy-on-close
    :title="ruleForm?.id ? '编辑课程' : '新增课程'"
    @ok="submitForm"
    :confirm-loading="loading"
  >
    <Form ref="ruleFormRef" :model="ruleForm" layout="vertical">
      <FormItem label="课程名称" name="courseName" required>
        <Input v-model:value="ruleForm.courseName" placeholder="请输入" />
      </FormItem>
      <FormItem label="课程介绍" name="courseIntroduce" required>
        <Textarea
          v-model:value="ruleForm.courseIntroduce"
          placeholder="请输入"
        />
      </FormItem>
      <FormItem label="状态" name="state" required>
        <Select
          v-model:value="ruleForm.state"
          placeholder="请选择"
          clearable
          class="!w-full"
          :options="[
            { label: '已上架', value: true },
            { label: '未上架', value: false },
          ]"
        >
        </Select>
      </FormItem>
      <FormItem
        label="是否设置精选"
        name="isFeatured"
        required
        extra="精选课程将在学生端展示"
      >
        <RadioGroup
          v-model:value="ruleForm.isFeatured"
          :options="[
            { label: '是', value: 1 },
            { label: '否', value: 0 },
          ]"
        />
      </FormItem>
      <FormItem label="上传图片" name="pictureUrl" required>
        <Upload
          uploadText="上传照片"
          accept=".png,.jpg,.jpeg,image/jpg,image/jpeg,image/png"
          tipText="建议尺寸：586px*360px"
          v-model="ruleForm.pictureUrl"
          @update:modelValue="validateFn"
        />
      </FormItem>
      <FormItem label="所属学科" name="subject" required>
        <Input v-model:value="ruleForm.subject" placeholder="请输入" />
      </FormItem>
      <FormItem label="授课教师" name="teacherName" required>
        <Input v-model:value="ruleForm.teacherName" placeholder="请输入" />
      </FormItem>
      <FormItem label="教师头像" name="headUrl" required>
        <Upload
          uploadText="上传照片"
          accept=".png,.jpg,.jpeg,image/jpg,image/jpeg,image/png"
          tipText="建议上传比例为1:1的图片"
          v-model:modelValue="ruleForm.headUrl"
          @update:modelValue="validateFn2"
        />
      </FormItem>
      <FormItem label="课程邀请码" name="inviteCode" required>
        <div class="flex items-center">
          <FormItem no-style>
            <Input v-model:value="ruleForm.inviteCode" placeholder="请输入" />
          </FormItem>
          <Button type="link" class="!pr-0" @click="createCode">
            一键生成
          </Button>
        </div>
      </FormItem>
      <FormItem label="邀请老师" name="teacherIds" required>
        <Select
          v-model:value="ruleForm.teacherIds"
          placeholder="请选择"
          :options="teacherList"
          mode="multiple"
          :field-names="{
            label: 'userName',
            value: 'id',
          }"
        />
      </FormItem>
    </Form>
  </Modal>
</template>
<script setup>
import { ref } from "vue";
import Upload from "@/components/Upload/index.vue";
import {
  Modal,
  Form,
  FormItem,
  Input,
  Select,
  Textarea,
  RadioGroup,
  Button,
  message,
} from "ant-design-vue";
import * as api from "@/api/courseLibrary";
import * as teacherApi from "@/api/teacher";
import { cloneDeep } from "lodash-es";
import useUserStore from "@/store/modules/user";
import * as templateApi from "@/api/courseConstruction";
import { pick } from "lodash-es";

const emits = defineEmits(["success"]);
const dialogOpen = ref(false);
const ruleFormRef = ref();
const ruleForm = ref({});
const loading = ref(false);
const userStore = useUserStore();
const teacherList = ref([]);
const submitForm = () => {
  ruleFormRef.value.validate().then(() => {
    if (ruleForm.value?.id) {
      loading.value = true;
      api
        .modify(ruleForm.value)
        .then((res) => {
          dialogOpen.value = false;
          message.success("修改成功");
          emits("success");
        })
        .finally(() => {
          loading.value = false;
        });
    } else {
      loading.value = true;
      const p = {
        ...ruleForm.value,
        schoolId: userStore.schoolId,
      };
      if (ruleForm.value.source == "template") {
        p.relationCourseId = ruleForm.value.templateId;
      }
      api
        .create(p)
        .then((res) => {
          dialogOpen.value = false;
          message.success("添加成功");
          emits("success");
        })
        .finally(() => {
          loading.value = false;
        });
    }
  });
};

const openModal = (e = {}) => {
  try {
    ruleFormRef.value?.resetFields?.();
  } catch (error) {}
  ruleForm.value = cloneDeep(e);
  if (e?.source == "template") {
    templateApi
      .getDetail({
        id: e.templateId,
      })
      .then((res) => {
        ruleForm.value = pick(res?.data, [
          "courseName",
          "courseIntroduce",
          "pictureUrl",
          "subject",
          "teacherName",
          "headUrl",
        ]);
        ruleForm.value.source = "template";
        ruleForm.value.templateId = e.templateId;
      });
  } else if (e?.id) {
    ruleForm.value.teacherIds = e.teachers?.map((item) => item?.id) || [];
  }
  teacherApi
    .getTeacherList({
      page: 1,
      size: 5000,
      schoolId: userStore.schoolId,
    })
    .then((res) => {
      teacherList.value = res?.data?.records || [];
    });
  dialogOpen.value = true;
  console.log(ruleForm.value);
};

const validateFn = () => {
  ruleFormRef.value?.validateFields(["pictureUrl"]);
};
const validateFn2 = () => {
  ruleFormRef.value?.validateFields(["headUrl"]);
};
const createCode = () => {
  api.generateCode().then((res) => {
    ruleForm.value.inviteCode = res.data;
  });
};

defineExpose({
  openModal,
});
</script>
