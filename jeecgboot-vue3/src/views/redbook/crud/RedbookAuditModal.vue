<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="modalTitle" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { approveNoteDraft, rejectNoteDraft } from './redbook.api';

  const emit = defineEmits(['success', 'register']);
  const actionType = ref<'approve' | 'reject'>('approve');
  const currentRecord = ref<Recordable>({});

  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 110,
    showActionButtonGroup: false,
    schemas: [
      {
        field: 'auditOpinion',
        label: '审核意见',
        component: 'InputTextArea',
        required: true,
        componentProps: {
          rows: 5,
          maxlength: 300,
          showCount: true,
          placeholder: '请输入审核意见',
        },
      },
    ],
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    await resetFields();
    setModalProps({ confirmLoading: false });
    actionType.value = data?.action || 'approve';
    currentRecord.value = data?.record || {};
    await setFieldsValue({
      auditOpinion: actionType.value === 'approve' ? '内容合规，可以发布' : currentRecord.value?.auditOpinion || '',
    });
  });

  const modalTitle = computed(() => (actionType.value === 'approve' ? '草稿审核通过' : '草稿退回修改'));

  async function handleSubmit() {
    const values = await validate();
    setModalProps({ confirmLoading: true });
    try {
      const payload = {
        id: currentRecord.value.id,
        auditOpinion: values.auditOpinion,
      };
      if (actionType.value === 'approve') {
        await approveNoteDraft(payload);
      } else {
        await rejectNoteDraft(payload);
      }
      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
