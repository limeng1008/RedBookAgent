import type { DescItem } from '/@/components/Description';
import type { BasicColumn, FormSchema } from '/@/components/Table';
import type { RedbookReferenceKind, RedbookStatusKind } from './redbook.shared';
import {
  createReferenceApi,
  createReferenceDesc,
  createStatusDesc,
  createTextDesc,
  getReferenceLabel,
  getStatusOptions,
  renderStatusTag,
} from './redbook.shared';

export interface RedbookModuleConfig {
  key: string;
  title: string;
  apiBase: string;
  columns: BasicColumn[];
  searchFormSchema: FormSchema[];
  formSchema: FormSchema[];
  detailSchema: DescItem[];
  referenceKinds?: RedbookReferenceKind[];
  actionColumnWidth?: number;
  rowActions?: RedbookRowActionConfig[];
}

export interface RedbookRowActionConfig {
  label: string;
  action: string;
  successMessage: string;
  actionType?: 'api' | 'copy';
  confirmTitle?: string;
  copyText?: (record: Recordable) => string;
  ifShow?: (record: Recordable) => boolean;
}

const idField: FormSchema = {
  label: 'ID',
  field: 'id',
  component: 'Input',
  show: false,
};

const platformOptions = [
  { label: '小红书', value: 'xiaohongshu' },
  { label: '微博', value: 'weibo' },
  { label: '抖音', value: 'douyin' },
  { label: 'B站', value: 'bilibili' },
];

const contentTypeOptions = ['干货', '测评', '避坑', '清单', '教程', '故事', '对比', '种草', '复盘'].map((value) => ({
  label: value,
  value,
}));

function col(title: string, dataIndex: string, width = 160): BasicColumn {
  return { title, dataIndex, width, align: 'left' };
}

function relationCol(title: string, dataIndex: string, relation: RedbookReferenceKind, width = 180): BasicColumn {
  return {
    title,
    dataIndex,
    width,
    align: 'left',
    customRender: ({ text }) => getReferenceLabel(relation, text),
  };
}

function statusCol(title: string, dataIndex: string, statusKind: RedbookStatusKind, width = 120): BasicColumn {
  return {
    title,
    dataIndex,
    width,
    customRender: ({ text }) => renderStatusTag(statusKind, text),
  };
}

function textInput(field: string, label: string, required = false, defaultValue?: string): FormSchema {
  return { field, label, component: 'Input', required, defaultValue };
}

function textarea(field: string, label: string, required = false, rows = 4): FormSchema {
  return {
    field,
    label,
    component: 'InputTextArea',
    required,
    componentProps: { rows },
  };
}

function number(field: string, label: string): FormSchema {
  return { field, label, component: 'InputNumber', componentProps: { min: 0 } };
}

function select(field: string, label: string, options: { label: string; value: string }[], required = false, defaultValue?: string): FormSchema {
  return {
    field,
    label,
    component: 'Select',
    required,
    defaultValue,
    componentProps: {
      options,
      allowClear: true,
      showSearch: true,
      optionFilterProp: 'label',
      placeholder: `请选择${label}`,
    },
  };
}

function status(field: string, label: string, statusKind: RedbookStatusKind, defaultValue?: string): FormSchema {
  return select(field, label, getStatusOptions(statusKind), false, defaultValue);
}

function relation(field: string, label: string, relationKind: RedbookReferenceKind, required = false): FormSchema {
  return {
    field,
    label,
    component: 'ApiSelect',
    required,
    componentProps: {
      api: createReferenceApi(relationKind),
      labelField: 'label',
      valueField: 'value',
      immediate: true,
      placeholder: `请选择${label}`,
      showSearch: true,
      optionFilterProp: 'label',
    },
  };
}

function dateTime(field: string, label: string): FormSchema {
  return {
    field,
    label,
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
    },
  };
}

function date(field: string, label: string): FormSchema {
  return {
    field,
    label,
    component: 'DatePicker',
    componentProps: {
      valueFormat: 'YYYY-MM-DD',
    },
  };
}

