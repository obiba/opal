<template>
  <div class="text-h6">{{ t('apps.self_register') }}</div>
  <html-anchor-hint
    class="text-help"
    trKey="apps.self_register_info"
    :text="t('apps.apps_admin')"
    url="https://opaldoc.obiba.org/en/latest/web-user-guide/administration/apps.html"
  />

  <div class="q-mt-sm q-gutter-sm row items-center">
    <q-btn size="sm" icon="edit" color="primary" :title="t('edit')" @click="onEditToken"></q-btn>
    <q-btn size="sm" icon="delete" color="negative" outline :title="t('delete')" @click="onClearToken"></q-btn>

    <span v-if="config.token" class="on-right">
      <code>{{ config.token }}</code>
      <q-btn
        flat
        dense
        size="sm"
        icon="content_copy"
        :title="t('clipboard.copy')"
        @click="onCopyToClipboard"
        aria-label="Copy to clipboard"
        class="on-right"
      />
    </span>
  </div>

  <div class="text-h6 q-mt-lg">{{ t('discovery') }}</div>
  <html-anchor-hint
    class="text-help"
    trKey="apps.discovery_info"
    :text="t('apps.apps_admin')"
    url="https://opaldoc.obiba.org/en/latest/web-user-guide/administration/apps.html"
  />

  <div class="text-bold q-mt-md">Rock</div>
  <html-anchor-hint class="text-help" trKey="apps.rock_info" text="OBiBa/Rock" url="https://rockdoc.obiba.org/" />
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
      <q-btn size="sm" icon="add" color="primary" :title="t('add')" @click="onAdd"></q-btn>
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
            :title="t('edit')"
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
            :title="t('remove')"
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
    :title="t('remove')"
    :text="t('apps.remove_config_confirm', { host: selectedConfig.host })"
    @confirm="doRemove"
  />

  <add-app-token-dialog v-model="showEditToken" :token="config.token || ''" @update="onTokenEdited" />

  <add-r-server-dialog
    v-model="showAdd"
    :config="config"
    :rock-app-config="selectedConfig"
    @update:model-value="onClose"
    @update="onUpdated"
  />
</template>

<script setup lang="ts">
import type { RockAppConfigDto } from 'src/models/Apps';
import HtmlAnchorHint from 'src/components/HtmlAnchorHint.vue';
import AddRServerDialog from 'src/components/admin/apps/AddRServerDialog.vue';
import AddAppTokenDialog from 'src/components/admin/apps/AddAppTokenDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { copyToClipboard } from 'quasar';
import { notifyError, notifySuccess } from 'src/utils/notify';
import { DefaultAlignment } from 'src/components/models';

const { t } = useI18n();
const appsStore = useAppsStore();
const loading = ref(false);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const selectedConfig = ref({} as RockAppConfigDto);
const showAdd = ref(false);
const showRemove = ref(false);
const showEditToken = ref(false);
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
    align: DefaultAlignment,
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
    config.rockConfigs = config.rockConfigs.filter((c) => c.host !== selectedConfig.value.host);
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

function onEditToken() {
  showEditToken.value = true;
}

function onClearToken() {
  config.value.token = '';
}

function onTokenEdited(newToken: string) {
  showEditToken.value = false;
  config.value.token = newToken;
}

function onCopyToClipboard() {
  copyToClipboard(config.value.token || '')
    .then(() => {
      notifySuccess(t('clipboard.copied'));
    })
    .catch(() => {
      notifyError(t('clipboard.failed'));
    });
}

async function onAdd() {
  showAdd.value = true;
}

async function onEdit(row: RockAppConfigDto) {
  selectedConfig.value = row;
  showAdd.value = true;
}

function onClose() {
  showAdd.value = false;
  selectedConfig.value = {} as RockAppConfigDto;
}

function onUpdated() {
  return init();
}

function onDelete(row: RockAppConfigDto) {
  selectedConfig.value = row;
  showRemove.value = true;
}

onMounted(() => init());
</script>
