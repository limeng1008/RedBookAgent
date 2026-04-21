import { defHttp } from '/@/utils/http/axios';

export interface RedbookReviewRankItem {
  publishPlanId: string;
  draftId: string;
  title: string;
  trackId: string;
  trackName: string;
  accountId: string;
  accountName: string;
  publishTime: string;
  collectNode: string;
  views: number;
  likes: number;
  collects: number;
  comments: number;
  shares: number;
  interactionRate: number;
  collectRate: number;
  score: number;
  noteUrl: string;
}

export interface RedbookReviewDimension {
  dimensionId: string;
  dimensionName: string;
  publishCount: number;
  collectedCount: number;
  totalViews: number;
  avgViews: number;
  avgInteractionRate: number;
  score: number;
}

export interface RedbookReviewDashboard {
  publishCount: number;
  collectedPublishCount: number;
  uncollectedPublishCount: number;
  metricCount: number;
  reviewReportCount: number;
  avgViews: number;
  avgInteractionRate: number;
  avgCollectRate: number;
  avgCommentRate: number;
  latestReportName: string;
  latestSummary: string;
  highPerformList: RedbookReviewRankItem[];
  lowPerformList: RedbookReviewRankItem[];
  trackBoard: RedbookReviewDimension[];
  accountBoard: RedbookReviewDimension[];
  publishWindowBoard: RedbookReviewDimension[];
  nextTopicSuggestions: string[];
  nextTitleSuggestions: string[];
  nextPublishSuggestions: string[];
}

export interface RedbookReviewReportItem {
  id: string;
  reportName: string;
  trackId?: string;
  accountId?: string;
  periodStart?: string;
  periodEnd?: string;
  summary?: string;
  highPerformingFactors?: string;
  lowPerformingReasons?: string;
  reusableTopics?: string;
  stoppedDirections?: string;
  nextTopicSuggestions?: string;
  nextTitleSuggestions?: string;
  nextPublishSuggestions?: string;
  rawResult?: string;
  status?: string;
  createTime?: string;
}

export interface RedbookNoteMetricItem {
  id: string;
  publishPlanId?: string;
  noteDraftId?: string;
  collectNode?: string;
  views?: number;
  likes?: number;
  collects?: number;
  comments?: number;
  shares?: number;
  interactionRate?: number;
  collectRate?: number;
  commentRate?: number;
  collectTime?: string;
}

export interface RedbookPublishPlanLite {
  id: string;
  draftId?: string;
  accountId?: string;
  plannedPublishTime?: string;
  actualPublishTime?: string;
  publishStatus?: string;
  noteUrl?: string;
}

export interface RedbookNoteDraftLite {
  id: string;
  title?: string;
  trackId?: string;
  accountId?: string;
  status?: string;
  auditStatus?: string;
}

export interface ReviewReportPayload {
  reportName: string;
  trackId?: string;
  accountId?: string;
  periodStart?: string;
  periodEnd?: string;
}

interface PageResult<T> {
  records: T[];
  total?: number;
}

const mockReports: RedbookReviewReportItem[] = [
  {
    id: 'report_mock_001',
    reportName: '2026-04 全局复盘',
    periodStart: '2026-04-01',
    periodEnd: '2026-04-30',
    summary: '晚间 20:00 左右发布的职场成长内容表现更稳定，清单与教程类更容易带来收藏和评论。',
    nextTopicSuggestions: '副业时间管理\n通勤碎片时间利用\n高收藏清单框架',
    nextTitleSuggestions: '副业不是越多越好，先看这 3 个判断\n通勤 90 分钟的人，怎么把碎片时间变成产出',
    nextPublishSuggestions: '工作日 20:00-22:00\n工作日 12:00-13:00',
    status: 'generated',
    createTime: '2026-04-30 22:10:00',
  },
  {
    id: 'report_mock_002',
    reportName: '职场成长赛道周复盘',
    trackId: 'track_mock_001',
    periodStart: '2026-04-15',
    periodEnd: '2026-04-21',
    summary: '职场成长赛道更吃真实场景和可执行步骤，泛泛观点类内容阅读不低，但收藏率偏弱。',
    nextTopicSuggestions: '副业优先级判断\n碎片时间任务拆分',
    nextTitleSuggestions: '别急着做第二份副业，先过这 3 个筛选器\n通勤时间不够？先把任务切成三段',
    nextPublishSuggestions: '工作日 20:30\n周末 19:30',
    status: 'generated',
    createTime: '2026-04-21 23:00:00',
  },
];

