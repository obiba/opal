<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('search')" to="/search" />
        <q-breadcrumbs-el :label="t('entity')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h6">{{ t('entity_search') }}</div>
      <div class="text-help q-mb-md">
        <q-markdown :src="t('entity_search_info')" no-heading-anchor-links />
      </div>
      <q-card flat class="bg-grey-2 q-mb-md">
        <q-card-section>
          <div class="row q-gutter-md">
            <q-input v-model="type" :label="t('entity_type')" flat dense size="sm" @update:model-value="onClear" />
            <q-input
              v-model="identifier"
              :label="t('id')"
              flat
              dense
              size="sm"
              @update:model-value="onClear"
              @keyup.enter="onSubmit"
            />
            <q-btn
              :label="t('search')"
              color="primary"
              size="sm"
              @click="onSubmit"
              :disable="loading || !isValid"
              class="q-mt-lg"
              style="height: 2.5em"
            />
          </div>
        </q-card-section>
      </q-card>

      <div v-if="loading">
        <q-spinner-dots size="lg" />
      </div>
      <div v-else-if="isValid && tables">
        <q-chip class="q-mb-md q-ml-none text-bold">{{ type }} - {{ identifier }}</q-chip>
        <q-select
          v-model="tableId"
          :options="tableOptions"
          :label="t('table')"
          flat
          dense
          emit-value
          @update:model-value="onTableSelected"
          style="width: 300px"
        />
        <q-table
          :rows="rows"
          row-key="variable"
          :columns="columns"
          :loading="loadingValues"
          :rows-per-page-options="[10, 20, 50, 100, 0]"
          flat
          class="q-mt-md"
        >
          <template v-slot:body-cell-variable="props">
            <q-td :props="props">
              <router-link
                :to="`/project/${table?.datasourceName}/table/${table?.name}/variable/${props.value}`"
                class="text-primary"
                >{{ props.value }}</router-link
              >
            </q-td>
          </template>
          <template v-slot:body-cell-value="props">
            <q-td :props="props">
              <value-cell :value="props.value" :variable="getVariable(props.row.variable)" />
            </q-td>
          </template>
        </q-table>
      </div>
      <div v-else class="text-hint">
        {{ t('no_results') }}
      </div>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import ValueCell from 'src/components/datasource/ValueCell.vue';
import type { TableDto, ValueSetsDto, ValueSetsDto_ValueDto, VariableDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';
import { DefaultAlignment } from 'src/components/models';

const route = useRoute();
const searchStore = useSearchStore();
const datasourceStore = useDatasourceStore();
const { t } = useI18n();

const type = ref<string>('Participant');
const identifier = ref<string>('');
const loading = ref<boolean>(false);
const loadingValues = ref<boolean>(false);
const tables = ref<TableDto[]>();
const tableId = ref<string>();
const valueSets = ref<ValueSetsDto>();
const variables = ref<VariableDto[]>();

const idParam = computed(() => route.query.id as string);
const typeParam = computed(() => route.query.type as string);

const isValid = computed(() => type.value && identifier.value);
const tableOptions = computed(
  () => tables.value?.map((table) => ({ label: asTableId(table), value: asTableId(table) })) || []
);
const table = computed(() => tables.value?.find((t) => asTableId(t) === tableId.value));

const columns = computed(() => {
  return [
    { name: 'variable', label: t('variable'), field: 'variable', align: DefaultAlignment, sortable: true },
    { name: 'value', label: t('value'), field: 'value', align: DefaultAlignment, sortable: true },
  ];
});

const rows = computed(() => {
  const variables = valueSets.value?.variables;
  if (!variables || !valueSets.value?.valueSets) {
    return [];
  }
  const values = valueSets.value?.valueSets[0]?.values;
  const result: { variable: string; value?: ValueSetsDto_ValueDto }[] = [];
  values?.forEach((value, idx: number) => {
    result.push({
      variable: variables[idx] || '',
      value: value,
    });
  });
  return result;
});

onMounted(() => {
  if (idParam.value) {
    identifier.value = idParam.value;
  }
  if (typeParam.value) {
    type.value = typeParam.value;
  }
  if (isValid.value) {
    onSubmit();
  }
});

function onClear() {
  tables.value = undefined;
  tableId.value = undefined;
  valueSets.value = undefined;
  variables.value = undefined;
}

function onSubmit() {
  if (!isValid.value) {
    return;
  }
  loading.value = true;
  onClear();
  searchStore
    .getEntityTables(type.value, identifier.value)
    .then((response) => {
      tables.value = response;
      if (response.length > 0) {
        // if there is a table in the app context state, select it
        if (datasourceStore.table) {
          const tbl = tables.value?.find((t) => t.link === datasourceStore.table.link);
          // check if the table is in the list
          if (tbl) {
            tableId.value = asTableId(tbl);
          }
        }
        // otherwise select the first table
        if (!tableId.value) {
          tableId.value = asTableId(response[0]);
        }
      } else {
        tableId.value = undefined;
      }
      onTableSelected();
    })
    .catch((error) => {
      notifyError(error);
    })
    .finally(() => {
      loading.value = false;
    });
}

function asTableId(table: TableDto) {
  return `${table.datasourceName}.${table.name}`;
}

function onTableSelected() {
  const table = tables.value?.find((t) => asTableId(t) === tableId.value);
  if (!table || !table.datasourceName || !table.name || !identifier.value) {
    return;
  }
  loadingValues.value = true;
  datasourceStore
    .getTableVariables(table.datasourceName, table.name)
    .then((res) => {
      variables.value = res;
      getEntityValueSets(table, identifier.value);
    })
    .catch((error) => {
      notifyError(error);
    });
}

function getEntityValueSets(table: TableDto, identifier: string) {
  loadingValues.value = true;
  if (!table.datasourceName || !table.name || !identifier) {
    loadingValues.value = false;
    return;
  }
  datasourceStore
    .getEntityValueSet(table.datasourceName, table.name, identifier)
    .then((response) => {
      valueSets.value = response;
    })
    .catch((error) => {
      notifyError(error);
    })
    .finally(() => {
      loadingValues.value = false;
    });
}

function getVariable(name: string) {
  return variables.value?.find((v) => v.name === name) || { name: name } as VariableDto;
}
</script>
