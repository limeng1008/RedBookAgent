CREATE DATABASE IF NOT EXISTS `jeecg-boot` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `jeecg-boot`;

CREATE TABLE IF NOT EXISTS `rb_account` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '账号名称',
  `platform` varchar(50) DEFAULT '小红书' COMMENT '平台',
  `positioning` varchar(500) DEFAULT NULL COMMENT '账号定位',
  `target_audience` varchar(500) DEFAULT NULL COMMENT '目标人群',
  `content_style` varchar(500) DEFAULT NULL COMMENT '内容风格',
  `primary_track_id` varchar(36) DEFAULT NULL COMMENT '主要赛道ID',
  `status` varchar(32) DEFAULT 'active' COMMENT '状态',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rb_account_track` (`primary_track_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小红书账号';

CREATE TABLE IF NOT EXISTS `rb_track` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `track_name` varchar(100) DEFAULT NULL COMMENT '赛道名称',
  `keywords` varchar(1000) DEFAULT NULL COMMENT '关键词',
  `target_audience` varchar(500) DEFAULT NULL COMMENT '目标人群',
  `content_direction` varchar(1000) DEFAULT NULL COMMENT '内容方向',
  `competitor_accounts` varchar(1000) DEFAULT NULL COMMENT '竞品账号',
  `priority` int DEFAULT 10 COMMENT '优先级',
  `status` varchar(32) DEFAULT 'active' COMMENT '状态',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rb_track_name` (`track_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运营赛道';

CREATE TABLE IF NOT EXISTS `rb_persona` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `persona_name` varchar(100) DEFAULT NULL COMMENT '人设名称',
  `tone_style` varchar(500) DEFAULT NULL COMMENT '语气风格',
  `professionalism` varchar(100) DEFAULT NULL COMMENT '专业程度',
  `target_audience` varchar(500) DEFAULT NULL COMMENT '目标用户',
  `forbidden_expressions` text COMMENT '禁止表达',
  `common_expressions` text COMMENT '常用表达',
  `status` varchar(32) DEFAULT 'active' COMMENT '状态',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账号人设';

CREATE TABLE IF NOT EXISTS `rb_product` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `product_name` varchar(100) DEFAULT NULL COMMENT '产品/服务名称',
  `core_selling_points` text COMMENT '核心卖点',
  `target_audience` varchar(500) DEFAULT NULL COMMENT '适用人群',
  `scenarios` varchar(1000) DEFAULT NULL COMMENT '使用场景',
  `advantages` varchar(1000) DEFAULT NULL COMMENT '差异化优势',
  `forbidden_promises` text COMMENT '禁止承诺',
  `status` varchar(32) DEFAULT 'active' COMMENT '状态',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品卖点';

CREATE TABLE IF NOT EXISTS `rb_prompt_template` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `template_name` varchar(100) DEFAULT NULL COMMENT '模板名称',
  `template_code` varchar(100) DEFAULT NULL COMMENT '模板编码',
  `workflow_type` varchar(50) DEFAULT NULL COMMENT '工作流类型',
  `model_provider` varchar(50) DEFAULT NULL COMMENT '模型提供方',
  `prompt_content` text COMMENT '提示词内容',
  `output_schema` text COMMENT '输出格式',
  `status` varchar(32) DEFAULT 'active' COMMENT '状态',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rb_prompt_template_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提示词模板';

CREATE TABLE IF NOT EXISTS `rb_sensitive_word` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `word` varchar(100) DEFAULT NULL COMMENT '词条',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `risk_level` varchar(32) DEFAULT NULL COMMENT '风险等级',
  `replacement_suggestion` varchar(500) DEFAULT NULL COMMENT '替换建议',
  `status` varchar(32) DEFAULT 'active' COMMENT '状态',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rb_sensitive_word_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词/禁用词';

CREATE TABLE IF NOT EXISTS `rb_hotspot` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `track_id` varchar(36) DEFAULT NULL COMMENT '赛道ID',
  `source_platform` varchar(50) DEFAULT NULL COMMENT '来源平台',
  `title` varchar(300) DEFAULT NULL COMMENT '热点标题',
  `summary` text COMMENT '热点摘要',
  `original_url` varchar(1000) DEFAULT NULL COMMENT '原文链接',
  `author_name` varchar(100) DEFAULT NULL COMMENT '作者/账号',
  `like_count` bigint DEFAULT 0 COMMENT '点赞数',
  `collect_count` bigint DEFAULT 0 COMMENT '收藏数',
  `comment_count` bigint DEFAULT 0 COMMENT '评论数',
  `share_count` bigint DEFAULT 0 COMMENT '分享数',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `collect_time` datetime DEFAULT NULL COMMENT '采集时间',
  `tags` varchar(1000) DEFAULT NULL COMMENT '标签',
  `heat_score` decimal(10,2) DEFAULT NULL COMMENT '热度评分',
  `remix_score` decimal(10,2) DEFAULT NULL COMMENT '可二创评分',
  `risk_level` varchar(32) DEFAULT NULL COMMENT '风险等级',
  `status` varchar(32) DEFAULT 'pending_analysis' COMMENT '状态',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rb_hotspot_track` (`track_id`),
  KEY `idx_rb_hotspot_status` (`status`),
  KEY `idx_rb_hotspot_collect_time` (`collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热点池';

CREATE TABLE IF NOT EXISTS `rb_hotspot_analysis` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `hotspot_id` varchar(36) DEFAULT NULL COMMENT '热点ID',
  `pain_points` text COMMENT '用户痛点',
  `hook_analysis` text COMMENT '爆点拆解',
  `content_angle` text COMMENT '内容角度',
  `title_directions` text COMMENT '标题方向',
  `outline_suggestion` text COMMENT '正文结构建议',
  `cover_copy_suggestion` text COMMENT '封面文案建议',
  `tag_suggestion` varchar(1000) DEFAULT NULL COMMENT '标签建议',
  `product_fit` text COMMENT '产品植入建议',
  `risk_warning` text COMMENT '风险提示',
  `originality_suggestion` text COMMENT '原创化建议',
  `score` decimal(10,2) DEFAULT NULL COMMENT '综合评分',
  `raw_result` longtext COMMENT 'AI 原始输出',
  `status` varchar(32) DEFAULT 'analyzed' COMMENT '状态',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rb_analysis_hotspot` (`hotspot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热点分析结果';

