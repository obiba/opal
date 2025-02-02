<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="dsName" :to="`/project/${dsName}`" />
        <q-breadcrumbs-el :label="t('tables')" :to="`/project/${dsName}/tables`" />
        <q-breadcrumbs-el :label="tName" />
      </q-breadcrumbs>
      <q-icon name="circle" :color="tableStatusColor(datasourceStore.table.status)" size="sm" class="on-right" />
      <bookmark-icon :resource="`/datasource/${dsName}/table/${tName}`" />
      <q-space />
      <q-btn
        outline
        no-caps
        icon="navigate_before"
        size="sm"
        :label="previousTable?.name"
        :to="`/project/${dsName}/table/${previousTable?.name}`"
        v-if="previousTable"
        class="on-right"
      />
      <q-btn
        outline
        no-caps
        icon-right="navigate_next"
        size="sm"
        :label="nextTable?.name"
        :to="`/project/${dsName}/table/${nextTable?.name}`"
        v-if="nextTable"
        class="on-right"
      />
    </q-toolbar>
    <q-page class="q-pa-md" v-show="datasourceStore.table.name">
      <div class="text-h5">
        <q-icon name="table_chart" size="sm" class="q-mb-xs"></q-icon
        ><span class="on-right">{{ datasourceStore.table.name }}</span>
        <q-btn-dropdown outline color="primary" icon="download" size="sm" :title="t('download')" class="on-right">
          <q-list>
            <q-item clickable v-close-popup @click="onDownloadDictionary">
              <q-item-section>
                <q-item-label>{{ t('download_dictionary') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item v-if="isView" clickable v-close-popup @click="onDownloadView">
              <q-item-section>
                <q-item-label>{{ t('download_view') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>
        <q-btn-dropdown
          v-if="datasourceStore.tables.length && projectsStore.perms.export?.canCreate()"
          color="secondary"
          icon="output"
          size="sm"
          :label="t('export')"
          class="on-right"
        >
          <q-list>
            <q-item clickable v-close-popup @click="onShowExportFile">
              <q-item-section>
                <q-item-label>{{ t('export_file') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item clickable v-close-popup @click="onShowExportDatabase">
              <q-item-section>
                <q-item-label>{{ t('export_database') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>
        <q-btn
          v-if="!isView && projectsStore.perms.copy?.canCreate()"
          color="secondary"
          icon="content_copy"
          :title="t('copy')"
          size="sm"
          @click="onShowCopyData"
          class="on-right"
        ></q-btn>
        <q-btn-dropdown
          v-if="isView"
          color="secondary"
          icon="content_copy"
          size="sm"
          :title="t('copy')"
          class="on-right"
        >
          <q-list>
            <q-item clickable v-close-popup @click="onShowCopyData">
              <q-item-section>
                <q-item-label>{{ t('copy_data') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item v-if="isView" clickable v-close-popup @click="onShowCopyView">
              <q-item-section>
                <q-item-label>{{ t('copy_view') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>

        <q-btn
          v-if="datasourceStore.perms.table?.canUpdate()"
          outline
          color="secondary"
          icon="edit"
          size="sm"
          @click="onShowEdit"
          class="on-right"
        ></q-btn>
        <q-btn
          v-if="datasourceStore.perms.table?.canDelete()"
          outline
          color="red"
          icon="delete"
          size="sm"
          @click="onShowDelete"
          class="on-right"
        ></q-btn>
        <q-btn
          v-if="isView && datasourceStore.perms.table?.canUpdate()"
          no-caps
          dense
          flat
          :label="t('reconnect_view')"
          icon="settings_ethernet"
          size="sm"
          @click="onReconnect"
          class="on-right"
        />
      </div>
      <div class="row q-col-gutter-md q-mt-md q-mb-md">
        <div class="col-12 col-md-6">
          <fields-list :items="items1" :dbobject="datasourceStore.table" />
        </div>
        <div class="col-12 col-md-6">
          <fields-list :items="items2" :dbobject="datasourceStore.table" />
        </div>
      </div>

      <div v-if="loading">
        <q-spinner-dots size="lg" class="q-mt-md" />
      </div>
      <div v-else>
        <q-tabs v-model="tab" dense class="text-grey" active-color="primary" indicator-color="primary" align="justify">
          <q-tab name="dictionary" :label="t('dictionary')" />
          <q-tab name="summary" :label="t('summary')" />
          <q-tab
            name="entity_filter"
            :label="t('entity_filter')"
            v-if="isTablesView && datasourceStore.perms.tableValueSets?.canRead()"
          />
          <q-tab name="values" :label="t('values')" v-if="datasourceStore.perms.tableValueSets?.canRead()" />
          <q-tab name="analyses" :label="t('analyses')" v-if="canAnalyseValidate" />
          <q-tab
            name="permissions"
            :label="t('permissions')"
            v-if="datasourceStore.perms.tablePermissions?.canRead()"
          />
        </q-tabs>

        <q-separator />

        <q-tab-panels v-model="tab">
          <q-tab-panel name="dictionary">
            <table-variables />
          </q-tab-panel>

          <q-tab-panel name="entity_filter" v-if="isTablesView">
            <view-where-script :view="datasourceStore.view" />
          </q-tab-panel>

          <q-tab-panel name="summary">
            <div v-if="datasourceStore.perms.tableValueSets?.canRead()" class="q-mb-md">
              <table-indexer />
            </div>
            <div class="row q-gutter-md">
              <q-card flat bordered class="on-left q-mb-md o-card-md">
                <q-card-section class="text-subtitle2 text-center bg-grey-2">
                  <div><q-icon name="view_column" class="on-left" />{{ t('variables') }}</div>
                </q-card-section>
                <q-separator />
                <q-card-section>
                  <div class="text-h6 text-center">
                    {{ datasourceStore.table.variableCount }}
                  </div>
                </q-card-section>
              </q-card>
              <q-card flat bordered class="on-left q-mb-md o-card-md">
                <q-card-section class="text-subtitle2 text-center bg-grey-2">
                  <div><q-icon name="table_rows" class="on-left" />{{ t('entities') }}</div>
                </q-card-section>
                <q-separator />
                <q-card-section>
                  <div class="text-h6 text-center">
                    {{ datasourceStore.table.valueSetCount }}
                  </div>
                </q-card-section>
              </q-card>
            </div>
            <div v-if="datasourceStore.perms.tableValueSets?.canRead()">
              <div v-if="datasourceStore.table.valueSetCount === 0">
                <div class="q-mb-md box-info">
                  <q-icon name="error" size="1.2rem" />
                  <span class="on-right">
                    {{ t('no_table_values') }}
                  </span>
                </div>
              </div>
              <div v-else>
                <contingency-table />
              </div>
            </div>
          </q-tab-panel>

          <q-tab-panel name="values" v-if="datasourceStore.perms.tableValueSets?.canRead()">
            <div v-if="datasourceStore.table.valueSetCount === 0">
              <div class="q-mb-md box-info">
                <q-icon name="error" size="1.2rem" />
                <span class="on-right">
                  {{ t('no_table_values') }}
                </span>
              </div>
            </div>
            <div v-else>
              <table-values />
            </div>
          </q-tab-panel>

          <q-tab-panel name="analyses" v-if="canAnalyseValidate">
            <project-anaylse-validate
              :project-name="dsName"
              :table-name="tName"
            />
          </q-tab-panel>

          <q-tab-panel name="permissions" v-if="datasourceStore.perms.tablePermissions?.canRead()">
            <access-control-list
              :resource="`/project/${dsName}/permissions/table/${tName}`"
              :options="['TABLE_READ', 'TABLE_VALUES', 'TABLE_EDIT', 'TABLE_VALUES_EDIT', 'TABLE_ALL']"
            />
          </q-tab-panel>
        </q-tab-panels>
      </div>

      <export-data-dialog v-model="showExport" :type="exportType" :tables="[datasourceStore.table]" />
      <copy-tables-dialog v-model="showCopyData" :tables="[datasourceStore.table]" />
      <copy-view-dialog v-model="showCopyView" :table="datasourceStore.table" :view="datasourceStore.view" />
      <edit-table-dialog
        v-model="showEdit"
        :table="datasourceStore.table"
        :view="datasourceStore.view"
        @update:table="onTableUpdate"
        @update:view="onViewUpdate"
      />
      <resource-view-dialog
        v-if="isResourceView"
        v-model="showEditResourceView"
        :view="datasourceStore.view"
        @update="onViewUpdate"
      />
      <confirm-dialog
        v-model="showDelete"
        :title="t('delete')"
        :text="t('delete_tables_confirm', { count: 1 })"
        @confirm="onDeleteTable"
      />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import BookmarkIcon from 'src/components/BookmarkIcon.vue';
import TableVariables from 'src/components/datasource/TableVariables.vue';
import TableValues from 'src/components/datasource/TableValues.vue';
import AccessControlList from 'src/components/permissions/AccessControlList.vue';
import FieldsList from 'src/components/FieldsList.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import ExportDataDialog from 'src/components/datasource/export/ExportDataDialog.vue';
import CopyTablesDialog from 'src/components/datasource/CopyTablesDialog.vue';
import CopyViewDialog from 'src/components/datasource/CopyViewDialog.vue';
import EditTableDialog from 'src/components/datasource/EditTableDialog.vue';
import ResourceViewDialog from 'src/components/resources/ResourceViewDialog.vue';
import TableIndexer from 'src/components/datasource/TableIndexer.vue';
import ContingencyTable from 'src/components/datasource/ContingencyTable.vue';
import ViewWhereScript from 'src/components/datasource/ViewWhereScript.vue';
import ProjectAnaylseValidate from 'src/components/project/ProjectAnaylseValidate.vue';
import type { TableDto, ViewDto } from 'src/models/Magma';
import { tableStatusColor } from 'src/utils/colors';
import { getDateLabel } from 'src/utils/dates';
import { notifyError } from 'src/utils/notify';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const projectsStore = useProjectsStore();
const datasourceStore = useDatasourceStore();
const pluginsStore = usePluginsStore();

const tab = ref('dictionary');
const showDelete = ref(false);
const showCopyData = ref(false);
const showCopyView = ref(false);
const showEdit = ref(false);
const showEditResourceView = ref(false);
const loading = ref(false);
const showExport = ref(false);
const exportType = ref<'file' | 'server' | 'database'>('file');

const isView = computed(() => datasourceStore.table.viewType !== undefined);
const isTablesView = computed(() => datasourceStore.table.viewType === 'View');
const isResourceView = computed(() => datasourceStore.table.viewType === 'ResourceView');
const canAnalyseValidate = computed(() => projectsStore.perms.analyses?.canRead());

const previousTable = computed(() => {
  const idx = datasourceStore.tables.findIndex((t) => t.name === tName.value);
  return idx > 0 ? datasourceStore.tables[idx - 1] : null;
});

const nextTable = computed(() => {
  const idx = datasourceStore.tables.findIndex((t) => t.name === tName.value);
  return idx === datasourceStore.tables.length - 1 ? null : datasourceStore.tables[idx + 1];
});

const items1 = computed(() => {
  return [
    {
      field: 'name',
    },
    {
      field: 'name',
      label: 'full_name',
      html: (val: TableDto) => (val ? `<code>${val.datasourceName}.${val.name}</code>` : ''),
    },
    {
      field: 'entityType',
      label: 'entity_type',
    },
    {
      field: 'from',
      label: isTablesView.value ? 'table_references' : 'resource_ref.label',
      links: (val: string) =>
        val
          ? datasourceStore.view.from?.map((f) => {
              return {
                label: f,
                to: `/project/${f.split('.')[0]}/${isTablesView.value ? 'table' : 'resource'}/${f.split('.')[1]}`,
                icon: datasourceStore.view.innerFrom?.includes(f) ? 'panorama_fish_eye' : 'circle',
              };
            })
          : [],
      visible: () => isView.value,
    },
    {
      field: 'idColumn',
      label: 'resource_ref.id_column',
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      format: () => (datasourceStore.view as any)['Magma.ResourceViewDto.view']?.idColumn || '',
      visible: () => isResourceView.value,
    },
  ];
});

const items2 = [
  {
    field: 'timestamps',
    label: 'created',
    format: (val: TableDto) => (val ? getDateLabel(val.timestamps?.created) : ''),
  },
  {
    field: 'timestamps',
    label: 'last_update',
    format: (val: TableDto) => (val ? getDateLabel(val.timestamps?.lastUpdate) : ''),
  },
];

const dsName = computed(() => route.params.id as string);
const tName = computed(() => route.params.tid as string);

watch([dsName, tName], () => {
  init();
});

function init() {
  datasourceStore
    .initDatasourceTable(dsName.value, tName.value)
    .then(() => {
      if (!datasourceStore.perms.tableValueSets?.canRead() && ['entity_filter', 'values'].includes(tab.value)) {
        tab.value = 'dictionary';
      }
      if (!isTablesView.value && tab.value === 'entity_filter') {
        tab.value = 'dictionary';
      }
    })
    .catch((err) => {
      notifyError(err);
      if (err.response?.status === 404) router.push(`/project/${dsName.value}/tables`);
    });
}

function onDownloadDictionary() {
  datasourceStore.downloadTableDictionary();
}

function onDownloadView() {
  datasourceStore.downloadView();
}

function onShowCopyData() {
  showCopyData.value = true;
}

function onShowCopyView() {
  showCopyView.value = true;
}

function onShowEdit() {
  if (isResourceView.value) {
    showEditResourceView.value = true;
  } else {
    datasourceStore.initDatasourceTables(dsName.value).then(() => (showEdit.value = true));
  }
}

function onShowDelete() {
  showDelete.value = true;
}

function onDeleteTable() {
  datasourceStore.deleteTable(tName.value).then(() => router.push(`/project/${dsName.value}/tables`));
}

function onTableUpdate(updated: TableDto) {
  router.push(`/project/${dsName.value}/table/${updated.name}`);
}

function onViewUpdate(updated: ViewDto) {
  if (updated.name === tName.value) {
    datasourceStore.view = updated;
    datasourceStore.loadTable(tName.value);
  } else {
    router.push(`/project/${dsName.value}/table/${updated.name}`);
  }
}

function onReconnect() {
  loading.value = true;
  datasourceStore.reconnectView(dsName.value, tName.value).finally(() => {
    datasourceStore.loadTable(tName.value).finally(() => (loading.value = false));
  });
}

function onShowExportFile() {
  exportType.value = 'file';
  showExport.value = true;
}

function onShowExportDatabase() {
  exportType.value = 'database';
  showExport.value = true;
}

onMounted(() => {
  pluginsStore.initAnalysisPlugins().then(() => {
    if (pluginsStore.analysisPlugins.packages) {
      projectsStore.loadAnalysesPermissions(dsName.value, tName.value);
    }
  });
});
</script>
