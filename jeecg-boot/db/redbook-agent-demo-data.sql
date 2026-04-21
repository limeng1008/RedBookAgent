CREATE DATABASE IF NOT EXISTS `jeecg-boot` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `jeecg-boot`;

-- ------------------------------------------------------------------
-- RedBook Agent V1 样例数据
-- 目标：
-- 1. 生成可试运营演示的数据闭环
-- 2. 包含 4 个赛道、4 个账号、50 条热点、30 条分析、20 条草稿
-- 3. 包含发布计划、数据回收、复盘报告，便于演示工作台/看板/筛选
-- ------------------------------------------------------------------

INSERT INTO `rb_track`
(`id`, `track_name`, `keywords`, `target_audience`, `content_direction`, `competitor_accounts`, `priority`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES
('rb_demo_track_001', '职场成长', '转岗,晋升,沟通,复盘', '23-35 岁白领', '职场提效、表达、复盘方法', '职场研究所,打工人效率局', 1, 'active', '样例赛道：适合演示高收藏干货内容', 'admin', '2026-04-01 10:00:00', 'admin', '2026-04-21 10:00:00'),
('rb_demo_track_002', '轻创业副业', '副业,接单,变现,个人IP', '25-40 岁希望提升收入的人群', '副业路径、接单经验、变现节奏', '副业增长实验室,轻创业观察', 2, 'active', '样例赛道：适合演示转化导向内容', 'admin', '2026-04-01 10:05:00', 'admin', '2026-04-21 10:05:00'),
('rb_demo_track_003', '知识IP运营', '知识付费,选题,直播,私域', '想做内容商业化的创作者', '知识产品包装、内容运营、成交设计', '知识变现实战派,内容增长俱乐部', 3, 'active', '样例赛道：适合演示账号运营类复盘', 'admin', '2026-04-01 10:10:00', 'admin', '2026-04-21 10:10:00'),
('rb_demo_track_004', '本地生活服务', '探店,到店转化,私域成交,套餐', '本地门店经营者', '本地门店获客、到店转化、服务体验', '门店增长手册,同城经营笔记', 4, 'active', '样例赛道：适合演示本地服务型内容', 'admin', '2026-04-01 10:15:00', 'admin', '2026-04-21 10:15:00')
ON DUPLICATE KEY UPDATE
`track_name` = VALUES(`track_name`),
`keywords` = VALUES(`keywords`),
`target_audience` = VALUES(`target_audience`),
`content_direction` = VALUES(`content_direction`),
`competitor_accounts` = VALUES(`competitor_accounts`),
`priority` = VALUES(`priority`),
`status` = VALUES(`status`),
`remark` = VALUES(`remark`),
`update_by` = VALUES(`update_by`),
`update_time` = VALUES(`update_time`);

INSERT INTO `rb_account`
(`id`, `account_name`, `platform`, `positioning`, `target_audience`, `content_style`, `primary_track_id`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES
('rb_demo_account_001', '打工人成长手册', 'xiaohongshu', '职场成长内容号', '想提升表达和晋升效率的白领', '直接、清晰、可执行', 'rb_demo_track_001', 'active', '样例账号：对应职场成长赛道', 'admin', '2026-04-01 11:00:00', 'admin', '2026-04-21 11:00:00'),
('rb_demo_account_002', '副业增收研究所', 'xiaohongshu', '副业路径拆解号', '希望稳定做副业的人群', '务实、拆解型、结果导向', 'rb_demo_track_002', 'active', '样例账号：对应轻创业副业赛道', 'admin', '2026-04-01 11:05:00', 'admin', '2026-04-21 11:05:00'),
('rb_demo_account_003', '知识IP实验室', 'xiaohongshu', '知识IP运营号', '想做知识产品和内容商业化的人群', '复盘型、数据型、案例型', 'rb_demo_track_003', 'active', '样例账号：对应知识IP运营赛道', 'admin', '2026-04-01 11:10:00', 'admin', '2026-04-21 11:10:00'),
('rb_demo_account_004', '同城门店增长笔记', 'xiaohongshu', '本地生活经营号', '门店老板与运营负责人', '真实场景、服务体验、转化导向', 'rb_demo_track_004', 'active', '样例账号：对应本地生活服务赛道', 'admin', '2026-04-01 11:15:00', 'admin', '2026-04-21 11:15:00')
ON DUPLICATE KEY UPDATE
`account_name` = VALUES(`account_name`),
`platform` = VALUES(`platform`),
`positioning` = VALUES(`positioning`),
`target_audience` = VALUES(`target_audience`),
`content_style` = VALUES(`content_style`),
`primary_track_id` = VALUES(`primary_track_id`),
`status` = VALUES(`status`),
`remark` = VALUES(`remark`),
`update_by` = VALUES(`update_by`),
`update_time` = VALUES(`update_time`);

INSERT INTO `rb_hotspot`
(`id`, `track_id`, `source_platform`, `title`, `summary`, `original_url`, `author_name`, `like_count`, `collect_count`, `comment_count`, `share_count`, `publish_time`, `collect_time`, `tags`, `heat_score`, `remix_score`, `risk_level`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT
  CONCAT('rb_demo_hotspot_', LPAD(seq.n, 3, '0')) AS id,
  CASE MOD(seq.n - 1, 4)
    WHEN 0 THEN 'rb_demo_track_001'
    WHEN 1 THEN 'rb_demo_track_002'
    WHEN 2 THEN 'rb_demo_track_003'
    ELSE 'rb_demo_track_004'
  END AS track_id,
  'xiaohongshu' AS source_platform,
  CONCAT(
    CASE MOD(seq.n - 1, 4)
      WHEN 0 THEN '职场成长'
      WHEN 1 THEN '副业增收'
      WHEN 2 THEN '知识IP'
      ELSE '本地门店'
    END,
    '热点选题第',
    LPAD(seq.n, 2, '0'),
    '期'
  ) AS title,
  CONCAT(
    '样例热点：围绕',
    CASE MOD(seq.n - 1, 4)
      WHEN 0 THEN '表达、晋升和复盘'
      WHEN 1 THEN '副业启动、接单和变现'
      WHEN 2 THEN '内容运营、产品包装和成交'
      ELSE '门店获客、探店和到店转化'
    END,
    '展开，适合用于演示热点池筛选、分析和二创生成。'
  ) AS summary,
  CONCAT('https://example.com/redbook/hotspot/', LPAD(seq.n, 3, '0')) AS original_url,
  CONCAT('样例作者', LPAD(seq.n, 2, '0')) AS author_name,
  600 + seq.n * 38 AS like_count,
  120 + seq.n * 12 AS collect_count,
  45 + seq.n * 4 AS comment_count,
  25 + seq.n * 3 AS share_count,
  DATE_SUB('2026-04-21 09:00:00', INTERVAL seq.n DAY) AS publish_time,
  DATE_SUB('2026-04-21 12:00:00', INTERVAL seq.n DAY) AS collect_time,
  CASE MOD(seq.n - 1, 4)
    WHEN 0 THEN '#职场成长 #沟通表达 #复盘'
    WHEN 1 THEN '#轻创业 #副业增收 #变现'
    WHEN 2 THEN '#知识IP #内容运营 #私域转化'
    ELSE '#本地生活 #门店增长 #到店转化'
  END AS tags,
  ROUND(70 + seq.n * 0.42, 2) AS heat_score,
  ROUND(68 + seq.n * 0.48, 2) AS remix_score,
  CASE
    WHEN MOD(seq.n, 10) = 0 THEN 'high'
    WHEN MOD(seq.n, 5) = 0 THEN 'medium'
    ELSE 'low'
  END AS risk_level,
  CASE
    WHEN seq.n <= 20 THEN 'draft_generated'
    WHEN seq.n <= 30 THEN 'analyzed'
    ELSE 'pending_analysis'
  END AS status,
  'admin' AS create_by,
  DATE_SUB('2026-04-21 12:00:00', INTERVAL seq.n DAY) AS create_time,
  'admin' AS update_by,
  DATE_SUB('2026-04-21 10:00:00', INTERVAL GREATEST(seq.n - 1, 0) DAY) AS update_time
FROM (
  SELECT ones.n + tens.n * 10 + 1 AS n
  FROM (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
  ) ones
  CROSS JOIN (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
  ) tens
  WHERE ones.n + tens.n * 10 + 1 <= 50
) seq
ON DUPLICATE KEY UPDATE
`track_id` = VALUES(`track_id`),
`source_platform` = VALUES(`source_platform`),
`title` = VALUES(`title`),
`summary` = VALUES(`summary`),
`original_url` = VALUES(`original_url`),
`author_name` = VALUES(`author_name`),
`like_count` = VALUES(`like_count`),
`collect_count` = VALUES(`collect_count`),
`comment_count` = VALUES(`comment_count`),
`share_count` = VALUES(`share_count`),
`publish_time` = VALUES(`publish_time`),
`collect_time` = VALUES(`collect_time`),
`tags` = VALUES(`tags`),
`heat_score` = VALUES(`heat_score`),
`remix_score` = VALUES(`remix_score`),
`risk_level` = VALUES(`risk_level`),
`status` = VALUES(`status`),
`update_by` = VALUES(`update_by`),
`update_time` = VALUES(`update_time`);

INSERT INTO `rb_hotspot_analysis`
(`id`, `hotspot_id`, `pain_points`, `hook_analysis`, `content_angle`, `title_directions`, `outline_suggestion`, `cover_copy_suggestion`, `tag_suggestion`, `product_fit`, `risk_warning`, `originality_suggestion`, `score`, `raw_result`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT
  CONCAT('rb_demo_analysis_', LPAD(seq.n, 3, '0')) AS id,
  CONCAT('rb_demo_hotspot_', LPAD(seq.n, 3, '0')) AS hotspot_id,
  CASE MOD(seq.n - 1, 4)
    WHEN 0 THEN '用户担心表达不清、做事很多但复盘不成体系。'
    WHEN 1 THEN '用户担心副业启动成本高、接不到单、节奏失控。'
    WHEN 2 THEN '用户担心内容持续性差、卖点不清、成交链路断裂。'
    ELSE '用户担心门店内容好看但不到店、活动没有复购。'
  END AS pain_points,
  '高表现内容普遍使用真实场景切入、先讲问题后给结构，再给可复制动作。' AS hook_analysis,
  '建议围绕真实场景拆解、避坑复盘、可执行步骤和结果对比来展开。' AS content_angle,
  '1. 问题先抛出；2. 用结果数字强化；3. 给出步骤或清单。' AS title_directions,
  '开头抛问题，中段给方法，结尾留行动清单和评论引导。' AS outline_suggestion,
  '把结果感和反差感放在封面第一行。' AS cover_copy_suggestion,
  '#样例分析 #二创选题 #小红书运营' AS tag_suggestion,
  '可在方法第三步自然嵌入产品/服务的使用场景。' AS product_fit,
  '避免绝对化承诺，涉及数据时保留区间描述。' AS risk_warning,
  '优先加入真实案例、失败经验和自己的执行细节。' AS originality_suggestion,
  ROUND(74 + seq.n * 0.55, 2) AS score,
  JSON_OBJECT('source', 'demo', 'analysis_no', seq.n, 'schema_valid', TRUE, 'provider', 'local-demo') AS raw_result,
  CASE WHEN seq.n <= 20 THEN 'adopted' ELSE 'analyzed' END AS status,
  'admin' AS create_by,
  DATE_SUB('2026-04-21 14:00:00', INTERVAL seq.n DAY) AS create_time,
  'admin' AS update_by,
  DATE_SUB('2026-04-21 13:00:00', INTERVAL seq.n DAY) AS update_time
FROM (
  SELECT ones.n + tens.n * 10 + 1 AS n
  FROM (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
  ) ones
  CROSS JOIN (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2
  ) tens
  WHERE ones.n + tens.n * 10 + 1 <= 30
) seq
ON DUPLICATE KEY UPDATE
`hotspot_id` = VALUES(`hotspot_id`),
`pain_points` = VALUES(`pain_points`),
`hook_analysis` = VALUES(`hook_analysis`),
`content_angle` = VALUES(`content_angle`),
`title_directions` = VALUES(`title_directions`),
`outline_suggestion` = VALUES(`outline_suggestion`),
`cover_copy_suggestion` = VALUES(`cover_copy_suggestion`),
`tag_suggestion` = VALUES(`tag_suggestion`),
`product_fit` = VALUES(`product_fit`),
`risk_warning` = VALUES(`risk_warning`),
`originality_suggestion` = VALUES(`originality_suggestion`),
`score` = VALUES(`score`),
`raw_result` = VALUES(`raw_result`),
`status` = VALUES(`status`),
`update_by` = VALUES(`update_by`),
`update_time` = VALUES(`update_time`);

INSERT INTO `rb_note_draft`
(`id`, `hotspot_id`, `analysis_id`, `track_id`, `account_id`, `title`, `cover_copy`, `body`, `tags`, `comment_guide`, `publish_time_suggestion`, `content_type`, `ai_version`, `manual_version`, `risk_check_result`, `audit_status`, `audit_opinion`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT
  CONCAT('rb_demo_draft_', LPAD(seq.n, 3, '0')) AS id,
  CONCAT('rb_demo_hotspot_', LPAD(seq.n, 3, '0')) AS hotspot_id,
  CONCAT('rb_demo_analysis_', LPAD(seq.n, 3, '0')) AS analysis_id,
  CASE MOD(seq.n - 1, 4)
    WHEN 0 THEN 'rb_demo_track_001'
    WHEN 1 THEN 'rb_demo_track_002'
    WHEN 2 THEN 'rb_demo_track_003'
    ELSE 'rb_demo_track_004'
  END AS track_id,
  CASE MOD(seq.n - 1, 4)
    WHEN 0 THEN 'rb_demo_account_001'
    WHEN 1 THEN 'rb_demo_account_002'
    WHEN 2 THEN 'rb_demo_account_003'
    ELSE 'rb_demo_account_004'
  END AS account_id,
  CONCAT(
    CASE MOD(seq.n - 1, 4)
      WHEN 0 THEN '职场成长'
      WHEN 1 THEN '副业增收'
      WHEN 2 THEN '知识IP'
      ELSE '门店增长'
    END,
    '样例草稿第',
    LPAD(seq.n, 2, '0'),
    '篇'
  ) AS title,
  CONCAT('先说结果，再给步骤，最后留行动清单 ', LPAD(seq.n, 2, '0')) AS cover_copy,
  CONCAT(
    '开头：用一个真实问题把用户拉进来。', '\n',
    '中段：拆成 3 个步骤，每一步都给具体动作。', '\n',
    '结尾：补一条常见误区，再给评论区行动引导。', '\n',
    '这是样例草稿第 ', seq.n, ' 篇，用于演示审核、排期和复盘闭环。'
  ) AS body,
  CASE MOD(seq.n - 1, 4)
    WHEN 0 THEN '#职场成长 #表达沟通 #复盘'
    WHEN 1 THEN '#副业增收 #轻创业 #接单'
    WHEN 2 THEN '#知识IP #内容运营 #成交设计'
    ELSE '#本地生活 #门店增长 #到店转化'
  END AS tags,
  '你最近最卡的是哪一步？评论区告诉我。' AS comment_guide,
  CASE MOD(seq.n, 3)
    WHEN 0 THEN '工作日 12:00-13:30'
    WHEN 1 THEN '工作日 19:00-21:00'
    ELSE '周末 10:00-12:00'
  END AS publish_time_suggestion,
  CASE MOD(seq.n, 4)
    WHEN 0 THEN '教程'
    WHEN 1 THEN '干货'
    WHEN 2 THEN '清单'
    ELSE '复盘'
  END AS content_type,
  'demo-ai-v1' AS ai_version,
  CASE WHEN seq.n <= 12 THEN 'demo-manual-v2' ELSE 'demo-manual-v1' END AS manual_version,
  '低风险，保留真实案例与边界表达。' AS risk_check_result,
  CASE
    WHEN seq.n <= 12 THEN 'approved'
    WHEN seq.n <= 16 THEN 'rejected'
    ELSE 'pending'
  END AS audit_status,
  CASE
    WHEN seq.n <= 8 THEN '已过审并进入发布闭环'
    WHEN seq.n <= 12 THEN '已过审，等待发布'
    WHEN seq.n <= 16 THEN '标题还可以更聚焦，建议补强第一屏结果感'
    ELSE '待人工审核，重点确认标题力度'
  END AS audit_opinion,
  CASE
    WHEN seq.n <= 8 THEN 'published'
    WHEN seq.n <= 12 THEN 'pending_publish'
    ELSE 'pending_review'
  END AS status,
  'admin' AS create_by,
  DATE_SUB('2026-04-21 16:00:00', INTERVAL seq.n DAY) AS create_time,
  'admin' AS update_by,
  DATE_SUB('2026-04-21 15:00:00', INTERVAL seq.n DAY) AS update_time
FROM (
  SELECT ones.n + tens.n * 10 + 1 AS n
  FROM (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
  ) ones
  CROSS JOIN (
    SELECT 0 AS n UNION ALL SELECT 1
  ) tens
  WHERE ones.n + tens.n * 10 + 1 <= 20
) seq
ON DUPLICATE KEY UPDATE
`hotspot_id` = VALUES(`hotspot_id`),
`analysis_id` = VALUES(`analysis_id`),
`track_id` = VALUES(`track_id`),
`account_id` = VALUES(`account_id`),
`title` = VALUES(`title`),
`cover_copy` = VALUES(`cover_copy`),
`body` = VALUES(`body`),
`tags` = VALUES(`tags`),
`comment_guide` = VALUES(`comment_guide`),
`publish_time_suggestion` = VALUES(`publish_time_suggestion`),
`content_type` = VALUES(`content_type`),
`ai_version` = VALUES(`ai_version`),
`manual_version` = VALUES(`manual_version`),
`risk_check_result` = VALUES(`risk_check_result`),
`audit_status` = VALUES(`audit_status`),
`audit_opinion` = VALUES(`audit_opinion`),
`status` = VALUES(`status`),
`update_by` = VALUES(`update_by`),
`update_time` = VALUES(`update_time`);

INSERT INTO `rb_publish_plan`
(`id`, `draft_id`, `account_id`, `planned_publish_time`, `actual_publish_time`, `publish_status`, `note_url`, `publisher`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT
  CONCAT('rb_demo_plan_', LPAD(seq.n, 3, '0')) AS id,
  CONCAT('rb_demo_draft_', LPAD(seq.n, 3, '0')) AS draft_id,
  CASE MOD(seq.n - 1, 4)
    WHEN 0 THEN 'rb_demo_account_001'
    WHEN 1 THEN 'rb_demo_account_002'
    WHEN 2 THEN 'rb_demo_account_003'
    ELSE 'rb_demo_account_004'
  END AS account_id,
  DATE_ADD('2026-04-09 20:00:00', INTERVAL seq.n DAY) AS planned_publish_time,
  CASE WHEN seq.n <= 8 THEN DATE_ADD('2026-04-09 20:10:00', INTERVAL seq.n DAY) ELSE NULL END AS actual_publish_time,
  CASE
    WHEN seq.n <= 6 THEN 'data_collected'
    WHEN seq.n <= 8 THEN 'published'
    WHEN seq.n = 10 THEN 'delayed'
    WHEN seq.n = 11 THEN 'canceled'
    ELSE 'pending'
  END AS publish_status,
  CASE WHEN seq.n <= 8 THEN CONCAT('https://www.xiaohongshu.com/explore/demo-plan-', LPAD(seq.n, 3, '0')) ELSE NULL END AS note_url,
  CASE WHEN seq.n <= 8 THEN '运营同学A' ELSE NULL END AS publisher,
  CASE
    WHEN seq.n <= 6 THEN '样例计划：已发布且已回收数据'
    WHEN seq.n <= 8 THEN '样例计划：已发布待补录回收'
    WHEN seq.n = 10 THEN '样例计划：延期到下周二'
    WHEN seq.n = 11 THEN '样例计划：该选题本轮取消'
    ELSE '样例计划：等待发布'
  END AS remark,
  'admin' AS create_by,
  DATE_SUB('2026-04-21 18:00:00', INTERVAL seq.n DAY) AS create_time,
  'admin' AS update_by,
  DATE_SUB('2026-04-21 17:00:00', INTERVAL seq.n DAY) AS update_time
FROM (
  SELECT ones.n + 1 AS n
  FROM (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    UNION ALL SELECT 10 UNION ALL SELECT 11
  ) ones
) seq
ON DUPLICATE KEY UPDATE
`draft_id` = VALUES(`draft_id`),
`account_id` = VALUES(`account_id`),
`planned_publish_time` = VALUES(`planned_publish_time`),
`actual_publish_time` = VALUES(`actual_publish_time`),
`publish_status` = VALUES(`publish_status`),
`note_url` = VALUES(`note_url`),
`publisher` = VALUES(`publisher`),
`remark` = VALUES(`remark`),
`update_by` = VALUES(`update_by`),
`update_time` = VALUES(`update_time`);

INSERT INTO `rb_note_metric`
(`id`, `publish_plan_id`, `note_draft_id`, `collect_node`, `impressions`, `views`, `likes`, `collects`, `comments`, `shares`, `followers`, `messages`, `leads`, `conversions`, `collect_time`, `interaction_rate`, `collect_rate`, `comment_rate`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT
  metric_data.id,
  metric_data.publish_plan_id,
  metric_data.note_draft_id,
  metric_data.collect_node,
  metric_data.impressions,
  metric_data.views,
  metric_data.likes,
  metric_data.collects,
  metric_data.comments,
  metric_data.shares,
  metric_data.followers,
  metric_data.messages,
  metric_data.leads,
  metric_data.conversions,
  metric_data.collect_time,
  ROUND((metric_data.likes + metric_data.collects + metric_data.comments + metric_data.shares) / NULLIF(metric_data.views, 0), 4) AS interaction_rate,
  ROUND(metric_data.collects / NULLIF(metric_data.views, 0), 4) AS collect_rate,
  ROUND(metric_data.comments / NULLIF(metric_data.views, 0), 4) AS comment_rate,
  metric_data.remark,
  'admin' AS create_by,
  metric_data.collect_time AS create_time,
  'admin' AS update_by,
  metric_data.collect_time AS update_time
FROM (
  SELECT
    CONCAT('rb_demo_metric_', LPAD(plan_seq.n, 3, '0'), '_', node_seq.collect_node) AS id,
    CONCAT('rb_demo_plan_', LPAD(plan_seq.n, 3, '0')) AS publish_plan_id,
    CONCAT('rb_demo_draft_', LPAD(plan_seq.n, 3, '0')) AS note_draft_id,
    node_seq.collect_node AS collect_node,
    3600 + plan_seq.n * 420 + node_seq.step_no * 260 AS impressions,
    1200 + plan_seq.n * 160 + node_seq.step_no * 120 AS views,
    90 + plan_seq.n * 8 + node_seq.step_no * 6 AS likes,
    28 + plan_seq.n * 4 + node_seq.step_no * 3 AS collects,
    18 + plan_seq.n * 3 + node_seq.step_no * 2 AS comments,
    10 + plan_seq.n * 2 + node_seq.step_no * 2 AS shares,
    6 + plan_seq.n + node_seq.step_no AS followers,
    3 + FLOOR((plan_seq.n + node_seq.step_no) / 2) AS messages,
    1 + FLOOR((plan_seq.n + node_seq.step_no) / 3) AS leads,
    FLOOR((plan_seq.n + node_seq.step_no) / 4) AS conversions,
    DATE_ADD(
      DATE_ADD('2026-04-09 20:10:00', INTERVAL plan_seq.n DAY),
      INTERVAL node_seq.hour_offset HOUR
    ) AS collect_time,
    CONCAT('样例数据回收节点：', node_seq.collect_node) AS remark
  FROM (
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6
  ) plan_seq
  CROSS JOIN (
    SELECT '2h' AS collect_node, 2 AS hour_offset, 1 AS step_no
    UNION ALL SELECT '24h', 24, 2
    UNION ALL SELECT '72h', 72, 3
    UNION ALL SELECT '7d', 168, 4
  ) node_seq
) metric_data
ON DUPLICATE KEY UPDATE
`publish_plan_id` = VALUES(`publish_plan_id`),
`note_draft_id` = VALUES(`note_draft_id`),
`collect_node` = VALUES(`collect_node`),
`impressions` = VALUES(`impressions`),
`views` = VALUES(`views`),
`likes` = VALUES(`likes`),
`collects` = VALUES(`collects`),
`comments` = VALUES(`comments`),
`shares` = VALUES(`shares`),
`followers` = VALUES(`followers`),
`messages` = VALUES(`messages`),
`leads` = VALUES(`leads`),
`conversions` = VALUES(`conversions`),
`collect_time` = VALUES(`collect_time`),
`interaction_rate` = VALUES(`interaction_rate`),
`collect_rate` = VALUES(`collect_rate`),
`comment_rate` = VALUES(`comment_rate`),
`remark` = VALUES(`remark`),
`update_by` = VALUES(`update_by`),
`update_time` = VALUES(`update_time`);

INSERT INTO `rb_review_report`
(`id`, `report_name`, `track_id`, `account_id`, `period_start`, `period_end`, `summary`, `high_performing_factors`, `low_performing_reasons`, `reusable_topics`, `stopped_directions`, `next_topic_suggestions`, `next_title_suggestions`, `next_publish_suggestions`, `raw_result`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES
(
  'rb_demo_review_001',
  '全量周复盘（样例）',
  NULL,
  NULL,
  '2026-04-01',
  '2026-04-21',
  '样例复盘结论：高收藏内容更偏真实场景拆解，晚间发布表现整体优于午间，副业与知识IP类内容互动更稳定。',
  '真实案例、清单式结构、第一屏先给结果、结尾给行动指令。',
  '标题过宽、铺垫太长、没有把反差点放到前两句。',
  '复盘清单、执行模板、低成本启动路径、到店转化拆解。',
  '空泛感悟、没有场景的鸡汤表达、过强承诺式标题。',
  '围绕高收藏内容继续拆解真实场景\n把高互动标题改写成系列化选题\n补充低表现内容的反向避坑复盘',
  '这类内容为什么更容易被收藏\n同样一个热点，换这个结构更容易出互动\n我从低表现内容里总结出的 3 个问题',
  '优先测试工作日 19:00-21:00\n保留中午 12:00-13:30 作为对照\n发布后 2h、24h、72h、7d 连续回收数据',
  JSON_OBJECT('source', 'demo', 'scope', 'all', 'generated', TRUE),
  'generated',
  'admin',
  '2026-04-21 20:30:00',
  'admin',
  '2026-04-21 20:30:00'
),
(
  'rb_demo_review_002',
  '职场成长赛道复盘（样例）',
  'rb_demo_track_001',
  'rb_demo_account_001',
  '2026-04-08',
  '2026-04-21',
  '职场成长赛道更适合用问题拆解和复盘框架，收藏表现优于评论表现。',
  '方法论清晰、问题直击痛点、清单结构稳定。',
  '案例不够具体时，阅读完成率会明显下滑。',
  '晋升复盘、沟通模板、汇报结构。',
  '纯观点输出、没有行动方案的情绪表达。',
  '继续做职场沟通模板系列\n把晋升复盘拆成更细的阶段选题',
  '这份汇报模板为什么总被收藏\n一次复盘，帮我改掉 3 个表达问题',
  '工作日 19:30 优先\n周三晚间作为连续测试点',
  JSON_OBJECT('source', 'demo', 'scope', 'track-account', 'generated', TRUE),
  'generated',
  'admin',
  '2026-04-21 20:40:00',
  'admin',
  '2026-04-21 20:40:00'
),
(
  'rb_demo_review_003',
  '副业赛道复盘（样例）',
  'rb_demo_track_002',
  'rb_demo_account_002',
  '2026-04-08',
  '2026-04-21',
  '副业赛道的评论和私信更明显，用户更关心真实收入路径和时间成本。',
  '真实收益区间、接单路径拆解、执行门槛清晰。',
  '如果步骤太抽象，用户会停留但不会留言。',
  '低成本启动、副业避坑、接单 SOP。',
  '模糊收益承诺、过于激进的致富表达。',
  '把副业启动拆成 7 天动作清单\n补一组反向避坑案例',
  '副业刚开始最该先做什么\n我为什么不建议你一上来就辞职做副业',
  '工作日中午和晚间双时段对照\n重点看 24h 私信与线索变化',
  JSON_OBJECT('source', 'demo', 'scope', 'track-account', 'generated', TRUE),
  'generated',
  'admin',
  '2026-04-21 20:50:00',
  'admin',
  '2026-04-21 20:50:00'
)
ON DUPLICATE KEY UPDATE
`report_name` = VALUES(`report_name`),
`track_id` = VALUES(`track_id`),
`account_id` = VALUES(`account_id`),
`period_start` = VALUES(`period_start`),
`period_end` = VALUES(`period_end`),
`summary` = VALUES(`summary`),
`high_performing_factors` = VALUES(`high_performing_factors`),
`low_performing_reasons` = VALUES(`low_performing_reasons`),
`reusable_topics` = VALUES(`reusable_topics`),
`stopped_directions` = VALUES(`stopped_directions`),
`next_topic_suggestions` = VALUES(`next_topic_suggestions`),
`next_title_suggestions` = VALUES(`next_title_suggestions`),
`next_publish_suggestions` = VALUES(`next_publish_suggestions`),
`raw_result` = VALUES(`raw_result`),
`status` = VALUES(`status`),
`update_by` = VALUES(`update_by`),
`update_time` = VALUES(`update_time`);
