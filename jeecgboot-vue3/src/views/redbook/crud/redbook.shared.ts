import { h, reactive } from 'vue';
import type { DescItem } from '/@/components/Description';
import { defHttp } from '/@/utils/http/axios';
import { Tag } from 'ant-design-vue';

export type RedbookReferenceKind = 'track' | 'account' | 'hotspot' | 'analysis' | 'draft' | 'publishPlan';
export type RedbookStatusKind =
  | 'common'
  | 'hotspot'
  | 'analysis'
  | 'draft'
  | 'audit'
  | 'publish'
  | 'review'
  | 'risk'
  | 'collectNode';

export interface RedbookOptionItem {
  label: string;
  value: string;
  raw?: Recordable;
}

interface ReferenceConfig {
  apiBase: string;
  labelField: string;
  statusField?: string;
  activeValue?: string;
}

interface StatusOption {
  label: string;
  value: string;
  color?: string;
}

const referenceConfigs: Record<RedbookReferenceKind, ReferenceConfig> = {
  track: {
    apiBase: '/redbook/track',
    labelField: 'trackName',
    statusField: 'status',
    activeValue: 'active',
  },
  account: {
    apiBase: '/redbook/account',
    labelField: 'accountName',
    statusField: 'status',
    activeValue: 'active',
  },
  hotspot: {
    apiBase: '/redbook/hotspot',
    labelField: 'title',
  },
  analysis: {
    apiBase: '/redbook/hotspotAnalysis',
    labelField: 'contentAngle',
  },
  draft: {
    apiBase: '/redbook/noteDraft',
    labelField: 'title',
  },
  publishPlan: {
    apiBase: '/redbook/publishPlan',
    labelField: 'id',
  },
};

const fallbackReferenceOptions: Record<RedbookReferenceKind, RedbookOptionItem[]> = {
  track: [
    { value: 'track_mock_001', label: '职场成长', raw: { id: 'track_mock_001', trackName: '职场成长' } },
    { value: 'track_mock_002', label: '生活方式', raw: { id: 'track_mock_002', trackName: '生活方式' } },
  ],
  account: [
    { value: 'account_mock_001', label: '职场成长号', raw: { id: 'account_mock_001', accountName: '职场成长号' } },
    { value: 'account_mock_002', label: '通勤效率号', raw: { id: 'account_mock_002', accountName: '通勤效率号' } },
  ],
  hotspot: [
    { value: 'hotspot_mock_001', label: '年轻人为什么越来越重视副业', raw: { id: 'hotspot_mock_001', title: '年轻人为什么越来越重视副业' } },
  ],
  analysis: [
    { value: 'analysis_mock_001', label: '副业焦虑切口', raw: { id: 'analysis_mock_001', contentAngle: '副业焦虑切口' } },
  ],
  draft: [
    { value: 'draft_mock_001', label: '副业不是越多越好，先看这 3 个判断', raw: { id: 'draft_mock_001', title: '副业不是越多越好，先看这 3 个判断' } },
  ],
  publishPlan: [{ value: 'plan_mock_001', label: 'plan_mock_001', raw: { id: 'plan_mock_001' } }],
};

const statusOptionsMap: Record<RedbookStatusKind, StatusOption[]> = {
  common: [
    { value: 'active', label: '启用', color: 'green' },
    { value: 'inactive', label: '停用', color: 'default' },
  ],
  hotspot: [
    { value: 'pending_analysis', label: '待分析', color: 'orange' },
    { value: 'analyzed', label: '已分析', color: 'blue' },
    { value: 'draft_generated', label: '已生成草稿', color: 'purple' },
    { value: 'discarded', label: '已废弃', color: 'default' },
  ],
  analysis: [
    { value: 'analyzed', label: '已分析', color: 'blue' },
    { value: 'adopted', label: '已采纳', color: 'green' },
    { value: 'discarded', label: '已废弃', color: 'default' },
  ],
  draft: [
    { value: 'pending_review', label: '待审核', color: 'orange' },
    { value: 'pending_publish', label: '待发布', color: 'blue' },
    { value: 'published', label: '已发布', color: 'green' },
  ],
  audit: [
    { value: 'pending', label: '待审核', color: 'orange' },
    { value: 'approved', label: '已通过', color: 'green' },
    { value: 'rejected', label: '已退回', color: 'red' },
  ],
  publish: [
    { value: 'pending', label: '待发布', color: 'orange' },
    { value: 'published', label: '已发布', color: 'green' },
    { value: 'delayed', label: '已延期', color: 'red' },
    { value: 'canceled', label: '已取消', color: 'default' },
    { value: 'data_collected', label: '已回收数据', color: 'blue' },
  ],
  review: [
    { value: 'draft', label: '草稿', color: 'orange' },
    { value: 'generated', label: '已生成', color: 'green' },
  ],
  risk: [
    { value: 'low', label: '低风险', color: 'green' },
    { value: 'medium', label: '中风险', color: 'orange' },
    { value: 'high', label: '高风险', color: 'red' },
  ],
  collectNode: [
    { value: '2h', label: '2h', color: 'blue' },
    { value: '24h', label: '24h', color: 'cyan' },
    { value: '72h', label: '72h', color: 'purple' },
    { value: '7d', label: '7d', color: 'gold' },
  ],
};

