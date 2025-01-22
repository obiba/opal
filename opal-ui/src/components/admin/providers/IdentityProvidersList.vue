<template>
  <div>
    <div class="text-h5 q-mb-md">
      {{ t('identity_providers') }}
    </div>
    <div
      class="text-help q-mb-md"
      v-html="t('identity_providers_info', { idProvider: idProviderDefinition, openId: openIdDefinition })"
    ></div>
    <q-table
      flat
      :rows="providers"
      :columns="columns"
      row-key="resource"
      binary-state-sort
      :pagination="initialPagination"
      :hide-pagination="providers.length <= initialPagination.rowsPerPage"
      :loading="loading"
    >
      <template v-slot:top-left>
        <q-btn no-caps color="primary" icon="add" size="sm" :label="t('add')" @click="onAddProfile" />
      </template>
      <template v-slot:body-cell-name="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <span>{{ props.value }}</span>
          <div class="float-right">
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="t('edit')"
              :icon="toolsVisible[props.row.name] ? 'edit' : 'none'"
              class="q-ml-xs"
              @click="onEditProvider(props.row)"
            />
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="t('duplicate')"
              :icon="toolsVisible[props.row.name] ? 'content_copy' : 'none'"
              class="q-ml-xs"
              @click="onDuplicateProvider(props.row)"
            />
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="props.row.enabled ? t('disable') : t('enable')"
              :icon="toolsVisible[props.row.name] ? (props.row.enabled ? 'close' : 'check') : 'none'"
              class="q-ml-xs"
              @click="onEnableProvider(props.row)"
            />
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="t('delete')"
              :icon="toolsVisible[props.row.name] ? 'delete' : 'none'"
              class="q-ml-xs"
              @click="onDeleteProvider(props.row)"
            />
          </div>
        </q-td>
      </template>
      <template v-slot:body-cell-groups="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-chip class="q-ml-none" v-for="(group, index) in props.col.format(props.row.groups)" :key="index">{{
            group
          }}</q-chip>
        </q-td>
      </template>
      <template v-slot:body-cell-providerUrl="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <a :href="props.value" target="_blank">
            {{ t('identity_provider_url') }}<q-icon name="open_in_new" style="margin-left: 3px"></q-icon>
          </a>
        </q-td>
      </template>
      <template v-slot:body-cell-parameters="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <a :href="props.value" target="_blank">
            {{ t('identity_provider_discovery_uri') }}<q-icon name="open_in_new" style="margin-left: 3px"></q-icon>
          </a>
        </q-td>
      </template>
      <template v-slot:body-cell-enabled="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
            <q-icon :name="props.value ? 'check' : 'close'" size="sm" />
          </q-td>
        </q-td>
      </template>
    </q-table>

    <confirm-dialog
      v-if="selectedProvider"
      v-model="showDelete"
      :title="t('delete')"
      :text="t('delete_identity_provider_confirm', { provider: selectedProvider.name })"
      @confirm="doDeleteProvider"
    />

    <add-identity-provider-dialog
      v-model="showAddProfile"
      :provider="selectedProvider"
      @update:modelValue="onProfileAdded"
    ></add-identity-provider-dialog>
  </div>
</template>

<script setup lang="ts">
import { notifyError } from 'src/utils/notify';
import type { IDProviderDto } from 'src/models/Opal';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import AddIdentityProviderDialog from './AddIdentityProviderDialog.vue';
import { DefaultAlignment } from 'src/components/models';

const identityProvidersStore = useIdentityProvidersStore();
const { t } = useI18n();
const loading = ref(false);
const providers = computed(() => identityProvidersStore.providers || []);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const selectedProvider = ref<IDProviderDto | null>(null);
const showDelete = ref(false);
const showAddProfile = ref(false);
// NOTE: Using interpolation mute i18n warnings for using html fragments in messages
const idProviderDefinition = computed(
  () => `<a href="https://en.wikipedia.org/wiki/Identity_provider" target="_blank">${t('identity_provider.title')}</a>`
);
const openIdDefinition = computed(
  () => `<a href="https://en.wikipedia.org/wiki/OpenID_Connect" target="_blank">${t('openid_connect')}</a>`
);

const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: DefaultAlignment,
    field: 'name',
    format: (val: string) => val,
    sortable: true,
    style: 'width: 25%',
  },
  {
    name: 'label',
    label: t('label'),
    align: DefaultAlignment,
    field: 'label',
    format: (val: string) => val,
  },
  {
    name: 'groups',
    label: t('groups'),
    align: DefaultAlignment,
    field: 'groups',
    format: (val: string) => ((val) => (val || '').split(/\s+/))(val).filter((group) => group),
  },
  {
    name: 'providerUrl',
    label: t('identity_provider_account_login'),
    align: DefaultAlignment,
    field: 'providerUrl',
    format: (val: string) => val,
  },
  {
    name: 'parameters',
    label: t('parameters'),
    align: DefaultAlignment,
    field: 'discoveryURI',
    format: (val: string) => val,
  },
  {
    name: 'enabled',
    label: t('enabled'),
    align: DefaultAlignment,
    field: 'enabled',
    format: (val: string) => val,
  },
]);

const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});

function onOverRow(row: IDProviderDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: IDProviderDto) {
  toolsVisible.value[row.name] = false;
}

function onEditProvider(provider: IDProviderDto) {
  showAddProfile.value = true;
  selectedProvider.value = provider;
}

function onDuplicateProvider(provider: IDProviderDto) {
  const clone = { ...provider };
  clone.name = '';
  onEditProvider(clone);
}

async function onEnableProvider(provider: IDProviderDto) {
  await identityProvidersStore.toggleEnableProvider(provider);
  await identityProvidersStore.initProviders();
}

async function onDeleteProvider(provider: IDProviderDto) {
  showDelete.value = true;
  selectedProvider.value = provider;
}

function onAddProfile() {
  showAddProfile.value = true;
}

async function onProfileAdded() {
  identityProvidersStore.initProviders();
  showAddProfile.value = false;
  selectedProvider.value = null;
}

async function doDeleteProvider() {
  showDelete.value = false;

  if (selectedProvider.value === null) {
    return;
  }

  const toDelete: IDProviderDto | null = selectedProvider.value;
  selectedProvider.value = null;

  try {
    await identityProvidersStore.deleteProvider(toDelete);
    await identityProvidersStore.initProviders();
  } catch (err) {
    notifyError(err);
  }
}

onMounted(async () => {
  loading.value = true;
  identityProvidersStore.initProviders().then(() => {
    loading.value = false;
  });
});
</script>
