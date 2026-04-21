# RedBookAgent V1 后端接口契约

更新时间：2026-04-21  
负责人：A（后端 / 数据 / AI 闭环）  
适用范围：账号管理、选题池、笔记生成、发布日历

## 1. 契约约定

### 1.1 基础路径

后端接口统一挂载在 JeecgBoot API 服务下，本文只写业务路径：

```text
/redbook/**
```

前端请求时按现有项目的 `defHttp` / API 前缀拼接即可。

### 1.2 通用请求头

| Header | 必填 | 说明 |
| --- | --- | --- |
| `X-Access-Token` | 是 | JeecgBoot 登录令牌 |
| `Content-Type: application/json` | 写接口必填 | JSON 请求体 |

Excel 导入接口使用 `multipart/form-data`。

### 1.3 通用返回结构

所有 JSON 接口返回 JeecgBoot `Result<T>` 包装：

```json
{
  "success": true,
  "message": "操作成功",
  "code": 200,
  "result": {},
  "timestamp": 1776758400000
}
```

失败返回：

```json
{
  "success": false,
  "message": "错误原因",
  "code": 500,
  "result": null,
  "timestamp": 1776758400000
}
```

### 1.4 分页返回结构

分页列表统一使用：

```json
{
  "success": true,
  "code": 200,
  "result": {
    "records": [],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

分页查询公共参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `pageNo` | number | 否 | `1` | 页码 |
| `pageSize` | number | 否 | `10` | 每页条数 |
| `column` | string | 否 | - | 排序字段 |
| `order` | string | 否 | - | `asc` / `desc` |

### 1.5 通用字段

业务实体继承 JeecgBoot 基础实体，返回中可能包含：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | string | 主键 |
| `createBy` | string | 创建人 |
| `createTime` | string | 创建时间，格式 `yyyy-MM-dd HH:mm:ss` |
| `updateBy` | string | 更新人 |
| `updateTime` | string | 更新时间，格式 `yyyy-MM-dd HH:mm:ss` |

### 1.6 枚举约定

所有状态字段后端保存英文枚举，前端显示中文文案。

| 字典 | 值 | 中文 |
| --- | --- | --- |
| `rb_common_status` | `active` | 启用 |
| `rb_common_status` | `inactive` | 停用 |
| `rb_hotspot_status` | `pending_analysis` | 待分析 |
| `rb_hotspot_status` | `analyzed` | 已分析 |
| `rb_hotspot_status` | `draft_generated` | 已生成草稿 |
| `rb_hotspot_status` | `discarded` | 已废弃 |
| `rb_analysis_status` | `analyzed` | 已分析 |
| `rb_analysis_status` | `adopted` | 已采纳 |
| `rb_analysis_status` | `discarded` | 已废弃 |
| `rb_draft_status` | `pending_review` | 待审核 |
| `rb_draft_status` | `pending_publish` | 待发布 |
| `rb_draft_status` | `published` | 已发布 |
| `rb_audit_status` | `pending` | 待审核 |
| `rb_audit_status` | `approved` | 已通过 |
| `rb_audit_status` | `rejected` | 已退回 |
| `rb_publish_status` | `pending` | 待发布 |
| `rb_publish_status` | `published` | 已发布 |
| `rb_publish_status` | `delayed` | 已延期 |
| `rb_publish_status` | `canceled` | 已取消 |
| `rb_publish_status` | `data_collected` | 已回收数据 |
| `rb_risk_level` | `low` | 低风险 |
| `rb_risk_level` | `medium` | 中风险 |
| `rb_risk_level` | `high` | 高风险 |

## 2. 账号管理

模块路径：

```text
/redbook/account
```

### 2.1 Account 字段

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | string | 编辑必填 | 账号 ID |
| `accountName` | string | 是 | 账号名称 |
| `platform` | string | 否 | 平台，V1 默认 `xiaohongshu` |
| `positioning` | string | 否 | 账号定位 |
| `targetAudience` | string | 否 | 目标人群 |
| `contentStyle` | string | 否 | 内容风格 |
| `primaryTrackId` | string | 否 | 主要赛道 ID |
| `status` | string | 否 | `active` / `inactive` |
| `remark` | string | 否 | 备注 |

### 2.2 查询账号分页

```http
GET /redbook/account/list
```

查询参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `accountName` | string | 按账号名称筛选 |
| `platform` | string | 按平台筛选 |
| `primaryTrackId` | string | 按主要赛道筛选 |
| `status` | string | 按状态筛选 |
| `pageNo` | number | 页码 |
| `pageSize` | number | 每页条数 |

返回：

```json
{
  "success": true,
  "code": 200,
  "result": {
    "records": [
      {
        "id": "account_001",
        "accountName": "职场成长号",
        "platform": "xiaohongshu",
        "positioning": "职场干货",
        "targetAudience": "25-35 岁职场人",
        "contentStyle": "专业、直接、可执行",
        "primaryTrackId": "track_001",
        "status": "active",
        "remark": ""
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

### 2.3 查询账号详情

```http
GET /redbook/account/queryById?id={id}
```

返回 `result` 为 `Account` 对象。

### 2.4 新增账号

```http
POST /redbook/account/add
```

请求体：

```json
{
  "accountName": "职场成长号",
  "platform": "xiaohongshu",
  "positioning": "职场干货",
  "targetAudience": "25-35 岁职场人",
  "contentStyle": "专业、直接、可执行",
  "primaryTrackId": "track_001",
  "status": "active",
  "remark": ""
}
```

返回：`Result<?>`，成功消息为 `添加成功！`。

### 2.5 编辑账号

```http
PUT /redbook/account/edit
POST /redbook/account/edit
```

请求体必须包含 `id`，其他字段按需传入。

### 2.6 删除账号

```http
DELETE /redbook/account/delete?id={id}
DELETE /redbook/account/deleteBatch?ids={id1,id2}
```

### 2.7 导入导出

```http
GET  /redbook/account/exportXls
POST /redbook/account/importExcel
```

导入表头与 Account 字段对应。导入失败时 `message` 需返回可读原因。

## 3. 选题池

V1 选题池由“热点池 + 选题分析”两部分组成：

```text
热点录入/导入 -> AI 分析 -> 选题分析结果 -> 采纳后生成草稿
```

### 3.1 热点池接口

模块路径：

```text
/redbook/hotspot
```

#### 3.1.1 Hotspot 字段

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | string | 编辑必填 | 热点 ID |
| `trackId` | string | 是 | 赛道 ID |
| `sourcePlatform` | string | 否 | 来源平台 |
| `title` | string | 是 | 热点标题 |
| `summary` | string | 否 | 热点摘要 |
| `originalUrl` | string | 否 | 原文链接 |
| `authorName` | string | 否 | 作者/账号 |
| `likeCount` | number | 否 | 点赞数 |
| `collectCount` | number | 否 | 收藏数 |
| `commentCount` | number | 否 | 评论数 |
| `shareCount` | number | 否 | 分享数 |
| `publishTime` | string | 否 | 发布时间 |
| `collectTime` | string | 否 | 采集时间 |
| `tags` | string | 否 | 标签，逗号分隔 |
| `heatScore` | number | 否 | 热度评分 |
| `remixScore` | number | 否 | 可二创评分 |
| `riskLevel` | string | 否 | `low` / `medium` / `high` |
| `status` | string | 否 | 热点状态 |

#### 3.1.2 查询热点分页

```http
GET /redbook/hotspot/list
```

查询参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `trackId` | string | 赛道筛选 |
| `sourcePlatform` | string | 来源平台筛选 |
| `title` | string | 标题筛选 |
| `riskLevel` | string | 风险等级筛选 |
| `status` | string | 状态筛选 |
| `pageNo` | number | 页码 |
| `pageSize` | number | 每页条数 |

返回 `result.records[]` 为 `Hotspot`。

#### 3.1.3 新增 / 编辑 / 删除热点

```http
POST   /redbook/hotspot/add
PUT    /redbook/hotspot/edit
POST   /redbook/hotspot/edit
DELETE /redbook/hotspot/delete?id={id}
DELETE /redbook/hotspot/deleteBatch?ids={id1,id2}
```

新增请求体示例：

```json
{
  "trackId": "track_001",
  "sourcePlatform": "xiaohongshu",
  "title": "年轻人为什么越来越重视副业",
  "summary": "围绕副业焦虑、收入结构和时间管理展开讨论",
  "originalUrl": "https://example.com/note/1",
  "authorName": "参考账号",
  "likeCount": 1200,
  "collectCount": 300,
  "commentCount": 88,
  "shareCount": 42,
  "publishTime": "2026-04-21 10:00:00",
  "collectTime": "2026-04-21 11:00:00",
  "tags": "副业,职场,成长",
  "heatScore": 86.5,
  "remixScore": 78.0,
  "riskLevel": "low",
  "status": "pending_analysis"
}
```

#### 3.1.4 热点一键分析

```http
POST /redbook/hotspot/analyze
```

请求体：

```json
{
  "id": "hotspot_001"
}
```

业务规则：

| 场景 | 规则 |
| --- | --- |
| 热点不存在 | 返回失败 |
| AI 可用 | 生成 `RbHotspotAnalysis`，热点状态更新为 `analyzed` |
| AI 不可用 | 使用本地模板降级生成，V1 主流程不能被远程 AI 阻断 |

返回 `result` 为 `HotspotAnalysis`。

#### 3.1.5 导入导出热点

```http
GET  /redbook/hotspot/exportXls
POST /redbook/hotspot/importExcel
```

V1 导入约定：

| 校验项 | 规则 |
| --- | --- |
| `trackId` | 必填，必须存在 |
| `title` | 必填 |
| `title` | 文件内和库内都做重复检查 |
| `originalUrl` | 非空时文件内和库内都做重复检查 |
| `status` | 为空默认 `pending_analysis` |

导入失败时返回行级错误，例如：

```text
第3行: 赛道ID不存在
第7行: 原文链接已存在
```

### 3.2 选题分析接口

模块路径：

```text
/redbook/hotspotAnalysis
```

#### 3.2.1 HotspotAnalysis 字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | string | 分析 ID |
| `hotspotId` | string | 热点 ID |
| `painPoints` | string | 用户痛点 |
| `hookAnalysis` | string | 爆点拆解 |
| `contentAngle` | string | 内容角度 |
| `titleDirections` | string | 标题方向 |
| `outlineSuggestion` | string | 正文结构建议 |
| `coverCopySuggestion` | string | 封面文案建议 |
| `tagSuggestion` | string | 标签建议 |
| `productFit` | string | 产品植入建议 |
| `riskWarning` | string | 风险提示 |
| `originalitySuggestion` | string | 原创化建议 |
| `score` | number | 综合评分 |
| `rawResult` | string | AI 原始输出 |
| `status` | string | `analyzed` / `adopted` / `discarded` |

#### 3.2.2 查询分析分页

```http
GET /redbook/hotspotAnalysis/list
```

查询参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `hotspotId` | string | 热点筛选 |
| `status` | string | 状态筛选 |
| `pageNo` | number | 页码 |
| `pageSize` | number | 每页条数 |

#### 3.2.3 查询分析详情

```http
GET /redbook/hotspotAnalysis/queryById?id={id}
```

#### 3.2.4 编辑 / 删除分析

```http
POST   /redbook/hotspotAnalysis/add
PUT    /redbook/hotspotAnalysis/edit
POST   /redbook/hotspotAnalysis/edit
DELETE /redbook/hotspotAnalysis/delete?id={id}
DELETE /redbook/hotspotAnalysis/deleteBatch?ids={id1,id2}
```

#### 3.2.5 分析生成笔记草稿

```http
POST /redbook/hotspotAnalysis/generateDraft
```

请求体：

```json
{
  "id": "analysis_001"
}
```

业务规则：

| 场景 | 规则 |
| --- | --- |
| 分析不存在 | 返回失败 |
| 分析可用 | 生成 `RbNoteDraft` |
| 生成成功 | 分析状态可更新为 `adopted`，热点状态可更新为 `draft_generated` |
| AI 不可用 | 使用本地模板降级生成 |

返回 `result` 为 `NoteDraft`。

## 4. 笔记生成

模块以“热点分析生成草稿 + 草稿人工审核 + 草稿版本”为主，不做自动发布。

模块路径：

```text
/redbook/noteDraft
```

### 4.1 NoteDraft 字段

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | string | 编辑必填 | 草稿 ID |
| `hotspotId` | string | 否 | 热点 ID |
| `analysisId` | string | 否 | 分析 ID |
| `trackId` | string | 否 | 赛道 ID |
| `accountId` | string | 否 | 账号 ID |
| `title` | string | 是 | 标题 |
| `coverCopy` | string | 否 | 封面文案 |
| `body` | string | 是 | 正文 |
| `tags` | string | 否 | 标签，建议以 `#` 或逗号分隔 |
| `commentGuide` | string | 否 | 评论区引导语 |
| `publishTimeSuggestion` | string | 否 | 发布时间建议 |
| `contentType` | string | 否 | 内容类型 |
| `aiVersion` | string | 否 | AI 生成版本 |
| `manualVersion` | string | 否 | 人工修改版本 |
| `riskCheckResult` | string | 否 | 风险检测结果 |
| `auditStatus` | string | 否 | 审核状态 |
| `auditOpinion` | string | 否 | 审核意见 |
| `status` | string | 否 | 草稿状态 |

### 4.2 状态流转

```text
pending_review + pending
  -> approve -> pending_publish + approved
  -> reject  -> pending_review + rejected
pending_publish + approved
  -> createPublishPlan -> 发布计划 pending
  -> markPublished     -> published + approved
```

约束：

| 规则 | 说明 |
| --- | --- |
| 待审核草稿不能直接变成已发布 | 必须先审核通过、加入排期 |
| 已发布草稿不能退回待审核 | 发布后内容只读，后续改动走新草稿 |
| 每次新增、编辑、审核、恢复版本 | 后端保存一条草稿版本 |

### 4.3 查询草稿分页

```http
GET /redbook/noteDraft/list
```

查询参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `hotspotId` | string | 热点筛选 |
| `analysisId` | string | 分析筛选 |
| `trackId` | string | 赛道筛选 |
| `accountId` | string | 账号筛选 |
| `title` | string | 标题筛选 |
| `auditStatus` | string | 审核状态筛选 |
| `status` | string | 草稿状态筛选 |
| `pageNo` | number | 页码 |
| `pageSize` | number | 每页条数 |

返回 `result.records[]` 为 `NoteDraft`。

### 4.4 查询草稿详情

```http
GET /redbook/noteDraft/queryById?id={id}
```

### 4.5 新增草稿

```http
POST /redbook/noteDraft/add
```

请求体：

```json
{
  "hotspotId": "hotspot_001",
  "analysisId": "analysis_001",
  "trackId": "track_001",
  "accountId": "account_001",
  "title": "副业不是越多越好，先看这 3 个判断",
  "coverCopy": "副业焦虑怎么破",
  "body": "正文内容...",
  "tags": "#副业,#职场成长,#时间管理",
  "commentGuide": "你现在最想解决的副业问题是什么？",
  "publishTimeSuggestion": "工作日 20:00-22:00",
  "contentType": "干货",
  "riskCheckResult": "低风险",
  "auditStatus": "pending",
  "status": "pending_review"
}
```

返回：成功时 `result` 为已保存的 `NoteDraft`。

### 4.6 编辑草稿

```http
PUT  /redbook/noteDraft/edit
POST /redbook/noteDraft/edit
```

请求体必须包含 `id`。编辑成功后后端写入一条 `manual_edit` 草稿版本。

### 4.7 审核通过

```http
POST /redbook/noteDraft/approve
```

请求体：

```json
{
  "id": "draft_001",
  "auditOpinion": "内容合规，可以发布"
}
```

返回 `result`：

```json
{
  "id": "draft_001",
  "auditStatus": "approved",
  "auditOpinion": "内容合规，可以发布",
  "status": "pending_publish"
}
```

### 4.8 审核退回

```http
POST /redbook/noteDraft/reject
```

请求体：

```json
{
  "id": "draft_001",
  "auditOpinion": "标题夸张，需要弱化承诺"
}
```

返回 `result.auditStatus = rejected`，`result.status = pending_review`。

### 4.9 草稿版本列表

```http
GET /redbook/noteDraft/versions?draftId={draftId}
```

返回 `result` 为版本数组，按版本号倒序。

版本字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | string | 版本 ID |
| `draftId` | string | 草稿 ID |
| `versionNo` | number | 版本号 |
| `versionType` | string | `create` / `ai_generate` / `manual_edit` / `audit_approved` / `audit_rejected` / `restore` |
| `title` | string | 标题快照 |
| `coverCopy` | string | 封面文案快照 |
| `body` | string | 正文快照 |
| `tags` | string | 标签快照 |
| `auditStatus` | string | 审核状态快照 |
| `status` | string | 草稿状态快照 |
| `remark` | string | 版本备注 |

### 4.10 恢复草稿版本

```http
POST /redbook/noteDraft/restoreVersion
```

请求体：

```json
{
  "id": "version_001"
}
```

业务规则：

| 场景 | 规则 |
| --- | --- |
| 草稿已发布 | 不允许恢复 |
| 恢复成功 | 草稿回到 `pending_review`，审核状态回到 `pending` |
| 恢复成功 | 后端追加一条 `restore` 版本 |

### 4.11 草稿加入发布计划

```http
POST /redbook/noteDraft/createPublishPlan
```

请求体：

```json
{
  "id": "draft_001"
}
```

业务规则：

| 场景 | 规则 |
| --- | --- |
| 草稿未审核通过 | 返回失败 |
| 草稿已审核通过 | 创建 `RbPublishPlan` |
| 创建成功 | 发布状态为 `pending` |

返回 `result` 为 `PublishPlan`。

### 4.12 导入导出草稿

```http
GET  /redbook/noteDraft/exportXls
POST /redbook/noteDraft/importExcel
```

V1 导入草稿应默认进入 `pending_review` + `pending`，不允许导入为 `published`。

## 5. 发布日历

发布日历使用发布计划数据源，前端按 `plannedPublishTime` 聚合到日历视图。

模块路径：

```text
/redbook/publishPlan
```

### 5.1 PublishPlan 字段

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | string | 编辑必填 | 发布计划 ID |
| `draftId` | string | 是 | 草稿 ID |
| `accountId` | string | 是 | 账号 ID |
| `plannedPublishTime` | string | 是 | 计划发布时间 |
| `actualPublishTime` | string | 否 | 实际发布时间 |
| `publishStatus` | string | 否 | 发布状态 |
| `noteUrl` | string | 否 | 小红书笔记链接 |
| `publisher` | string | 否 | 发布人 |
| `remark` | string | 否 | 备注 |

### 5.2 查询发布计划分页

```http
GET /redbook/publishPlan/list
```

查询参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `draftId` | string | 草稿筛选 |
| `accountId` | string | 账号筛选 |
| `publishStatus` | string | 发布状态筛选 |
| `plannedPublishTime_begin` | string | 日历起始时间 |
| `plannedPublishTime_end` | string | 日历结束时间 |
| `pageNo` | number | 页码 |
| `pageSize` | number | 每页条数 |

发布日历月视图请求示例：

```http
GET /redbook/publishPlan/list?plannedPublishTime_begin=2026-04-01 00:00:00&plannedPublishTime_end=2026-04-30 23:59:59&pageNo=1&pageSize=500
```

返回 `result.records[]` 为 `PublishPlan`，前端按 `plannedPublishTime` 渲染。

### 5.3 查询发布计划详情

```http
GET /redbook/publishPlan/queryById?id={id}
```

### 5.4 新增发布计划

```http
POST /redbook/publishPlan/add
```

请求体：

```json
{
  "draftId": "draft_001",
  "accountId": "account_001",
  "plannedPublishTime": "2026-04-22 20:30:00",
  "publishStatus": "pending",
  "publisher": "运营A",
  "remark": "晚间发布"
}
```

建议优先使用 `/redbook/noteDraft/createPublishPlan` 从审核通过草稿生成发布计划；手动新增接口用于补录。

### 5.5 编辑发布计划

```http
PUT  /redbook/publishPlan/edit
POST /redbook/publishPlan/edit
```

常见编辑场景：

| 场景 | 字段变化 |
| --- | --- |
| 日历拖动改期 | 更新 `plannedPublishTime` |
| 更换发布账号 | 更新 `accountId` |
| 补充备注 | 更新 `remark` |

### 5.6 删除发布计划

```http
DELETE /redbook/publishPlan/delete?id={id}
DELETE /redbook/publishPlan/deleteBatch?ids={id1,id2}
```

### 5.7 标记已发布

```http
POST /redbook/publishPlan/markPublished
```

请求体：

```json
{
  "id": "publish_plan_001"
}
```

业务规则：

| 场景 | 规则 |
| --- | --- |
| 发布计划不存在 | 返回失败 |
| 标记成功 | `publishStatus = published` |
| 标记成功 | `actualPublishTime` 写入当前时间 |
| 标记成功 | 关联草稿状态更新为 `published` |

返回 `result` 为更新后的 `PublishPlan`。

### 5.8 生成数据回收记录

```http
POST /redbook/publishPlan/createMetric
```

请求体：

```json
{
  "id": "publish_plan_001"
}
```

业务规则：

| 场景 | 规则 |
| --- | --- |
| 未发布计划 | 返回失败或提示先标记发布 |
| 已发布计划 | 创建 `RbNoteMetric` 初始记录 |
| 创建成功 | 发布计划状态可推进为 `data_collected` |

返回 `result` 为数据回收记录。

### 5.9 发布状态流转

```text
pending -> published -> data_collected
pending -> delayed -> pending
pending -> canceled
```

### 5.10 发布计划延期

```http
POST /redbook/publishPlan/delay
```

请求体：

```json
{
  "id": "publish_plan_001",
  "plannedPublishTime": "2026-04-23 20:30:00",
  "remark": "临时改到明晚"
}
```

返回 `result.publishStatus = delayed`。

### 5.11 发布计划取消

```http
POST /redbook/publishPlan/cancel
```

请求体：

```json
{
  "id": "publish_plan_001",
  "remark": "本次选题取消"
}
```

返回 `result.publishStatus = canceled`。

### 5.12 发布计划恢复待发布

```http
POST /redbook/publishPlan/restorePending
```

请求体：

```json
{
  "id": "publish_plan_001",
  "plannedPublishTime": "2026-04-24 20:30:00",
  "remark": "调整后重新排期"
}
```

返回 `result.publishStatus = pending`。

### 5.13 补录笔记链接

```http
POST /redbook/publishPlan/updateNoteUrl
```

请求体：

```json
{
  "id": "publish_plan_001",
  "noteUrl": "https://www.xiaohongshu.com/explore/xxxx",
  "remark": "补录真实笔记链接"
}
```

返回 `result.noteUrl` 为最新链接。

### 5.14 导入导出发布计划

```http
GET  /redbook/publishPlan/exportXls
POST /redbook/publishPlan/importExcel
```

导入约束：

| 字段 | 规则 |
| --- | --- |
| `draftId` | 必填，必须存在 |
| `accountId` | 必填，必须存在 |
| `plannedPublishTime` | 必填 |
| `publishStatus` | 为空默认 `pending` |

## 6. 工作台与复盘看板

### 6.1 获取复盘看板

```http
GET /redbook/workbench/reviewDashboard
```

可选查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `periodStart` | string | 否 | 开始日期，格式 `yyyy-MM-dd` |
| `periodEnd` | string | 否 | 结束日期，格式 `yyyy-MM-dd` |
| `trackId` | string | 否 | 按赛道筛选 |
| `accountId` | string | 否 | 按账号筛选 |

返回字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `publishCount` | number | 筛选范围内已发布计划数 |
| `collectedPublishCount` | number | 已有回收数据的发布计划数 |
| `uncollectedPublishCount` | number | 尚未回收数据的发布计划数 |
| `metricCount` | number | 命中筛选条件的数据回收记录总数 |
| `reviewReportCount` | number | 命中筛选条件的复盘报告数 |
| `avgViews` | number | 最新回收节点的平均阅读/播放量 |
| `avgInteractionRate` | number | 最新回收节点的平均互动率 |
| `avgCollectRate` | number | 最新回收节点的平均收藏率 |
| `avgCommentRate` | number | 最新回收节点的平均评论率 |
| `latestReportName` | string | 最近一份匹配复盘报告名称 |
| `latestSummary` | string | 最近一份匹配复盘报告摘要 |
| `highPerformList` | array | 高表现笔记榜 |
| `lowPerformList` | array | 低表现笔记榜 |
| `trackBoard` | array | 赛道维度表现榜 |
| `accountBoard` | array | 账号维度表现榜 |
| `publishWindowBoard` | array | 发布时间段表现榜 |
| `nextTopicSuggestions` | array | 下一轮选题建议 |
| `nextTitleSuggestions` | array | 下一轮标题建议 |
| `nextPublishSuggestions` | array | 下一轮发布时间建议 |

业务规则：

| 场景 | 规则 |
| --- | --- |
| 不传筛选参数 | 返回全量复盘看板 |
| 传时间范围 | 仅统计命中发布时间区间的已发布计划 |
| 传 `trackId` / `accountId` | 仅统计命中赛道 / 账号的发布数据 |
| 存在匹配复盘报告 | 下一轮建议优先取最近一份匹配报告 |
| 无匹配复盘报告 | 返回内置兜底建议 |

## 7. 前后端联调清单

| 流程 | 后端接口 | 前端预期 |
| --- | --- | --- |
| 账号选择器 | `GET /redbook/account/list?pageSize=100&status=active` | 显示账号名称，保存账号 ID |
| 热点列表 | `GET /redbook/hotspot/list` | 支持赛道、状态、风险筛选 |
| 热点分析 | `POST /redbook/hotspot/analyze` | 成功后跳转或刷新分析结果 |
| 分析生成草稿 | `POST /redbook/hotspotAnalysis/generateDraft` | 成功后打开草稿详情 |
| 草稿审核通过 | `POST /redbook/noteDraft/approve` | 状态变为待发布 |
| 草稿审核退回 | `POST /redbook/noteDraft/reject` | 状态回到待审核并显示意见 |
| 草稿版本 | `GET /redbook/noteDraft/versions` | 展示版本列表，可恢复 |
| 加入发布计划 | `POST /redbook/noteDraft/createPublishPlan` | 生成日历事件 |
| 发布日历 | `GET /redbook/publishPlan/list` | 按计划发布时间渲染 |
| 发布改期 | `POST /redbook/publishPlan/delay` | 改成延期状态并更新排期时间 |
| 恢复待发布 | `POST /redbook/publishPlan/restorePending` | 延期/取消后重新进入待发布 |
| 取消排期 | `POST /redbook/publishPlan/cancel` | 排期取消 |
| 补录链接 | `POST /redbook/publishPlan/updateNoteUrl` | 保存真实笔记链接 |
| 标记已发布 | `POST /redbook/publishPlan/markPublished` | 更新发布状态和草稿状态 |
| 复盘看板筛选 | `GET /redbook/workbench/reviewDashboard` | 支持按时间 / 赛道 / 账号刷新看板 |

## 8. V1 不支持能力

- 不做小红书自动登录。
- 不做自动发布。
- 不做自动评论、私信或批量养号。
- 不绕过平台风控。
- 不做未授权采集或搬运。
