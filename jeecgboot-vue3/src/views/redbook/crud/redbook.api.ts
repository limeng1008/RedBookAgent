import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

export interface RedbookAuditParams {
  id: string;
  auditOpinion: string;
}

export interface RedbookVersionItem {
  id: string;
  draftId: string;
  versionNo: number;
  versionType: string;
  title: string;
  coverCopy?: string;
  body?: string;
  tags?: string;
  commentGuide?: string;
  publishTimeSuggestion?: string;
  contentType?: string;
  riskCheckResult?: string;
  auditStatus?: string;
  auditOpinion?: string;
  status?: string;
  remark?: string;
  createTime?: string;
}

export interface RedbookNoteMetricItem {
  id?: string;
  publishPlanId?: string;
  noteDraftId?: string;
  collectNode?: string;
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
  interactionRate?: number | string;
  collectRate?: number | string;
  commentRate?: number | string;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface RedbookMetricNodeStatus {
  metricId?: string;
  collectNode: string;
  filled: boolean;
  views?: number;
  interactionRate?: number | string;
  collectTime?: string;
}

export interface RedbookMetricCompleteness {
  publishPlanId: string;
  draftId?: string;
  publishStatus?: string;
  completed?: boolean;
  filledNodeCount?: number;
  requiredNodeCount?: number;
  coverageRate?: number | string;
  requiredNodes?: string[];
  existingNodes?: string[];
  missingNodes?: string[];
  latestCollectTime?: string;
  summary?: string;
  nodeStatusList?: RedbookMetricNodeStatus[];
}

export interface RedbookMetricBatchSaveParams {
  publishPlanId: string;
  metrics: RedbookNoteMetricItem[];
}

export interface RedbookMetricBatchSaveResult {
  publishPlanId: string;
  records: RedbookNoteMetricItem[];
  completeness: RedbookMetricCompleteness;
}

export interface RedbookPublishPlanLite {
  id: string;
  draftId?: string;
  accountId?: string;
  plannedPublishTime?: string;
  actualPublishTime?: string;
  publishStatus?: string;
  noteUrl?: string;
  remark?: string;
}

export function buildRedbookApi(apiBase: string) {
  const listUrl = `${apiBase}/list`;
  const addUrl = `${apiBase}/add`;
  const editUrl = `${apiBase}/edit`;
  const getUrl = `${apiBase}/queryById`;
  const deleteUrl = `${apiBase}/delete`;
  const deleteBatchUrl = `${apiBase}/deleteBatch`;
  const exportXlsUrl = `${apiBase}/exportXls`;
  const importExcelUrl = `${apiBase}/importExcel`;

  return {
    getExportUrl: exportXlsUrl,
    getImportUrl: importExcelUrl,
    list(params) {
      return defHttp.get({ url: listUrl, params });
    },
    saveOrUpdate(params, isUpdate) {
      return defHttp.post({ url: isUpdate ? editUrl : addUrl, params });
    },
    queryById(params) {
      return defHttp.get({ url: getUrl, params });
    },
    action(action: string, params) {
      return defHttp.post({ url: `${apiBase}/${action}`, params });
    },
    update(params) {
      return defHttp.post({ url: editUrl, params });
    },
    deleteRecord(params, handleSuccess) {
      return defHttp.delete({ url: deleteUrl, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
    batchDelete(params, handleSuccess) {
      Modal.confirm({
        title: '确认删除',
        content: '是否删除选中数据',
        okText: '确认',
        cancelText: '取消',
        onOk: () => {
          return defHttp.delete({ url: deleteBatchUrl, data: params }, { joinParamsToUrl: true }).then(() => {
            handleSuccess();
          });
        },
      });
    },
  };
}

export function approveNoteDraft(params: RedbookAuditParams) {
  return defHttp.post({
    url: '/redbook/noteDraft/approve',
    params,
  });
}

export function rejectNoteDraft(params: RedbookAuditParams) {
  return defHttp.post({
    url: '/redbook/noteDraft/reject',
    params,
  });
}

export function getNoteDraftVersions(draftId: string) {
  return defHttp.get<RedbookVersionItem[]>({
    url: '/redbook/noteDraft/versions',
    params: { draftId },
  });
}

export function restoreNoteDraftVersion(id: string) {
  return defHttp.post({
    url: '/redbook/noteDraft/restoreVersion',
    params: { id },
  });
}

export function listPublishPlans(params = {}) {
  return defHttp.get<{ records?: RedbookPublishPlanLite[]; total?: number }>({
    url: '/redbook/publishPlan/list',
    params,
  });
}

export function listNoteMetrics(params = {}) {
  return defHttp.get<{ records?: RedbookNoteMetricItem[]; total?: number }>({
    url: '/redbook/noteMetric/list',
    params,
  });
}

export function getMetricCompleteness(publishPlanId: string) {
  return defHttp.get<RedbookMetricCompleteness>({
    url: '/redbook/noteMetric/completeness',
    params: { publishPlanId },
  });
}

export function batchSaveNoteMetrics(params: RedbookMetricBatchSaveParams) {
  return defHttp.post<RedbookMetricBatchSaveResult>({
    url: '/redbook/noteMetric/batchSave',
    params,
  });
}
