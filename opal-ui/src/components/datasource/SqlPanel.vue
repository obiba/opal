<template>
  <div>
    <div class="text-help q-mb-md">{{ $t('sql_info') }}</div>

    <div>
      <q-chip clickable :color="tab === 'query' ? 'primary' : 'grey-6'" :label="$t('query')" class="text-white" @click="tab = 'query'"/>
      <q-chip clickable :color="tab === 'history' ? 'primary' : 'grey-6'" :label="$t('history')" class="text-white" @click="tab = 'history'"/>
    </div>
    <q-tab-panels v-model="tab">
      <q-tab-panel name="query">
        <q-input
          v-model="sql"
          filled
          placeholder="SELECT * FROM ..."
          autogrow
          type="text-area"
          class="q-mb-md" />
        <q-btn
          :label="$t('execute')"
          color="primary"
          size="sm"
          icon="play_arrow"
          :disable="sql.trim().length === 0"
          @click="onExecute"
          class="q-mb-md" />

        <div v-if="loading">
          <q-spinner-dots size="md" />
        </div>
        <div v-else>
          <q-table
            v-if="rows"
            :rows="rows"
            row-key="_id"
            flat
            class="q-mt-md" />
        </div>
      </q-tab-panel>
      <q-tab-panel name="history">
        <q-table
          :rows="historyRows"
          row-key="timestamp"
          @row-dblclick="onHistoryClick"
          flat
          class="q-mt-md" />
      </q-tab-panel>
    </q-tab-panels>
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'SqlPanel',
});
</script>
<script setup lang="ts">
import { SqlCommand, SqlResults } from 'src/components/models';
import { notifyError } from 'src/utils/notify';
const sql = ref('');

const sqlStore = useSqlStore();
const datasourceStore = useDatasourceStore();

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
    return columns ? row.reduce((acc, val, i) => {
      acc[columns[i]] = val;
      return acc;
    }, rowObj) : rowObj;
  });
});

const historyRows = computed(() => {
  return sqlStore.history.filter((cmd) => cmd.datasource === datasourceStore.datasource.name);
});


function onHistoryClick(evt: unknown, row: SqlCommand) {
  sql.value = row.query;
  results.value = null;
  tab.value = 'query';
}

function onExecute() {
  loading.value = true;
  sqlStore.execute(sql.value).then((res) => {
    results.value = res;
  }).catch((err) => {
    notifyError(err);
    results.value = null;
  }).finally(() => {
    loading.value = false;
  });
}
</script>
