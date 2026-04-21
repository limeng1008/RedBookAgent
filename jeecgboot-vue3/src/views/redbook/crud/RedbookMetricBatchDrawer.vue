<template>
  <BasicDrawer @register="registerDrawer" title="数据回收批量录入" width="1080">
    <div class="metric-batch">
      <div class="batch-toolbar">
        <a-select
          v-model:value="selectedPublishPlanId"
          class="plan-select"
          show-search
          :options="publishPlanOptions"
          :filter-option="filterPlanOption"
          placeholder="选择已发布的发布计划"
          @change="handlePublishPlanChange"
        />
        <a-button @click="reloadCurrentPlan" :disabled="!selectedPublishPlanId">刷新完整性</a-button>
      </div>

      <a-empty v-if="!publishPlans.length" description="暂无可录入数据的已发布计划" />

      <template v-else>
        <div v-if="selectedPlan" class="plan-summary">
          <div>
            <div class="summary-label">草稿</div>
            <div class="summary-value">{{ getReferenceLabel('draft', selectedPlan.draftId) }}</div>
          </div>
          <div>
            <div class="summary-label">账号</div>
            <div class="summary-value">{{ getReferenceLabel('account', selectedPlan.accountId) }}</div>
          </div>
          <div>
            <div class="summary-label">发布时间</div>
            <div class="summary-value">{{ selectedPlan.actualPublishTime || selectedPlan.plannedPublishTime || '-' }}</div>
          </div>
          <div>
            <div class="summary-label">状态</div>
            <a-tag :color="statusColor(selectedPlan.publishStatus)">{{ statusLabel(selectedPlan.publishStatus) }}</a-tag>
          </div>
        </div>

        <div v-if="completeness" class="completeness-panel">
          <div class="completeness-head">
            <div>
              <div class="panel-title">节点完整性</div>
              <div class="panel-subtitle">{{ completeness.summary || '-' }}</div>
            </div>
            <div class="progress-box">
              <a-progress type="circle" :percent="coveragePercent" :width="72" />
              <div class="progress-text">{{ completeness.filledNodeCount || 0 }}/{{ completeness.requiredNodeCount || 4 }}</div>
            </div>
          </div>

          <div class="node-grid">
            <div v-for="node in requiredNodes" :key="node" class="node-card" :class="{ filled: nodeStatus(node)?.filled }">
              <div class="node-top">
                <a-tag :color="nodeStatus(node)?.filled ? 'green' : 'orange'">{{ node }}</a-tag>
                <span>{{ nodeStatus(node)?.filled ? '已录入' : '缺失' }}</span>
              </div>
              <div class="node-metric">阅读 {{ nodeStatus(node)?.views || 0 }}</div>
              <div class="node-meta">互动率 {{ percentText(nodeStatus(node)?.interactionRate) }}</div>
              <div class="node-meta">{{ nodeStatus(node)?.collectTime || '-' }}</div>
            </div>
          </div>
        </div>

        <div class="metric-editor">
          <div class="editor-head">
            <div>
              <div class="panel-title">批量录入</div>
              <div class="panel-subtitle">2h、24h、72h、7d</div>
            </div>
            <a-space>
              <a-button @click="resetRows" :disabled="!selectedPublishPlanId">重置</a-button>
              <a-button type="primary" @click="saveRows" :loading="saving" :disabled="!selectedPublishPlanId">保存数据</a-button>
            </a-space>
          </div>

          <div class="metric-table-scroll">
            <table class="metric-table">
              <thead>
                <tr>
                  <th>节点</th>
                  <th>曝光</th>
                  <th>阅读</th>
                  <th>点赞</th>
                  <th>收藏</th>
                  <th>评论</th>
                  <th>分享</th>
                  <th>关注</th>
                  <th>私信</th>
                  <th>线索</th>
                  <th>转化</th>
                  <th>采集时间</th>
                  <th>备注</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in rows" :key="row.collectNode" :class="{ filled: !!row.id }">
                  <td class="node-cell">
                    <a-tag :color="row.id ? 'green' : 'blue'">{{ row.collectNode }}</a-tag>
                    <span>{{ row.id ? '已保存' : '待录入' }}</span>
                  </td>
                  <td><a-input-number v-model:value="row.impressions" :min="0" :precision="0" /></td>
                  <td><a-input-number v-model:value="row.views" :min="0" :precision="0" /></td>
                  <td><a-input-number v-model:value="row.likes" :min="0" :precision="0" /></td>
                  <td><a-input-number v-model:value="row.collects" :min="0" :precision="0" /></td>
                  <td><a-input-number v-model:value="row.comments" :min="0" :precision="0" /></td>
                  <td><a-input-number v-model:value="row.shares" :min="0" :precision="0" /></td>
                  <td><a-input-number v-model:value="row.followers" :min="0" :precision="0" /></td>
                  <td><a-input-number v-model:value="row.messages" :min="0" :precision="0" /></td>
                  <td><a-input-number v-model:value="row.leads" :min="0" :precision="0" /></td>
                  <td><a-input-number v-model:value="row.conversions" :min="0" :precision="0" /></td>
                  <td>
                    <a-date-picker
                      v-model:value="row.collectTime"
                      show-time
                      value-format="YYYY-MM-DD HH:mm:ss"
                      format="YYYY-MM-DD HH:mm"
                      class="time-picker"
                    />
                  </td>
                  <td><a-input v-model:value="row.remark" class="remark-input" /></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </template>
    </div>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    batchSaveNoteMetrics,
    getMetricCompleteness,
    listNoteMetrics,
    listPublishPlans,
    type RedbookMetricCompleteness,
    type RedbookMetricNodeStatus,
    type RedbookNoteMetricItem,
    type RedbookPublishPlanLite,
  } from './redbook.api';
  import { ensureReferences, getReferenceLabel, getStatusMeta } from './redbook.shared';

  interface MetricBatchRow {
    id?: string;
    collectNode: string;
    impressions?: number;
    views?: number;
    likes?: number;
    collects?: number;
    comments?: number;
    shares?: number;
    followers?: number;
    messages?: number;
    leads?: number;
    conversions?: number;
    collectTime?: string;
    remark?: string;
  }

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();
  const requiredNodes = ['2h', '24h', '72h', '7d'];
  const publishPlans = ref<RedbookPublishPlanLite[]>([]);
  const selectedPublishPlanId = ref('');
  const rows = ref<MetricBatchRow[]>(createEmptyRows());
  const completeness = ref<RedbookMetricCompleteness | null>(null);
  const saving = ref(false);

  const [registerDrawer, { setDrawerProps }] = useDrawerInner(async (data) => {
    setDrawerProps({ loading: true });
    try {
      await ensureReferences(['draft', 'account']);
      await loadPublishPlans();
      const targetPlanId = data?.publishPlanId || selectedPublishPlanId.value || publishPlans.value[0]?.id || '';
      selectedPublishPlanId.value = publishPlans.value.some((item) => item.id === targetPlanId) ? targetPlanId : publishPlans.value[0]?.id || '';
      if (selectedPublishPlanId.value) {
        await loadPlanData(selectedPublishPlanId.value);
      } else {
        rows.value = createEmptyRows();
        completeness.value = null;
      }
    } finally {
      setDrawerProps({ loading: false });
    }
  });

  const publishPlanOptions = computed(() =>
    publishPlans.value.map((plan) => ({
      label: buildPlanLabel(plan),
      value: plan.id,
    }))
  );

  const selectedPlan = computed(() => publishPlans.value.find((item) => item.id === selectedPublishPlanId.value));

  const nodeStatusMap = computed(() => {
    const statusMap: Record<string, RedbookMetricNodeStatus> = {};
    (completeness.value?.nodeStatusList || []).forEach((item) => {
      statusMap[normalizeNode(item.collectNode)] = item;
    });
    return statusMap;
  });

  const coveragePercent = computed(() => {
    const raw = Number(completeness.value?.coverageRate || 0);
    if (!Number.isFinite(raw)) {
      return 0;
    }
    return Math.min(100, Math.max(0, Math.round(raw * 100)));
  });

  async function loadPublishPlans() {
    const result = await listPublishPlans({ pageNo: 1, pageSize: 300 });
    const records = Array.isArray(result?.records) ? result.records : [];
    publishPlans.value = records.filter((item) => ['published', 'data_collected'].includes(item.publishStatus || ''));
  }

  async function handlePublishPlanChange(value: string) {
    if (!value) {
      return;
    }
    await loadPlanData(value);
  }

  async function reloadCurrentPlan() {
    if (!selectedPublishPlanId.value) {
      return;
    }
    await loadPlanData(selectedPublishPlanId.value);
  }

  async function loadPlanData(publishPlanId: string) {
    setDrawerProps({ loading: true });
    try {
      const [metricResult, completenessResult] = await Promise.all([
        listNoteMetrics({ pageNo: 1, pageSize: 200, publishPlanId }),
        getMetricCompleteness(publishPlanId),
      ]);
      completeness.value = completenessResult;
      rows.value = buildRows(Array.isArray(metricResult?.records) ? metricResult.records : []);
    } finally {
      setDrawerProps({ loading: false });
    }
  }

  function buildRows(metrics: RedbookNoteMetricItem[]) {
    const metricByNode: Record<string, RedbookNoteMetricItem> = {};
    metrics.forEach((metric) => {
      const node = normalizeNode(metric.collectNode);
      if (!requiredNodes.includes(node)) {
        return;
      }
      const current = metricByNode[node];
      if (!current || metricTime(metric) >= metricTime(current)) {
        metricByNode[node] = metric;
      }
    });
    return requiredNodes.map((node) => toRow(node, metricByNode[node]));
  }

  function resetRows() {
    rows.value = buildRows([]);
    if (selectedPublishPlanId.value) {
      loadPlanData(selectedPublishPlanId.value);
    }
  }

  async function saveRows() {
    if (!selectedPublishPlanId.value) {
      createMessage.warning('请选择发布计划');
      return;
    }
    const metrics = rows.value.filter(hasMetricInput).map(toPayload);
    if (!metrics.length) {
      createMessage.warning('请至少录入一条节点数据');
      return;
    }
    saving.value = true;
    try {
      const result = await batchSaveNoteMetrics({
        publishPlanId: selectedPublishPlanId.value,
        metrics,
      });
      completeness.value = result?.completeness || (await getMetricCompleteness(selectedPublishPlanId.value));
      createMessage.success('数据回收已保存');
      await loadPlanData(selectedPublishPlanId.value);
      emit('success');
    } finally {
      saving.value = false;
    }
  }

  function createEmptyRows() {
    return requiredNodes.map((node) => toRow(node));
  }

  function toRow(node: string, metric?: RedbookNoteMetricItem): MetricBatchRow {
    return {
      id: metric?.id,
      collectNode: node,
      impressions: metric?.impressions,
      views: metric?.views,
      likes: metric?.likes,
      collects: metric?.collects,
      comments: metric?.comments,
      shares: metric?.shares,
      followers: metric?.followers,
      messages: metric?.messages,
      leads: metric?.leads,
      conversions: metric?.conversions,
      collectTime: metric?.collectTime,
      remark: metric?.remark,
    };
  }

  function toPayload(row: MetricBatchRow): RedbookNoteMetricItem {
    return {
      id: row.id,
      publishPlanId: selectedPublishPlanId.value,
      collectNode: row.collectNode,
      impressions: toNumber(row.impressions),
      views: toNumber(row.views),
      likes: toNumber(row.likes),
      collects: toNumber(row.collects),
      comments: toNumber(row.comments),
      shares: toNumber(row.shares),
      followers: toNumber(row.followers),
      messages: toNumber(row.messages),
      leads: toNumber(row.leads),
      conversions: toNumber(row.conversions),
      collectTime: row.collectTime,
      remark: row.remark,
    };
  }

  function hasMetricInput(row: MetricBatchRow) {
    return !!(
      row.id ||
      row.collectTime ||
      row.remark ||
      row.impressions ||
      row.views ||
      row.likes ||
      row.collects ||
      row.comments ||
      row.shares ||
      row.followers ||
      row.messages ||
      row.leads ||
      row.conversions
    );
  }

  function nodeStatus(node: string) {
    return nodeStatusMap.value[normalizeNode(node)];
  }

  function buildPlanLabel(plan: RedbookPublishPlanLite) {
    const draft = getReferenceLabel('draft', plan.draftId);
    const account = getReferenceLabel('account', plan.accountId);
    const time = plan.actualPublishTime || plan.plannedPublishTime || '-';
    return `${draft} / ${account} / ${time}`;
  }

  function filterPlanOption(input: string, option: { label?: string }) {
    return String(option?.label || '').toLowerCase().includes(input.toLowerCase());
  }

  function statusLabel(status?: string) {
    return getStatusMeta('publish', status).label;
  }

  function statusColor(status?: string) {
    return getStatusMeta('publish', status).color || 'default';
  }

  function percentText(value?: number | string) {
    const numberValue = Number(value || 0);
    if (!Number.isFinite(numberValue)) {
      return '0.0%';
    }
    return `${(numberValue * 100).toFixed(1)}%`;
  }

  function metricTime(metric?: RedbookNoteMetricItem) {
    const text = metric?.collectTime || metric?.updateTime || metric?.createTime || '';
    const time = text ? new Date(text.replace(/-/g, '/')).getTime() : 0;
    return Number.isFinite(time) ? time : 0;
  }

  function normalizeNode(value?: string) {
    return String(value || '').trim().toLowerCase();
  }

  function toNumber(value?: number) {
    return Number(value || 0);
  }
