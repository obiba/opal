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
    >
      <template v-slot:top>
        <q-btn-dropdown v-if="datasourceStore.perms.variables?.canCreate()" color="primary" icon="add" :label="$t('add')" size="sm"
          class="on-left">
          <q-list> </q-list>
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
        </q-td>
      </template>
      <template v-slot:body-cell-valueType="props">
        <q-td :props="props">
          <pre class="q-ma-none">{{ props.value }}</pre>
        </q-td>
      </template>
      <template v-slot:body-cell-label="props">
        <q-td :props="props">
          <div v-for="attr in getLabels(props.value)" :key="attr.locale">
            <q-badge
              v-if="attr.locale"
              color="grey-6"
              :label="attr.locale"
              class="on-left"
            />
            <span>{{ attr.value }}</span>
          </div>
        </q-td>
      </template>
      <template v-slot:body-cell-categories="props">
        <q-td :props="props">
          <span>{{ getCategoryNames(props.value) }}</span>
        </q-td>
      </template>
    </q-table>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'TableVariables',
});
</script>
<script setup lang="ts">
import { Category } from '../models';
import { getLabels } from 'src/utils/attributes';

const route = useRoute();
const router = useRouter();
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
    name: 'name',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'name',
    format: (val: string) => val,
    sortable: true,
  },
  {
    name: 'label',
    required: true,
    label: t('label'),
    align: 'left',
    field: 'attributes',
  },
  {
    name: 'valueType',
    required: true,
    label: t('value_type'),
    align: 'left',
    field: 'valueType',
    format: (val: string) => t(val),
    sortable: true,
  },
  {
    name: 'categories',
    required: true,
    label: t('categories'),
    align: 'left',
    field: 'categories',
  },
];

const rows = computed(() => datasourceStore.variables);

const dsName = computed(() => route.params.id as string);
const tName = computed(() => route.params.tid as string);

onMounted(() => {
  init();
});

function init() {
  loading.value = true;
  datasourceStore
    .initDatasourceTableVariables(dsName.value, tName.value)
    .then(() => {
      loading.value = false;
    });
}

function getCategoryNames(categories: Category[]) {
  return categories ? categories.map((c) => c.name).join(', ') : undefined;
}

function onRowClick(evt: unknown, row: { name: string }) {
  router.push(
    `/project/${dsName.value}/table/${tName.value}/variable/${row.name}`
  );
}
</script>
