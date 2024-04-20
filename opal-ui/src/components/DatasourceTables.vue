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
        <q-btn-dropdown color="primary" icon="add" :label="$t('add')" size="sm">
          <q-list>
            <q-item clickable v-close-popup @click="onShowAddTable">
              <q-item-section>
                <q-item-label>{{ $t('add_table') }}</q-item-label>
              </q-item-section>
            </q-item>

            <q-item clickable v-close-popup @click="onAddTables">
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
          class="on-right"
          @click="init"
        />
      </template>
      <template v-slot:body-cell-name="props">
        <q-td :props="props">
          <span class="text-primary">{{ props.value }}</span>
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

    <q-dialog v-model="showAddTable">
      <q-card>
        <q-card-section>
          <div class="text-h6">{{ $t('add_table') }}</div>
        </q-card-section>

        <q-separator />

        <q-card-section>
          <q-input
            v-model="tableName"
            dense
            type="text"
            :label="$t('name')"
            style="width: 300px"
          >
          </q-input>
        </q-card-section>
        <q-card-section class="q-mb-md">
          <q-input
            v-model="entityType"
            dense
            type="text"
            :label="$t('entity_type')"
            style="width: 300px"
          >
          </q-input>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="bg-grey-3">
          <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
          <q-btn
            flat
            :label="$t('add')"
            color="primary"
            :disable="!isTableNameValid || !isEntityTypeValid"
            @click="onAddTable"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
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
import { tableStatusColor } from 'src/utils/colors';

const route = useRoute();
const router = useRouter();
const datasourceStore = useDatasourceStore();
const { t } = useI18n();

const showAddTable = ref(false);
const tableName = ref('');
const entityType = ref('Participant');

const tableRef = ref();
const loading = ref(false);
const initialPagination = ref({
  sortBy: 'desc',
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

const isTableNameValid = computed(() => datasourceStore.isNewTableNameValid(tableName.value));

const isEntityTypeValid = computed(() => entityType.value.trim() !== '');

function onShowAddTable() {
  tableName.value = '';
  entityType.value = 'Participant';
  showAddTable.value = true;
}

function onAddTable() {
  datasourceStore.addTable(tableName.value, entityType.value);
}
</script>
