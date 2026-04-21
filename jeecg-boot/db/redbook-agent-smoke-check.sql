USE `jeecg-boot`;

-- ------------------------------------------------------------------
-- RedBook Agent V1 只读冒烟检查
-- 用途：
-- 1. 检查表结构、权限、字典是否初始化
-- 2. 检查样例数据是否导入
-- 3. 检查闭环状态分布是否基本符合预期
-- ------------------------------------------------------------------

SELECT 'table_check' AS check_type, table_name AS item, COUNT(*) AS total
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
    'rb_track',
    'rb_account',
    'rb_hotspot',
    'rb_hotspot_analysis',
    'rb_note_draft',
    'rb_note_draft_version',
    'rb_publish_plan',
    'rb_note_metric',
    'rb_review_report'
  )
GROUP BY table_name
ORDER BY table_name;

SELECT 'permission_check' AS check_type, 'redbook_permission_count' AS item, COUNT(*) AS total
FROM sys_permission
WHERE perms LIKE 'redbook:%';

SELECT 'dict_check' AS check_type, dict_code AS item, COUNT(*) AS total
FROM sys_dict
WHERE dict_code IN (
  'rb_common_status',
  'rb_hotspot_status',
  'rb_analysis_status',
  'rb_draft_status',
  'rb_audit_status',
  'rb_publish_status',
  'rb_review_status',
  'rb_risk_level',
  'rb_collect_node',
  'rb_content_type'
)
GROUP BY dict_code
ORDER BY dict_code;

SELECT 'demo_count' AS check_type, 'track' AS item, COUNT(*) AS total
FROM rb_track WHERE id LIKE 'rb_demo_%'
UNION ALL
SELECT 'demo_count', 'account', COUNT(*) FROM rb_account WHERE id LIKE 'rb_demo_%'
UNION ALL
SELECT 'demo_count', 'hotspot', COUNT(*) FROM rb_hotspot WHERE id LIKE 'rb_demo_%'
UNION ALL
SELECT 'demo_count', 'analysis', COUNT(*) FROM rb_hotspot_analysis WHERE id LIKE 'rb_demo_%'
UNION ALL
SELECT 'demo_count', 'draft', COUNT(*) FROM rb_note_draft WHERE id LIKE 'rb_demo_%'
UNION ALL
SELECT 'demo_count', 'publish_plan', COUNT(*) FROM rb_publish_plan WHERE id LIKE 'rb_demo_%'
UNION ALL
SELECT 'demo_count', 'note_metric', COUNT(*) FROM rb_note_metric WHERE id LIKE 'rb_demo_%'
UNION ALL
SELECT 'demo_count', 'review_report', COUNT(*) FROM rb_review_report WHERE id LIKE 'rb_demo_%';

SELECT 'hotspot_status' AS check_type, status AS item, COUNT(*) AS total
FROM rb_hotspot
WHERE id LIKE 'rb_demo_%'
GROUP BY status
ORDER BY status;

SELECT 'draft_status' AS check_type, CONCAT(status, '/', audit_status) AS item, COUNT(*) AS total
FROM rb_note_draft
WHERE id LIKE 'rb_demo_%'
GROUP BY status, audit_status
ORDER BY status, audit_status;

SELECT 'publish_status' AS check_type, publish_status AS item, COUNT(*) AS total
FROM rb_publish_plan
WHERE id LIKE 'rb_demo_%'
GROUP BY publish_status
ORDER BY publish_status;
