<template>
  <div class="text-h6">{{ t('apps.registry_list') }}</div>
  <q-table
    flat
    :rows="apps"
    :columns="columns"
    row-key="name"
    :pagination="initialPagination"
    :hide-pagination="apps.length <= initialPagination.rowsPerPage"
    :loading="loading"
  >
    <template v-slot:top-left>
      <q-btn size="sm" outline color="secondary" icon="refresh" :title="t('refresh')" @click="onRefresh"></q-btn>
    </template>
    <template v-slot:body-cell-name="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <span
          >{{ props.value }} <code :title="props.row.id">{{ props.row.id.replace(/-.*$/, '') }}</code></span
        >
        <div class="float-right">
          <q-btn
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="t('unregister')"
            :icon="toolsVisible[props.row.name] ? 'close' : 'none'"
            class="q-ml-xs"
            @click="onUnregister(props.row)"
          />
        </div>
      </q-td>
    </template>
    <template v-slot:body-cell-type="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        {{ props.value }}
      </q-td>
    </template>
    <template v-slot:body-cell-cluster="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        {{ props.value }}
      </q-td>
    </template>
    <template v-slot:body-cell-host="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <a :href="props.row.server" target="_blank">{{ props.value }}</a>
      </q-td>
    </template>
    <template v-slot:body-cell-tags="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <q-chip class="q-ml-none" v-for="(tag, index) in props.value" :key="index">
          {{ tag }}
        </q-chip>
      </q-td>
    </template>
  </q-table>

  <confirm-dialog
    v-model="showDelete"
    :title="t('unregister')"
    :text="t('apps.unregister_confirm', { app: selectedApp.name })"
    @confirm="doUnregister"
  />
</template>

<script setup lang="ts">
import type { AppDto } from 'src/models/Apps';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { notifyError } from 'src/utils/notify';
import { DefaultAlignment } from 'src/components/models';

const { t } = useI18n();
const appsStore = useAppsStore();
const loading = ref(false);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const showDelete = ref(false);
const selectedApp = ref({} as AppDto);
const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});

const apps = computed(() => appsStore.apps);
const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: DefaultAlignment,
    field: 'name',
    sortable: true,
    style: 'width: 30%',
  },
  {
    name: 'type',
    label: t('type'),
    align: DefaultAlignment,
    field: 'type',
  },
  {
    name: 'cluster',
    label: t('cluster'),
    align: DefaultAlignment,
    field: 'cluster',
  },
  {
    name: 'host',
    label: t('host'),
    align: DefaultAlignment,
    field: 'server',
  },
  {
    name: 'tags',
    label: t('tags'),
    align: DefaultAlignment,
    field: 'tags',
  },
]);

async function init() {
  loading.value = true;
  return appsStore.initApps().then(() => (loading.value = false));
}

async function doUnregister() {
  if (!selectedApp.value.id) {
    return;
  }

  try {
    showDelete.value = false;
    await appsStore.unregisterApp(selectedApp.value.id);
    await init();
  } catch (error) {
    notifyError(error);
  }
}

// Handlers

function onOverRow(row: AppDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: AppDto) {
  toolsVisible.value[row.name] = false;
}

async function onUnregister(row: AppDto) {
  showDelete.value = true;
  selectedApp.value = row;
}

async function onRefresh() {
  return init();
}

onMounted(async () => init());
</script>
