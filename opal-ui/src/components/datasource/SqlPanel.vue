<template>
  <div>
    <div class="text-help q-mb-md">
      <q-markdown :src="t('sql_info')" no-heading-anchor-links />
    </div>
    <div>
      <q-chip
        clickable
        :color="tab === 'query' ? 'primary' : 'grey-6'"
        :label="t('query')"
        class="text-white"
        @click="tab = 'query'"
      />
      <q-chip
        clickable
        :color="tab === 'history' ? 'primary' : 'grey-6'"
        :label="t('history')"
        class="text-white"
        @click="tab = 'history'"
      />
    </div>
    <q-tab-panels v-model="tab">
      <q-tab-panel name="query">
        <div class="text-hint q-mb-md">
          <q-icon name="info" />
          <span class="q-ml-xs">{{ t('sql_query_hint') }}</span>
        </div>
        <q-input
          v-model="sql"
          filled
          placeholder="SELECT * FROM ..."
          autogrow
          @keydown="onKeydown"
          type="textarea"
          class="q-mb-md"
        />
        <q-btn
          :label="t('execute')"
          color="primary"
          size="sm"
          icon="play_arrow"
          :disable="sql.trim().length === 0"
          @click="onExecute"
        />
        <q-btn
          v-if="rows"
          outline
          color="secondary"
          icon="cleaning_services"
          :title="t('clear')"
          size="sm"
          @click="onClear"
          class="on-right"
        />
        <q-btn
          v-if="rows"
          color="secondary"
          icon="file_download"
          :label="t('download')"
          size="sm"
          @click="onDownload"
          class="on-right"
        />
        <div v-if="loading">
          <q-spinner-dots size="md" class="q-mt-md" />
        </div>
        <div v-else class="q-mt-md">
          <q-table v-if="rows" :rows="rows" row-key="_id" flat
          table-header-style="background-color: #efefef">
            <template v-slot:header-cell="props">
              <q-th :props="props">
                {{ props.col.name }}
              </q-th>
            </template>
          </q-table>
        </div>
      </q-tab-panel>
      <q-tab-panel name="history">
        <div v-if="historyRows.length">
          <div class="text-hint">
            <q-icon name="info" />
            <span class="q-ml-xs">{{ t('sql_history_hint') }}</span>
          </div>
          <q-table
            :rows="historyRows"
            :columns="historyColumns"
            row-key="timestamp"
            @row-dblclick="onHistoryClick"
            flat
          >
            <template v-slot:body-cell-query="props">
              <q-td :props="props">
                <span class="text-caption">{{ props.value }}</span>
              </q-td>
            </template>
            <template v-slot:body-cell-delay="props">
              <q-td :props="props"> {{ props.value }} ms </q-td>
            </template>
          </q-table>
        </div>
        <div v-else class="text-help">
          {{ t('no_sql_history') }}
        </div>
      </q-tab-panel>
    </q-tab-panels>
  </div>
</template>

<script setup lang="ts">
import type { SqlCommand, SqlResults } from 'src/components/models';
import { notifyError } from 'src/utils/notify';
import Papa from 'papaparse';
import { DefaultAlignment } from 'src/components/models';

const sql = ref('');

const sqlStore = useSqlStore();
const datasourceStore = useDatasourceStore();
const { t } = useI18n();

const tab = ref('query');
const results = ref<SqlResults | null>(null);
const loading = ref(false);

interface RowResult {
  [key: string]: boolean | number | string | null | undefined;
}

const rows = computed(() => {
  return results.value?.rows.map((row) => {
    const rowObj = {} as RowResult;
    const columns = results.value?.columns;
    return columns
      ? row.reduce((acc, val, i) => {
          if (columns[i])  
            acc[columns[i]] = val;
          return acc;
        }, rowObj)
      : rowObj;
  });
});

const historyRows = computed(() => {
  return sqlStore.history
    .filter((cmd) => cmd.datasource === datasourceStore.datasource.name)
    .sort((a, b) => b.timestamp - a.timestamp);
});

const historyColumns = [
  { name: 'query', label: t('query'), align: DefaultAlignment, field: 'query', sortable: true },
  { name: 'delay', label: t('delay'), align: DefaultAlignment, field: 'delay', sortable: true },
];

function onHistoryClick(evt: unknown, row: SqlCommand) {
  sql.value = row.query;
  results.value = null;
  tab.value = 'query';
}

function onKeydown(evt: KeyboardEvent) {
  if (evt.key === 'Enter' && evt.ctrlKey) {
    onExecute();
  }
}

function onExecute() {
  loading.value = true;
  sqlStore
    .execute(sql.value)
    .then((res) => {
      results.value = res;
    })
    .catch((err) => {
      notifyError(err);
      results.value = null;
    })
    .finally(() => {
      loading.value = false;
    });
}

function onClear() {
  results.value = null;
}

function onDownload() {
  if (results.value) {
    const csv = Papa.unparse({
      fields: results.value.columns,
      data: results.value.rows,
    });
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'data.csv';
    a.click();
    URL.revokeObjectURL(url);
  }
}
</script>
