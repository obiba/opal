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
      <template v-slot:top>
        <div class="row q-gutter-sm">
          <q-btn-dropdown v-if="datasourceStore.perms.variables?.canCreate()" color="primary" icon="add" :label="$t('add')" size="sm">
            <q-list> </q-list>
          </q-btn-dropdown>
          <q-btn
            color="secondary"
            icon="refresh"
            :label="$t('refresh')"
            size="sm"
            @click="init"
          />
          <q-btn v-if="datasourceStore.perms.table?.canUpdate()" outline color="red" icon="delete" size="sm" @click="onShowDeleteVariables"></q-btn>
        </div>
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

    <confirm-dialog v-model="showDeleteVariables" :title="$t('delete')" :text="$t('delete_variables_confirm', { count: selected.length || rows.length })" @confirm="onDeleteVariables" />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'TableVariables',
});
</script>
<script setup lang="ts">
import { CategoryDto, VariableDto } from 'src/models/Magma';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { notifyError } from 'src/utils/notify';
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
const selected = ref([] as VariableDto[]);
const showDeleteVariables = ref(false);

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

watch([dsName, tName], () => {
  init();
});

function init() {
  loading.value = true;
  datasourceStore
    .initDatasourceTableVariables(dsName.value, tName.value)
    .then(() => {
      loading.value = false;
    })
    .catch((err) => {
      loading.value = false;
      notifyError(err);
    });
}

function getCategoryNames(categories: CategoryDto[]) {
  return categories ? categories.map((c) => c.name).join(', ') : undefined;
}

function onRowClick(evt: unknown, row: { name: string }) {
  router.push(
    `/project/${dsName.value}/table/${tName.value}/variable/${row.name}`
  );
}

function onShowDeleteVariables() {
  showDeleteVariables.value = true;
}

function onDeleteVariables() {
  const names = (selected.value.length === 0 ? rows.value : selected.value).map((v) => v.name);
  datasourceStore
    .deleteVariables(names)
    .then(() => {
      selected.value = [];
      init();
    });
}
</script>
