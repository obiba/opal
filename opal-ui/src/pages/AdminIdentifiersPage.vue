<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="t('id_mappings.title')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="q-gutter-md">
        <div class="text-h5">
          {{ t('id_mappings.title') }}
        </div>
        <div class="text-help q-mb-md">
          {{ t('id_mappings.info') }}
        </div>
      </div>

      <q-banner v-if="!hasIdsDatabase" inline-actions rounded class="bg-warning">
        {{ t('id_mappings.no_database_warning') }}

        <template v-slot:action>
          <q-btn flat :label="t('configure')" to="/admin/databases" />
        </template>
      </q-banner>
      <div v-else class="row q-gutter-md">
        <div class="col">
          <div class="text-h6 q-mb-md row q-gutter-sm items-center">
            <span>{{ t('id_mappings.ids_list_title') }}</span>
            <q-btn
              size="sm"
              icon="add"
              color="primary"
              outline
              :title="t('add')"
              @click="onAddIdentifierTable"
            ></q-btn>
            <q-btn
              v-if="hasIdentifiersTables"
              size="sm"
              icon="delete"
              color="negative"
              outline
              :title="t('delete')"
              @click="onDeleteIdentifierTable"
            ></q-btn>
          </div>
          <q-list dense padding>
            <q-item
              clickable
              active-class="bg-grey-2"
              v-for="(identifier, index) in identifiersTables"
              :key="index"
              :active="identifier.name === selectedIdentifierTable?.name"
              @click="onSelectIdentifierTable(identifier)"
            >
              <q-item-section class="q-pa-none q-mr-sm text-caption"> {{ identifier.entityType }} </q-item-section>
            </q-item>
          </q-list>
        </div>

        <div v-if="hasIdentifiersTables" class="col-9">
          <div class="text-h6">
            {{ selectedIdentifierTable?.entityType }}
            <q-btn :label="t('export')" color="secondary" icon="output" @click="onExportIdentifiers" size="sm" />
            <q-btn-dropdown class="q-ml-sm" color="secondary" :label="t('import')" icon="input" size="sm">
              <q-list>
                <q-item clickable v-close-popup @click.prevent="onImportSystemIdentifiersList">
                  <q-item-section>
                    <q-item-label>{{ t('id_mappings.import_identifiers_list') }}</q-item-label>
                  </q-item-section>
                </q-item>

                <q-item clickable v-close-popup @click.prevent="onImportTableSystemIdentifiersList">
                  <q-item-section>
                    <q-item-label>{{ t('id_mappings.import_identifiers_table') }}</q-item-label>
                  </q-item-section>
                </q-item>

                <q-item clickable v-close-popup @click.prevent="onImportMappedIdentifiers">
                  <q-item-section>
                    <q-item-label>{{ t('id_mappings.import_identifiers_mapping') }}</q-item-label>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-btn-dropdown>
          </div>

          <fields-list class="q-mt-md" :items="properties" :dbobject="selectedIdentifierTable" />

          <q-tabs
            v-model="tab"
            dense
            class="text-grey q-mt-lg"
            active-color="primary"
            indicator-color="primary"
            align="justify"
          >
            <q-tab name="mappings" :label="t('mappings')" />
            <q-tab name="identifiers" :label="t('identifiers')" />
          </q-tabs>

          <q-separator />

          <q-tab-panels v-model="tab">
            <q-tab-panel name="mappings">
              <table-mappings-list
                :identifier-table="selectedIdentifierTable"
                :mappings="mappings"
                @update="onMappingUpdated"
              />
            </q-tab-panel>
            <q-tab-panel name="identifiers">
              <table-identifiers-list :identifier-table="selectedIdentifierTable" />
            </q-tab-panel>
          </q-tab-panels>
        </div>
      </div>
      <confirm-dialog v-model="showConfirm" :title="confirm.title" :text="confirm.text" @confirm="confirm.onCallback" />

      <add-identifier-table-dialog v-model="showAddIdentifierTable" @update="onIdentifierAdded" />

      <import-system-identifiers-list
        v-model="showImportList"
        :identifier="selectedIdentifierTable"
        @update="onMappingUpdated"
      />

      <import-table-system-identifiers-list
        v-model="showImportTableList"
        :identifier="selectedIdentifierTable"
        @update="onMappingUpdated"
      />

      <import-mapped-identifiers
        v-model="showImportMapped"
        :identifier="selectedIdentifierTable"
        @update="onMappingUpdated"
      />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import type { TableDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';
import FieldsList from 'src/components/FieldsList.vue';
import { getDateLabel } from 'src/utils/dates';
import AddIdentifierTableDialog from 'src/components/admin/identifiers/AddIdentifierTableDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import ImportSystemIdentifiersList from 'src/components/admin/identifiers/ImportSystemIdentifiersList.vue';
import ImportTableSystemIdentifiersList from 'src/components/admin/identifiers/ImportTableSystemIdentifiersList.vue';
import ImportMappedIdentifiers from 'src/components/admin/identifiers/ImportMappedIdentifiers.vue';
import TableIdentifiersList from 'src/components/admin/identifiers/TableIdentifiersList.vue';
import TableMappingsList from 'src/components/admin/identifiers/TableMappingsList.vue';
import { baseUrl } from 'src/boot/api';

const { t } = useI18n();
const loading = ref(false);

const tab = ref('mappings');
const systemStore = useSystemStore();
const identifiersStore = useIdentifiersStore();
const selectedIdentifierTable = ref({} as TableDto);
const confirm = ref({ title: '', text: '', onCallback: () => ({}) });
const hasIdsDatabase = ref(true);
const showConfirm = ref(false);
const showAddIdentifierTable = ref(false);
const showImportList = ref(false);
const showImportTableList = ref(false);
const showImportMapped = ref(false);
const identifiersTables = ref([] as TableDto[]);
const hasIdentifiersTables = computed(() => identifiersTables.value.length > 0);
const mappings = computed(() => identifiersStore.mappings || []);
const properties = computed(() => {
  return [
    {
      field: 'timestamps',
      label: 'last_update',
      format: (val: TableDto) => (val ? getDateLabel(val.timestamps?.lastUpdate) : ''),
    },
    {
      field: 'variableCount',
      label: 'id_mappings.mappings_count',
    },
    {
      field: 'valueSetCount',
      label: 'id_mappings.system_ids_count',
    },
  ];
});

async function getIdentifiersTables() {
  identifiersStore
    .initIdentifiersTables()
    .then(() => {
      identifiersTables.value = identifiersStore.identifiers || [];
      if (identifiersTables.value.length > 0) {
        const candidate = selectedIdentifierTable.value.name
          ? identifiersTables.value.find((id) => id.name === selectedIdentifierTable.value.name)
          : identifiersTables.value[0];
        if (candidate) onSelectIdentifierTable(candidate);
      } else {
        selectedIdentifierTable.value = {} as TableDto;
      }
    })
    .catch(notifyError);
}

async function getMappings(identifierName: string) {
  loading.value = true;
  return identifiersStore.initMappings(identifierName).then(() => {
    loading.value = false;
  });
}

async function onSelectIdentifierTable(identifier: TableDto) {
  selectedIdentifierTable.value = identifier;
  getMappings(identifier.name);
}

async function _onDeleteIdentifierTable() {
  try {
    await identifiersStore.deleteIdentifierTable(selectedIdentifierTable.value);
    selectedIdentifierTable.value = {} as TableDto;
    confirm.value = { title: '', text: '', onCallback: () => ({}) };
    await getIdentifiersTables();
  } catch (error) {
    notifyError(error);
  }
}

function onAddIdentifierTable() {
  showAddIdentifierTable.value = true;
}

function onIdentifierAdded(updated: TableDto) {
  selectedIdentifierTable.value = updated;
  getIdentifiersTables();
}

function onDeleteIdentifierTable() {
  showConfirm.value = true;
  confirm.value = {
    title: t('id_mappings.delete_identifier'),
    text: t('id_mappings.delete_identifier_confirm', { entityType: selectedIdentifierTable.value.entityType }),
    onCallback: async () => await _onDeleteIdentifierTable(),
  };
}

function onExportIdentifiers() {
  window.open(`${baseUrl}/identifiers/mappings/_export?type=${selectedIdentifierTable.value.entityType}`);
}

function onImportSystemIdentifiersList() {
  showImportList.value = true;
}

function onImportTableSystemIdentifiersList() {
  showImportTableList.value = true;
}

async function onImportMappedIdentifiers() {
  showImportMapped.value = true;
}

async function onMappingUpdated() {
  getIdentifiersTables();
}

onMounted(() => {
  systemStore
    .getDatabasesStatus()
    .then((status) => {
      hasIdsDatabase.value = status.hasIdentifiers;
      if (hasIdsDatabase.value) {
        getIdentifiersTables();
      }
    })
    .catch(notifyError);
});
</script>
