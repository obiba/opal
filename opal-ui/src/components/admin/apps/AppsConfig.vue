<template>
  <div class="text-h5">{{ $t('discovery') }}</div>

  <q-card flat>
    <q-card-section class="q-px-none">
      <html-anchor-hint
        class="text-help"
        trKey="apps.discovery_info"
        :text="$t('apps.apps_admin')"
        url="https://opaldoc.obiba.org/en/latest/web-user-guide/administration/apps.html"
      />
    </q-card-section>
    <q-card-section class="q-px-none">
      <div class="text-h6">Rock</div>
      <html-anchor-hint class="text-help" trKey="apps.rock_info" text="OBiBa/Rock" url="https://rockdoc.obiba.org/" />
    </q-card-section>
  </q-card>

  <q-table
    flat
    :rows="rockConfigs"
    :columns="columns"
    row-key="host"
    :pagination="initialPagination"
    :hide-pagination="rockConfigs.length <= initialPagination.rowsPerPage"
    :loading="loading"
  >
    <template v-slot:top-left>
      <q-btn size="sm" icon="add" color="primary" :label="$t('add')" @click="onAdd"></q-btn>
    </template>
    <template v-slot:body-cell-host="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <a :href="props.value" target="_blank" class="text-primary">{{ props.value }}</a>
        <div class="float-right">
          <q-btn
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="$t('edit')"
            :icon="toolsVisible[props.row.host] ? 'edit' : 'none'"
            class="q-ml-xs"
            @click="onEdit(props.row)"
          />
          <q-btn
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="$t('remove')"
            :icon="toolsVisible[props.row.host] ? 'delete' : 'none'"
            class="q-ml-xs"
            @click="onDelete(props.row)"
          />
        </div>
      </q-td>
    </template>
  </q-table>

  <confirm-dialog
    v-model="showRemove"
    :title="$t('remove')"
    :text="$t('apps.remove_config_confirm', { host: selectedConfig.host })"
    @confirm="doRemove"
  />

  <add-r-server-dialog v-model="showAdd" :config="config" :rock-app-config="selectedConfig" @update="onUpdated" />
</template>

<script lang="ts">
export default defineComponent({
  name: 'AppsConfig',
});
</script>

<script setup lang="ts">
import { RockAppConfigDto } from 'src/models/Apps';
import HtmlAnchorHint from 'src/components/HtmlAnchorHint.vue';
import AddRServerDialog from 'src/components/admin/apps/AddRServerDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { notifyError } from 'src/utils/notify';

const { t } = useI18n();
const appsStore = useAppsStore();
const loading = ref(false);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const selectedConfig: RockAppConfigDto = {} as RockAppConfigDto;
const showAdd = ref(false);
const showRemove = ref(false);
const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});

const config = computed(() => appsStore.config);
const rockConfigs = computed(() => config.value.rockConfigs ?? []);
const columns = computed(() => [
  {
    name: 'host',
    required: true,
    label: t('host'),
    align: 'left',
    field: 'host',
    sortable: true,
    style: 'width: 25%',
  },
]);

async function init() {
  loading.value = true;
  return appsStore.initConfig().then(() => (loading.value = false));
}

async function doRemove() {
  try {
    const config = { ...appsStore.config };
    config.rockConfigs = config.rockConfigs.filter((c) => c.host !== selectedConfig.host);
    await appsStore.updateConfig(config);
    showRemove.value = false;
    return init();
  } catch (error) {
    notifyError(error);
  }
}

// Handlers

function onOverRow(row: RockAppConfigDto) {
  toolsVisible.value[row.host] = true;
}

function onLeaveRow(row: RockAppConfigDto) {
  toolsVisible.value[row.host] = false;
}

async function onAdd(row: RockAppConfigDto) {
  showAdd.value = true;
}

async function onEdit(row: RockAppConfigDto) {
  //return init();
}

function onUpdated() {
  showAdd.value = false;
  return init();
}

function onDelete(row: RockAppConfigDto) {
  selectedConfig.host = row.host;
  showRemove.value = true;
}

onMounted(() => init());
</script>
