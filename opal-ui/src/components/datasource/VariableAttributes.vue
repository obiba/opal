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
    >
      <template v-slot:top>
        <q-btn-dropdown v-if="datasourceStore.perms.variable?.canUpdate()" color="primary" icon="add" :label="$t('add')" size="sm">
          <q-list> </q-list>
        </q-btn-dropdown>
      </template>
      <template v-slot:body-cell-name="props">
        <q-td :props="props">
          <span class="text-primary">{{ props.value }}</span>
        </q-td>
      </template>
      <template v-slot:body-cell-locale="props">
        <q-td :props="props">
          <q-badge
            v-if="props.value"
            color="grey-6"
            :label="props.value"
            class="on-left"
          />
        </q-td>
      </template>
    </q-table>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'VariableAttributes',
});
</script>
<script setup lang="ts">
const { t } = useI18n();
const datasourceStore = useDatasourceStore();

const tableRef = ref();
const loading = ref(false);
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});

const columns = [
  {
    name: 'namespace',
    required: true,
    align: 'left',
    label: t('namespace'),
    field: 'namespace',
    format: (val: string) => val,
    sortable: true,
  },
  {
    name: 'name',
    required: true,
    align: 'left',
    label: t('name'),
    field: 'name',
    format: (val: string) => val,
    sortable: true,
  },
  {
    name: 'locale',
    required: true,
    align: 'left',
    label: t('locale'),
    field: 'locale',
  },
  {
    name: 'value',
    required: true,
    align: 'left',
    label: t('value'),
    field: 'value',
  },
];

const rows = computed(() => datasourceStore.variable?.attributes ? datasourceStore.variable.attributes : []);
</script>