function search(field: string, label: string): FormSchema {
  return { field, label, component: 'Input', colProps: { span: 8 } };
}

function searchSelect(field: string, label: string, options: { label: string; value: string }[]): FormSchema {
  return {
    field,
    label,
    component: 'Select',
    colProps: { span: 8 },
    componentProps: {
      options,
      allowClear: true,
      showSearch: true,
      optionFilterProp: 'label',
      placeholder: `请选择${label}`,
    },
  };
}

function searchRelation(field: string, label: string, relationKind: RedbookReferenceKind): FormSchema {
  return {
    field,
    label,
    component: 'ApiSelect',
    colProps: { span: 8 },
    componentProps: {
      api: createReferenceApi(relationKind),
      labelField: 'label',
      valueField: 'value',
      immediate: true,
      placeholder: `请选择${label}`,
    },
  };
}

function buildNotePublishText(record: Recordable): string {
  return [
    record.title || '',
    record.coverCopy ? `封面文案：${record.coverCopy}` : '',
    record.body || '',
    record.tags ? `标签：${record.tags}` : '',
    record.commentGuide ? `评论区引导：${record.commentGuide}` : '',
  ]
    .filter(Boolean)
    .join('\n')
    .trim();
}

export const redbookModuleConfigs: Record<string, RedbookModuleConfig> = {
  account: {
    key: 'account',
    title: '账号管理',
    apiBase: '/redbook/account',
    referenceKinds: ['track'],
    columns: [
      col('账号名称', 'accountName', 180),
      col('平台', 'platform', 120),
      col('账号定位', 'positioning', 220),
      col('目标人群', 'targetAudience', 200),
      relationCol('主要赛道', 'primaryTrackId', 'track', 160),
      statusCol('状态', 'status', 'common', 110),
    ],
    searchFormSchema: [search('accountName', '账号名称'), searchSelect('platform', '平台', platformOptions), searchRelation('primaryTrackId', '主要赛道', 'track')],
    formSchema: [
      idField,
      textInput('accountName', '账号名称', true),
      select('platform', '平台', platformOptions, false, 'xiaohongshu'),
      textarea('positioning', '账号定位'),
      textarea('targetAudience', '目标人群'),
      textarea('contentStyle', '内容风格'),
      relation('primaryTrackId', '主要赛道', 'track'),
      status('status', '状态', 'common', 'active'),
      textarea('remark', '备注', false, 3),
    ],
    detailSchema: [
      createTextDesc('accountName', '账号名称'),
      createTextDesc('platform', '平台'),
      createReferenceDesc('primaryTrackId', '主要赛道', 'track'),
      createStatusDesc('status', '状态', 'common'),
      createTextDesc('positioning', '账号定位', 3),
      createTextDesc('targetAudience', '目标人群', 3),
      createTextDesc('contentStyle', '内容风格', 3),
      createTextDesc('remark', '备注', 3),
    ],
  },
  track: {
    key: 'track',
    title: '赛道管理',
    apiBase: '/redbook/track',
    columns: [
      col('赛道名称', 'trackName', 180),
      col('关键词', 'keywords', 260),
      col('目标人群', 'targetAudience', 220),
      col('内容方向', 'contentDirection', 220),
      col('优先级', 'priority', 90),
      statusCol('状态', 'status', 'common', 110),
    ],
    searchFormSchema: [search('trackName', '赛道名称'), search('keywords', '关键词'), searchSelect('status', '状态', getStatusOptions('common'))],
    formSchema: [
      idField,
      textInput('trackName', '赛道名称', true),
      textarea('keywords', '关键词'),
      textarea('targetAudience', '目标人群'),
      textarea('contentDirection', '内容方向'),
      textarea('competitorAccounts', '竞品账号'),
      number('priority', '优先级'),
      status('status', '状态', 'common', 'active'),
      textarea('remark', '备注', false, 3),
    ],
    detailSchema: [
      createTextDesc('trackName', '赛道名称'),
      createStatusDesc('status', '状态', 'common'),
      createTextDesc('priority', '优先级'),
      createTextDesc('keywords', '关键词', 3),
      createTextDesc('targetAudience', '目标人群', 3),
      createTextDesc('contentDirection', '内容方向', 3),
      createTextDesc('competitorAccounts', '竞品账号', 3),
      createTextDesc('remark', '备注', 3),
    ],
  },
  persona: {
    key: 'persona',
    title: '人设管理',
    apiBase: '/redbook/persona',
    columns: [
      col('人设名称', 'personaName', 180),
      col('语气风格', 'toneStyle', 220),
      col('专业程度', 'professionalism', 120),
      col('目标用户', 'targetAudience', 220),
      statusCol('状态', 'status', 'common', 110),
    ],
    searchFormSchema: [search('personaName', '人设名称'), searchSelect('status', '状态', getStatusOptions('common'))],
    formSchema: [
      idField,
      textInput('personaName', '人设名称', true),
      textarea('toneStyle', '语气风格'),
      textInput('professionalism', '专业程度'),
      textarea('targetAudience', '目标用户'),
      textarea('forbiddenExpressions', '禁止表达'),
      textarea('commonExpressions', '常用表达'),
      status('status', '状态', 'common', 'active'),
      textarea('remark', '备注', false, 3),
    ],
    detailSchema: [
      createTextDesc('personaName', '人设名称'),
      createTextDesc('professionalism', '专业程度'),
      createStatusDesc('status', '状态', 'common'),
      createTextDesc('toneStyle', '语气风格', 3),
      createTextDesc('targetAudience', '目标用户', 3),
      createTextDesc('forbiddenExpressions', '禁止表达', 3),
      createTextDesc('commonExpressions', '常用表达', 3),
      createTextDesc('remark', '备注', 3),
    ],
  },
  product: {
    key: 'product',
    title: '产品卖点',
    apiBase: '/redbook/product',
    columns: [
      col('产品/服务名称', 'productName', 200),
      col('核心卖点', 'coreSellingPoints', 260),
      col('适用人群', 'targetAudience', 220),
      col('使用场景', 'scenarios', 220),
      statusCol('状态', 'status', 'common', 110),
    ],
    searchFormSchema: [search('productName', '产品名称'), searchSelect('status', '状态', getStatusOptions('common'))],
    formSchema: [
      idField,
      textInput('productName', '产品/服务名称', true),
      textarea('coreSellingPoints', '核心卖点'),
      textarea('targetAudience', '适用人群'),
      textarea('scenarios', '使用场景'),
      textarea('advantages', '差异化优势'),
      textarea('forbiddenPromises', '禁止承诺'),
      status('status', '状态', 'common', 'active'),
      textarea('remark', '备注', false, 3),
    ],
    detailSchema: [
      createTextDesc('productName', '产品/服务名称'),
      createStatusDesc('status', '状态', 'common'),
      createTextDesc('coreSellingPoints', '核心卖点', 3),
      createTextDesc('targetAudience', '适用人群', 3),
      createTextDesc('scenarios', '使用场景', 3),
      createTextDesc('advantages', '差异化优势', 3),
      createTextDesc('forbiddenPromises', '禁止承诺', 3),
      createTextDesc('remark', '备注', 3),
    ],
  },
  promptTemplate: {
    key: 'promptTemplate',
    title: '提示词模板',
    apiBase: '/redbook/promptTemplate',
    columns: [
      col('模板名称', 'templateName', 180),
      col('模板编码', 'templateCode', 180),
      col('工作流类型', 'workflowType', 160),
      col('模型提供方', 'modelProvider', 140),
      statusCol('状态', 'status', 'common', 110),
    ],
    searchFormSchema: [search('templateName', '模板名称'), search('templateCode', '模板编码'), searchSelect('status', '状态', getStatusOptions('common'))],
    formSchema: [
      idField,
      textInput('templateName', '模板名称', true),
      textInput('templateCode', '模板编码', true),
      textInput('workflowType', '工作流类型'),
      textInput('modelProvider', '模型提供方'),
      textarea('promptContent', '提示词内容', false, 8),
      textarea('outputSchema', '输出格式', false, 6),
      status('status', '状态', 'common', 'active'),
      textarea('remark', '备注', false, 3),
    ],
    detailSchema: [
      createTextDesc('templateName', '模板名称'),
      createTextDesc('templateCode', '模板编码'),
      createTextDesc('workflowType', '工作流类型'),
      createTextDesc('modelProvider', '模型提供方'),
      createStatusDesc('status', '状态', 'common'),
      createTextDesc('promptContent', '提示词内容', 3),
      createTextDesc('outputSchema', '输出格式', 3),
      createTextDesc('remark', '备注', 3),
    ],
  },
  sensitiveWord: {
    key: 'sensitiveWord',
    title: '敏感词',
    apiBase: '/redbook/sensitiveWord',
    columns: [
      col('词条', 'word', 180),
      col('分类', 'category', 120),
      statusCol('风险等级', 'riskLevel', 'risk', 120),
      col('替换建议', 'replacementSuggestion', 240),
      statusCol('状态', 'status', 'common', 110),
    ],
    searchFormSchema: [search('word', '词条'), search('category', '分类'), searchSelect('riskLevel', '风险等级', getStatusOptions('risk'))],
    formSchema: [
      idField,
      textInput('word', '词条', true),
      textInput('category', '分类'),
      status('riskLevel', '风险等级', 'risk', 'medium'),
      textarea('replacementSuggestion', '替换建议'),
      status('status', '状态', 'common', 'active'),
      textarea('remark', '备注', false, 3),
    ],
    detailSchema: [
      createTextDesc('word', '词条'),
      createTextDesc('category', '分类'),
      createStatusDesc('riskLevel', '风险等级', 'risk'),
      createStatusDesc('status', '状态', 'common'),
      createTextDesc('replacementSuggestion', '替换建议', 3),
      createTextDesc('remark', '备注', 3),
    ],
  },
  hotspot: {
    key: 'hotspot',
    title: '热点池',
    apiBase: '/redbook/hotspot',
    referenceKinds: ['track'],
    columns: [
      col('热点标题', 'title', 280),
      relationCol('赛道', 'trackId', 'track', 160),
      col('来源平台', 'sourcePlatform', 120),
      col('热度评分', 'heatScore', 110),
      col('可二创评分', 'remixScore', 110),
      statusCol('风险等级', 'riskLevel', 'risk', 110),
      statusCol('状态', 'status', 'hotspot', 120),
    ],
    searchFormSchema: [
      search('title', '热点标题'),
      searchRelation('trackId', '赛道', 'track'),
      searchSelect('riskLevel', '风险等级', getStatusOptions('risk')),
      searchSelect('status', '状态', getStatusOptions('hotspot')),
    ],
    formSchema: [
      idField,
      relation('trackId', '赛道', 'track', true),
      select('sourcePlatform', '来源平台', platformOptions, false, 'xiaohongshu'),
      textInput('title', '热点标题', true),
      textarea('summary', '热点摘要', false, 5),
      textInput('originalUrl', '原文链接'),
      textInput('authorName', '作者/账号'),
      number('likeCount', '点赞数'),
      number('collectCount', '收藏数'),
      number('commentCount', '评论数'),
      number('shareCount', '分享数'),
      dateTime('publishTime', '发布时间'),
      dateTime('collectTime', '采集时间'),
      textarea('tags', '标签', false, 3),
      number('heatScore', '热度评分'),
      number('remixScore', '可二创评分'),
      status('riskLevel', '风险等级', 'risk', 'low'),
      status('status', '状态', 'hotspot', 'pending_analysis'),
    ],
    detailSchema: [
      createReferenceDesc('trackId', '赛道', 'track'),
      createTextDesc('sourcePlatform', '来源平台'),
      createStatusDesc('riskLevel', '风险等级', 'risk'),
      createStatusDesc('status', '状态', 'hotspot'),
      createTextDesc('title', '热点标题', 3),
      createTextDesc('summary', '热点摘要', 3),
      createTextDesc('originalUrl', '原文链接', 3),
      createTextDesc('authorName', '作者/账号'),
      createTextDesc('likeCount', '点赞数'),
      createTextDesc('collectCount', '收藏数'),
      createTextDesc('commentCount', '评论数'),
      createTextDesc('shareCount', '分享数'),
      createTextDesc('publishTime', '发布时间'),
      createTextDesc('collectTime', '采集时间'),
      createTextDesc('heatScore', '热度评分'),
      createTextDesc('remixScore', '可二创评分'),
      createTextDesc('tags', '标签', 3),
    ],
    actionColumnWidth: 260,
    rowActions: [{ label: 'AI分析', action: 'analyze', successMessage: '热点分析已生成，可到热点分析查看结果' }],
  },
  hotspotAnalysis: {
    key: 'hotspotAnalysis',
    title: '热点分析',
    apiBase: '/redbook/hotspotAnalysis',
    referenceKinds: ['hotspot'],
    columns: [
      relationCol('关联热点', 'hotspotId', 'hotspot', 240),
      col('内容角度', 'contentAngle', 240),
      col('综合评分', 'score', 100),
      col('风险提示', 'riskWarning', 220),
      statusCol('状态', 'status', 'analysis', 110),
    ],
    searchFormSchema: [searchRelation('hotspotId', '热点', 'hotspot'), searchSelect('status', '状态', getStatusOptions('analysis'))],
    formSchema: [
      idField,
      relation('hotspotId', '关联热点', 'hotspot', true),
      textarea('painPoints', '用户痛点'),
      textarea('hookAnalysis', '爆点拆解'),
      textarea('contentAngle', '内容角度'),
      textarea('titleDirections', '标题方向'),
      textarea('outlineSuggestion', '正文结构建议'),
      textarea('coverCopySuggestion', '封面文案建议'),
      textarea('tagSuggestion', '标签建议'),
      textarea('productFit', '产品植入建议'),
      textarea('riskWarning', '风险提示'),
      textarea('originalitySuggestion', '原创化建议'),
      number('score', '综合评分'),
      textarea('rawResult', 'AI原始输出', false, 6),
      status('status', '状态', 'analysis', 'analyzed'),
    ],
    detailSchema: [
      createReferenceDesc('hotspotId', '关联热点', 'hotspot', 2),
      createStatusDesc('status', '状态', 'analysis'),
      createTextDesc('score', '综合评分'),
      createTextDesc('painPoints', '用户痛点', 3),
      createTextDesc('hookAnalysis', '爆点拆解', 3),
      createTextDesc('contentAngle', '内容角度', 3),
      createTextDesc('titleDirections', '标题方向', 3),
      createTextDesc('outlineSuggestion', '正文结构建议', 3),
      createTextDesc('coverCopySuggestion', '封面文案建议', 3),
      createTextDesc('tagSuggestion', '标签建议', 3),
      createTextDesc('productFit', '产品植入建议', 3),
      createTextDesc('riskWarning', '风险提示', 3),
      createTextDesc('originalitySuggestion', '原创化建议', 3),
      createTextDesc('rawResult', 'AI原始输出', 3),
    ],
    actionColumnWidth: 240,
    rowActions: [{ label: '生成草稿', action: 'generateDraft', successMessage: '笔记草稿已生成，可到笔记草稿继续处理' }],
  },
  noteDraft: {
    key: 'noteDraft',
    title: '笔记草稿',
    apiBase: '/redbook/noteDraft',
    referenceKinds: ['hotspot', 'analysis', 'track', 'account'],
    columns: [
      col('标题', 'title', 260),
      relationCol('赛道', 'trackId', 'track', 160),
      relationCol('账号', 'accountId', 'account', 160),
      col('内容类型', 'contentType', 110),
      statusCol('审核状态', 'auditStatus', 'audit', 110),
      statusCol('草稿状态', 'status', 'draft', 120),
    ],
    searchFormSchema: [
      search('title', '标题'),
      searchRelation('trackId', '赛道', 'track'),
      searchRelation('accountId', '账号', 'account'),
      searchSelect('auditStatus', '审核状态', getStatusOptions('audit')),
      searchSelect('status', '草稿状态', getStatusOptions('draft')),
    ],
    formSchema: [
      idField,
      relation('hotspotId', '热点', 'hotspot'),
      relation('analysisId', '分析', 'analysis'),
      relation('trackId', '赛道', 'track'),
      relation('accountId', '账号', 'account'),
      textInput('title', '标题', true),
      textInput('coverCopy', '封面文案'),
      textarea('body', '正文', true, 10),
      textarea('tags', '标签', false, 3),
      textarea('commentGuide', '评论区引导语', false, 3),
      textInput('publishTimeSuggestion', '发布时间建议'),
      select('contentType', '内容类型', contentTypeOptions),
      textInput('aiVersion', 'AI版本'),
      textInput('manualVersion', '人工修改版本'),
      textarea('riskCheckResult', '风险检测结果', false, 4),
      status('auditStatus', '审核状态', 'audit', 'pending'),
      textarea('auditOpinion', '审核意见', false, 3),
      status('status', '草稿状态', 'draft', 'pending_review'),
    ],
    detailSchema: [
      createReferenceDesc('hotspotId', '关联热点', 'hotspot'),
      createReferenceDesc('analysisId', '分析来源', 'analysis'),
      createReferenceDesc('trackId', '赛道', 'track'),
      createReferenceDesc('accountId', '账号', 'account'),
      createStatusDesc('auditStatus', '审核状态', 'audit'),
      createStatusDesc('status', '草稿状态', 'draft'),
      createTextDesc('contentType', '内容类型'),
      createTextDesc('publishTimeSuggestion', '发布时间建议'),
      createTextDesc('title', '标题', 3),
      createTextDesc('coverCopy', '封面文案', 3),
      createTextDesc('body', '正文', 3),
      createTextDesc('tags', '标签', 3),
      createTextDesc('commentGuide', '评论区引导语', 3),
      createTextDesc('riskCheckResult', '风险检测结果', 3),
      createTextDesc('auditOpinion', '审核意见', 3),
      createTextDesc('aiVersion', 'AI版本'),
      createTextDesc('manualVersion', '人工修改版本'),
    ],
    actionColumnWidth: 320,
    rowActions: [
      {
        label: '复制文案',
        action: 'copyPublishText',
        actionType: 'copy',
        successMessage: '发布文案已复制',
        copyText: buildNotePublishText,
      },
      {
        label: '加入排期',
        action: 'createPublishPlan',
        successMessage: '发布计划已生成，可到发布计划安排发布时间',
        ifShow: (record) => record.auditStatus === 'approved' && record.status !== 'published',
      },
    ],
  },
  publishPlan: {
    key: 'publishPlan',
    title: '发布计划',
    apiBase: '/redbook/publishPlan',
    referenceKinds: ['draft', 'account'],
    columns: [
      relationCol('草稿标题', 'draftId', 'draft', 240),
      relationCol('发布账号', 'accountId', 'account', 160),
      col('计划发布时间', 'plannedPublishTime', 180),
      col('实际发布时间', 'actualPublishTime', 180),
      statusCol('发布状态', 'publishStatus', 'publish', 120),
      col('笔记链接', 'noteUrl', 220),
    ],
    searchFormSchema: [
      searchRelation('draftId', '草稿', 'draft'),
      searchRelation('accountId', '账号', 'account'),
      searchSelect('publishStatus', '发布状态', getStatusOptions('publish')),
    ],
    formSchema: [
      idField,
      relation('draftId', '草稿', 'draft', true),
      relation('accountId', '账号', 'account', true),
      dateTime('plannedPublishTime', '计划发布时间'),
      dateTime('actualPublishTime', '实际发布时间'),
      status('publishStatus', '发布状态', 'publish', 'pending'),
      textInput('noteUrl', '小红书笔记链接'),
      textInput('publisher', '发布人'),
      textarea('remark', '备注', false, 3),
    ],
    detailSchema: [
      createReferenceDesc('draftId', '草稿', 'draft'),
      createReferenceDesc('accountId', '账号', 'account'),
      createStatusDesc('publishStatus', '发布状态', 'publish'),
      createTextDesc('plannedPublishTime', '计划发布时间'),
      createTextDesc('actualPublishTime', '实际发布时间'),
      createTextDesc('noteUrl', '笔记链接', 3),
      createTextDesc('publisher', '发布人'),
      createTextDesc('remark', '备注', 3),
    ],
    actionColumnWidth: 280,
    rowActions: [
      {
        label: '标记已发布',
        action: 'markPublished',
        successMessage: '发布状态已更新为已发布',
        confirmTitle: '确认将当前排期标记为已发布吗？',
        ifShow: (record) => !['published', 'data_collected', 'canceled'].includes(record.publishStatus),
      },
      {
        label: '生成回收记录',
        action: 'createMetric',
        successMessage: '数据回收记录已生成，可到数据回收页录入表现数据',
        ifShow: (record) => ['published', 'data_collected'].includes(record.publishStatus),
      },
    ],
  },
  noteMetric: {
    key: 'noteMetric',
    title: '数据回收',
    apiBase: '/redbook/noteMetric',
    referenceKinds: ['publishPlan', 'draft'],
    columns: [
      relationCol('发布计划', 'publishPlanId', 'publishPlan', 180),
      relationCol('草稿', 'noteDraftId', 'draft', 220),
      statusCol('采集节点', 'collectNode', 'collectNode', 100),
      col('阅读/播放量', 'views', 110),
      col('点赞数', 'likes', 90),
      col('收藏数', 'collects', 90),
      col('互动率', 'interactionRate', 100),
    ],
    searchFormSchema: [searchRelation('publishPlanId', '发布计划', 'publishPlan'), searchSelect('collectNode', '采集节点', getStatusOptions('collectNode'))],
    formSchema: [
      idField,
      relation('publishPlanId', '发布计划', 'publishPlan', true),
      relation('noteDraftId', '草稿', 'draft'),
      status('collectNode', '采集节点', 'collectNode', '24h'),
      number('impressions', '曝光量'),
      number('views', '阅读/播放量'),
      number('likes', '点赞数'),
      number('collects', '收藏数'),
      number('comments', '评论数'),
      number('shares', '分享数'),
      number('followers', '关注数'),
      number('messages', '私信数'),
      number('leads', '线索数'),
      number('conversions', '转化数'),
      dateTime('collectTime', '采集时间'),
      number('interactionRate', '互动率'),
      number('collectRate', '收藏率'),
      number('commentRate', '评论率'),
      textarea('remark', '备注', false, 3),
    ],
    detailSchema: [
      createReferenceDesc('publishPlanId', '发布计划', 'publishPlan'),
      createReferenceDesc('noteDraftId', '草稿', 'draft'),
      createStatusDesc('collectNode', '采集节点', 'collectNode'),
      createTextDesc('impressions', '曝光量'),
      createTextDesc('views', '阅读/播放量'),
      createTextDesc('likes', '点赞数'),
      createTextDesc('collects', '收藏数'),
      createTextDesc('comments', '评论数'),
      createTextDesc('shares', '分享数'),
      createTextDesc('followers', '关注数'),
      createTextDesc('messages', '私信数'),
      createTextDesc('leads', '线索数'),
      createTextDesc('conversions', '转化数'),
      createTextDesc('collectTime', '采集时间'),
      createTextDesc('interactionRate', '互动率'),
      createTextDesc('collectRate', '收藏率'),
      createTextDesc('commentRate', '评论率'),
      createTextDesc('remark', '备注', 3),
    ],
  },
  reviewReport: {
    key: 'reviewReport',
    title: '复盘报告',
    apiBase: '/redbook/reviewReport',
    referenceKinds: ['track', 'account'],
    columns: [
      col('报告名称', 'reportName', 220),
      relationCol('赛道', 'trackId', 'track', 160),
      relationCol('账号', 'accountId', 'account', 160),
      col('开始日期', 'periodStart', 120),
      col('结束日期', 'periodEnd', 120),
      statusCol('状态', 'status', 'review', 110),
    ],
    searchFormSchema: [search('reportName', '报告名称'), searchRelation('trackId', '赛道', 'track'), searchRelation('accountId', '账号', 'account')],
    formSchema: [
      idField,
      textInput('reportName', '报告名称', true),
      relation('trackId', '赛道', 'track'),
      relation('accountId', '账号', 'account'),
      date('periodStart', '开始日期'),
      date('periodEnd', '结束日期'),
      textarea('summary', '表现总结', false, 5),
      textarea('highPerformingFactors', '高表现因素'),
      textarea('lowPerformingReasons', '低表现原因'),
      textarea('reusableTopics', '可复用选题'),
      textarea('stoppedDirections', '停止方向'),
      textarea('nextTopicSuggestions', '下一轮选题建议'),
      textarea('nextTitleSuggestions', '下一轮标题建议'),
      textarea('nextPublishSuggestions', '下一轮发布时间建议'),
      textarea('rawResult', 'AI原始输出', false, 6),
      status('status', '状态', 'review', 'draft'),
    ],
    detailSchema: [
      createTextDesc('reportName', '报告名称'),
      createReferenceDesc('trackId', '赛道', 'track'),
      createReferenceDesc('accountId', '账号', 'account'),
      createStatusDesc('status', '状态', 'review'),
      createTextDesc('periodStart', '开始日期'),
      createTextDesc('periodEnd', '结束日期'),
      createTextDesc('summary', '表现总结', 3),
      createTextDesc('highPerformingFactors', '高表现因素', 3),
      createTextDesc('lowPerformingReasons', '低表现原因', 3),
      createTextDesc('reusableTopics', '可复用选题', 3),
      createTextDesc('stoppedDirections', '停止方向', 3),
      createTextDesc('nextTopicSuggestions', '下一轮选题建议', 3),
      createTextDesc('nextTitleSuggestions', '下一轮标题建议', 3),
      createTextDesc('nextPublishSuggestions', '下一轮发布时间建议', 3),
      createTextDesc('rawResult', 'AI原始输出', 3),
    ],
    actionColumnWidth: 260,
    rowActions: [
      { label: '生成复盘', action: 'generate', successMessage: '复盘报告已生成，可继续编辑和导出' },
      { label: '回流热点池', action: 'createHotspots', successMessage: '下一轮选题已回流热点池，可继续做热点分析' },
    ],
  },
};

export function getRedbookModuleConfig(key: string): RedbookModuleConfig {
  return redbookModuleConfigs[key] || redbookModuleConfigs.hotspot;
}

const routeKeyMap: Record<string, string> = {
  account: 'account',
  track: 'track',
  persona: 'persona',
  product: 'product',
  'prompt-template': 'promptTemplate',
  'sensitive-word': 'sensitiveWord',
  hotspot: 'hotspot',
  'hotspot-analysis': 'hotspotAnalysis',
  'note-draft': 'noteDraft',
  'publish-plan': 'publishPlan',
  'note-metric': 'noteMetric',
  'review-report': 'reviewReport',
};

export function resolveRedbookModuleKey(routePath: string, metaModuleKey?: string): string {
  if (metaModuleKey && redbookModuleConfigs[metaModuleKey]) {
    return metaModuleKey;
  }
  const matchedKey = routePath.split('/').filter(Boolean).pop() || 'hotspot';
  return routeKeyMap[matchedKey] || 'hotspot';
}
