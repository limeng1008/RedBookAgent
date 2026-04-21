<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="drawerTitle"
    :width="760"
    showFooter
    destroyOnClose
    @ok="handleSubmit"
  >
    <div class="plan-drawer">
      <div class="action-row">
        <a-space wrap>
          <a-button size="small" @click="copyDraftText" :disabled="!currentDraft.id">复制发布文案</a-button>
          <a-button size="small" type="primary" @click="handleMarkPublished" :disabled="!canMarkPublished">标记已发布</a-button>
          <a-button size="small" @click="handleCreateMetric" :disabled="!canCreateMetric">生成回收记录</a-button>
          <a-button size="small" @click="changeStatus('delayed', 1)" :disabled="!canDelay">延期一天</a-button>
          <a-button size="small" danger @click="changeStatus('canceled')" :disabled="!canCancel">取消排期</a-button>
          <a-button size="small" @click="changeStatus('pending')" :disabled="!canRestorePending">恢复待发布</a-button>
        </a-space>
      </div>

      <div class="summary-grid">
        <div class="summary-card">
          <span class="summary-label">草稿</span>
          <strong class="summary-value">{{ getReferenceLabel('draft', currentPlan.draftId) }}</strong>
        </div>
        <div class="summary-card">
          <span class="summary-label">账号</span>
          <strong class="summary-value">{{ getReferenceLabel('account', currentPlan.accountId) }}</strong>
        </div>
        <div class="summary-card">
          <span class="summary-label">状态</span>
          <a-tag :color="publishStatusMeta.color">{{ publishStatusMeta.label }}</a-tag>
        </div>
        <div class="summary-card">
          <span class="summary-label">建议发布时间</span>
          <strong class="summary-value">{{ currentDraft.publishTimeSuggestion || currentPlan.plannedPublishTime || '-' }}</strong>
        </div>
      </div>

      <BasicForm @register="registerForm" />

      <section class="draft-section">
        <div class="section-head">
          <div>
            <div class="section-title">{{ currentDraft.title || '未绑定草稿' }}</div>
            <div class="section-subtitle">{{ currentDraft.coverCopy || '封面文案会显示在这里，方便发布前快速校对。' }}</div>
          </div>
          <div class="section-tags">
            <a-tag v-if="currentDraft.auditStatus" :color="auditStatusMeta.color">{{ auditStatusMeta.label }}</a-tag>
            <a-tag v-if="currentDraft.status" :color="draftStatusMeta.color">{{ draftStatusMeta.label }}</a-tag>
          </div>
        </div>

        <div class="draft-meta">
          <span>内容类型：{{ currentDraft.contentType || '-' }}</span>
          <span>风险检查：{{ currentDraft.riskCheckResult || '-' }}</span>
        </div>

        <div class="draft-block">
          <div class="draft-block-label">正文</div>
          <div class="draft-block-value">{{ currentDraft.body || '暂无正文内容' }}</div>
        </div>

        <div class="draft-grid">
          <div class="draft-block">
            <div class="draft-block-label">标签</div>
            <div class="draft-block-value compact">{{ currentDraft.tags || '-' }}</div>
          </div>
          <div class="draft-block">
            <div class="draft-block-label">评论区引导</div>
            <div class="draft-block-value compact">{{ currentDraft.commentGuide || '-' }}</div>
          </div>
        </div>
      </section>
    </div>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import dayjs from 'dayjs';
  import { computed, reactive } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicForm, useForm } from '/@/components/Form';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { createReferenceApi, ensureReferences, getReferenceLabel, getStatusMeta, getStatusOptions } from '../crud/redbook.shared';
  import {
    createPublishMetric,
    getNoteDraftDetail,
    getPublishPlanDetail,
    markPublishPlanPublished,
    savePublishPlan,
    type RedbookNoteDraftPreview,
    type RedbookPublishPlanItem,
  } from './publish-calendar.api';

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();
  const currentPlan = reactive<Partial<RedbookPublishPlanItem>>({});
  const currentDraft = reactive<Partial<RedbookNoteDraftPreview>>({});

  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 110,
    showActionButtonGroup: false,
    schemas: [
      {
        field: 'id',
        label: 'ID',
        component: 'Input',
        show: false,
      },
      {
        field: 'accountId',
        label: '发布账号',
        component: 'ApiSelect',
        required: true,
        componentProps: {
          api: createReferenceApi('account'),
          labelField: 'label',
          valueField: 'value',
          immediate: true,
          showSearch: true,
          optionFilterProp: 'label',
          placeholder: '请选择发布账号',
        },
      },
      {
        field: 'plannedPublishTime',
        label: '计划发布时间',
        component: 'DatePicker',
        required: true,
        componentProps: {
          showTime: true,
          valueFormat: 'YYYY-MM-DD HH:mm:ss',
        },
      },
      {
        field: 'publishStatus',
        label: '发布状态',
        component: 'Select',
        componentProps: {
          options: getStatusOptions('publish'),
          allowClear: false,
        },
      },
      {
        field: 'noteUrl',
        label: '笔记链接',
        component: 'Input',
      },
      {
        field: 'publisher',
        label: '发布人',
        component: 'Input',
      },
      {
        field: 'remark',
        label: '备注',
        component: 'InputTextArea',
        componentProps: {
          rows: 3,
          maxlength: 300,
          showCount: true,
        },
      },
    ],
  });

  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await resetFields();
    clearReactive(currentPlan);
    clearReactive(currentDraft);
    setDrawerProps({ loading: true, confirmLoading: false });
    try {
      await ensureReferences(['account', 'draft', 'track']);
      const id = data?.record?.id || data?.id;
      const detail = await getPublishPlanDetail(id);
      Object.assign(currentPlan, detail || {});
      await setFieldsValue({
        ...detail,
      });
      if (detail?.draftId) {
        const draft = await getNoteDraftDetail(detail.draftId);
        Object.assign(currentDraft, draft || {});
      }
    } finally {
      setDrawerProps({ loading: false });
    }
  });

  const drawerTitle = computed(() => `${getReferenceLabel('draft', currentPlan.draftId)} 发布安排`);
  const publishStatusMeta = computed(() => getStatusMeta('publish', currentPlan.publishStatus));
  const auditStatusMeta = computed(() => getStatusMeta('audit', currentDraft.auditStatus));
  const draftStatusMeta = computed(() => getStatusMeta('draft', currentDraft.status));
  const canMarkPublished = computed(() => !!currentPlan.id && !['published', 'data_collected', 'canceled'].includes(currentPlan.publishStatus || ''));
  const canCreateMetric = computed(() => !!currentPlan.id && ['published', 'data_collected'].includes(currentPlan.publishStatus || ''));
  const canDelay = computed(() => !!currentPlan.id && !['published', 'data_collected', 'canceled'].includes(currentPlan.publishStatus || ''));
  const canCancel = computed(() => !!currentPlan.id && !['published', 'data_collected', 'canceled'].includes(currentPlan.publishStatus || ''));
  const canRestorePending = computed(() => !!currentPlan.id && ['delayed', 'canceled'].includes(currentPlan.publishStatus || ''));

  function clearReactive(target: Recordable) {
    Object.keys(target).forEach((key) => delete target[key]);
  }

  function buildDraftPublishText() {
    return [
      currentDraft.title || '',
      currentDraft.coverCopy ? `封面文案：${currentDraft.coverCopy}` : '',
      currentDraft.body || '',
      currentDraft.tags ? `标签：${currentDraft.tags}` : '',
      currentDraft.commentGuide ? `评论区引导：${currentDraft.commentGuide}` : '',
    ]
      .filter(Boolean)
      .join('\n')
      .trim();
  }

  async function reloadCurrent() {
    if (!currentPlan.id) {
      return;
    }
    const detail = await getPublishPlanDetail(currentPlan.id);
    clearReactive(currentPlan);
    Object.assign(currentPlan, detail || {});
    await setFieldsValue({
      ...detail,
    });
    if (detail?.draftId) {
      const draft = await getNoteDraftDetail(detail.draftId);
      clearReactive(currentDraft);
      Object.assign(currentDraft, draft || {});
    }
  }

  function copyDraftText() {
    const publishText = buildDraftPublishText();
    if (!publishText) {
      createMessage.warning('当前草稿暂无可复制内容');
      return;
    }
    const copied = copyTextToClipboard(publishText);
    copied ? createMessage.success('发布文案已复制') : createMessage.error('复制失败，请手动复制');
  }

  async function handleSubmit() {
    const values = await validate();
    setDrawerProps({ confirmLoading: true });
    try {
      await savePublishPlan({
        ...currentPlan,
        ...values,
        id: currentPlan.id,
      });
      createMessage.success('发布计划已更新');
      emit('success');
      closeDrawer();
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }

  async function handleMarkPublished() {
    if (!currentPlan.id) {
      return;
    }
    await markPublishPlanPublished(currentPlan.id);
    createMessage.success('已标记为已发布');
    await reloadCurrent();
    emit('success');
  }

  async function handleCreateMetric() {
    if (!currentPlan.id) {
      return;
    }
    await createPublishMetric(currentPlan.id);
    createMessage.success('已生成数据回收记录');
    emit('success');
  }

  async function changeStatus(status: string, delayDays = 0) {
    if (!currentPlan.id) {
      return;
    }
    const nextTime =
      delayDays > 0
        ? dayjs(currentPlan.plannedPublishTime || undefined)
            .add(delayDays, 'day')
            .format('YYYY-MM-DD HH:mm:ss')
        : currentPlan.plannedPublishTime;
    await savePublishPlan({
      ...currentPlan,
      id: currentPlan.id,
      publishStatus: status,
      plannedPublishTime: nextTime,
    });
    createMessage.success(status === 'delayed' ? '发布计划已延期一天' : status === 'canceled' ? '发布计划已取消' : '已恢复为待发布');
    await reloadCurrent();
    emit('success');
  }
</script>

<style lang="less" scoped>
  .plan-drawer {
    display: grid;
    gap: 18px;
  }

  .action-row {
    display: flex;
    justify-content: flex-end;
  }

  .summary-grid,
  .draft-grid {
    display: grid;
    gap: 12px;
  }

  .summary-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .summary-card,
  .draft-block,
  .draft-section {
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    background: #fff;
  }

  .summary-card {
    display: grid;
    gap: 8px;
    padding: 14px;
  }

  .summary-label,
  .section-subtitle,
  .draft-meta {
    color: #64748b;
  }

  .summary-value {
    color: #0f172a;
    font-size: 14px;
    line-height: 1.6;
  }

  .draft-section {
    padding: 16px;
    background: #f8fafc;
  }

  .section-head,
  .draft-meta {
    display: flex;
    justify-content: space-between;
    gap: 12px;
  }

  .section-head {
    align-items: flex-start;
  }

  .section-title {
    color: #0f172a;
    font-size: 16px;
    font-weight: 700;
  }

  .section-subtitle,
  .draft-meta {
    margin-top: 6px;
    font-size: 13px;
  }

  .section-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .draft-block {
    margin-top: 14px;
    padding: 14px;
  }

  .draft-grid {
    margin-top: 14px;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .draft-block-label {
    color: #0f172a;
    font-size: 13px;
    font-weight: 700;
  }

  .draft-block-value {
    margin-top: 8px;
    color: #334155;
    line-height: 1.8;
    white-space: pre-wrap;
  }

  .draft-block-value.compact {
    min-height: 48px;
  }

  @media only screen and (max-width: 960px) {
    .summary-grid,
    .draft-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
