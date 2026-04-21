<template>
  <PageWrapper title="复盘看板">
    <div class="review-dashboard">
      <section class="filter-toolbar">
        <a-range-picker v-model:value="periodRange" valueFormat="YYYY-MM-DD" class="range-picker" />
        <a-select v-model:value="trackFilter" class="filter-select" :options="trackOptions" />
        <a-select v-model:value="accountFilter" class="filter-select" :options="accountOptions" />
        <a-button preIcon="ant-design:reload-outlined" @click="loadDashboard">刷新</a-button>
        <a-button type="primary" preIcon="ant-design:file-add-outlined" @click="openGenerateWizard">生成报告</a-button>
      </section>

      <a-skeleton active :loading="loading" :paragraph="{ rows: 8 }">
        <section class="summary-grid">
          <button v-for="item in summaryCards" :key="item.key" class="summary-card" type="button" @click="go(item.path)">
            <div>
              <div class="summary-label">{{ item.label }}</div>
              <div class="summary-desc">{{ item.desc }}</div>
            </div>
            <div class="summary-value">{{ item.value }}</div>
          </button>
        </section>

        <section class="report-panel">
          <div class="panel-head">
            <div>
              <div class="panel-title">{{ latestReportTitle }}</div>
              <div class="panel-subtitle">{{ activeFilterSummary }}</div>
            </div>
            <div class="panel-actions">
              <a-button preIcon="ant-design:reload-outlined" @click="loadDashboard">刷新</a-button>
              <a-button @click="handleCreateHotspots" :disabled="!canCreateHotspots">建议回流</a-button>
              <a-button type="primary" preIcon="ant-design:file-text-outlined" @click="go('/redbook/review-report')">复盘报告</a-button>
            </div>
          </div>
          <p class="report-summary">{{ latestReportSummary }}</p>
        </section>

        <section class="rank-grid">
          <div class="panel">
            <div class="panel-head">
              <div>
                <div class="panel-title">高表现笔记榜</div>
                <div class="panel-subtitle">按阅读、收藏和互动综合排序</div>
              </div>
            </div>
            <div v-if="highPerformList.length" class="rank-list">
              <button v-for="item in highPerformList" :key="`high-${item.publishPlanId}`" class="rank-item" type="button" @click="openNote(item)">
                <div class="rank-main">
                  <div class="rank-title">{{ item.title }}</div>
                  <div class="rank-meta">{{ item.trackName }} / {{ item.accountName }} / {{ item.collectNode || '最新' }}</div>
                </div>
                <div class="rank-side">
                  <strong>{{ formatNumber(item.views) }}</strong>
                  <span>{{ formatPercent(item.interactionRate) }}</span>
                </div>
              </button>
            </div>
            <a-empty v-else description="暂无已回收数据" />
          </div>

          <div class="panel">
            <div class="panel-head">
              <div>
                <div class="panel-title">低表现笔记榜</div>
                <div class="panel-subtitle">优先复查标题、开头和评论引导</div>
              </div>
            </div>
            <div v-if="lowPerformList.length" class="rank-list">
              <button v-for="item in lowPerformList" :key="`low-${item.publishPlanId}`" class="rank-item" type="button" @click="openNote(item)">
                <div class="rank-main">
                  <div class="rank-title">{{ item.title }}</div>
                  <div class="rank-meta">{{ item.trackName }} / {{ item.accountName }} / {{ item.collectNode || '最新' }}</div>
                </div>
                <div class="rank-side danger">
                  <strong>{{ formatNumber(item.views) }}</strong>
                  <span>{{ formatPercent(item.interactionRate) }}</span>
                </div>
              </button>
            </div>
            <a-empty v-else description="暂无已回收数据" />
          </div>
        </section>

        <section class="dimension-grid">
          <DimensionBoard title="赛道表现" :items="trackBoard" />
          <DimensionBoard title="账号表现" :items="accountBoard" />
          <DimensionBoard title="发布时间表现" :items="publishWindowBoard" />
        </section>

        <section class="suggestion-grid">
          <SuggestionPanel title="下一轮选题" :items="nextTopicSuggestions" />
          <SuggestionPanel title="下一轮标题" :items="nextTitleSuggestions" />
          <SuggestionPanel title="发布时间建议" :items="nextPublishSuggestions" />
        </section>
      </a-skeleton>
    </div>
    <ReviewReportGenerateModal @register="registerGenerateModal" @success="handleGenerateSuccess" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import dayjs from 'dayjs';
  import { Modal } from 'ant-design-vue';
  import { computed, defineComponent, h, onMounted, reactive, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { useModal } from '/@/components/Modal';
  import { PageWrapper } from '/@/components/Page';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ensureReferences, getReferenceLabel, getReferenceOptions } from '../crud/redbook.shared';
  import ReviewReportGenerateModal from './ReviewReportGenerateModal.vue';
  import {
    createHotspotsFromReviewReport,
    getRedbookReviewDashboard,
    listMetricRecords,
    listNoteDraftRecords,
    listPublishPlanRecords,
    listReviewReports,
    type RedbookNoteDraftLite,
    type RedbookNoteMetricItem,
    type RedbookPublishPlanLite,
    type RedbookReviewDashboard,
    type RedbookReviewDimension,
    type RedbookReviewRankItem,
    type RedbookReviewReportItem,
  } from './review-dashboard.api';

  const DimensionBoard = defineComponent({
    name: 'DimensionBoard',
    props: {
      title: { type: String, required: true },
      items: { type: Array as () => RedbookReviewDimension[], default: () => [] },
    },
    setup(props) {
      return () =>
        h('div', { class: 'panel' }, [
          h('div', { class: 'panel-head' }, [
            h('div', [h('div', { class: 'panel-title' }, props.title), h('div', { class: 'panel-subtitle' }, '发布量、回收量和平均互动率')]),
          ]),
          props.items.length
            ? h(
                'div',
                { class: 'dimension-list' },
                props.items.map((item) =>
                  h('div', { class: 'dimension-item', key: item.dimensionId }, [
                    h('div', { class: 'dimension-row' }, [
                      h('strong', item.dimensionName),
                      h('span', `${formatNumber(item.avgViews)} / ${formatPercent(item.avgInteractionRate)}`),
                    ]),
                    h('div', { class: 'dimension-bar' }, [h('span', { style: { width: `${dimensionPercent(item)}%` } })]),
                    h('div', { class: 'dimension-meta' }, `发布 ${item.publishCount} / 回收 ${item.collectedCount} / 总阅读 ${formatNumber(item.totalViews)}`),
                  ])
                )
              )
            : h('div', { class: 'empty-lite' }, '暂无数据'),
        ]);
    },
  });

  const SuggestionPanel = defineComponent({
    name: 'SuggestionPanel',
    props: {
      title: { type: String, required: true },
      items: { type: Array as () => string[], default: () => [] },
    },
    setup(props) {
      return () =>
        h('div', { class: 'panel' }, [
          h('div', { class: 'panel-head' }, [h('div', [h('div', { class: 'panel-title' }, props.title), h('div', { class: 'panel-subtitle' }, '来自当前筛选下的最新复盘报告')])]),
          props.items.length
            ? h(
                'div',
                { class: 'suggestion-list' },
                props.items.map((item, index) => h('div', { class: 'suggestion-item', key: `${props.title}-${index}` }, [h('span', `${index + 1}`), h('p', item)]))
              )
            : h('div', { class: 'empty-lite' }, '暂无建议'),
        ]);
    },
  });

  function createEmptyDashboard(): RedbookReviewDashboard {
    return {
      publishCount: 0,
      collectedPublishCount: 0,
      uncollectedPublishCount: 0,
      metricCount: 0,
      reviewReportCount: 0,
      avgViews: 0,
      avgInteractionRate: 0,
      avgCollectRate: 0,
      avgCommentRate: 0,
      latestReportName: '',
      latestSummary: '',
      highPerformList: [],
      lowPerformList: [],
      trackBoard: [],
      accountBoard: [],
      publishWindowBoard: [],
      nextTopicSuggestions: [],
      nextTitleSuggestions: [],
      nextPublishSuggestions: [],
    };
  }

  const router = useRouter();
  const { createMessage } = useMessage();
  const [registerGenerateModal, { openModal: openGenerateModal }] = useModal();
  const loading = ref(false);
  const periodRange = ref<string[]>([]);
  const trackFilter = ref('all');
  const accountFilter = ref('all');
  const dashboard = reactive<RedbookReviewDashboard>(createEmptyDashboard());
  const reports = ref<RedbookReviewReportItem[]>([]);
  const metrics = ref<RedbookNoteMetricItem[]>([]);
  const publishPlans = ref<RedbookPublishPlanLite[]>([]);
  const drafts = ref<RedbookNoteDraftLite[]>([]);

  const trackOptions = computed(() => [{ label: '全部赛道', value: 'all' }, ...getReferenceOptions('track')]);
  const accountOptions = computed(() => [{ label: '全部账号', value: 'all' }, ...getReferenceOptions('account')]);

  const draftMap = computed<Record<string, RedbookNoteDraftLite>>(() => {
    return drafts.value.reduce(
      (result, item) => {
        result[item.id] = item;
        return result;
      },
      {} as Record<string, RedbookNoteDraftLite>
    );
  });

  const publishPlanMap = computed<Record<string, RedbookPublishPlanLite>>(() => {
    return publishPlans.value.reduce(
      (result, item) => {
        result[item.id] = item;
        return result;
      },
      {} as Record<string, RedbookPublishPlanLite>
    );
  });

  const filteredReports = computed(() => {
    return [...reports.value]
      .filter((item) => matchesReportFilter(item))
      .sort((left, right) => dayjs(right.periodEnd || right.createTime || '').valueOf() - dayjs(left.periodEnd || left.createTime || '').valueOf());
  });

  const filteredPublishPlans = computed(() => {
    return publishPlans.value.filter((item) => matchesPlanFilter(item));
  });

  const filteredPlanIdSet = computed(() => new Set(filteredPublishPlans.value.map((item) => item.id)));

  const filteredMetrics = computed(() => {
    return metrics.value.filter((item) => item.publishPlanId && filteredPlanIdSet.value.has(item.publishPlanId));
  });

  const latestMetricByPlan = computed<Record<string, RedbookNoteMetricItem>>(() => {
    const result: Record<string, RedbookNoteMetricItem> = {};
    for (const item of filteredMetrics.value) {
      if (!item.publishPlanId) {
        continue;
      }
      const current = result[item.publishPlanId];
      if (!current || compareMetric(item, current) > 0) {
        result[item.publishPlanId] = item;
      }
    }
    return result;
  });

  const rankItems = computed<RedbookReviewRankItem[]>(() => {
    return Object.values(latestMetricByPlan.value)
      .map((item) => buildRankItem(item))
      .filter((item) => !!item.publishPlanId);
  });

  const highPerformList = computed(() => [...rankItems.value].sort((left, right) => right.score - left.score).slice(0, 5));
  const lowPerformList = computed(() => [...rankItems.value].sort((left, right) => left.score - right.score).slice(0, 5));
  const summaryView = computed(() => buildSummaryView());
  const latestReport = computed(() => filteredReports.value[0]);
  const latestReportTitle = computed(() => latestReport.value?.reportName || dashboard.latestReportName || '暂无复盘报告');
  const latestReportSummary = computed(
    () => latestReport.value?.summary || dashboard.latestSummary || '先生成一份复盘报告，看板会自动展示下一轮选题、标题和发布时间建议。'
  );
  const canCreateHotspots = computed(() => !!latestReport.value?.id);
  const trackBoard = computed(() => buildDimensionBoard('track'));
  const accountBoard = computed(() => buildDimensionBoard('account'));
  const publishWindowBoard = computed(() => buildDimensionBoard('window'));
  const nextTopicSuggestions = computed(() => readSuggestions(latestReport.value?.nextTopicSuggestions, dashboard.nextTopicSuggestions));
  const nextTitleSuggestions = computed(() => readSuggestions(latestReport.value?.nextTitleSuggestions, dashboard.nextTitleSuggestions));
  const nextPublishSuggestions = computed(() => readSuggestions(latestReport.value?.nextPublishSuggestions, dashboard.nextPublishSuggestions));
  const activeFilterSummary = computed(() => {
    const parts: string[] = [];
    if (periodRange.value.length === 2) {
      parts.push(`${periodRange.value[0]} 至 ${periodRange.value[1]}`);
    }
    if (trackFilter.value !== 'all') {
      parts.push(`赛道：${getReferenceLabel('track', trackFilter.value)}`);
    }
    if (accountFilter.value !== 'all') {
      parts.push(`账号：${getReferenceLabel('account', accountFilter.value)}`);
    }
    return parts.length ? parts.join(' / ') : '当前展示全量发布与复盘数据';
  });

  const summaryCards = computed(() => [
    { key: 'publish', label: '发布总数', value: summaryView.value.publishCount, desc: '进入复盘样本池', path: '/redbook/publish-plan' },
    { key: 'collected', label: '已回收', value: summaryView.value.collectedPublishCount, desc: `未回收 ${summaryView.value.uncollectedPublishCount}`, path: '/redbook/note-metric' },
    { key: 'views', label: '平均阅读', value: formatNumber(summaryView.value.avgViews), desc: `${summaryView.value.metricCount} 条回收记录`, path: '/redbook/note-metric' },
    { key: 'interaction', label: '平均互动率', value: formatPercent(summaryView.value.avgInteractionRate), desc: `收藏率 ${formatPercent(summaryView.value.avgCollectRate)}`, path: '/redbook/review-report' },
  ]);

  onMounted(async () => {
    await ensureReferences(['track', 'account', 'draft']);
    await loadDashboard();
  });

  async function loadDashboard() {
    loading.value = true;
    try {
      const [dashboardData, reportData, metricData, publishPlanData, draftData] = await Promise.all([
        getRedbookReviewDashboard(),
        listReviewReports(),
        listMetricRecords(),
        listPublishPlanRecords(),
        listNoteDraftRecords(),
      ]);
      Object.assign(dashboard, createEmptyDashboard(), dashboardData || {});
      reports.value = reportData?.records || [];
      metrics.value = metricData?.records || [];
      publishPlans.value = publishPlanData?.records || [];
      drafts.value = draftData?.records || [];
    } catch {
      createMessage.error('复盘看板加载失败');
    } finally {
      loading.value = false;
    }
  }

  function matchesPlanFilter(item: RedbookPublishPlanLite) {
    const draft = draftMap.value[item.draftId || ''];
    if (trackFilter.value !== 'all' && draft?.trackId !== trackFilter.value) {
      return false;
    }
    if (accountFilter.value !== 'all' && item.accountId !== accountFilter.value) {
      return false;
    }
    if (!matchesPeriod(item.actualPublishTime || item.plannedPublishTime)) {
      return false;
    }
    return ['published', 'data_collected', 'pending'].includes(item.publishStatus || '');
  }

  function matchesReportFilter(item: RedbookReviewReportItem) {
    if (trackFilter.value !== 'all' && item.trackId !== trackFilter.value) {
      return false;
    }
    if (accountFilter.value !== 'all' && item.accountId !== accountFilter.value) {
      return false;
    }
    if (!periodRange.value.length) {
      return true;
    }
    const start = dayjs(item.periodStart || item.createTime || '');
    const end = dayjs(item.periodEnd || item.createTime || '');
    const filterStart = dayjs(periodRange.value[0]);
    const filterEnd = dayjs(periodRange.value[1]);
    if (!start.isValid() || !end.isValid()) {
      return false;
    }
    return !end.isBefore(filterStart, 'day') && !start.isAfter(filterEnd, 'day');
  }

  function matchesPeriod(value?: string) {
    if (!periodRange.value.length || !value) {
      return true;
    }
    const target = dayjs(value);
    if (!target.isValid()) {
      return false;
    }
    return !target.isBefore(dayjs(periodRange.value[0]), 'day') && !target.isAfter(dayjs(periodRange.value[1]), 'day');
  }

  function compareMetric(left: RedbookNoteMetricItem, right: RedbookNoteMetricItem) {
    const leftTime = dayjs(left.collectTime || '').valueOf();
    const rightTime = dayjs(right.collectTime || '').valueOf();
    if (leftTime !== rightTime) {
      return leftTime - rightTime;
    }
    return collectNodeWeight(left.collectNode) - collectNodeWeight(right.collectNode);
  }

  function collectNodeWeight(value?: string) {
    return {
      '2h': 1,
      '24h': 2,
      '72h': 3,
      '7d': 4,
    }[value || ''] || 0;
  }

  function buildRankItem(item: RedbookNoteMetricItem): RedbookReviewRankItem {
    const plan = publishPlanMap.value[item.publishPlanId || ''];
    const draft = draftMap.value[item.noteDraftId || plan?.draftId || ''];
    const views = Number(item.views || 0);
    const interactionRate = Number(item.interactionRate || 0);
    const collectRate = Number(item.collectRate || 0);
    const likes = Number(item.likes || 0);
    const collects = Number(item.collects || 0);
    const comments = Number(item.comments || 0);
    const shares = Number(item.shares || 0);
    return {
      publishPlanId: plan?.id || item.publishPlanId || '',
      draftId: draft?.id || item.noteDraftId || '',
      title: draft?.title || getReferenceLabel('draft', plan?.draftId) || '未命名草稿',
      trackId: draft?.trackId || '',
      trackName: draft?.trackId ? getReferenceLabel('track', draft.trackId) : '未设置赛道',
      accountId: plan?.accountId || draft?.accountId || '',
      accountName: getReferenceLabel('account', plan?.accountId || draft?.accountId) || '未设置账号',
      publishTime: plan?.actualPublishTime || plan?.plannedPublishTime || '',
      collectNode: item.collectNode || '',
      views,
      likes,
      collects,
      comments,
      shares,
      interactionRate,
      collectRate,
      score: views / 100 + interactionRate * 1000 + collects + comments * 0.5 + shares * 0.5,
      noteUrl: plan?.noteUrl || '',
    };
  }

  function buildSummaryView() {
    const publishCount = filteredPublishPlans.value.length;
    const collectedPublishCount = rankItems.value.length;
    const uncollectedPublishCount = Math.max(0, publishCount - collectedPublishCount);
    const metricCount = filteredMetrics.value.length;
    const reviewReportCount = filteredReports.value.length;
    const avgViews = average(filteredMetrics.value.map((item) => Number(item.views || 0)));
    const avgInteractionRate = average(filteredMetrics.value.map((item) => Number(item.interactionRate || 0)));
    const avgCollectRate = average(filteredMetrics.value.map((item) => Number(item.collectRate || 0)));
    const avgCommentRate = average(filteredMetrics.value.map((item) => Number(item.commentRate || 0)));
    return {
      publishCount,
      collectedPublishCount,
      uncollectedPublishCount,
      metricCount,
      reviewReportCount,
      avgViews,
      avgInteractionRate,
      avgCollectRate,
      avgCommentRate,
    };
  }

  function buildDimensionBoard(kind: 'track' | 'account' | 'window'): RedbookReviewDimension[] {
    const bucketMap: Record<
      string,
      {
        dimensionId: string;
        dimensionName: string;
        publishCount: number;
        collectedCount: number;
        totalViews: number;
        totalInteractionRate: number;
      }
    > = {};

    for (const plan of filteredPublishPlans.value) {
      const draft = draftMap.value[plan.draftId || ''];
      const dimension = resolveDimension(kind, plan, draft);
      if (!dimension.dimensionId) {
        continue;
      }
      if (!bucketMap[dimension.dimensionId]) {
        bucketMap[dimension.dimensionId] = {
          dimensionId: dimension.dimensionId,
          dimensionName: dimension.dimensionName,
          publishCount: 0,
          collectedCount: 0,
          totalViews: 0,
          totalInteractionRate: 0,
        };
      }
      const bucket = bucketMap[dimension.dimensionId];
      bucket.publishCount += 1;
      const metric = latestMetricByPlan.value[plan.id];
      if (metric) {
        bucket.collectedCount += 1;
        bucket.totalViews += Number(metric.views || 0);
        bucket.totalInteractionRate += Number(metric.interactionRate || 0);
      }
    }

    return Object.values(bucketMap)
      .map((item) => {
        const avgViews = item.collectedCount ? item.totalViews / item.collectedCount : 0;
        const avgInteractionRate = item.collectedCount ? item.totalInteractionRate / item.collectedCount : 0;
        const score = Math.min(100, avgViews / 100 + avgInteractionRate * 300 + item.publishCount * 5);
        return {
          dimensionId: item.dimensionId,
          dimensionName: item.dimensionName,
          publishCount: item.publishCount,
          collectedCount: item.collectedCount,
          totalViews: item.totalViews,
          avgViews,
          avgInteractionRate,
          score,
        };
      })
      .sort((left, right) => right.score - left.score)
      .slice(0, 6);
  }

  function resolveDimension(kind: 'track' | 'account' | 'window', plan: RedbookPublishPlanLite, draft?: RedbookNoteDraftLite) {
    if (kind === 'track') {
      const trackId = draft?.trackId || '';
      return {
        dimensionId: trackId,
        dimensionName: trackId ? getReferenceLabel('track', trackId) : '未设置赛道',
      };
    }
    if (kind === 'account') {
      const accountId = plan.accountId || draft?.accountId || '';
      return {
        dimensionId: accountId,
        dimensionName: accountId ? getReferenceLabel('account', accountId) : '未设置账号',
      };
    }
    const publishTime = plan.actualPublishTime || plan.plannedPublishTime;
    const label = resolvePublishWindow(publishTime);
    return {
      dimensionId: label,
      dimensionName: label,
    };
  }

  function resolvePublishWindow(value?: string) {
    const hour = dayjs(value || '').hour();
    if (hour >= 6 && hour < 11) {
      return '早间 06:00-10:59';
    }
    if (hour >= 11 && hour < 15) {
      return '午间 11:00-14:59';
    }
    if (hour >= 15 && hour < 19) {
      return '下午 15:00-18:59';
    }
    if (hour >= 19 && hour < 24) {
      return '晚间 19:00-23:59';
    }
    return '深夜 00:00-05:59';
  }

  function readSuggestions(value?: string, fallback: string[] = []) {
    const items = String(value || '')
      .split(/\r?\n|[；;]/)
      .map((item) => item.trim())
      .filter(Boolean);
    return items.length ? items : fallback;
  }

  function average(values: number[]) {
    if (!values.length) {
      return 0;
    }
    const total = values.reduce((sum, item) => sum + item, 0);
    return total / values.length;
  }

  function buildReportName() {
    const nameParts: string[] = [];
    if (periodRange.value.length === 2) {
      nameParts.push(`${periodRange.value[0]} 至 ${periodRange.value[1]}`);
    } else {
      nameParts.push(dayjs().format('YYYY-MM'));
    }
    if (trackFilter.value !== 'all') {
      nameParts.push(getReferenceLabel('track', trackFilter.value));
    }
    if (accountFilter.value !== 'all') {
      nameParts.push(getReferenceLabel('account', accountFilter.value));
    }
    nameParts.push('复盘');
    return nameParts.join(' ');
  }

  function openGenerateWizard() {
    openGenerateModal(true, {
      reportName: buildReportName(),
      trackId: trackFilter.value === 'all' ? '' : trackFilter.value,
      accountId: accountFilter.value === 'all' ? '' : accountFilter.value,
      periodStart: periodRange.value[0] || dayjs().startOf('month').format('YYYY-MM-DD'),
      periodEnd: periodRange.value[1] || dayjs().endOf('month').format('YYYY-MM-DD'),
    });
  }

  async function handleGenerateSuccess() {
    await loadDashboard();
    go('/redbook/review-report');
  }

  function handleCreateHotspots() {
    if (!latestReport.value?.id) {
      createMessage.warning('当前筛选下暂无可回流的复盘报告');
      return;
    }
    Modal.confirm({
      title: '确认回流下一轮选题？',
      content: '将把当前报告中的下一轮选题建议回流到热点池。',
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        await createHotspotsFromReviewReport(latestReport.value!.id);
        createMessage.success('下一轮选题已回流到热点池');
        router.push('/redbook/hotspot');
      },
    });
  }

  function go(path: string) {
    router.push(path);
  }

  function openNote(item: RedbookReviewRankItem) {
    if (item.noteUrl) {
      window.open(item.noteUrl, '_blank');
      return;
    }
    router.push('/redbook/note-metric');
  }

  function formatNumber(value?: number) {
    const numericValue = Number(value || 0);
    if (numericValue >= 10000) {
      return `${(numericValue / 10000).toFixed(1)}w`;
    }
    return numericValue.toFixed(numericValue % 1 === 0 ? 0 : 1);
  }

  function formatPercent(value?: number) {
    return `${(Number(value || 0) * 100).toFixed(2)}%`;
  }

  function dimensionPercent(item: RedbookReviewDimension) {
    return Math.min(100, Math.max(8, Number(item.score || 0)));
  }
