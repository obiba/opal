<template>
  <q-table
    ref="tableRef"
    flat
    row-key="id"
    :rows="idRows"
    :columns="idColumns"
    v-model:pagination="pagination"
    :rows-per-page-options="[10, 20, 50, 100]"
    :loading="loading"
    class="table-values"
    @request="onRequest"
  ></q-table>
</template>

<script setup lang="ts">
import type { QTableColumn } from 'quasar';
import type { TableDto, ValueSetsDto } from 'src/models/Magma';
import { DefaultAlignment } from 'src/components/models';

interface Props {
  identifierTable: TableDto;
}

type ROW = { [key: string]: string };

const identifiersStore = useIdentifiersStore();
const props = defineProps<Props>();
const tableRef = ref();
const idColumns = ref<QTableColumn[]>([]);
const idRows = ref([] as ROW[]);
const loading = ref(false);
const pagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 10,
  rowsNumber: 0,
  sortBy: 'id',
});

function formatIdentifiersForTable(identifiers: ValueSetsDto) {
  const variables = identifiers.variables || [];
  idColumns.value = [];
  idColumns.value.push({
    name: 'id',
    label: 'ID',
    align: DefaultAlignment,
    field: 'id',
  } as QTableColumn);

  variables.forEach((vItem) => {
    idColumns.value.push({
      name: vItem,
      label: vItem,
      align: DefaultAlignment,
      field: vItem,
    } as QTableColumn);
  });

  idRows.value = [];
  (identifiers.valueSets || []).forEach((item) => {
    const d = { id: item.identifier } as { [key: string]: string };
    variables.forEach((vItem, index) => {
      d[vItem] = item.values[index]?.value || '';
    });
    idRows.value.push(d);
  });
}

async function getIdentifiers(offset = 0, limit = 10, identifiers?: ValueSetsDto) {
  if (identifiers) {
    formatIdentifiersForTable(identifiers);
    return;
  }

  loading.value = true;
  const identifierName = props.identifierTable.name;
  identifiersStore.loadIdentifiers(identifierName, offset, limit).then((identifiers: ValueSetsDto) => {
    formatIdentifiersForTable(identifiers);
    loading.value = false;
  });
}

// Handlers

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function onRequest(props: any) {
  const { page, rowsPerPage, sortBy, descending } = props.pagination;
  const offset = (page - 1) * rowsPerPage;
  const limit = rowsPerPage;
  getIdentifiers(offset, limit);
  // don't forget to update local pagination object
  pagination.value.page = page;
  pagination.value.rowsPerPage = rowsPerPage;
  pagination.value.sortBy = sortBy;
  pagination.value.descending = descending;
}

watch(
  () => props.identifierTable,
  () => {
    if (props.identifierTable) {
      getIdentifiers(0, 10);
    }
  }
);

onMounted(() => {
  pagination.value.rowsNumber = props.identifierTable.valueSetCount || 0;
  getIdentifiers();
});
</script>
