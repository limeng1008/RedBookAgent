<template>
  <BasicDrawer @register="registerDrawer" :title="drawerTitle" width="760">
    <div class="detail-drawer">
      <div class="detail-toolbar">
        <div class="toolbar-title">{{ detailTitle }}</div>
        <div class="toolbar-actions">
          <a-button v-for="item in actionButtons" :key="item.key" size="small" :type="item.type" @click="item.onClick">
            {{ item.label }}
          </a-button>
        </div>
      </div>

      <Description size="middle" :bordered="true" :column="3" :schema="config.detailSchema" :data="detailRecord" />

      <section v-if="config.key === 'noteDraft'" class="detail-section">
        <div class="section-head">
          <div class="section-title">版本记录</div>
          <div class="section-subtitle">新增、编辑、审核和恢复都会留下快照</div>
        </div>
        <div v-if="versions.length" class="version-list">
          <div v-for="item in versions" :key="item.id" class="version-item">
            <div class="version-main">
              <div class="version-line">
                <strong>v{{ item.versionNo }}</strong>
                <a-tag>{{ item.versionType }}</a-tag>
                <span>{{ item.createTime || '-' }}</span>
              </div>
              <div class="version-title">{{ item.title || '未命名草稿' }}</div>
              <div class="version-meta">
                <span>审核：{{ item.auditStatus || '-' }}</span>
                <span>状态：{{ item.status || '-' }}</span>
              </div>
              <div v-if="item.remark" class="version-remark">{{ item.remark }}</div>
            </div>
            <a-button size="small" @click="restoreVersion(item)" :disabled="detailRecord.status === 'published'">恢复到此版本</a-button>
          </div>
        </div>
        <a-empty v-else description="当前没有版本记录" />
      </section>
    </div>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { computed, reactive, ref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { Description } from '/@/components/Description';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { useMessage } from '/@/hooks/web/useMessage';
  import type { RedbookModuleConfig } from './redbook.config';
  import { getNoteDraftVersions, restoreNoteDraftVersion, type RedbookVersionItem } from './redbook.api';
  import { ensureReferences } from './redbook.shared';

  const props = defineProps<{
    config: RedbookModuleConfig;
    moduleApi: Recordable;
  }>();

  const emit = defineEmits<{
    (e: 'register'): void;
    (e: 'refresh'): void;
    (e: 'audit', payload: { action: 'approve' | 'reject'; record: Recordable }): void;
  }>();

  const { createMessage } = useMessage();
  const currentId = ref('');
  const detailRecord = reactive<Recordable>({});
  const versions = ref<RedbookVersionItem[]>([]);

  const [registerDrawer, { setDrawerProps }] = useDrawerInner(async (data) => {
    const id = data?.record?.id;
    currentId.value = id || '';
    setDrawerProps({ loading: true });
    try {
      await ensureReferences(props.config.referenceKinds || []);
      const detail = await props.moduleApi.queryById({ id });
      Object.keys(detailRecord).forEach((key) => delete detailRecord[key]);
      Object.assign(detailRecord, detail || {});
      if (props.config.key === 'noteDraft' && id) {
        versions.value = await getNoteDraftVersions(id);
      } else {
        versions.value = [];
      }
    } finally {
      setDrawerProps({ loading: false });
    }
  });

  const drawerTitle = computed(() => `${props.config.title}详情`);
  const detailTitle = computed(() => {
    const titleField = detailRecord.title || detailRecord.reportName || detailRecord.accountName || detailRecord.trackName;
    return titleField || currentId.value || '详情';
  });

  const actionButtons = computed(() => {
    const actions: { key: string; label: string; type?: 'primary' | 'default' | 'link'; onClick: () => void }[] = [];
    for (const item of props.config.rowActions || []) {
      if (item.ifShow && !item.ifShow(detailRecord)) {
        continue;
      }
      actions.push({
        key: item.action,
        label: item.label,
        type: item.actionType === 'copy' ? 'default' : 'primary',
        onClick: () => handleConfigAction(item),
      });
    }
    if (props.config.key === 'noteDraft' && detailRecord.id) {
      actions.unshift(
        {
          key: 'approve',
          label: '审核通过',
          type: 'primary',
          onClick: () => emit('audit', { action: 'approve', record: { ...detailRecord } }),
        },
        {
          key: 'reject',
          label: '退回修改',
          type: 'default',
          onClick: () => emit('audit', { action: 'reject', record: { ...detailRecord } }),
        }
      );
    }
    return actions;
  });

  async function handleConfigAction(action) {
    if (!detailRecord.id) {
      return;
    }
    if (action.actionType === 'copy') {
      const text = action.copyText?.(detailRecord) || '';
      if (!text) {
        createMessage.warning('当前记录暂无可复制内容');
        return;
      }
      const copied = copyTextToClipboard(text);
      copied ? createMessage.success(action.successMessage) : createMessage.error('复制失败，请手动复制');
      return;
    }
    await props.moduleApi.action(action.action, { id: detailRecord.id });
    createMessage.success(action.successMessage);
    await reloadDetail();
    emit('refresh');
  }

  async function reloadDetail() {
    if (!currentId.value) {
      return;
    }
    const detail = await props.moduleApi.queryById({ id: currentId.value });
    Object.keys(detailRecord).forEach((key) => delete detailRecord[key]);
    Object.assign(detailRecord, detail || {});
    if (props.config.key === 'noteDraft') {
      versions.value = await getNoteDraftVersions(currentId.value);
    }
  }

  async function restoreVersion(item: RedbookVersionItem) {
    await restoreNoteDraftVersion(item.id);
    createMessage.success(`已恢复到 v${item.versionNo}`);
    await reloadDetail();
    emit('refresh');
  }
</script>

<style lang="less" scoped>
  .detail-drawer {
    padding: 4px;
  }

  .detail-toolbar,
  .version-line,
  .version-meta,
  .version-item {
    display: flex;
    align-items: center;
  }

  .detail-toolbar,
  .version-item {
    justify-content: space-between;
  }

  .detail-toolbar {
    margin-bottom: 16px;
    gap: 12px;
  }

  .toolbar-title {
    font-size: 18px;
    font-weight: 700;
    color: #0f172a;
  }

  .toolbar-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .detail-section {
    margin-top: 20px;
  }

  .section-head {
    margin-bottom: 12px;
  }

  .section-title {
    font-size: 16px;
    font-weight: 700;
    color: #0f172a;
  }

  .section-subtitle,
  .version-meta,
  .version-remark {
    color: #64748b;
  }

  .section-subtitle {
    margin-top: 4px;
    font-size: 13px;
  }

  .version-list {
    display: grid;
    gap: 10px;
  }

  .version-item {
    gap: 12px;
    padding: 12px;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    background: #f8fafc;
  }

  .version-main {
    min-width: 0;
    flex: 1;
  }

  .version-line {
    gap: 8px;
    flex-wrap: wrap;
    color: #475569;
  }

  .version-title {
    margin-top: 6px;
    font-weight: 700;
    color: #0f172a;
  }

  .version-meta {
    gap: 12px;
    margin-top: 6px;
    font-size: 12px;
  }

  .version-remark {
    margin-top: 6px;
    font-size: 13px;
    line-height: 1.6;
  }
</style>
