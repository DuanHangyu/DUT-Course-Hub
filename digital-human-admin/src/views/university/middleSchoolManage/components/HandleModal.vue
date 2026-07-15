<template>
  <Modal
    v-model:open="dialogOpen"
    :width="800"
    destroy-on-close
    :title="ruleForm?.id ? '编辑学校' : '新增学校'"
    @ok="submitForm"
    :confirm-loading="loading"
    :ok-text="step < 3 ? '下一步' : '确定'"
  >
    <div class="mb-4">
      <Steps
        v-model:current="step"
        :items="[
          {
            title: '基础信息',
          },
          {
            title: '域名与门户',
          },
          {
            title: '权限与配额',
          },
          {
            title: '管理员账号',
          },
        ]"
      ></Steps>
    </div>
    <Form ref="ruleFormRef" :model="ruleForm" layout="vertical">
      <template v-if="step == 0">
        <FormItem label="学校名称" name="schoolName" required>
          <Input v-model:value="ruleForm.schoolName" placeholder="请输入" />
        </FormItem>
        <div class="grid grid-cols-2 gap-x-6">
          <FormItem label="校长姓名" name="principalName" required>
            <Input
              v-model:value="ruleForm.principalName"
              placeholder="请输入"
            />
          </FormItem>
          <FormItem label="联系电话" name="phone" required>
            <Input v-model:value="ruleForm.phone" placeholder="请输入" />
          </FormItem>
        </div>
        <FormItem label="地理位置" name="location" required>
          <Input v-model:value="ruleForm.location" placeholder="请输入" />
        </FormItem>
        <FormItem label="状态" name="state" required>
          <Switch
            inline-prompt
            checked-children="启用"
            un-checked-children="停用"
            :checked-value="1"
            :un-checked-value="0"
            v-model:checked="ruleForm.state"
          />
        </FormItem>
      </template>
      <template v-if="step == 1">
        <FormItem label="专属域名配置" name="domain" required>
          <Input
            v-model:value="ruleForm.domain"
            placeholder="请输入"
            addon-after=".com"
          />
        </FormItem>
        <FormItem label="访问策略" name="accessPolicy" required>
          <RadioGroup
            :options="[
              { label: '公网访问', value: 1 },
              { label: 'IP白名单 (内网)', value: 2 },
            ]"
            v-model:value="ruleForm.accessPolicy"
          />
          <Input
            v-if="ruleForm.accessPolicy == 2"
            class="mt-2"
            v-model:value="ruleForm.ipWhitelist"
            placeholder="请输入允许的IP段"
          />
        </FormItem>
        <FormItem label="学校Logo(URL)" name="logoUrl" required>
          <Upload
            v-model:modelValue="ruleForm.logoUrl"
            accept=".png,.jpg,.jpeg,image/jpg,image/jpeg,image/png"
            @update:modelValue="validateFn"
          />
        </FormItem>
      </template>
      <template v-if="step == 2">
        <div class="flex items-center space-x-2">
          <div class="w-1 h-4 bg-[#1977FF]"></div>
          <span class="text-sm text-[rgba(10,8,26,0.88);] font-medium">
            功能模块授权
          </span>
        </div>
        <FormItem label="" name="authModules" required>
          <CheckboxGroup
            :options="[
              {
                label: '数据概览',
                value: 'dataOverview',
              },
              {
                label: '校本课程库',
                value: 'courseLibrary',
              },
              {
                label: '班级架构',
                value: 'classStructure',
              },
              {
                label: '学生管理',
                value: 'studentManage',
              },
              {
                label: '教师管理',
                value: 'teacherManage',
              },
              {
                label: '学科配置',
                value: 'curriculumConfig',
              },
            ]"
            v-model:value="ruleForm.authModules"
          />
        </FormItem>
        <div class="flex items-center space-x-2">
          <div class="w-1 h-4 bg-[#1977FF]"></div>
          <span class="text-sm text-[rgba(10,8,26,0.88);] font-medium">
            功能模块授权
          </span>
        </div>
        <FormItem label="最大注册学生数" name="maxStudents" class="mt-2">
          <FormItemRest>
            <div class="flex items-center space-x-4 w-full px-2">
              <Slider
                class="grow"
                :max="10000"
                v-model:value="ruleForm.maxStudents"
              />
              <InputNumber
                v-model:value="ruleForm.maxStudents"
                class="shrink-0"
                placeholder="请输入"
              />
            </div>
          </FormItemRest>
        </FormItem>
        <FormItem label="AI调用额度（Tokens/月）" name="aiTokens" required>
          <FormItemRest>
            <div
              class="flex items-center space-x-4 w-full px-2 overflow-hidden"
            >
              <Slider
                class="grow"
                :max="10000"
                v-model:value="ruleForm.aiTokens"
              />
              <InputNumber
                v-model:value="ruleForm.aiTokens"
                class="shrink-0"
                placeholder="请输入"
                addon-after="万"
              />
            </div>
          </FormItemRest>
        </FormItem>
      </template>
      <template v-if="step == 3">
        <FormItem label="管理员名称" name="adminName" required>
          <Input v-model:value="ruleForm.adminName" placeholder="请输入" />
        </FormItem>
        <FormItem label="账号" name="adminAccount" required>
          <Input v-model:value="ruleForm.adminAccount" placeholder="请输入" />
        </FormItem>
        <FormItem
          label="密码"
          name="adminPassword"
          :required="ruleForm?.id ? false : true"
        >
          <Input v-model:value="ruleForm.adminPassword" placeholder="请输入" />
        </FormItem>
      </template>
    </Form>
  </Modal>
</template>
<script setup>
import { ref } from "vue";
import {
  Modal,
  Steps,
  Form,
  FormItem,
  Input,
  RadioGroup,
  CheckboxGroup,
  Switch,
  Slider,
  message,
  InputNumber,
  FormItemRest,
} from "ant-design-vue";
import Upload from "@/components/Upload/index.vue";
import * as api from "@/api/middleSchoolManage.js";
import { cloneDeep } from "lodash-es";

const emits = defineEmits(["success"]);
const dialogOpen = ref(false);
const ruleFormRef = ref();
const ruleForm = ref({});
const loading = ref(false);
const step = ref(0);

const submitForm = () => {
  ruleFormRef.value.validate().then(async () => {
    if (step.value < 3) {
      step.value += 1;
    } else {
      if (ruleForm.value?.id) {
        await api.modify(ruleForm.value);
      } else {
        await api.create(ruleForm.value);
      }
      dialogOpen.value = false;
      message.success("添加成功");
      emits("success");
    }
  });
};
const openModal = (e = { state: 1 }) => {
  try {
    ruleFormRef.value?.resetFields?.();
  } catch (error) {}
  ruleForm.value = cloneDeep(e);
  step.value = 0;
  dialogOpen.value = true;
};

const validateFn = () => {
  ruleFormRef.value?.validateFields(["logoUrl"]);
};

defineExpose({
  openModal,
});
</script>