const mockDrafts: RedbookNoteDraftLite[] = [
  { id: 'draft_mock_001', title: '副业不是越多越好，先看这 3 个判断', trackId: 'track_mock_001', accountId: 'account_mock_001', status: 'pending_publish', auditStatus: 'approved' },
  { id: 'draft_mock_002', title: '通勤 90 分钟的人，怎么把碎片时间变成产出', trackId: 'track_mock_001', accountId: 'account_mock_002', status: 'published', auditStatus: 'approved' },
  { id: 'draft_mock_003', title: '为什么同样写清单，有的人发一篇就涨粉', trackId: 'track_mock_002', accountId: 'account_mock_001', status: 'published', auditStatus: 'approved' },
  { id: 'draft_mock_004', title: '标题党不一定有用，真正决定收藏的是这一步', trackId: 'track_mock_002', accountId: 'account_mock_002', status: 'published', auditStatus: 'approved' },
];

const mockPublishPlans: RedbookPublishPlanLite[] = [
  { id: 'plan_mock_001', draftId: 'draft_mock_001', accountId: 'account_mock_001', plannedPublishTime: '2026-04-22 20:30:00', publishStatus: 'pending' },
  { id: 'plan_mock_002', draftId: 'draft_mock_002', accountId: 'account_mock_002', actualPublishTime: '2026-04-20 12:12:00', plannedPublishTime: '2026-04-20 12:00:00', publishStatus: 'data_collected', noteUrl: 'https://www.xiaohongshu.com/explore/mock-note-002' },
  { id: 'plan_mock_003', draftId: 'draft_mock_003', accountId: 'account_mock_001', actualPublishTime: '2026-04-18 20:03:00', plannedPublishTime: '2026-04-18 19:45:00', publishStatus: 'data_collected', noteUrl: 'https://www.xiaohongshu.com/explore/mock-note-003' },
  { id: 'plan_mock_004', draftId: 'draft_mock_004', accountId: 'account_mock_002', actualPublishTime: '2026-04-16 09:32:00', plannedPublishTime: '2026-04-16 09:00:00', publishStatus: 'data_collected', noteUrl: 'https://www.xiaohongshu.com/explore/mock-note-004' },
];

const mockMetrics: RedbookNoteMetricItem[] = [
  { id: 'metric_mock_001', publishPlanId: 'plan_mock_002', noteDraftId: 'draft_mock_002', collectNode: '24h', views: 8200, likes: 660, collects: 490, comments: 126, shares: 46, interactionRate: 0.161, collectRate: 0.059, commentRate: 0.015, collectTime: '2026-04-21 12:12:00' },
  { id: 'metric_mock_002', publishPlanId: 'plan_mock_003', noteDraftId: 'draft_mock_003', collectNode: '24h', views: 12800, likes: 980, collects: 760, comments: 148, shares: 72, interactionRate: 0.153, collectRate: 0.059, commentRate: 0.012, collectTime: '2026-04-19 20:03:00' },
  { id: 'metric_mock_003', publishPlanId: 'plan_mock_004', noteDraftId: 'draft_mock_004', collectNode: '24h', views: 3400, likes: 168, collects: 74, comments: 28, shares: 9, interactionRate: 0.082, collectRate: 0.022, commentRate: 0.008, collectTime: '2026-04-17 09:32:00' },
  { id: 'metric_mock_004', publishPlanId: 'plan_mock_003', noteDraftId: 'draft_mock_003', collectNode: '72h', views: 16500, likes: 1230, collects: 910, comments: 208, shares: 98, interactionRate: 0.148, collectRate: 0.055, commentRate: 0.013, collectTime: '2026-04-21 20:03:00' },
];