CREATE TABLE IF NOT EXISTS `rb_note_draft` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `hotspot_id` varchar(36) DEFAULT NULL COMMENT '热点ID',
  `analysis_id` varchar(36) DEFAULT NULL COMMENT '分析ID',
  `track_id` varchar(36) DEFAULT NULL COMMENT '赛道ID',
  `account_id` varchar(36) DEFAULT NULL COMMENT '账号ID',
  `title` varchar(300) DEFAULT NULL COMMENT '标题',
  `cover_copy` varchar(300) DEFAULT NULL COMMENT '封面文案',
  `body` longtext COMMENT '正文',
  `tags` varchar(1000) DEFAULT NULL COMMENT '标签',
  `comment_guide` varchar(500) DEFAULT NULL COMMENT '评论区引导语',
  `publish_time_suggestion` varchar(500) DEFAULT NULL COMMENT '发布时间建议',
  `content_type` varchar(50) DEFAULT NULL COMMENT '内容类型',
  `ai_version` varchar(100) DEFAULT NULL COMMENT 'AI版本',
  `manual_version` varchar(100) DEFAULT NULL COMMENT '人工修改版本',
  `risk_check_result` text COMMENT '风险检测结果',
  `audit_status` varchar(32) DEFAULT 'pending' COMMENT '审核状态',
  `audit_opinion` varchar(1000) DEFAULT NULL COMMENT '审核意见',
  `status` varchar(32) DEFAULT 'pending_review' COMMENT '状态',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rb_draft_hotspot` (`hotspot_id`),
  KEY `idx_rb_draft_account` (`account_id`),
  KEY `idx_rb_draft_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记草稿';

CREATE TABLE IF NOT EXISTS `rb_note_draft_version` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `draft_id` varchar(36) NOT NULL COMMENT '草稿ID',
  `version_no` int DEFAULT 1 COMMENT '版本号',
  `version_type` varchar(50) DEFAULT NULL COMMENT '版本类型',
  `title` varchar(300) DEFAULT NULL COMMENT '标题',
  `cover_copy` varchar(300) DEFAULT NULL COMMENT '封面文案',
  `body` longtext COMMENT '正文',
  `tags` varchar(1000) DEFAULT NULL COMMENT '标签',
  `comment_guide` varchar(500) DEFAULT NULL COMMENT '评论区引导语',
  `publish_time_suggestion` varchar(500) DEFAULT NULL COMMENT '发布时间建议',
  `content_type` varchar(50) DEFAULT NULL COMMENT '内容类型',
  `risk_check_result` text COMMENT '风险检测结果',
  `audit_status` varchar(32) DEFAULT NULL COMMENT '审核状态',
  `audit_opinion` varchar(1000) DEFAULT NULL COMMENT '审核意见',
  `status` varchar(32) DEFAULT NULL COMMENT '状态',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rb_draft_version_no` (`draft_id`, `version_no`),
  KEY `idx_rb_draft_version_draft` (`draft_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记草稿版本';

CREATE TABLE IF NOT EXISTS `rb_publish_plan` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `draft_id` varchar(36) DEFAULT NULL COMMENT '草稿ID',
  `account_id` varchar(36) DEFAULT NULL COMMENT '账号ID',
  `planned_publish_time` datetime DEFAULT NULL COMMENT '计划发布时间',
  `actual_publish_time` datetime DEFAULT NULL COMMENT '实际发布时间',
  `publish_status` varchar(32) DEFAULT 'pending' COMMENT '发布状态',
  `note_url` varchar(1000) DEFAULT NULL COMMENT '小红书笔记链接',
  `publisher` varchar(100) DEFAULT NULL COMMENT '发布人',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rb_publish_draft` (`draft_id`),
  KEY `idx_rb_publish_account` (`account_id`),
  KEY `idx_rb_publish_time` (`planned_publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布计划';

CREATE TABLE IF NOT EXISTS `rb_note_metric` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `publish_plan_id` varchar(36) DEFAULT NULL COMMENT '发布计划ID',
  `note_draft_id` varchar(36) DEFAULT NULL COMMENT '草稿ID',
  `collect_node` varchar(32) DEFAULT NULL COMMENT '采集节点',
  `impressions` bigint DEFAULT 0 COMMENT '曝光量',
  `views` bigint DEFAULT 0 COMMENT '阅读/播放量',
  `likes` bigint DEFAULT 0 COMMENT '点赞数',
  `collects` bigint DEFAULT 0 COMMENT '收藏数',
  `comments` bigint DEFAULT 0 COMMENT '评论数',
  `shares` bigint DEFAULT 0 COMMENT '分享数',
  `followers` bigint DEFAULT 0 COMMENT '关注数',
  `messages` bigint DEFAULT 0 COMMENT '私信数',
  `leads` bigint DEFAULT 0 COMMENT '线索数',
  `conversions` bigint DEFAULT 0 COMMENT '转化数',
  `collect_time` datetime DEFAULT NULL COMMENT '采集时间',
  `interaction_rate` decimal(10,4) DEFAULT NULL COMMENT '互动率',
  `collect_rate` decimal(10,4) DEFAULT NULL COMMENT '收藏率',
  `comment_rate` decimal(10,4) DEFAULT NULL COMMENT '评论率',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rb_metric_publish` (`publish_plan_id`),
  KEY `idx_rb_metric_node` (`collect_node`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记数据回收';

CREATE TABLE IF NOT EXISTS `rb_review_report` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `report_name` varchar(200) DEFAULT NULL COMMENT '报告名称',
  `track_id` varchar(36) DEFAULT NULL COMMENT '赛道ID',
  `account_id` varchar(36) DEFAULT NULL COMMENT '账号ID',
  `period_start` date DEFAULT NULL COMMENT '开始日期',
  `period_end` date DEFAULT NULL COMMENT '结束日期',
  `summary` text COMMENT '表现总结',
  `high_performing_factors` text COMMENT '高表现因素',
  `low_performing_reasons` text COMMENT '低表现原因',
  `reusable_topics` text COMMENT '可复用选题',
  `stopped_directions` text COMMENT '停止方向',
  `next_topic_suggestions` text COMMENT '下一轮选题建议',
  `next_title_suggestions` text COMMENT '下一轮标题建议',
  `next_publish_suggestions` text COMMENT '下一轮发布时间建议',
  `raw_result` longtext COMMENT 'AI 原始输出',
  `status` varchar(32) DEFAULT 'draft' COMMENT '状态',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rb_review_track` (`track_id`),
  KEY `idx_rb_review_account` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复盘报告';

INSERT INTO `rb_prompt_template` (`id`, `template_name`, `template_code`, `workflow_type`, `model_provider`, `prompt_content`, `output_schema`, `status`, `create_time`)
VALUES
('rb_prompt_hotspot_analysis', '热点分析模板', 'hotspot_analysis', 'hotspot_analysis', 'auto', '根据赛道、账号信息和热点内容，输出结构化的小红书热点分析结果。请严格按 JSON 返回，不要输出解释，不要使用 Markdown。', '{"pain_points":"","hook_analysis":"","content_angle":"","title_directions":[""],"outline_suggestion":"","cover_copy_suggestion":"","tag_suggestion":[""],"product_fit":"","risk_warning":"","originality_suggestion":"","score":0}', 'active', NOW()),
('rb_prompt_note_draft', '笔记草稿生成模板', 'note_draft', 'note_draft', 'auto', '根据热点分析结果生成小红书原创化笔记草稿。请严格按 JSON 返回，不要输出解释，不要使用 Markdown。', '{"title":"","cover_copy":"","body":"","tags":[""],"comment_guide":"","publish_time_suggestion":"","content_type":"","risk_check_result":""}', 'active', NOW()),
('rb_prompt_review_report', '复盘分析模板', 'review_report', 'review_report', 'auto', '根据发布内容、发布时间和数据回收结果，输出结构化复盘结论与下一轮建议。请严格按 JSON 返回，不要输出解释，不要使用 Markdown。', '{"summary":"","high_performing_factors":[""],"low_performing_reasons":[""],"reusable_topics":[""],"stopped_directions":[""],"next_topic_suggestions":[""],"next_title_suggestions":[""],"next_publish_suggestions":[""]}', 'active', NOW())
ON DUPLICATE KEY UPDATE `template_name` = VALUES(`template_name`), `prompt_content` = VALUES(`prompt_content`), `output_schema` = VALUES(`output_schema`);

