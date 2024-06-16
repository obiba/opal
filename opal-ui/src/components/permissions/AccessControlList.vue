<template>
  <div>
    <q-table
      :rows="filteredRows"
      flat
      :row-key="getRowKey"
      :columns="columns"
      :pagination="initialPagination"
      :filter="filter"
    >
      <template v-slot:top-right>
        <q-input dense debounce="500" v-model="filter">
          <template v-slot:append>
            <q-icon name="search" />
          </template>
        </q-input>
      </template>
      <template v-slot:body="props">
      <q-tr :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-td key="name" :props="props">
            <span class="text-primary">{{  props.row.subject.principal }}</span>
            <div class="float-right" >
              <q-btn
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :icon="toolsVisible[getRowKey(props.row)] ? 'edit' : 'none'"
                class="q-ml-xs"
                @click="onShowEdit(props.row)"
              />
              <q-btn
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="$t('delete')"
                :icon="toolsVisible[getRowKey(props.row)] ? 'delete' : 'none'"
                class="q-ml-xs"
                @click="onShowDelete(props.row)"
              />
            </div>
          </q-td>
          <q-td key="type" :props="props">
            {{ $t(props.row.subject.type.toLowerCase()) }}
          </q-td>
          <q-td key="permissions" :props="props" class="text-help">
            {{ props.row.actions.map((action: string) => $t(`acls.${action}`)).join(', ') }}
          </q-td>
        </q-tr>
      </template>
    </q-table>
  </div>
</template>

<script setup lang="ts">
import { Acl } from 'src/models/Opal';
interface Props {
  resource: string;
}

const props = defineProps<Props>();

const authzStore = useAuthzStore();
const { t } = useI18n();

const filter = ref<string>('');
const toolsVisible = ref<{ [key: string]: boolean }>({});
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const showEdit = ref(false);
const showDelete = ref(false);
const selected = ref();

const columns = [
  { name: 'name', label: t('name'), align: 'left', field: 'subject' },
  { name: 'type', label: t('type'), align: 'left', field: 'subject' },
  { name: 'permissions', label: t('permissions'), align: 'left', field: 'actions' },
];

const filteredRows = computed(() => {
  if (!filter.value) {
    return authzStore.acls;
  }
  return authzStore.acls.filter((row) => {
    return row.subject?.principal.toLowerCase().includes(filter.value.toLowerCase());
  });
});

onMounted(async () => {
  authzStore.initAcls(props.resource);
});

watch(() => props.resource, async (resource) => {
  authzStore.initAcls(resource);
});

function getRowKey(row: Acl) {
  return `${row.subject?.principal}:${row.subject?.type}`;
}

function onOverRow(row: Acl) {
  toolsVisible.value[getRowKey(row)] = true;
}

function onLeaveRow(row: Acl) {
  toolsVisible.value[getRowKey(row)] = false;
}

function onShowEdit(row: Acl) {
  selected.value = row;
  showEdit.value = true;
}

function onShowDelete(row: Acl) {
  selected.value = row;
  showDelete.value = true;
}
</script>