const mockDashboard: RedbookReviewDashboard = {
  publishCount: 4,
  collectedPublishCount: 3,
  uncollectedPublishCount: 1,
  metricCount: 4,
  reviewReportCount: 2,
  avgViews: 10225,
  avgInteractionRate: 0.136,
  avgCollectRate: 0.049,
  avgCommentRate: 0.012,
  latestReportName: '2026-04 全局复盘',
  latestSummary: '晚间发布的职场成长内容表现更稳定，教程和清单更容易形成收藏。',
  highPerformList: [],
  lowPerformList: [],
  trackBoard: [],
  accountBoard: [],
  publishWindowBoard: [],
  nextTopicSuggestions: ['副业时间管理', '通勤碎片时间利用', '高收藏清单框架'],
  nextTitleSuggestions: ['副业不是越多越好，先看这 3 个判断', '通勤 90 分钟的人，怎么把碎片时间变成产出'],
  nextPublishSuggestions: ['工作日 20:00-22:00', '工作日 12:00-13:00'],
};

function normalizePageResult<T>(result: any, fallbackRecords: T[]): PageResult<T> {
  if (Array.isArray(result?.records)) {
    return result;
  }
  if (Array.isArray(result)) {
    return {
      records: result,
      total: result.length,
    };
  }
  return {
    records: fallbackRecords,
    total: fallbackRecords.length,
  };
}

export async function getRedbookReviewDashboard() {
  try {
    return await defHttp.get<RedbookReviewDashboard>({
      url: '/redbook/workbench/reviewDashboard',
    });
  } catch {
    return mockDashboard;
  }
}

export async function listReviewReports(params: Recordable = {}): Promise<PageResult<RedbookReviewReportItem>> {
  try {
    const result = await defHttp.get({
      url: '/redbook/reviewReport/list',
      params: {
        pageNo: 1,
        pageSize: 200,
        ...params,
      },
    });
    return normalizePageResult<RedbookReviewReportItem>(result, mockReports);
  } catch {
    return normalizePageResult<RedbookReviewReportItem>(null, mockReports);
  }
}

export async function listMetricRecords(params: Recordable = {}): Promise<PageResult<RedbookNoteMetricItem>> {
  try {
    const result = await defHttp.get({
      url: '/redbook/noteMetric/list',
      params: {
        pageNo: 1,
        pageSize: 500,
        ...params,
      },
    });
    return normalizePageResult<RedbookNoteMetricItem>(result, mockMetrics);
  } catch {
    return normalizePageResult<RedbookNoteMetricItem>(null, mockMetrics);
  }
}

export async function listPublishPlanRecords(params: Recordable = {}): Promise<PageResult<RedbookPublishPlanLite>> {
  try {
    const result = await defHttp.get({
      url: '/redbook/publishPlan/list',
      params: {
        pageNo: 1,
        pageSize: 500,
        ...params,
      },
    });
    return normalizePageResult<RedbookPublishPlanLite>(result, mockPublishPlans);
  } catch {
    return normalizePageResult<RedbookPublishPlanLite>(null, mockPublishPlans);
  }
}

export async function listNoteDraftRecords(params: Recordable = {}): Promise<PageResult<RedbookNoteDraftLite>> {
  try {
    const result = await defHttp.get({
      url: '/redbook/noteDraft/list',
      params: {
        pageNo: 1,
        pageSize: 500,
        ...params,
      },
    });
    return normalizePageResult<RedbookNoteDraftLite>(result, mockDrafts);
  } catch {
    return normalizePageResult<RedbookNoteDraftLite>(null, mockDrafts);
  }
}

export function createReviewReport(params: ReviewReportPayload) {
  return defHttp.post<RedbookReviewReportItem>({
    url: '/redbook/reviewReport/add',
    params,
  });
}

export function generateReviewReport(id: string) {
  return defHttp.post<RedbookReviewReportItem>({
    url: '/redbook/reviewReport/generate',
    params: { id },
  });
}

export function createHotspotsFromReviewReport(id: string) {
  return defHttp.post({
    url: '/redbook/reviewReport/createHotspots',
    params: { id },
  });
}
