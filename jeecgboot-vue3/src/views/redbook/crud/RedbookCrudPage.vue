<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">新增</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls">导出</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item v-if="config.key === 'hotspot'" key="discard" @click="batchDiscardHotspots">
                <Icon icon="ant-design:stop-outlined" />
                批量标废弃
              </a-menu-item>
              <a-menu-item key="delete" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined" />
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            批量操作
            <Icon icon="ant-design:down-outlined" />
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" :dropDownActions="getDropDownActions(record)" />
      </template>
    </BasicTable>
    <RedbookCrudModal :config="config" :moduleApi="moduleApi" @register="registerModal" @success="reload" />
    <RedbookDetailDrawer :config="config" :moduleApi="moduleApi" @register="registerDetailDrawer" @refresh="reload" @audit="handleAuditFromDrawer" />
    <RedbookAuditModal @register="registerAuditModal" @success="handleAuditSuccess" />
  </div>
</template>

<script lang="ts" setup>
  import { onMounted } from 'vue';
  import { useRoute } from 'vue-router';
  import { useDrawer } from '/@/components/Drawer';
  import type { ActionItem } from '/@/components/Table';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { Icon } from '/@/components/Icon';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { useMessage } from '/@/hooks/web/useMessage';
  import RedbookAuditModal from './RedbookAuditModal.vue';
  import RedbookDetailDrawer from './RedbookDetailDrawer.vue';
  import RedbookCrudModal from './RedbookCrudModal.vue';
  import { buildRedbookApi } from './redbook.api';
  import { getRedbookModuleConfig, resolveRedbookModuleKey } from './redbook.config';
  import { ensureReferences } from './redbook.shared';

  const route = useRoute();
  const config = getRedbookModuleConfig(resolveRedbookModuleKey(route.path, String(route.meta.moduleKey || '')));
  const moduleApi = buildRedbookApi(config.apiBase);
  const [registerModal, { openModal }] = useModal();
  const [registerAuditModal, { openModal: openAuditModal }] = useModal();
  const [registerDetailDrawer, { openDrawer: openDetailDrawer }] = useDrawer();
  const { createMessage } = useMessage();

  const { onExportXls, onImportXls, tableContext } = useListPage({
    designScope: `redbook-${config.key}`,
    tableProps: {
      title: config.title,
      api: moduleApi.list,
      columns: config.columns,
      formConfig: {
        schemas: config.searchFormSchema,
      },
      actionColumn: {
        width: config.actionColumnWidth || 160,
      },
      showIndexColumn: true,
      canResize: true,
      defSort: {
        column: '',
        order: '',
      },
    },
    exportConfig: {
      name: config.title,
      url: moduleApi.getExportUrl,
    },
    importConfig: {
      url: moduleApi.getImportUrl,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  onMounted(() => {
    ensureReferences(config.referenceKinds || []);
  });

  function getActions(record): ActionItem[] {
    const actions: ActionItem[] = [
      {
        label: '详情',
        onClick: handleView.bind(null, record),
      },
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
      },
    ];
    const customActions = getVisibleCustomActions(record);
    if (customActions.length > 0) {
      actions.push(createActionItem(customActions[0], record));
    }
    return actions;
  }

  function getDropDownActions(record): ActionItem[] {
    const actions: ActionItem[] = [];
    if (config.key === 'noteDraft' && record.status !== 'published') {
      actions.push(
        {
          label: '审核通过',
          onClick: handleAudit.bind(null, record, 'approve'),
        },
        {
          label: '退回修改',
          onClick: handleAudit.bind(null, record, 'reject'),
        }
      );
    }
    actions.push(
      ...getVisibleCustomActions(record).slice(1).map((item) => createActionItem(item, record)),
      {
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
        },
      }
    );
    return actions;
  }

  function getVisibleCustomActions(record) {
    return (config.rowActions || []).filter((item) => (item.ifShow ? item.ifShow(record) : true));
  }

  function createActionItem(action, record): ActionItem {
    if (action.confirmTitle) {
      return {
        label: action.label,
        popConfirm: {
          title: action.confirmTitle,
          confirm: handleCustomAction.bind(null, action, record),
        },
      };
    }
    return {
      label: action.label,
      onClick: handleCustomAction.bind(null, action, record),
    };
  }

  function handleAdd() {
    openModal(true, { isUpdate: false });
  }

  function handleEdit(record) {
    openModal(true, { record, isUpdate: true });
  }

  function handleView(record) {
    openDetailDrawer(true, { record });
  }

  function handleAudit(record, action: 'approve' | 'reject') {
    openAuditModal(true, { record, action });
  }

  function handleAuditFromDrawer(payload: { action: 'approve' | 'reject'; record: Recordable }) {
    handleAudit(payload.record, payload.action);
  }

  function handleAuditSuccess() {
    createMessage.success('草稿审核状态已更新');
    reload();
  }

  async function handleDelete(record) {
    await moduleApi.deleteRecord({ id: record.id }, reload);
  }

  async function handleCustomAction(action, record) {
    if (action.actionType === 'copy') {
      const text = action.copyText?.(record) || '';
      if (!text) {
        createMessage.warning('当前记录暂无可复制内容');
        return;
      }
      const copied = copyTextToClipboard(text);
      if (copied) {
        createMessage.success(action.successMessage);
      } else {
        createMessage.error('复制失败，请手动复制');
      }
      return;
    }
    await moduleApi.action(action.action, { id: record.id });
    createMessage.success(action.successMessage);
    reload();
  }

  async function batchDiscardHotspots() {
    const ids = [...selectedRowKeys.value];
    for (const id of ids) {
      await moduleApi.update({ id, status: 'discarded' });
    }
    createMessage.success(`已标记 ${ids.length} 条热点为已废弃`);
    selectedRowKeys.value = [];
    reload();
  }

  async function batchHandleDelete() {
    await moduleApi.batchDelete({ ids: selectedRowKeys.value }, () => {
      selectedRowKeys.value = [];
      reload();
    });
  }
</script>
