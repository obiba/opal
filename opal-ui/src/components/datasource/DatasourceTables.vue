<template>
  <div>
    <q-table
      ref="tableRef"
      flat
      :rows="datasourceStore.tables"
      :columns="columns"
      row-key="name"
      :pagination="initialPagination"
      :loading="loading"
      @row-click="onRowClick"
      selection="multiple"
      v-model:selected="selected"
    >
      <template v-slot:top>
        <q-btn-dropdown v-if="datasourceStore.perms.tables?.canCreate()" color="primary" icon="add" :label="$t('add')" size="sm"
          class="on-left q-mb-sm">
          <q-list>
            <q-item clickable v-close-popup @click="onShowAddTable">
              <q-item-section>
                <q-item-label>{{ $t('add_table') }}</q-item-label>
              </q-item-section>
            </q-item>

            <q-item clickable v-close-popup @click="onShowAddTables">
              <q-item-section>
                <q-item-label>{{ $t('add_tables') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item clickable v-close-popup @click="onAddView">
              <q-item-section>
                <q-item-label>{{ $t('add_a_view') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item clickable v-close-popup @click="onRestoreViews">
              <q-item-section>
                <q-item-label>{{ $t('restore_views') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>
        <q-btn
          color="grey-6"
          text-color="white"
          icon="refresh"
          :label="$t('refresh')"
          size="sm"
          @click="init"
          class="q-mb-sm"
        />
        <q-btn-dropdown outline color="primary" icon="download" size="sm" :label="$t('download')" class="on-right q-mb-sm">
          <q-list>
            <q-item clickable v-close-popup @click="onDownloadDictionary">
              <q-item-section>
                <q-item-label>{{ $t('download_dictionaries') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item v-if="hasViews" clickable v-close-popup @click="onDownloadViews">
              <q-item-section>
                <q-item-label>{{ $t('download_views_backup') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>
        <q-btn v-if="projectsStore.perms.import?.canCreate()" color="secondary" icon="input" :label="$t('import')" size="sm" @click="onShowImport" class="on-right q-mb-sm"></q-btn>
        <q-btn v-if="projectsStore.perms.export?.canCreate()" color="secondary" icon="output" :label="$t('export')" size="sm" @click="onShowExport" class="on-right q-mb-sm"></q-btn>
        <q-btn v-if="projectsStore.perms.copy?.canCreate()" color="secondary" icon="content_copy" :label="$t('copy')" size="sm" @click="onShowCopy" class="on-right q-mb-sm"></q-btn>
        <q-btn v-if="datasourceStore.perms.tables?.canDelete()" :disable="removableTables.length === 0" outline color="red" icon="delete" size="sm" @click="onShowDeleteTables" class="on-right q-mb-sm"></q-btn>
      </template>
      <template v-slot:body-cell-name="props">
        <q-td :props="props">
          <span class="text-primary">{{ props.value }}</span>
          <q-icon v-if="props.row.viewType" name="visibility" size="xs" color="primary" class="on-right" />
        </q-td>
      </template>
      <template v-slot:body-cell-status="props">
        <q-td :props="props">
          <q-icon
            name="circle"
            size="sm"
            :color="tableStatusColor(props.value)"
          />
        </q-td>
      </template>
    </q-table>

    <add-table-dialog v-model="showAddTable" />

    <add-tables-dialog v-model="showAddTables" />

    <copy-tables-dialog v-model="showCopy" :tables="readableTables"/>

    <confirm-dialog v-model="showDeleteTables" :title="$t('delete')" :text="removableTables.length === 1 ? $t('delete_table_confirm') : $t('delete_tables_confirm')" @confirm="onDeleteTables" />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'DatasourceTables',
});
</script>
<script setup lang="ts">
import { Table, Timestamps } from 'src/components/models';
import AddTableDialog from 'src/components/datasource/AddTableDialog.vue';
import AddTablesDialog from 'src/components/datasource/AddTablesDialog.vue';
import CopyTablesDialog from 'src/components/datasource/CopyTablesDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { tableStatusColor } from 'src/utils/colors';
import { getDateLabel } from 'src/utils/dates';

const route = useRoute();
const router = useRouter();
const datasourceStore = useDatasourceStore();
const projectsStore = useProjectsStore();
const { t } = useI18n();

const showAddTable = ref(false);
const showAddTables = ref(false);
const showCopy = ref(false);

const tableRef = ref();
const loading = ref(false);
const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const selected = ref([] as Table[]);
const showDeleteTables = ref(false);

const columns = [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'name',
    sortable: true,
  },
  {
    name: 'entityType',
    required: true,
    label: t('entity_type'),
    align: 'left',
    field: 'entityType',
  },
  {
    name: 'variableCount',
    required: true,
    label: t('variables'),
    align: 'left',
    field: 'variableCount',
  },
  {
    name: 'valueSetCount',
    required: true,
    label: t('entities'),
    align: 'left',
    field: 'valueSetCount',
  },
  {
    name: 'lastUpdate',
    required: true,
    label: t('last_update'),
    align: 'left',
    field: 'timestamps',
    format: (val: Timestamps) => getDateLabel(val.lastUpdate),
  },
  {
    name: 'status',
    required: true,
    label: t('status'),
    align: 'left',
    field: 'status',
  },
];

onMounted(() => {
  init();
});

const dsName = computed(() => route.params.id as string);
const removableTables = computed(() => selected.value.length === 0 ? datasourceStore.tables : selected.value);
const readableTables = computed(() => selected.value.length === 0 ? datasourceStore.tables : selected.value);


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

function onShowCopy() {
  showCopy.value = true;
}
</script>
