<template>
  <div>
    <q-table
      ref="tableRef"
      flat
      :rows="rows"
      :columns="columns"
      row-key="name"
      v-model:pagination="pagination"
      :rows-per-page-options="[10, 20, 50, 100]"
      :loading="loading"
      :visible-columns="visibleColumns"
      @request="onRequest"
      class="table-values"
    >
      <template v-slot:top>
        <div class="row q-gutter-sm">
          <q-select
            v-model="visibleColumns"
            multiple
            flat
            dense
            options-dense
            size="sm"
            display-value=""
            :label="$t('select_columns')"
            emit-value
            map-options
            :options="selectableColumns"
            option-value="name"
            options-cover
            use-input
            @filter="onFilter"
            style="min-width: 150px"
            @popup-hide="onVariableSelection"
          >
            <template v-slot:option="{ itemProps, opt, selected, toggleOption }">
              <q-item v-bind="itemProps" v-if="!opt.required">
                <q-item-section>
                  <q-item-label>{{ opt.label }}</q-item-label>
                </q-item-section>
                <q-item-section side>
                  <q-toggle :model-value="selected" @update:model-value="toggleOption(opt)" />
                </q-item-section>
              </q-item>
            </template>
          </q-select>
        </div>
      </template>
      <template v-slot:header="props">
        <q-tr :props="props">
          <q-th
            v-for="col in props.cols"
            :key="col.name"
            :props="props"
          >
            <span>{{ col.label }}</span>
            <div v-if="col.variable" class="text-grey-5 text-caption">
              <span>{{  col.variable.valueType }}</span>
              <q-badge
                v-if="col.variable.isRepeatable && col.variable.occurrenceGroup"
                color="grey-6 on-right">
                {{ col.variable.occurrenceGroup }}
              </q-badge>
            </div>
          </q-th>
        </q-tr>
      </template>
      <template v-slot:body-cell-_id="props">
        <q-td :props="props" auto-width>
          <span class="text-bold">{{ props.value }}</span>
        </q-td>
      </template>
      <template v-for="col in visibleColumns" v-slot:[`body-cell-${col}`]="props" :key="col">
        <q-td :props="props">
          <value-cell :value="props.value" :variable="getVariable(col)" />
        </q-td>
      </template>
    </q-table>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'TableValues',
});
</script>
<script setup lang="ts">
import { Variable } from 'src/components/models';
import ValueCell from 'src/components/datasource/ValueCell.vue';
import { t } from 'src/boot/i18n';

interface TableValuesProps {
  variable: Variable | undefined;
}

const props = withDefaults(defineProps<TableValuesProps>(), {
  variable: undefined,
});

const route = useRoute();
const datasourceStore = useDatasourceStore();

const tableRef = ref();
const loading = ref(false);
const pagination = ref({
  sortBy: 'desc',
  descending: false,
  page: 1,
  rowsPerPage: 20,
  rowsNumber: 0,
});
const COLUMNS_COUNT = 20;

const rows = ref([]);
const columns = ref([]);
const visibleColumns = ref<string[]>([]);
const varFilter = ref<string>();

const dsName = computed(() => route.params.id as string);
const tName = computed(() => route.params.tid as string);
const selectableColumns = computed(() => columns.value.filter((c) => c.name !== 'ID' && (varFilter.value === '' || c.name.toLowerCase().indexOf(varFilter.value) > -1)));

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
      pagination.value.rowsNumber = datasourceStore.table.valueSetCount;
      columns.value = datasourceStore.variables.map((v) => ({
        name: v.name,
        label: v.name,
        align: 'left',
        field: v.name,
        required: false,
        variable: v,
      }));
      visibleColumns.value = columns.value.map((c) => c.name).filter((n) => props.variable ? props.variable.name === n : true).slice(0, COLUMNS_COUNT);
      columns.value.unshift({
        name: '_id',
        required: true,
        label: t('id'),
        align: 'left',
        field: '_id',
        format: (val: string) => val,
        sortable: false,
      });
    }).then(() => {
      tableRef.value.requestServerInteraction();
    });
}

function onFilter(val: string, update, abort) {
  update(() => {
    varFilter.value = val.toLowerCase();
  })
}

function onRequest(props) {
  const { page, rowsPerPage, sortBy, descending } = props.pagination;

  const offset = (page - 1) * rowsPerPage;
  const limit = rowsPerPage;
  const select = visibleColumns.value;
  loading.value = true;
  datasourceStore.loadValueSets(offset, limit, select).then((res) => {
    if (res.valueSets) {
      rows.value = res.valueSets.map((vs) => {
        const row = { _id: vs.identifier };
        vs.values.forEach((val, idx) => {
          row[res.variables[idx]] = val;
        });
        return row;
      });
    } else {
      rows.value = [];
    }
    // don't forget to update local pagination object
    pagination.value.page = page
    pagination.value.rowsPerPage = rowsPerPage
    pagination.value.sortBy = sortBy
    pagination.value.descending = descending
    loading.value = false;
  });
}

function onVariableSelection() {
  tableRef.value.requestServerInteraction();
}

function getVariable(name: string) {
  return datasourceStore.variables.find((v) => v.name === name);
}
</script>
