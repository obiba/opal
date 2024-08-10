<template>
  <div>
    <div class="text-help q-mb-md">{{ $t('sql_info') }}</div>
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
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'SqlPanel',
});
</script>
<script setup lang="ts">
import { SqlResults } from 'src/components/models';
import { notifyError } from 'src/utils/notify';
const sql = ref('');

const sqlStore = useSqlStore();

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
