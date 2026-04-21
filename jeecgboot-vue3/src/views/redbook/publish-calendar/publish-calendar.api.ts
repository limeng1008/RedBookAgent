import { defHttp } from '/@/utils/http/axios';

export interface RedbookPublishPlanItem {
  id: string;
  draftId?: string;
  accountId?: string;
  plannedPublishTime?: string;
  actualPublishTime?: string;
  publishStatus?: string;
  noteUrl?: string;
  publisher?: string;
  remark?: string;
}

export interface RedbookNoteDraftPreview {
  id: string;
  title?: string;
  trackId?: string;
  accountId?: string;
  coverCopy?: string;
  body?: string;
  tags?: string;
  commentGuide?: string;
  publishTimeSuggestion?: string;
  contentType?: string;
  riskCheckResult?: string;
  auditStatus?: string;
  status?: string;
}

interface PageResult<T> {
  records: T[];
  total?: number;
}

const mockPlans: RedbookPublishPlanItem[] = [
  {
    id: 'plan_mock_001',
    draftId: 'draft_mock_001',
    accountId: 'account_mock_001',
    plannedPublishTime: '2026-04-22 20:30:00',
    publishStatus: 'pending',
    publisher: '运营A',
    remark: '晚间发布，适合职场成长人群',
  },
  {
    id: 'plan_mock_002',
    draftId: 'draft_mock_002',
    accountId: 'account_mock_002',
    plannedPublishTime: '2026-04-24 12:10:00',
    publishStatus: 'delayed',
    publisher: '运营B',
    remark: '午间流量测试',
  },
  {
    id: 'plan_mock_003',
    draftId: 'draft_mock_003',
    accountId: 'account_mock_001',
    plannedPublishTime: '2026-04-26 19:45:00',
    actualPublishTime: '2026-04-26 20:03:00',
    publishStatus: 'published',
    noteUrl: 'https://www.xiaohongshu.com/explore/mock-note-003',
    publisher: '运营A',
  },
];

const mockDraftMap: Record<string, RedbookNoteDraftPreview> = {
  draft_mock_001: {
    id: 'draft_mock_001',
    title: '副业不是越多越好，先看这 3 个判断',
    trackId: 'track_mock_001',
    accountId: 'account_mock_001',
    coverCopy: '副业做太多，反而会拖垮主业',
    body: '很多人焦虑不是因为没副业，而是副业和主业都没有形成节奏。先看时间、现金流和可复制性，再决定要不要开第二条线。',
    tags: '副业,职场成长,时间管理',
    commentGuide: '你现在卡在时间、方向还是执行？',
    publishTimeSuggestion: '工作日 20:00-22:00',
    contentType: '干货',
    riskCheckResult: '低风险',
    auditStatus: 'approved',
    status: 'pending_publish',
  },
  draft_mock_002: {
    id: 'draft_mock_002',
    title: '通勤 90 分钟的人，怎么把碎片时间变成产出',
    trackId: 'track_mock_001',
    accountId: 'account_mock_002',
    coverCopy: '不是没有时间，是没有把时间切成任务',
    body: '通勤时间最怕被短视频和聊天吃掉。把碎片时间拆成输入、记录、产出三段，执行一周就能看到积累。',
    tags: '通勤效率,时间管理',
    commentGuide: '你的通勤时间一般被什么吞掉？',
    publishTimeSuggestion: '工作日 12:00-13:00',
    contentType: '教程',
    riskCheckResult: '低风险',
    auditStatus: 'approved',
    status: 'pending_publish',
  },
  draft_mock_003: {
    id: 'draft_mock_003',
    title: '为什么同样写清单，有的人发一篇就涨粉',
    trackId: 'track_mock_002',
    accountId: 'account_mock_001',
    coverCopy: '高收藏清单不是堆信息，而是帮用户少走弯路',
    body: '清单类内容的核心不是罗列，而是替用户完成筛选。你要把每一条都写成“为什么值得记下来”。',
    tags: '内容运营,清单模板',
    commentGuide: '你最想要哪类清单模板？',
    publishTimeSuggestion: '周末 19:00-21:00',
    contentType: '清单',
    riskCheckResult: '低风险',
    auditStatus: 'approved',
    status: 'published',
  },
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

export async function listPublishPlans(params: Recordable = {}): Promise<PageResult<RedbookPublishPlanItem>> {
  try {
    const result = await defHttp.get({
      url: '/redbook/publishPlan/list',
      params: {
        pageNo: 1,
        pageSize: 500,
        ...params,
      },
    });
    return normalizePageResult<RedbookPublishPlanItem>(result, mockPlans);
  } catch {
    return normalizePageResult<RedbookPublishPlanItem>(null, mockPlans);
  }
}

export async function getPublishPlanDetail(id: string): Promise<RedbookPublishPlanItem> {
  try {
    return await defHttp.get({
      url: '/redbook/publishPlan/queryById',
      params: { id },
    });
  } catch {
    return mockPlans.find((item) => item.id === id) || mockPlans[0];
  }
}

export function savePublishPlan(params: Recordable) {
  return defHttp.post({
    url: '/redbook/publishPlan/edit',
    params,
  });
}

export function markPublishPlanPublished(id: string) {
  return defHttp.post({
    url: '/redbook/publishPlan/markPublished',
    params: { id },
  });
}

export function createPublishMetric(id: string) {
  return defHttp.post({
    url: '/redbook/publishPlan/createMetric',
    params: { id },
  });
}

export async function getNoteDraftDetail(id: string): Promise<RedbookNoteDraftPreview> {
  try {
    return await defHttp.get({
      url: '/redbook/noteDraft/queryById',
      params: { id },
    });
  } catch {
    return mockDraftMap[id] || mockDraftMap.draft_mock_001;
  }
}
