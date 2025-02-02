<template>
  <div>
    <q-table
      ref="tableRef"
      flat
      :rows="rows"
      :columns="columns"
      row-key="name"
      :pagination="initialPagination"
      :loading="loading"
      @row-click="onRowClick"
      selection="multiple"
      v-model:selected="selected"
    >
      <template v-slot:top-left>
        <div class="row q-gutter-sm">
          <q-btn-dropdown
            v-if="datasourceStore.perms.tables?.canCreate()"
            color="primary"
            icon="add"
            :title="t('add')"
            size="sm"
          >
            <q-list>
              <q-item v-if="projectsStore.isReady" clickable v-close-popup @click="onShowAddTable">
                <q-item-section>
                  <q-item-label>{{ t('add_table') }}</q-item-label>
                </q-item-section>
              </q-item>
              <q-item clickable v-close-popup @click="onShowAddTables">
                <q-item-section>
                  <q-item-label>{{ t('add_tables') }}</q-item-label>
                </q-item-section>
              </q-item>
              <q-item clickable v-close-popup @click="onShowAddView">
                <q-item-section>
                  <q-item-label>{{ t('add_a_view') }}</q-item-label>
                </q-item-section>
              </q-item>
              <q-item clickable v-close-popup @click="onRestoreViews">
                <q-item-section>
                  <q-item-label>{{ t('restore_views') }}</q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-btn-dropdown>
          <q-btn color="secondary" icon="refresh" :title="t('refresh')" outline size="sm" @click="init" />
          <q-btn-dropdown outline color="primary" icon="download" size="sm" :title="t('download')">
            <q-list>
              <q-item clickable v-close-popup @click="onDownloadDictionary">
                <q-item-section>
                  <q-item-label>{{ t('download_dictionaries') }}</q-item-label>
                </q-item-section>
              </q-item>
              <q-item v-if="hasViews" clickable v-close-popup @click="onDownloadViews">
                <q-item-section>
                  <q-item-label>{{ t('download_views_backup') }}</q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-btn-dropdown>
          <q-btn-dropdown
            v-if="projectsStore.isReady && projectsStore.perms.import?.canCreate()"
            color="secondary"
            icon="input"
            size="sm"
            :label="t('import')"
          >
            <q-list>
              <q-item clickable v-close-popup @click="onShowImportFile">
                <q-item-section>
                  <q-item-label>{{ t('import_file') }}</q-item-label>
                </q-item-section>
              </q-item>
              <q-item clickable v-close-popup @click="onShowImportServer">
                <q-item-section>
                  <q-item-label>{{ t('import_server') }}</q-item-label>
                </q-item-section>
              </q-item>
              <q-item clickable v-close-popup @click="onShowImportDatabase">
                <q-item-section>
                  <q-item-label>{{ t('import_database') }}</q-item-label>
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
            v-if="datasourceStore.tables.length && projectsStore.perms.copy?.canCreate()"
            color="secondary"
            icon="content_copy"
            :title="t('copy')"
            size="sm"
            @click="onShowCopy"
          ></q-btn>
          <q-btn
            v-if="datasourceStore.tables.length"
            color="secondary"
            outline
            icon="search"
            :title="t('variables_search')"
            size="sm"
            @click="onSearch"
          ></q-btn>
          <q-btn
            v-if="datasourceStore.perms.tables?.canDelete()"
            :disable="removableTables.length === 0"
            outline
            color="red"
            icon="delete"
            :title="t('delete')"
            size="sm"
            @click="onShowDeleteTables"
          ></q-btn>
        </div>
      </template>
      <template v-slot:top-right>
        <q-input dense clearable debounce="400" color="primary" v-model="filter">
          <template v-slot:append>
            <q-icon name="search" />
          </template>
        </q-input>
      </template>
      <template v-slot:body-cell-name="props">
        <q-td :props="props">
          <span class="text-primary">{{ props.value }}</span>
          <q-icon v-if="props.row.viewType" name="visibility" size="xs" color="primary" class="on-right" />
        </q-td>
      </template>
      <template v-slot:body-cell-status="props">
        <q-td :props="props">
          <q-icon name="circle" size="sm" :color="tableStatusColor(props.value)" />
        </q-td>
      </template>
    </q-table>

    <add-table-dialog v-model="showAddTable" />

    <add-view-dialog v-model="showAddView" />

    <add-tables-dialog v-model="showAddTables" />

    <import-data-dialog v-model="showImport" :type="importType" />

    <export-data-dialog v-model="showExport" :type="exportType" :tables="readableTables" />

    <copy-tables-dialog v-model="showCopy" :tables="readableTables" />

    <restore-views-dialog v-model="showRestoreViews" />

    <confirm-dialog
      v-model="showDeleteTables"
      :title="t('delete')"
      :text="t('delete_tables_confirm', { count: removableTables.length || datasourceStore.tables.length })"
      @confirm="onDeleteTables"
    />
  </div>