-- ------------------------------------------------------------------
-- RedBook V1 状态字典初始化
-- ------------------------------------------------------------------

INSERT INTO `sys_dict` (`id`, `dict_name`, `dict_code`, `description`, `del_flag`, `create_by`, `create_time`, `type`, `tenant_id`)
VALUES
(MD5('redbook-dict:rb_common_status'), '小红书通用状态', 'rb_common_status', '小红书运营通用启停状态', 0, 'admin', NOW(), 0, 0),
(MD5('redbook-dict:rb_hotspot_status'), '小红书热点状态', 'rb_hotspot_status', '热点池状态', 0, 'admin', NOW(), 0, 0),
(MD5('redbook-dict:rb_analysis_status'), '小红书选题分析状态', 'rb_analysis_status', '选题分析状态', 0, 'admin', NOW(), 0, 0),
(MD5('redbook-dict:rb_draft_status'), '小红书草稿状态', 'rb_draft_status', '笔记草稿状态', 0, 'admin', NOW(), 0, 0),
(MD5('redbook-dict:rb_audit_status'), '小红书审核状态', 'rb_audit_status', '草稿审核状态', 0, 'admin', NOW(), 0, 0),
(MD5('redbook-dict:rb_publish_status'), '小红书发布状态', 'rb_publish_status', '发布计划状态', 0, 'admin', NOW(), 0, 0),
(MD5('redbook-dict:rb_review_status'), '小红书复盘状态', 'rb_review_status', '复盘报告状态', 0, 'admin', NOW(), 0, 0),
(MD5('redbook-dict:rb_risk_level'), '小红书风险等级', 'rb_risk_level', '内容风险等级', 0, 'admin', NOW(), 0, 0),
(MD5('redbook-dict:rb_collect_node'), '小红书采集节点', 'rb_collect_node', '数据回收采集节点', 0, 'admin', NOW(), 0, 0),
(MD5('redbook-dict:rb_content_type'), '小红书内容类型', 'rb_content_type', '笔记内容类型', 0, 'admin', NOW(), 0, 0)
ON DUPLICATE KEY UPDATE
`dict_name` = VALUES(`dict_name`),
`description` = VALUES(`description`),
`del_flag` = VALUES(`del_flag`),
`update_by` = 'admin',
`update_time` = NOW();

INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':active')), d.id, '启用', 'active', NULL, '启用', 1, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_common_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':inactive')), d.id, '停用', 'inactive', NULL, '停用', 2, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_common_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();

INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':pending_analysis')), d.id, '待分析', 'pending_analysis', NULL, '等待 AI 选题分析', 1, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_hotspot_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':analyzed')), d.id, '已分析', 'analyzed', NULL, '已生成选题分析', 2, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_hotspot_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':draft_generated')), d.id, '已生成草稿', 'draft_generated', NULL, '已生成笔记草稿', 3, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_hotspot_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':discarded')), d.id, '已废弃', 'discarded', NULL, '不进入后续选题', 4, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_hotspot_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();

INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':analyzed')), d.id, '已分析', 'analyzed', NULL, '选题分析已生成', 1, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_analysis_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':adopted')), d.id, '已采纳', 'adopted', NULL, '已采纳并进入草稿', 2, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_analysis_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':discarded')), d.id, '已废弃', 'discarded', NULL, '分析结果废弃', 3, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_analysis_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();

INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':pending_review')), d.id, '待审核', 'pending_review', NULL, '等待人工审核', 1, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_draft_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':pending_publish')), d.id, '待发布', 'pending_publish', NULL, '审核通过，等待排期或发布', 2, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_draft_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':published')), d.id, '已发布', 'published', NULL, '已人工发布', 3, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_draft_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();

INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':pending')), d.id, '待审核', 'pending', NULL, '待人工审核', 1, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_audit_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':approved')), d.id, '已通过', 'approved', NULL, '审核通过', 2, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_audit_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':rejected')), d.id, '已退回', 'rejected', NULL, '审核退回修改', 3, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_audit_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();

INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':pending')), d.id, '待发布', 'pending', NULL, '等待人工发布', 1, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_publish_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':published')), d.id, '已发布', 'published', NULL, '已人工发布', 2, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_publish_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':delayed')), d.id, '已延期', 'delayed', NULL, '发布计划延期', 3, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_publish_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':canceled')), d.id, '已取消', 'canceled', NULL, '发布计划取消', 4, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_publish_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':data_collected')), d.id, '已回收数据', 'data_collected', NULL, '已生成或录入回收数据', 5, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_publish_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();

INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':draft')), d.id, '草稿', 'draft', NULL, '复盘报告草稿', 1, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_review_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':generated')), d.id, '已生成', 'generated', NULL, '复盘报告已生成', 2, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_review_status'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();

INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':low')), d.id, '低风险', 'low', NULL, '低风险', 1, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_risk_level'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':medium')), d.id, '中风险', 'medium', NULL, '中风险', 2, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_risk_level'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':high')), d.id, '高风险', 'high', NULL, '高风险', 3, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_risk_level'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();

INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':2h')), d.id, '2小时', '2h', NULL, '发布后 2 小时', 1, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_collect_node'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':24h')), d.id, '24小时', '24h', NULL, '发布后 24 小时', 2, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_collect_node'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':72h')), d.id, '72小时', '72h', NULL, '发布后 72 小时', 3, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_collect_node'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':7d')), d.id, '7天', '7d', NULL, '发布后 7 天', 4, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_collect_node'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();

INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':ganhuo')), d.id, '干货', '干货', NULL, '干货内容', 1, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_content_type'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':ceping')), d.id, '测评', '测评', NULL, '测评内容', 2, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_content_type'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':bikeng')), d.id, '避坑', '避坑', NULL, '避坑内容', 3, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_content_type'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':qingdan')), d.id, '清单', '清单', NULL, '清单内容', 4, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_content_type'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':jiaocheng')), d.id, '教程', '教程', NULL, '教程内容', 5, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_content_type'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':gushi')), d.id, '故事', '故事', NULL, '故事内容', 6, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_content_type'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':duibi')), d.id, '对比', '对比', NULL, '对比内容', 7, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_content_type'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':zhongcao')), d.id, '种草', '种草', NULL, '种草内容', 8, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_content_type'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `item_color`, `description`, `sort_order`, `status`, `create_by`, `create_time`)
SELECT MD5(CONCAT('redbook-dict-item:', d.dict_code, ':fupan')), d.id, '复盘', '复盘', NULL, '复盘内容', 9, 1, 'admin', NOW()
FROM `sys_dict` d WHERE d.dict_code = 'rb_content_type'
ON DUPLICATE KEY UPDATE `item_text` = VALUES(`item_text`), `description` = VALUES(`description`), `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`), `update_by` = 'admin', `update_time` = NOW();