const referenceState = reactive<Record<RedbookReferenceKind, RedbookOptionItem[]>>({
  track: [],
  account: [],
  hotspot: [],
  analysis: [],
  draft: [],
  publishPlan: [],
});

export async function loadReferenceOptions(kind: RedbookReferenceKind, force = false): Promise<RedbookOptionItem[]> {
  if (!force && referenceState[kind].length) {
    return referenceState[kind];
  }
  const config = referenceConfigs[kind];
  try {
    const result = await defHttp.get<{ records?: Recordable[] }>({
      url: `${config.apiBase}/list`,
      params: {
        pageNo: 1,
        pageSize: 200,
      },
    });
    const records = Array.isArray(result?.records) ? result.records : [];
    const options = records
      .filter((item) => {
        if (!config.statusField || !config.activeValue) {
          return true;
        }
        return !item[config.statusField] || item[config.statusField] === config.activeValue;
      })
      .map((item) => ({
        label: String(item[config.labelField] || item.id || '-'),
        value: String(item.id || ''),
        raw: item,
      }));
    referenceState[kind] = options.length ? options : fallbackReferenceOptions[kind];
  } catch (error) {
    referenceState[kind] = fallbackReferenceOptions[kind];
  }
  return referenceState[kind];
}

export async function ensureReferences(kinds: RedbookReferenceKind[]) {
  await Promise.all([...new Set(kinds)].map((kind) => loadReferenceOptions(kind)));
}

export function getReferenceOptions(kind: RedbookReferenceKind): RedbookOptionItem[] {
  return referenceState[kind].length ? referenceState[kind] : fallbackReferenceOptions[kind];
}

export function getReferenceLabel(kind: RedbookReferenceKind, value?: string) {
  if (!value) {
    return '-';
  }
  const item = getReferenceOptions(kind).find((option) => option.value === value);
  return item?.label || value;
}

export function createReferenceApi(kind: RedbookReferenceKind) {
  return async () => {
    const options = await loadReferenceOptions(kind);
    return options.map(({ label, value }) => ({ label, value }));
  };
}

export function getStatusOptions(kind: RedbookStatusKind): StatusOption[] {
  return statusOptionsMap[kind] || [];
}

export function getStatusMeta(kind: RedbookStatusKind, value?: string): StatusOption {
  const matched = getStatusOptions(kind).find((item) => item.value === value);
  if (matched) {
    return matched;
  }
  return {
    value: value || '',
    label: value || '未设置',
    color: 'default',
  };
}

export function renderStatusTag(kind: RedbookStatusKind, value?: string) {
  const meta = getStatusMeta(kind, value);
  return h(Tag, { color: meta.color || 'default' }, () => meta.label);
}

export function createTextDesc(field: string, label: string, span = 1): DescItem {
  return {
    field,
    label,
    span,
    render: (value) => (value === null || value === undefined || value === '' ? '-' : value),
  };
}

export function createReferenceDesc(field: string, label: string, kind: RedbookReferenceKind, span = 1): DescItem {
  return {
    field,
    label,
    span,
    render: (value) => getReferenceLabel(kind, value),
  };
}

export function createStatusDesc(field: string, label: string, kind: RedbookStatusKind, span = 1): DescItem {
  return {
    field,
    label,
    span,
    render: (value) => renderStatusTag(kind, value),
  };
}
