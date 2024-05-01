<template>
  <div>
    <q-table
      ref="tableRef"
      flat
      :rows="rows"
      :columns="columns"
      row-key="name"
      v-model:pagination="pagination"
      :loading="loading"
      :visible-columns="visibleColumns"
      @request="onRequest"
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
          />
        </div>
      </template>
      <template v-slot:body-cell-_id="props">
        <q-td :props="props" class=" bg-grey-3">
          <span class="text-bold">{{ props.value }}</span>
        </q-td>
      </template>
      <template v-for="col in visibleColumns" v-slot:[`body-cell-${col}`]="props" :key="col">
        <q-td :props="props">
          <div v-if="props.value.link">
            <q-btn flat dense no-caps icon="download" size="sm" :label="$t('download')" class="q-mr-sm" :to="props.value.link"/>
          </div>
          <div v-else-if="props.value.value">
            <span>{{ props.value.value }}</span>
          </div>
          <div v-else-if="props.value.values">
            <span>{{ props.value.values }}</span>
          </div>
          <div v-else>
            <span class="text-caption text-grey-5">null</span>
          </div>
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
import { t } from 'src/boot/i18n';

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
        //format: (val: unknown) => val.value ? val.value : '',
        // sortable: true,
        // classes: 'ellipsis',
        // style: 'max-width: 200px',
        // headerStyle: 'max-width: 200px',
        // headerClasses: 'ellipsis',
        required: false,
        // filter: true,
        // filterable: true,
        // filterIcon: 'search',
        // filterFn: (row: { name: string }, v: string) => row.name.toLowerCase().includes(v.toLowerCase()),
      }));
      visibleColumns.value = columns.value.map((c) => c.name).slice(0, 10);
      columns.value.unshift({
        name: '_id',
        required: true,
        label: t('id'),
        align: 'left',
        field: '_id',
        format: (val: string) => val,
        sortable: true,
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
    console.log(rows.value);
    // don't forget to update local pagination object
    pagination.value.page = page
    pagination.value.rowsPerPage = rowsPerPage
    pagination.value.sortBy = sortBy
    pagination.value.descending = descending
    loading.value = false;
  });
}
</script>