-- ------------------------------------------------------------------
-- Jeecg 菜单初始化
-- 说明：
-- 1. 当前 JeecgBoot Vue3 项目运行在 BACK 后台权限模式。
-- 2. 左侧菜单来自 sys_permission，而不是前端本地路由文件。
-- 3. 这里会补齐“小红书运营”菜单，并默认授权给 admin / vue3 角色。
-- ------------------------------------------------------------------

INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('9b0b9b0b000000000000000000000001', NULL, '小红书运营', '/redbook', 'layouts/default/index', 1, 'RedbookRoot', '/redbook/workbench', 0, NULL, '0', 2.50, 0, 'ant-design:fire-outlined', 0, 0, 0, 0, 'RedBook Agent 顶级菜单', 'admin', '2026-04-20 00:00:00', 'admin', '2026-04-20 00:00:00', 0, 0, '1', 0),
('9b0b9b0b000000000000000000000002', '9b0b9b0b000000000000000000000001', '运营工作台', '/redbook/workbench', 'redbook/workbench/index', 1, 'RedbookWorkbench', NULL, 1, NULL, '0', 0.00, 0, NULL, 1, 0, 0, 0, '小红书运营工作台', 'admin', '2026-04-20 00:00:00', 'admin', '2026-04-20 00:00:00', 0, 0, '1', 0),
('9b0b9b0b000000000000000000000003', '9b0b9b0b000000000000000000000001', '账号管理', '/redbook/account', 'redbook/crud/RedbookCrudPage', 1, 'RedbookAccount', NULL, 1, NULL, '0', 1.00, 0, NULL, 1, 0, 0, 0, '小红书账号管理', 'admin', '2026-04-20 00:00:00', 'admin', '2026-04-20 00:00:00', 0, 0, '1', 0),
('9b0b9b0b000000000000000000000004', '9b0b9b0b000000000000000000000001', '赛道管理', '/redbook/track', 'redbook/crud/RedbookCrudPage', 1, 'RedbookTrack', NULL, 1, NULL, '0', 2.00, 0, NULL, 1, 0, 0, 0, '小红书赛道管理', 'admin', '2026-04-20 00:00:00', 'admin', '2026-04-20 00:00:00', 0, 0, '1', 0),
('9b0b9b0b000000000000000000000005', '9b0b9b0b000000000000000000000001', '人设管理', '/redbook/persona', 'redbook/crud/RedbookCrudPage', 1, 'RedbookPersona', NULL, 1, NULL, '0', 3.00, 0, NULL, 1, 0, 0, 0, '小红书人设管理', 'admin', '2026-04-20 00:00:00', 'admin', '2026-04-20 00:00:00', 0, 0, '1', 0),
('9b0b9b0b000000000000000000000006', '9b0b9b0b000000000000000000000001', '产品卖点', '/redbook/product', 'redbook/crud/RedbookCrudPage', 1, 'RedbookProduct', NULL, 1, NULL, '0', 4.00, 0, NULL, 1, 0, 0, 0, '小红书产品卖点管理', 'admin', '2026-04-20 00:00:00', 'admin', '2026-04-20 00:00:00', 0, 0, '1', 0),
('9b0b9b0b000000000000000000000007', '9b0b9b0b000000000000000000000001', '提示词模板', '/redbook/prompt-template', 'redbook/crud/RedbookCrudPage', 1, 'RedbookPromptTemplate', NULL, 1, NULL, '0', 5.00, 0, NULL, 1, 0, 0, 0, '小红书提示词模板', 'admin', '2026-04-20 00:00:00', 'admin', '2026-04-20 00:00:00', 0, 0, '1', 0),
('9b0b9b0b000000000000000000000008', '9b0b9b0b000000000000000000000001', '敏感词', '/redbook/sensitive-word', 'redbook/crud/RedbookCrudPage', 1, 'RedbookSensitiveWord', NULL, 1, NULL, '0', 6.00, 0, NULL, 1, 0, 0, 0, '小红书敏感词管理', 'admin', '2026-04-20 00:00:00', 'admin', '2026-04-20 00:00:00', 0, 0, '1', 0),
('9b0b9b0b000000000000000000000009', '9b0b9b0b000000000000000000000001', '热点池', '/redbook/hotspot', 'redbook/crud/RedbookCrudPage', 1, 'RedbookHotspot', NULL, 1, NULL, '0', 7.00, 0, NULL, 1, 0, 0, 0, '小红书热点池', 'admin', '2026-04-20 00:00:00', 'admin', '2026-04-20 00:00:00', 0, 0, '1', 0),
('9b0b9b0b00000000000000000000000a', '9b0b9b0b000000000000000000000001', '热点分析', '/redbook/hotspot-analysis', 'redbook/crud/RedbookCrudPage', 1, 'RedbookHotspotAnalysis', NULL, 1, NULL, '0', 8.00, 0, NULL, 1, 0, 0, 0, '小红书热点分析', 'admin', '2026-04-20 00:00:00', 'admin', '2026-04-20 00:00:00', 0, 0, '1', 0),
('9b0b9b0b00000000000000000000000b', '9b0b9b0b000000000000000000000001', '笔记草稿', '/redbook/note-draft', 'redbook/crud/RedbookCrudPage', 1, 'RedbookNoteDraft', NULL, 1, NULL, '0', 9.00, 0, NULL, 1, 0, 0, 0, '小红书笔记草稿', 'admin', '2026-04-20 00:00:00', 'admin', '2026-04-20 00:00:00', 0, 0, '1', 0),
('9b0b9b0b00000000000000000000000c', '9b0b9b0b000000000000000000000001', '发布计划', '/redbook/publish-plan', 'redbook/crud/RedbookCrudPage', 1, 'RedbookPublishPlan', NULL, 1, NULL, '0', 10.00, 0, NULL, 1, 0, 0, 0, '小红书发布计划', 'admin', '2026-04-20 00:00:00', 'admin', '2026-04-20 00:00:00', 0, 0, '1', 0),
('9b0b9b0b00000000000000000000000f', '9b0b9b0b000000000000000000000001', '发布日历', '/redbook/publish-calendar', 'redbook/publish-calendar/index', 1, 'RedbookPublishCalendar', NULL, 1, NULL, '0', 10.50, 0, NULL, 1, 0, 0, 0, '小红书发布日历', 'admin', '2026-04-20 00:00:00', 'admin', '2026-04-20 00:00:00', 0, 0, '1', 0),
('9b0b9b0b00000000000000000000000d', '9b0b9b0b000000000000000000000001', '数据回收', '/redbook/note-metric', 'redbook/crud/RedbookCrudPage', 1, 'RedbookNoteMetric', NULL, 1, NULL, '0', 11.00, 0, NULL, 1, 0, 0, 0, '小红书数据回收', 'admin', '2026-04-20 00:00:00', 'admin', '2026-04-20 00:00:00', 0, 0, '1', 0),
('9b0b9b0b000000000000000000000010', '9b0b9b0b000000000000000000000001', '复盘看板', '/redbook/review-dashboard', 'redbook/review-dashboard/index', 1, 'RedbookReviewDashboard', NULL, 1, NULL, '0', 11.50, 0, NULL, 1, 0, 0, 0, '小红书复盘看板', 'admin', '2026-04-20 00:00:00', 'admin', '2026-04-20 00:00:00', 0, 0, '1', 0),
('9b0b9b0b00000000000000000000000e', '9b0b9b0b000000000000000000000001', '复盘报告', '/redbook/review-report', 'redbook/crud/RedbookCrudPage', 1, 'RedbookReviewReport', NULL, 1, NULL, '0', 12.00, 0, NULL, 1, 0, 0, 0, '小红书复盘报告', 'admin', '2026-04-20 00:00:00', 'admin', '2026-04-20 00:00:00', 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`),
`name` = VALUES(`name`),
`url` = VALUES(`url`),
`component` = VALUES(`component`),
`is_route` = VALUES(`is_route`),
`component_name` = VALUES(`component_name`),
`redirect` = VALUES(`redirect`),
`menu_type` = VALUES(`menu_type`),
`perms` = VALUES(`perms`),
`perms_type` = VALUES(`perms_type`),
`sort_no` = VALUES(`sort_no`),
`always_show` = VALUES(`always_show`),
`icon` = VALUES(`icon`),
`is_leaf` = VALUES(`is_leaf`),
`keep_alive` = VALUES(`keep_alive`),
`hidden` = VALUES(`hidden`),
`hide_tab` = VALUES(`hide_tab`),
`description` = VALUES(`description`),
`update_by` = 'admin',
`update_time` = NOW(),
`del_flag` = VALUES(`del_flag`),
`rule_flag` = VALUES(`rule_flag`),
`status` = VALUES(`status`),
`internal_or_external` = VALUES(`internal_or_external`);

-- 5. RedBook 按钮权限初始化（后端 Shiro 权限 + 前端按钮授权）
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
SELECT
  MD5(CONCAT('redbook-perm:', t.perms)),
  t.parent_id,
  t.name,
  NULL,
  NULL,
  0,
  NULL,
  NULL,
  2,
  t.perms,
  '1',
  t.sort_no,
  0,
  NULL,
  1,
  0,
  0,
  0,
  t.description,
  'admin',
  NOW(),
  'admin',
  NOW(),
  0,
  0,
  '1',
  0
FROM (
SELECT '9b0b9b0b000000000000000000000002' AS parent_id, '工作台概览' AS name, 'redbook:workbench:overview' AS perms, 1.00 AS sort_no, '运营工作台概览接口权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000002' AS parent_id, '复盘看板' AS name, 'redbook:workbench:reviewDashboard' AS perms, 2.00 AS sort_no, '复盘看板接口权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000003' AS parent_id, '新增' AS name, 'redbook:account:add' AS perms, 1.00 AS sort_no, '账号新增按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000003' AS parent_id, '编辑' AS name, 'redbook:account:edit' AS perms, 2.00 AS sort_no, '账号编辑按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000003' AS parent_id, '删除' AS name, 'redbook:account:delete' AS perms, 3.00 AS sort_no, '账号删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000003' AS parent_id, '批量删除' AS name, 'redbook:account:deleteBatch' AS perms, 4.00 AS sort_no, '账号批量删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000003' AS parent_id, '导出' AS name, 'redbook:account:exportXls' AS perms, 5.00 AS sort_no, '账号导出按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000003' AS parent_id, '导入' AS name, 'redbook:account:importExcel' AS perms, 6.00 AS sort_no, '账号导入按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000004' AS parent_id, '新增' AS name, 'redbook:track:add' AS perms, 1.00 AS sort_no, '赛道新增按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000004' AS parent_id, '编辑' AS name, 'redbook:track:edit' AS perms, 2.00 AS sort_no, '赛道编辑按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000004' AS parent_id, '删除' AS name, 'redbook:track:delete' AS perms, 3.00 AS sort_no, '赛道删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000004' AS parent_id, '批量删除' AS name, 'redbook:track:deleteBatch' AS perms, 4.00 AS sort_no, '赛道批量删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000004' AS parent_id, '导出' AS name, 'redbook:track:exportXls' AS perms, 5.00 AS sort_no, '赛道导出按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000004' AS parent_id, '导入' AS name, 'redbook:track:importExcel' AS perms, 6.00 AS sort_no, '赛道导入按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000005' AS parent_id, '新增' AS name, 'redbook:persona:add' AS perms, 1.00 AS sort_no, '人设新增按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000005' AS parent_id, '编辑' AS name, 'redbook:persona:edit' AS perms, 2.00 AS sort_no, '人设编辑按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000005' AS parent_id, '删除' AS name, 'redbook:persona:delete' AS perms, 3.00 AS sort_no, '人设删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000005' AS parent_id, '批量删除' AS name, 'redbook:persona:deleteBatch' AS perms, 4.00 AS sort_no, '人设批量删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000005' AS parent_id, '导出' AS name, 'redbook:persona:exportXls' AS perms, 5.00 AS sort_no, '人设导出按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000005' AS parent_id, '导入' AS name, 'redbook:persona:importExcel' AS perms, 6.00 AS sort_no, '人设导入按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000006' AS parent_id, '新增' AS name, 'redbook:product:add' AS perms, 1.00 AS sort_no, '产品卖点新增按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000006' AS parent_id, '编辑' AS name, 'redbook:product:edit' AS perms, 2.00 AS sort_no, '产品卖点编辑按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000006' AS parent_id, '删除' AS name, 'redbook:product:delete' AS perms, 3.00 AS sort_no, '产品卖点删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000006' AS parent_id, '批量删除' AS name, 'redbook:product:deleteBatch' AS perms, 4.00 AS sort_no, '产品卖点批量删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000006' AS parent_id, '导出' AS name, 'redbook:product:exportXls' AS perms, 5.00 AS sort_no, '产品卖点导出按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000006' AS parent_id, '导入' AS name, 'redbook:product:importExcel' AS perms, 6.00 AS sort_no, '产品卖点导入按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000007' AS parent_id, '新增' AS name, 'redbook:promptTemplate:add' AS perms, 1.00 AS sort_no, '提示词模板新增按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000007' AS parent_id, '编辑' AS name, 'redbook:promptTemplate:edit' AS perms, 2.00 AS sort_no, '提示词模板编辑按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000007' AS parent_id, '删除' AS name, 'redbook:promptTemplate:delete' AS perms, 3.00 AS sort_no, '提示词模板删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000007' AS parent_id, '批量删除' AS name, 'redbook:promptTemplate:deleteBatch' AS perms, 4.00 AS sort_no, '提示词模板批量删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000007' AS parent_id, '导出' AS name, 'redbook:promptTemplate:exportXls' AS perms, 5.00 AS sort_no, '提示词模板导出按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000007' AS parent_id, '导入' AS name, 'redbook:promptTemplate:importExcel' AS perms, 6.00 AS sort_no, '提示词模板导入按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000008' AS parent_id, '新增' AS name, 'redbook:sensitiveWord:add' AS perms, 1.00 AS sort_no, '敏感词新增按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000008' AS parent_id, '编辑' AS name, 'redbook:sensitiveWord:edit' AS perms, 2.00 AS sort_no, '敏感词编辑按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000008' AS parent_id, '删除' AS name, 'redbook:sensitiveWord:delete' AS perms, 3.00 AS sort_no, '敏感词删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000008' AS parent_id, '批量删除' AS name, 'redbook:sensitiveWord:deleteBatch' AS perms, 4.00 AS sort_no, '敏感词批量删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000008' AS parent_id, '导出' AS name, 'redbook:sensitiveWord:exportXls' AS perms, 5.00 AS sort_no, '敏感词导出按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000008' AS parent_id, '导入' AS name, 'redbook:sensitiveWord:importExcel' AS perms, 6.00 AS sort_no, '敏感词导入按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000009' AS parent_id, '新增' AS name, 'redbook:hotspot:add' AS perms, 1.00 AS sort_no, '热点新增按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000009' AS parent_id, '编辑' AS name, 'redbook:hotspot:edit' AS perms, 2.00 AS sort_no, '热点编辑按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000009' AS parent_id, '删除' AS name, 'redbook:hotspot:delete' AS perms, 3.00 AS sort_no, '热点删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000009' AS parent_id, '批量删除' AS name, 'redbook:hotspot:deleteBatch' AS perms, 4.00 AS sort_no, '热点批量删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000009' AS parent_id, '导出' AS name, 'redbook:hotspot:exportXls' AS perms, 5.00 AS sort_no, '热点导出按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000009' AS parent_id, '导入' AS name, 'redbook:hotspot:importExcel' AS perms, 6.00 AS sort_no, '热点导入按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b000000000000000000000009' AS parent_id, '热点分析' AS name, 'redbook:hotspot:analyze' AS perms, 7.00 AS sort_no, '热点分析按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000a' AS parent_id, '新增' AS name, 'redbook:hotspotAnalysis:add' AS perms, 1.00 AS sort_no, '热点分析新增按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000a' AS parent_id, '编辑' AS name, 'redbook:hotspotAnalysis:edit' AS perms, 2.00 AS sort_no, '热点分析编辑按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000a' AS parent_id, '删除' AS name, 'redbook:hotspotAnalysis:delete' AS perms, 3.00 AS sort_no, '热点分析删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000a' AS parent_id, '批量删除' AS name, 'redbook:hotspotAnalysis:deleteBatch' AS perms, 4.00 AS sort_no, '热点分析批量删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000a' AS parent_id, '导出' AS name, 'redbook:hotspotAnalysis:exportXls' AS perms, 5.00 AS sort_no, '热点分析导出按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000a' AS parent_id, '导入' AS name, 'redbook:hotspotAnalysis:importExcel' AS perms, 6.00 AS sort_no, '热点分析导入按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000a' AS parent_id, '生成草稿' AS name, 'redbook:hotspotAnalysis:generateDraft' AS perms, 7.00 AS sort_no, '分析生成草稿按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000b' AS parent_id, '新增' AS name, 'redbook:noteDraft:add' AS perms, 1.00 AS sort_no, '草稿新增按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000b' AS parent_id, '编辑' AS name, 'redbook:noteDraft:edit' AS perms, 2.00 AS sort_no, '草稿编辑按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000b' AS parent_id, '删除' AS name, 'redbook:noteDraft:delete' AS perms, 3.00 AS sort_no, '草稿删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000b' AS parent_id, '批量删除' AS name, 'redbook:noteDraft:deleteBatch' AS perms, 4.00 AS sort_no, '草稿批量删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000b' AS parent_id, '导出' AS name, 'redbook:noteDraft:exportXls' AS perms, 5.00 AS sort_no, '草稿导出按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000b' AS parent_id, '导入' AS name, 'redbook:noteDraft:importExcel' AS perms, 6.00 AS sort_no, '草稿导入按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000b' AS parent_id, '加入排期' AS name, 'redbook:noteDraft:createPublishPlan' AS perms, 7.00 AS sort_no, '草稿加入发布计划按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000b' AS parent_id, '审核通过' AS name, 'redbook:noteDraft:approve' AS perms, 8.00 AS sort_no, '草稿审核通过按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000b' AS parent_id, '审核退回' AS name, 'redbook:noteDraft:reject' AS perms, 9.00 AS sort_no, '草稿审核退回按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000b' AS parent_id, '风险检查' AS name, 'redbook:noteDraft:riskCheck' AS perms, 10.00 AS sort_no, '草稿独立风险检查按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000b' AS parent_id, '版本列表' AS name, 'redbook:noteDraft:versions' AS perms, 11.00 AS sort_no, '草稿版本列表按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000b' AS parent_id, '恢复版本' AS name, 'redbook:noteDraft:restoreVersion' AS perms, 12.00 AS sort_no, '草稿恢复版本按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000c' AS parent_id, '新增' AS name, 'redbook:publishPlan:add' AS perms, 1.00 AS sort_no, '发布计划新增按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000c' AS parent_id, '编辑' AS name, 'redbook:publishPlan:edit' AS perms, 2.00 AS sort_no, '发布计划编辑按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000c' AS parent_id, '删除' AS name, 'redbook:publishPlan:delete' AS perms, 3.00 AS sort_no, '发布计划删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000c' AS parent_id, '批量删除' AS name, 'redbook:publishPlan:deleteBatch' AS perms, 4.00 AS sort_no, '发布计划批量删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000c' AS parent_id, '导出' AS name, 'redbook:publishPlan:exportXls' AS perms, 5.00 AS sort_no, '发布计划导出按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000c' AS parent_id, '导入' AS name, 'redbook:publishPlan:importExcel' AS perms, 6.00 AS sort_no, '发布计划导入按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000c' AS parent_id, '标记已发布' AS name, 'redbook:publishPlan:markPublished' AS perms, 7.00 AS sort_no, '发布计划标记已发布按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000c' AS parent_id, '生成回收记录' AS name, 'redbook:publishPlan:createMetric' AS perms, 8.00 AS sort_no, '发布计划生成回收记录按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000c' AS parent_id, '延期' AS name, 'redbook:publishPlan:delay' AS perms, 9.00 AS sort_no, '发布计划延期按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000c' AS parent_id, '取消' AS name, 'redbook:publishPlan:cancel' AS perms, 10.00 AS sort_no, '发布计划取消按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000c' AS parent_id, '恢复待发布' AS name, 'redbook:publishPlan:restorePending' AS perms, 11.00 AS sort_no, '发布计划恢复待发布按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000c' AS parent_id, '补录链接' AS name, 'redbook:publishPlan:updateNoteUrl' AS perms, 12.00 AS sort_no, '发布计划补录链接按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000d' AS parent_id, '新增' AS name, 'redbook:noteMetric:add' AS perms, 1.00 AS sort_no, '数据回收新增按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000d' AS parent_id, '编辑' AS name, 'redbook:noteMetric:edit' AS perms, 2.00 AS sort_no, '数据回收编辑按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000d' AS parent_id, '删除' AS name, 'redbook:noteMetric:delete' AS perms, 3.00 AS sort_no, '数据回收删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000d' AS parent_id, '批量删除' AS name, 'redbook:noteMetric:deleteBatch' AS perms, 4.00 AS sort_no, '数据回收批量删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000d' AS parent_id, '导出' AS name, 'redbook:noteMetric:exportXls' AS perms, 5.00 AS sort_no, '数据回收导出按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000d' AS parent_id, '导入' AS name, 'redbook:noteMetric:importExcel' AS perms, 6.00 AS sort_no, '数据回收导入按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000d' AS parent_id, '完整性检查' AS name, 'redbook:noteMetric:completeness' AS perms, 7.00 AS sort_no, '数据回收完整性检查按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000e' AS parent_id, '新增' AS name, 'redbook:reviewReport:add' AS perms, 1.00 AS sort_no, '复盘报告新增按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000e' AS parent_id, '编辑' AS name, 'redbook:reviewReport:edit' AS perms, 2.00 AS sort_no, '复盘报告编辑按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000e' AS parent_id, '删除' AS name, 'redbook:reviewReport:delete' AS perms, 3.00 AS sort_no, '复盘报告删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000e' AS parent_id, '批量删除' AS name, 'redbook:reviewReport:deleteBatch' AS perms, 4.00 AS sort_no, '复盘报告批量删除按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000e' AS parent_id, '导出' AS name, 'redbook:reviewReport:exportXls' AS perms, 5.00 AS sort_no, '复盘报告导出按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000e' AS parent_id, '导入' AS name, 'redbook:reviewReport:importExcel' AS perms, 6.00 AS sort_no, '复盘报告导入按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000e' AS parent_id, '生成复盘' AS name, 'redbook:reviewReport:generate' AS perms, 7.00 AS sort_no, '复盘报告生成按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000e' AS parent_id, '范围生成' AS name, 'redbook:reviewReport:generateScoped' AS perms, 8.00 AS sort_no, '复盘报告按范围生成按钮权限' AS description
UNION ALL
SELECT '9b0b9b0b00000000000000000000000e' AS parent_id, '回流热点池' AS name, 'redbook:reviewReport:createHotspots' AS perms, 9.00 AS sort_no, '复盘报告回流热点池按钮权限' AS description
) t
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.id = MD5(CONCAT('redbook-perm:', t.perms))
);

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT
  LOWER(CONCAT('9b', SUBSTRING(MD5(CONCAT(r.role_code, '-', t.permission_id)), 1, 30))),
  r.id,
  t.permission_id,
  NULL,
  NOW(),
  '127.0.0.1'
FROM `sys_role` r
JOIN (
SELECT MD5(CONCAT('redbook-perm:', 'redbook:workbench:overview')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:workbench:reviewDashboard')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:account:add')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:account:edit')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:account:delete')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:account:deleteBatch')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:account:exportXls')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:account:importExcel')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:track:add')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:track:edit')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:track:delete')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:track:deleteBatch')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:track:exportXls')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:track:importExcel')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:persona:add')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:persona:edit')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:persona:delete')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:persona:deleteBatch')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:persona:exportXls')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:persona:importExcel')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:product:add')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:product:edit')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:product:delete')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:product:deleteBatch')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:product:exportXls')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:product:importExcel')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:promptTemplate:add')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:promptTemplate:edit')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:promptTemplate:delete')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:promptTemplate:deleteBatch')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:promptTemplate:exportXls')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:promptTemplate:importExcel')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:sensitiveWord:add')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:sensitiveWord:edit')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:sensitiveWord:delete')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:sensitiveWord:deleteBatch')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:sensitiveWord:exportXls')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:sensitiveWord:importExcel')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:hotspot:add')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:hotspot:edit')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:hotspot:delete')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:hotspot:deleteBatch')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:hotspot:exportXls')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:hotspot:importExcel')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:hotspot:analyze')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:hotspotAnalysis:add')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:hotspotAnalysis:edit')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:hotspotAnalysis:delete')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:hotspotAnalysis:deleteBatch')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:hotspotAnalysis:exportXls')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:hotspotAnalysis:importExcel')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:hotspotAnalysis:generateDraft')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteDraft:add')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteDraft:edit')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteDraft:delete')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteDraft:deleteBatch')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteDraft:exportXls')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteDraft:importExcel')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteDraft:createPublishPlan')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteDraft:approve')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteDraft:reject')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteDraft:riskCheck')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteDraft:versions')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteDraft:restoreVersion')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:publishPlan:add')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:publishPlan:edit')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:publishPlan:delete')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:publishPlan:deleteBatch')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:publishPlan:exportXls')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:publishPlan:importExcel')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:publishPlan:markPublished')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:publishPlan:createMetric')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:publishPlan:delay')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:publishPlan:cancel')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:publishPlan:restorePending')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:publishPlan:updateNoteUrl')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteMetric:add')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteMetric:edit')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteMetric:delete')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteMetric:deleteBatch')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteMetric:exportXls')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteMetric:importExcel')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:noteMetric:completeness')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:reviewReport:add')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:reviewReport:edit')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:reviewReport:delete')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:reviewReport:deleteBatch')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:reviewReport:exportXls')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:reviewReport:importExcel')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:reviewReport:generate')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:reviewReport:generateScoped')) AS permission_id
UNION ALL
SELECT MD5(CONCAT('redbook-perm:', 'redbook:reviewReport:createHotspots')) AS permission_id
) t
WHERE r.role_code IN ('admin', 'vue3')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` srp WHERE srp.role_id = r.id AND srp.permission_id = t.permission_id
  );