</script>

<style lang="less" scoped>
  .metric-batch {
    display: grid;
    gap: 16px;
  }

  .batch-toolbar,
  .plan-summary,
  .completeness-head,
  .editor-head,
  .node-top,
  .node-cell {
    display: flex;
    align-items: center;
  }

  .batch-toolbar,
  .editor-head,
  .completeness-head {
    justify-content: space-between;
    gap: 12px;
  }

  .plan-select {
    width: min(620px, 100%);
  }

  .plan-summary,
  .completeness-panel,
  .metric-editor {
    padding: 14px;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    background: #fff;
  }

  .plan-summary {
    display: grid;
    grid-template-columns: 1.5fr 1fr 1.2fr 100px;
    gap: 12px;
  }

  .summary-label,
  .panel-subtitle,
  .node-meta {
    color: #64748b;
  }

  .summary-label {
    margin-bottom: 4px;
    font-size: 12px;
  }

  .summary-value,
  .panel-title {
    color: #111827;
    font-weight: 700;
  }

  .completeness-panel,
  .metric-editor {
    display: grid;
    gap: 14px;
  }

  .panel-title {
    font-size: 16px;
  }

  .panel-subtitle {
    margin-top: 4px;
  }

  .progress-box {
    display: grid;
    justify-items: center;
    gap: 4px;
  }

  .progress-text {
    color: #475569;
    font-size: 12px;
  }

  .node-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 10px;
  }

  .node-card {
    display: grid;
    gap: 6px;
    min-height: 104px;
    padding: 12px;
    border: 1px solid #fed7aa;
    border-radius: 8px;
    background: #fff7ed;
  }

  .node-card.filled {
    border-color: #bbf7d0;
    background: #f0fdf4;
  }

  .node-top,
  .node-cell {
    gap: 8px;
  }

  .node-metric {
    color: #111827;
    font-size: 18px;
    font-weight: 700;
  }

  .metric-table-scroll {
    overflow-x: auto;
  }

  .metric-table {
    width: 100%;
    min-width: 1240px;
    border-spacing: 0;
    border-collapse: separate;
  }

  .metric-table th,
  .metric-table td {
    padding: 8px;
    border-bottom: 1px solid #e5e7eb;
    text-align: left;
    white-space: nowrap;
  }

  .metric-table th {
    color: #475569;
    background: #f8fafc;
    font-weight: 700;
  }

  .metric-table tr.filled td {
    background: #fcfffd;
  }

  .metric-table :deep(.ant-input-number) {
    width: 82px;
  }

  .time-picker {
    width: 170px;
  }

  .remark-input {
    width: 180px;
  }

  @media (max-width: 900px) {
    .batch-toolbar,
    .editor-head,
    .completeness-head {
      align-items: flex-start;
      flex-direction: column;
    }

    .plan-summary,
    .node-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
