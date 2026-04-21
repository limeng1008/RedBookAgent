<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="modalTitle" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import dayjs from 'dayjs';
  import { computed } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { createReferenceApi } from '../crud/redbook.shared';
  import { createReviewReport, generateReviewReport } from './review-dashboard.api';

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();

  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 110,
    showActionButtonGroup: false,
    schemas: [
      {
        field: 'reportName',
        label: '报告名称',
        component: 'Input',
        required: true,
      },
      {
        field: 'trackId',
        label: '赛道',
        component: 'ApiSelect',
        componentProps: {
          api: createReferenceApi('track'),
          labelField: 'label',
          valueField: 'value',
          immediate: true,
          allowClear: true,
          showSearch: true,
          optionFilterProp: 'label',
          placeholder: '全部赛道',
        },
      },
      {
        field: 'accountId',
        label: '账号',
        component: 'ApiSelect',
        componentProps: {
          api: createReferenceApi('account'),
          labelField: 'label',
          valueField: 'value',
          immediate: true,
          allowClear: true,
          showSearch: true,
          optionFilterProp: 'label',
          placeholder: '全部账号',
        },
      },
      {
        field: 'periodStart',
        label: '开始日期',
        component: 'DatePicker',
        required: true,
        componentProps: {
          valueFormat: 'YYYY-MM-DD',
        },
      },
      {
        field: 'periodEnd',
        label: '结束日期',
        component: 'DatePicker',
        required: true,
        componentProps: {
          valueFormat: 'YYYY-MM-DD',
        },
      },
    ],
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    await resetFields();
    setModalProps({ confirmLoading: false });
    const periodStart = data?.periodStart || dayjs().startOf('month').format('YYYY-MM-DD');
    const periodEnd = data?.periodEnd || dayjs().endOf('month').format('YYYY-MM-DD');
    await setFieldsValue({
      reportName: data?.reportName || `${dayjs(periodEnd).format('YYYY-MM')} 运营复盘`,
      trackId: data?.trackId || '',
      accountId: data?.accountId || '',
      periodStart,
      periodEnd,
    });
  });

  const modalTitle = computed(() => '生成复盘报告');

  async function handleSubmit() {
    const values = await validate();
    setModalProps({ confirmLoading: true });
    try {
      const created = await createReviewReport(values);
      await generateReviewReport(created.id);
      createMessage.success('复盘报告已生成');
      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