-- 默认授权给管理员和 Vue3 角色，避免菜单入库后仍看不到入口。
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT LOWER(CONCAT('9b', SUBSTRING(MD5(CONCAT(r.role_code, '-', '9b0b9b0b000000000000000000000001')), 1, 30))), r.id, '9b0b9b0b000000000000000000000001', NULL, NOW(), '127.0.0.1'
FROM `sys_role` r
WHERE r.role_code IN ('admin', 'vue3')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` srp WHERE srp.role_id = r.id AND srp.permission_id = '9b0b9b0b000000000000000000000001'
  );
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT LOWER(CONCAT('9b', SUBSTRING(MD5(CONCAT(r.role_code, '-', '9b0b9b0b000000000000000000000002')), 1, 30))), r.id, '9b0b9b0b000000000000000000000002', NULL, NOW(), '127.0.0.1'
FROM `sys_role` r
WHERE r.role_code IN ('admin', 'vue3')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` srp WHERE srp.role_id = r.id AND srp.permission_id = '9b0b9b0b000000000000000000000002'
  );
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT LOWER(CONCAT('9b', SUBSTRING(MD5(CONCAT(r.role_code, '-', '9b0b9b0b000000000000000000000003')), 1, 30))), r.id, '9b0b9b0b000000000000000000000003', NULL, NOW(), '127.0.0.1'
FROM `sys_role` r
WHERE r.role_code IN ('admin', 'vue3')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` srp WHERE srp.role_id = r.id AND srp.permission_id = '9b0b9b0b000000000000000000000003'
  );
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT LOWER(CONCAT('9b', SUBSTRING(MD5(CONCAT(r.role_code, '-', '9b0b9b0b000000000000000000000004')), 1, 30))), r.id, '9b0b9b0b000000000000000000000004', NULL, NOW(), '127.0.0.1'
FROM `sys_role` r
WHERE r.role_code IN ('admin', 'vue3')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` srp WHERE srp.role_id = r.id AND srp.permission_id = '9b0b9b0b000000000000000000000004'
  );
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT LOWER(CONCAT('9b', SUBSTRING(MD5(CONCAT(r.role_code, '-', '9b0b9b0b000000000000000000000005')), 1, 30))), r.id, '9b0b9b0b000000000000000000000005', NULL, NOW(), '127.0.0.1'
FROM `sys_role` r
WHERE r.role_code IN ('admin', 'vue3')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` srp WHERE srp.role_id = r.id AND srp.permission_id = '9b0b9b0b000000000000000000000005'
  );
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT LOWER(CONCAT('9b', SUBSTRING(MD5(CONCAT(r.role_code, '-', '9b0b9b0b000000000000000000000006')), 1, 30))), r.id, '9b0b9b0b000000000000000000000006', NULL, NOW(), '127.0.0.1'
FROM `sys_role` r
WHERE r.role_code IN ('admin', 'vue3')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` srp WHERE srp.role_id = r.id AND srp.permission_id = '9b0b9b0b000000000000000000000006'
  );
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT LOWER(CONCAT('9b', SUBSTRING(MD5(CONCAT(r.role_code, '-', '9b0b9b0b000000000000000000000007')), 1, 30))), r.id, '9b0b9b0b000000000000000000000007', NULL, NOW(), '127.0.0.1'
FROM `sys_role` r
WHERE r.role_code IN ('admin', 'vue3')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` srp WHERE srp.role_id = r.id AND srp.permission_id = '9b0b9b0b000000000000000000000007'
  );
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT LOWER(CONCAT('9b', SUBSTRING(MD5(CONCAT(r.role_code, '-', '9b0b9b0b000000000000000000000008')), 1, 30))), r.id, '9b0b9b0b000000000000000000000008', NULL, NOW(), '127.0.0.1'
FROM `sys_role` r
WHERE r.role_code IN ('admin', 'vue3')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` srp WHERE srp.role_id = r.id AND srp.permission_id = '9b0b9b0b000000000000000000000008'
  );
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT LOWER(CONCAT('9b', SUBSTRING(MD5(CONCAT(r.role_code, '-', '9b0b9b0b000000000000000000000009')), 1, 30))), r.id, '9b0b9b0b000000000000000000000009', NULL, NOW(), '127.0.0.1'
FROM `sys_role` r
WHERE r.role_code IN ('admin', 'vue3')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` srp WHERE srp.role_id = r.id AND srp.permission_id = '9b0b9b0b000000000000000000000009'
  );
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT LOWER(CONCAT('9b', SUBSTRING(MD5(CONCAT(r.role_code, '-', '9b0b9b0b00000000000000000000000a')), 1, 30))), r.id, '9b0b9b0b00000000000000000000000a', NULL, NOW(), '127.0.0.1'
FROM `sys_role` r
WHERE r.role_code IN ('admin', 'vue3')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` srp WHERE srp.role_id = r.id AND srp.permission_id = '9b0b9b0b00000000000000000000000a'
  );
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT LOWER(CONCAT('9b', SUBSTRING(MD5(CONCAT(r.role_code, '-', '9b0b9b0b00000000000000000000000b')), 1, 30))), r.id, '9b0b9b0b00000000000000000000000b', NULL, NOW(), '127.0.0.1'
FROM `sys_role` r
WHERE r.role_code IN ('admin', 'vue3')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` srp WHERE srp.role_id = r.id AND srp.permission_id = '9b0b9b0b00000000000000000000000b'
  );
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT LOWER(CONCAT('9b', SUBSTRING(MD5(CONCAT(r.role_code, '-', '9b0b9b0b00000000000000000000000c')), 1, 30))), r.id, '9b0b9b0b00000000000000000000000c', NULL, NOW(), '127.0.0.1'
FROM `sys_role` r
WHERE r.role_code IN ('admin', 'vue3')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` srp WHERE srp.role_id = r.id AND srp.permission_id = '9b0b9b0b00000000000000000000000c'
  );
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT LOWER(CONCAT('9b', SUBSTRING(MD5(CONCAT(r.role_code, '-', '9b0b9b0b00000000000000000000000d')), 1, 30))), r.id, '9b0b9b0b00000000000000000000000d', NULL, NOW(), '127.0.0.1'
FROM `sys_role` r
WHERE r.role_code IN ('admin', 'vue3')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` srp WHERE srp.role_id = r.id AND srp.permission_id = '9b0b9b0b00000000000000000000000d'
  );
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT LOWER(CONCAT('9b', SUBSTRING(MD5(CONCAT(r.role_code, '-', '9b0b9b0b00000000000000000000000e')), 1, 30))), r.id, '9b0b9b0b00000000000000000000000e', NULL, NOW(), '127.0.0.1'
FROM `sys_role` r
WHERE r.role_code IN ('admin', 'vue3')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` srp WHERE srp.role_id = r.id AND srp.permission_id = '9b0b9b0b00000000000000000000000e'
  );
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT LOWER(CONCAT('9b', SUBSTRING(MD5(CONCAT(r.role_code, '-', '9b0b9b0b00000000000000000000000f')), 1, 30))), r.id, '9b0b9b0b00000000000000000000000f', NULL, NOW(), '127.0.0.1'
FROM `sys_role` r
WHERE r.role_code IN ('admin', 'vue3')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` srp WHERE srp.role_id = r.id AND srp.permission_id = '9b0b9b0b00000000000000000000000f'
  );
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT LOWER(CONCAT('9b', SUBSTRING(MD5(CONCAT(r.role_code, '-', '9b0b9b0b000000000000000000000010')), 1, 30))), r.id, '9b0b9b0b000000000000000000000010', NULL, NOW(), '127.0.0.1'
FROM `sys_role` r
WHERE r.role_code IN ('admin', 'vue3')
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` srp WHERE srp.role_id = r.id AND srp.permission_id = '9b0b9b0b000000000000000000000010'
  );
