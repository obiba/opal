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
    >
      <template v-slot:top>
        <q-btn-dropdown v-if="datasourceStore.perms.tables?.canCreate()" color="primary" icon="add" :label="$t('add')" size="sm"
          class="on-left">
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
        />
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
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'DatasourceTables',
});
</script>
<script setup lang="ts">
import { Timestamps } from 'src/components/models';
import AddTableDialog from 'src/components/datasource/AddTableDialog.vue';
import AddTablesDialog from 'src/components/datasource/AddTablesDialog.vue';
import { tableStatusColor } from 'src/utils/colors';

const route = useRoute();
const router = useRouter();
const datasourceStore = useDatasourceStore();
const { t } = useI18n();

const showAddTable = ref(false);
const showAddTables = ref(false);

const tableRef = ref();
const loading = ref(false);
const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 20,
});

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
    format: (val: Timestamps) => val.lastUpdate,
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
</script>
