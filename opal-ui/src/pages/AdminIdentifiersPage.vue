<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('id_mappings.title')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-help q-mb-md">
        {{ $t('id_mappings.info') }}
      </div>
      <div class="row q-gutter-md">
        <div class="col">
          <div class="text-h5 q-mb-md row q-gutter-sm items-center">
            <span>{{ $t('id_mappings.ids_list_title') }}</span>
            <q-btn size="sm" icon="add" color="primary" outline :title="$t('add')" @click="onAddIdentifier"></q-btn>
            <q-btn
              v-if="hasIdentifiers"
              size="sm"
              icon="delete"
              color="negative"
              outline
              :title="$t('delete')"
              @click="onDeleteIdentifier"
            ></q-btn>
          </div>
          <q-list dense padding>
            <q-item
              clickable
              active-class="bg-grey-2"
              v-for="(identifier, index) in identifiers"
              :key="index"
              :active="identifier.name === selectedIdentifier?.name"
              @click="onSelectIdentifier(identifier)"
            >
              <q-item-section class="q-pa-none q-mr-sm text-caption"> {{ identifier.entityType }} </q-item-section>
            </q-item>
          </q-list>
        </div>

        <div v-if="hasIdentifiers" class="col-9">
          <div class="text-h5">
            {{ selectedIdentifier?.entityType }}
          </div>

          <fields-list :items="properties" :dbobject="selectedIdentifier" />

          <q-tabs
            v-model="tab"
            dense
            class="text-grey q-mt-lg"
            active-color="primary"
            indicator-color="primary"
            align="justify"
          >
            <q-tab name="mappings" :label="$t('mappings')" />
            <q-tab name="identifiers" :label="$t('identifiers')" />
          </q-tabs>

          <q-separator />

          <q-tab-panels v-model="tab">
            <q-tab-panel name="mappings">
              <q-table
                flat
                :rows="mappings"
                :columns="columns"
                row-key="name"
                :pagination="initialPagination"
                :hide-pagination="mappings.length <= initialPagination.rowsPerPage"
                :loading="loading"
              >
                <template v-slot:top-left>
                  <div class="q-gutter-sm">
                    <q-btn no-caps color="primary" icon="add" size="sm" :label="$t('add')" @click="onAddMapping" />
                    <q-btn-dropdown
                      color="primary"
                      :label="$t('id_mappings.import_identifiers')"
                      icon="input"
                      size="sm"
                    >
                      <q-list>
                        <q-item clickable v-close-popup @click.prevent="onImportIdentifiersList">
                          <q-item-section>
                            <q-item-label>{{ $t('id_mappings.import_identifiers_list') }}</q-item-label>
                          </q-item-section>
                        </q-item>

                        <q-item clickable v-close-popup @click.prevent="(onAddWithCertificate) => ({})">
                          <q-item-section>
                            <q-item-label>{{ $t('id_mappings.import_identifiers_table') }}</q-item-label>
                          </q-item-section>
                        </q-item>

                        <q-item clickable v-close-popup @click.prevent="(onAddWithCertificate) => ({})">
                          <q-item-section>
                            <q-item-label>{{ $t('id_mappings.import_identifiers_mapping') }}</q-item-label>
                          </q-item-section>
                        </q-item>
                      </q-list>
                    </q-btn-dropdown>
                  </div>
                </template>
                <template v-slot:body-cell-name="props">
                  <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
                    {{ props.value }}
                    <div class="float-right">
                      <q-btn
                        rounded
                        dense
                        flat
                        size="sm"
                        color="secondary"
                        :title="$t('edit')"
                        :icon="toolsVisible[props.row.name] ? 'edit' : 'none'"
                        class="q-ml-xs"
                        @click="onEditMapping(props.row)"
                      />
                      <q-btn
                        rounded
                        dense
                        flat
                        size="sm"
                        color="secondary"
                        :title="$t('delete')"
                        :icon="toolsVisible[props.row.name] ? 'delete' : 'none'"
                        class="q-ml-xs"
                        @click="onDeleteMapping(props.row)"
                      />
                      <q-btn
                        rounded
                        dense
                        flat
                        size="sm"
                        color="secondary"
                        :title="$t('id_mappings.generate_identifiers')"
                        :icon="toolsVisible[props.row.name] ? 'autorenew' : 'none'"
                        class="q-ml-xs"
                        @click="onGenerateIdentifiers(props.row)"
                      />
                      <q-btn
                        rounded
                        dense
                        flat
                        size="sm"
                        color="secondary"
                        :title="$t('id_mappings.download_identifiers')"
                        :icon="toolsVisible[props.row.name] ? 'download' : 'none'"
                        class="q-ml-xs"
                        @click="(onEnableUser) => ({})"
                      />
                    </div>
                  </q-td>
                </template>
                <template v-slot:body-cell-description="props">
                  <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
                    <span>{{ props.value }}</span>
                  </q-td>
                </template>
              </q-table>
            </q-tab-panel>
            <q-tab-panel name="identifiers"> </q-tab-panel>
          </q-tab-panels>
        </div>
      </div>
      <confirm-dialog v-model="showConfirm" :title="confirm.title" :text="confirm.text" @confirm="confirm.onCallback" />

      <add-identifier-dialog v-model="showAddIdentifier" @update="onIdentifierAdded" />

      <add-mapping-dialog
        v-model="showAddMapping"
        :identifier="selectedIdentifier"
        :mapping="selectedMapping"
        @update:model-value="onCloseMappingDialog"
        @update="onMappingAdded"
      />

      <generate-mapping-identifiers-dialog
        v-model="showGenerateIdentifiers"
        :identifier="selectedIdentifier"
        :mapping="selectedMapping"
        @update:model-value="onCloseMappingDialog"
        @update="onGenerateIdentifiers"
      />

      <import-identifiers-list v-model="showImportList" :identifier="selectedIdentifier" @update="onIdentifierAdded" />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import { TableDto, VariableDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';
import FieldsList, { FieldItem } from 'src/components/FieldsList.vue';
import { getDateLabel } from 'src/utils/dates';
import AddIdentifierDialog from 'src/components/admin/identifiers/AddIdentifierDialog.vue';
import AddMappingDialog from 'src/components/admin/identifiers/AddMappingDialog.vue';
import GenerateMappingIdentifiersDialog from 'src/components/admin/identifiers/GenerateMappingIdentifiersDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import ImportIdentifiersList from 'src/components/admin/identifiers/ImportIdentifiersList.vue';

const { t } = useI18n();
const initialPagination = ref({
  sortBy: 'resource',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});
const loading = ref(false);
const tab = ref('mappings');
const identifiersStore = useIdentifiersStore();
const selectedIdentifier = ref({} as TableDto);
const selectedMapping = ref({} as VariableDto);
const confirm = ref({ title: '', text: '', onCallback: () => ({}) });
const showConfirm = ref(false);
const showAddMapping = ref(false);
const showAddIdentifier = ref(false);
const showGenerateIdentifiers = ref(false);
const showImportList = ref(false);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const identifiers = ref([] as TableDto[]);
const hasIdentifiers = computed(() => identifiers.value.length > 0);
const mappings = computed(() => identifiersStore.mappings || []);
const columns = computed(() => [
  {
    name: 'name',
    label: t('name'),
    align: 'left',
    field: 'name',
    headerStyle: 'width: 35%; white-space: normal;',
    style: 'width: 35%; white-space: normal;',
  },
  { name: 'description', label: t('description'), align: 'left' },
]);
const properties: FieldItem<TableDto>[] = computed(() => {
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

async function getIdentifiers() {
  identifiersStore
    .initIdentifiers()
    .then(() => {
      identifiers.value = identifiersStore.identifiers || [];
      if (identifiers.value.length > 0) {
        const candidate = selectedIdentifier.value.name
          ? identifiers.value.find((id) => id.name === selectedIdentifier.value.name)
          : identifiers.value[0];
        if (candidate) onSelectIdentifier(candidate);
      } else {
        selectedIdentifier.value = {} as TableDto;
      }
    })
    .catch(notifyError);
}

async function getMappings(identifierName: string) {
  console.log('Getting mappings for', identifierName);
  loading.value = true;
  return identifiersStore.initMappings(identifierName).then(() => {
    loading.value = false;
  });
}

async function onSelectIdentifier(identifier: TableDto) {
  selectedIdentifier.value = identifier;
  getMappings(identifier.name);
}

async function _onDeleteIdentifier() {
  try {
    await identifiersStore.deleteIdentifier(selectedIdentifier.value);
    selectedIdentifier.value = {} as TableDto;
    confirm.value = { title: '', text: '', onCallback: () => ({}) };
    await getIdentifiers();
  } catch (error) {
    notifyError(error);
  }
}

async function _onDeleteMapping() {
  try {
    await identifiersStore.deleteMapping(selectedIdentifier.value.name, selectedMapping.value.name);
    selectedMapping.value = {} as VariableDto;
    confirm.value = { title: '', text: '', onCallback: () => ({}) };
    await getIdentifiers();
  } catch (error) {
    notifyError(error);
  }
}

function onAddIdentifier() {
  showAddIdentifier.value = true;
}

function onIdentifierAdded() {
  getIdentifiers();
}

function onAddMapping() {
  showAddMapping.value = true;
  selectedMapping.value = {} as VariableDto;
}

function onEditMapping(row: VariableDto) {
  showAddMapping.value = true;
  selectedMapping.value = row;
}

function onDeleteIdentifier() {
  showConfirm.value = true;
  confirm.value = {
    title: t('id_mappings.delete_identifier'),
    text: t('id_mappings.delete_identifier_confirm', { entityType: selectedIdentifier.value.entityType }),
    onCallback: async () => await _onDeleteIdentifier(),
  };
}

function onImportIdentifiersList() {
  showImportList.value = true;
}

async function onIdentifiersImported() {
  //
}

function onGenerateIdentifiers(row: VariableDto) {
  selectedMapping.value = row;
  showGenerateIdentifiers.value = true;
}

function onDeleteMapping(row: VariableDto) {
  selectedMapping.value = row;
  showConfirm.value = true;
  confirm.value = {
    title: t('id_mappings.delete_mapping'),
    text: t('id_mappings.delete_mapping_confirm', { name: row.name }),
    onCallback: async () => await _onDeleteMapping(),
  };
}

function onOverRow(row: VariableDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: VariableDto) {
  toolsVisible.value[row.name] = false;
}

function onCloseMappingDialog() {
  selectedMapping.value = {} as VariableDto;
}

async function onMappingAdded() {
  onCloseMappingDialog();
  getIdentifiers();
}

onMounted(() => getIdentifiers());
</script>