</template>

<script setup lang="ts">
import type { TableDto } from 'src/models/Magma';
import AddTableDialog from 'src/components/datasource/AddTableDialog.vue';
import AddViewDialog from 'src/components/datasource/AddViewDialog.vue';
import AddTablesDialog from 'src/components/datasource/AddTablesDialog.vue';
import ImportDataDialog from 'src/components/datasource/import/ImportDataDialog.vue';
import ExportDataDialog from 'src/components/datasource/export/ExportDataDialog.vue';
import CopyTablesDialog from 'src/components/datasource/CopyTablesDialog.vue';
import RestoreViewsDialog from 'src/components/datasource/RestoreViewsDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { tableStatusColor } from 'src/utils/colors';
import { getDateLabel } from 'src/utils/dates';
import { DefaultAlignment } from 'src/components/models';

const route = useRoute();
const router = useRouter();
const datasourceStore = useDatasourceStore();
const projectsStore = useProjectsStore();
const searchStore = useSearchStore();
const { t } = useI18n();

const filter = ref('');
const showAddTable = ref(false);
const showAddView = ref(false);
const showAddTables = ref(false);
const showCopy = ref(false);
const showImport = ref(false);
const importType = ref<'file' | 'server' | 'database'>('file');
const showExport = ref(false);
const showRestoreViews = ref(false);
const exportType = ref<'file' | 'server' | 'database'>('file');

const tableRef = ref();
const loading = ref(false);
const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const selected = ref([] as TableDto[]);
const showDeleteTables = ref(false);

const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: DefaultAlignment,
    field: 'name',
    sortable: true,
  },
  {
    name: 'entityType',
    required: true,
    label: t('entity_type'),
    align: DefaultAlignment,
    field: 'entityType',
    sortable: true,
  },
  {
    name: 'variableCount',
    required: true,
    label: t('variables'),
    align: DefaultAlignment,
    field: 'variableCount',
    sortable: true,
  },
  {
    name: 'valueSetCount',
    required: true,
    label: t('entities'),
    align: DefaultAlignment,
    field: 'valueSetCount',
    sortable: true,
  },
  {
    name: 'lastUpdate',
    required: true,
    label: t('last_update'),
    align: DefaultAlignment,
    sortable: true,
    field: (row: TableDto) => (row.timestamps || {}).lastUpdate,
    format: (val: string) => getDateLabel(val),
  },
  {
    name: 'status',
    required: true,
    label: t('status'),
    align: DefaultAlignment,
    field: 'status',
  },
]);

onMounted(() => {
  init();
});

const rows = computed(() => {
  const f = filter.value ? filter.value.toLowerCase() : '';
  return datasourceStore.tables?.filter((t) => t.name.toLowerCase().includes(f));
});
const dsName = computed(() => route.params.id as string);
const removableTables = computed(() => (selected.value.length === 0 ? datasourceStore.tables : selected.value));
const readableTables = computed(() => (selected.value.length === 0 ? datasourceStore.tables : selected.value));

function init() {
  loading.value = true;
  datasourceStore.initDatasourceTables(dsName.value).then(() => {
    loading.value = false;
  });
}

function onRowClick(evt: unknown, row: { name: string }) {
  router.push(`/project/${dsName.value}/table/${row.name}`);
}

function onShowAddTable() {
  showAddTable.value = true;
}

function onShowAddView() {
  showAddView.value = true;
}

function onShowAddTables() {
  showAddTables.value = true;
}

const hasViews = computed(() => datasourceStore.tables.some((table) => table.viewType !== undefined));

function onDownloadDictionary() {
  datasourceStore.downloadTablesDictionary(selected.value ? selected.value.map((t) => t.name) : []);
}

function onDownloadViews() {
  datasourceStore.downloadViews(selected.value ? selected.value.filter((t) => t.viewType).map((t) => t.name) : []);
}

function onShowDeleteTables() {
  showDeleteTables.value = true;
}

function onDeleteTables() {
  datasourceStore.deleteTables(removableTables.value.map((t) => t.name)).then(() => {
    selected.value = [];
    init();
  });
}

function onShowImportFile() {
  importType.value = 'file';
  showImport.value = true;
}

function onShowImportServer() {
  importType.value = 'server';
  showImport.value = true;
}

function onShowImportDatabase() {
  importType.value = 'database';
  showImport.value = true;
}

function onShowExportFile() {
  exportType.value = 'file';
  showExport.value = true;
}

function onShowExportDatabase() {
  exportType.value = 'database';
  showExport.value = true;
}

function onShowCopy() {
  showCopy.value = true;
}

function onRestoreViews() {
  showRestoreViews.value = true;
}

function onSearch() {
  searchStore.reset();
  searchStore.variablesQuery.criteria['project'] = [dsName.value];
  router.push('/search/variables');
}
</script>