</script>

<style lang="less" scoped>
  .review-dashboard {
    padding: 16px;
    background: #f5f7fb;
  }

  .filter-toolbar,
  .summary-grid,
  .rank-grid,
  .dimension-grid,
  .suggestion-grid {
    display: grid;
    gap: 16px;
  }

  .filter-toolbar {
    grid-template-columns: minmax(0, 260px) repeat(2, 160px) auto auto;
    align-items: center;
  }

  .summary-grid {
    margin-top: 16px;
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .rank-grid {
    margin-top: 16px;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dimension-grid,
  .suggestion-grid {
    margin-top: 16px;
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .range-picker,
  .filter-select {
    width: 100%;
  }

  .summary-card,
  .report-panel,
  .panel {
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    background: #fff;
  }

  .summary-card {
    display: flex;
    min-height: 104px;
    justify-content: space-between;
    align-items: center;
    padding: 18px;
    text-align: left;
    cursor: pointer;
  }

  .summary-label,
  .panel-subtitle,
  .rank-meta,
  .dimension-meta {
    color: #64748b;
  }

  .summary-label {
    font-size: 14px;
    font-weight: 600;
  }

  .summary-value {
    color: #0f172a;
    font-size: 28px;
    font-weight: 800;
  }

  .summary-desc {
    margin-top: 8px;
    color: #94a3b8;
  }

  .report-panel,
  .panel {
    padding: 18px;
  }

  .report-panel {
    margin-top: 16px;
  }

  .panel-head {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    align-items: flex-start;
  }

  .panel-title {
    color: #0f172a;
    font-size: 17px;
    font-weight: 800;
  }

  .panel-actions {
    display: flex;
    gap: 8px;
  }

  .report-summary {
    margin: 14px 0 0;
    color: #334155;
    line-height: 1.8;
    white-space: pre-wrap;
  }

  .rank-list,
  .dimension-list,
  .suggestion-list {
    display: grid;
    gap: 10px;
    margin-top: 14px;
  }

  .rank-item {
    display: flex;
    width: 100%;
    justify-content: space-between;
    gap: 12px;
    padding: 12px;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    background: #f8fafc;
    text-align: left;
    cursor: pointer;
  }

  .rank-title {
    color: #0f172a;
    font-weight: 700;
  }

  .rank-side {
    display: grid;
    min-width: 88px;
    justify-items: end;
    color: #0f766e;
  }

  .rank-side.danger {
    color: #b91c1c;
  }

  .dimension-item {
    padding: 12px;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    background: #f8fafc;
  }

  .dimension-row {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    color: #0f172a;
  }

  .dimension-bar {
    height: 8px;
    margin: 10px 0;
    overflow: hidden;
    border-radius: 999px;
    background: #e2e8f0;
  }

  .dimension-bar span {
    display: block;
    height: 100%;
    border-radius: inherit;
    background: #2563eb;
  }

  .suggestion-item {
    display: grid;
    grid-template-columns: 28px minmax(0, 1fr);
    gap: 10px;
    align-items: flex-start;
    padding: 12px;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    background: #f8fafc;
  }

  .suggestion-item span {
    display: grid;
    width: 28px;
    height: 28px;
    place-items: center;
    border-radius: 50%;
    background: #dbeafe;
    color: #1d4ed8;
    font-weight: 700;
  }

  .suggestion-item p {
    margin: 0;
    color: #334155;
    line-height: 1.7;
  }

  .empty-lite {
    margin-top: 14px;
    color: #94a3b8;
  }

  @media only screen and (max-width: 1200px) {
    .filter-toolbar,
    .summary-grid,
    .rank-grid,
    .dimension-grid,
    .suggestion-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media only screen and (max-width: 720px) {
    .filter-toolbar,
    .summary-grid,
    .rank-grid,
    .dimension-grid,
    .suggestion-grid {
      grid-template-columns: 1fr;
    }

    .panel-head,
    .summary-card {
      flex-direction: column;
      align-items: flex-start;
    }

    .panel-actions {
      flex-wrap: wrap;
    }
  }
</style>
