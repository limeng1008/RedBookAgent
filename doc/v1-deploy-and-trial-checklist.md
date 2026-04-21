# RedBookAgent V1 部署与试运营检查表

更新时间：2026-04-21  
负责人：A（后端 / 数据 / AI 闭环）

## 1. 适用范围

本文用于 RedBookAgent V1 的本地联调、测试环境部署和试运营前检查。

V1 目标是跑通半自动运营闭环，不包含自动爬取、自动发布、模拟登录和绕过平台风控能力。

## 2. 环境准备

### 2.1 基础依赖

- JDK 8 或项目当前 JeecgBoot 兼容版本
- Maven 3.6+
- MySQL 5.7+ / 8.x
- Node.js 18+ 与 `pnpm`

### 2.2 项目目录

```text
RedBoolAgent
├── jeecg-boot
├── jeecgboot-vue3
├── jeecg-boot/db
└── doc
```

### 2.3 关键配置文件

- 后端开发配置：`jeecg-boot/jeecg-module-system/jeecg-system-start/src/main/resources/application-dev.yml`
- 业务建表脚本：`jeecg-boot/db/redbook-agent-schema.sql`
- 演示数据脚本：`jeecg-boot/db/redbook-agent-demo-data.sql`

## 3. 数据库初始化

### 3.1 首次初始化顺序

先导入 JeecgBoot 基础库：

```bash
mysql -uroot -proot < jeecg-boot/db/jeecgboot-mysql-5.7.sql
```

再导入 RedBookAgent 业务表、字典、菜单和按钮权限：

```bash
mysql -uroot -proot < jeecg-boot/db/redbook-agent-schema.sql
```

如需演示完整闭环，再导入样例数据：

```bash
mysql -uroot -proot < jeecg-boot/db/redbook-agent-demo-data.sql
```

导入后建议执行一次只读冒烟检查：

```bash
mysql -uroot -proot < jeecg-boot/db/redbook-agent-smoke-check.sql
```

### 3.2 初始化结果检查

至少确认以下结果：

- 左侧菜单已出现“小红书运营”
- `sys_permission` 中已存在 `redbook:%` 权限
- `rb_note_draft_version` 表已创建
- `rb_*` 状态字典已初始化
- 导入样例数据后，演示数据数量满足：
  - 4 个赛道
  - 4 个账号
  - 50 条热点
  - 30 条分析
  - 20 条草稿
  - 12 条发布计划
  - 24 条数据回收
  - 3 条复盘报告

## 4. AI 配置

### 4.1 配置项

`application-dev.yml` 当前支持：

```yaml
redbook:
  ai:
    enabled: false
    provider: local
    base-url:
    api-key:
    timeout-seconds: 60
    retry-times: 1
    user-prefix: redbook-agent
```

### 4.2 推荐使用方式

- 本地联调：`enabled: false` 或 `provider: local`
- 接 Dify：`provider: dify`
- 接 FastGPT：`provider: fastgpt`

### 4.3 上线前检查

- `base-url` 和 `api-key` 已配置
- 提示词模板表 `rb_prompt_template` 已维护 `template_code`
- 远程 AI 输出严格返回 JSON
- 输出字段与当前后端解析字段一致
- AI 不可用时，本地降级流程仍可跑通主链路

## 5. 部署建议

### 5.1 测试环境建议

- 单独数据库实例或独立库名
- 使用独立 `application-test.yml` / 环境变量覆盖敏感信息
- 演示环境优先导入样例数据，方便走全链路

### 5.2 生产前建议

- 不使用 `root/root` 作为数据库账号
- 为数据库开启定期备份
- AI 密钥通过环境变量或密文配置管理
- 日志目录与上传目录单独挂载
- 前后端版本一起发版，避免菜单和接口不一致

## 6. 备份与回滚

### 6.1 备份建议

每次升级前至少备份：

- `jeecg-boot` 数据库
- `application-*.yml` 配置文件
- 自定义提示词模板与字典数据

示例：

```bash
mysqldump -uroot -proot --databases jeecg-boot > backup-jeecg-boot.sql
```

### 6.2 回滚建议

如果升级后出现菜单异常、字典缺失或业务表结构不一致：

1. 停止当前服务
2. 恢复数据库备份
3. 回退后端代码到上一个可运行版本
4. 重新启动前后端并验证菜单、登录和 RedBook 页面

## 7. 试运营前检查

### 7.1 配置检查

- 账号、赛道、人设、产品、提示词、敏感词已配置
- 状态字典和按钮权限已可用
- 至少有 1 个可用账号和 1 个可用赛道

### 7.2 功能检查

按以下顺序走一遍：

1. 新增或导入热点
2. 执行热点分析
3. 从分析生成草稿
4. 审核通过或退回草稿
5. 把草稿加入发布计划
6. 标记已发布并补录笔记链接
7. 生成 2h / 24h / 72h / 7d 数据回收记录
8. 录入或导入数据回收指标
9. 生成复盘报告
10. 将下一轮建议回流热点池

### 7.3 演示检查

如果用于现场演示，建议提前准备：

- 已导入样例数据
- 至少 1 条已分析热点
- 至少 1 条已审核草稿
- 至少 1 条已发布计划
- 至少 1 组已回收数据
- 至少 1 份已生成复盘报告

## 8. 当前已知限制

- V1 不提供自动采集小红书内容能力
- V1 不提供自动发布能力
- Maven 全量编译目前仍受项目基础模块既有错误影响，若要做整仓发版，需先处理 `jeecg-boot-base-core` 的编译问题

## 9. 建议的验收口径

满足以下条件即可进入 V1 试运营：

- 菜单、权限、字典、业务表初始化成功
- 热点到复盘的主链路可在一个环境中完整跑通
- AI 可远程调用或可本地降级
- 样例数据可支撑演示
- 运营人员无需手工录入大部分关系 ID
