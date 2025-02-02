<template>
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
        <q-btn no-caps color="primary" icon="add" size="sm" :label="t('add')" @click="onAddMapping" />
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
            :title="t('edit')"
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
            :title="t('delete')"
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
            :title="t('id_mappings.generate_identifiers')"
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
            :title="t('id_mappings.download_identifiers')"
            :icon="toolsVisible[props.row.name] ? 'download' : 'none'"
            class="q-ml-xs"
            @click="onExportMappingIdentifiers(props.row.name)"
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

  <confirm-dialog v-model="showConfirm" :title="confirm.title" :text="confirm.text" @confirm="confirm.onCallback" />

  <add-mapping-dialog
    v-model="showAddMapping"
    :identifier="props.identifierTable"
    :mapping="selectedMapping"
    @update:model-value="onCloseMappingDialog"
    @update="onMappingAdded"
  />

  <generate-mapping-identifiers-dialog
    v-model="showGenerateIdentifiers"
    :identifier="props.identifierTable"
    :mapping="selectedMapping"
    @update:model-value="onCloseMappingDialog"
    @update="onGenerateIdentifiers"
  />
</template>

<script setup lang="ts">
import type { TableDto, VariableDto } from 'src/models/Magma';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import AddMappingDialog from 'src/components/admin/identifiers/AddMappingDialog.vue';
import GenerateMappingIdentifiersDialog from 'src/components/admin/identifiers/GenerateMappingIdentifiersDialog.vue';
import { notifyError } from 'src/utils/notify';
import { baseUrl } from 'src/boot/api';
import { DefaultAlignment } from 'src/components/models';

interface Props {
  identifierTable: TableDto;
  mappings: VariableDto[];
}

const props = defineProps<Props>();
const emits = defineEmits(['update']);
const { t } = useI18n();
const identifiersStore = useIdentifiersStore();
const toolsVisible = ref<{ [key: string]: boolean }>({});
const selectedMapping = ref({} as VariableDto);
const loading = ref(false);
const showConfirm = ref(false);
const showAddMapping = ref(false);
const showGenerateIdentifiers = ref(false);
const confirm = ref({ title: '', text: '', onCallback: () => ({}) });
const initialPagination = ref({
  sortBy: 'resource',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});
const columns = computed(() => [
  {
    name: 'name',
    label: t('name'),
    align: DefaultAlignment,
    field: 'name',
    headerStyle: 'width: 35%; white-space: normal;',
    style: 'width: 35%; white-space: normal;',
  },
  {
    name: 'description',
    label: t('description'),
    align: DefaultAlignment,
    field: (mapping: VariableDto) => {
      const description = (mapping.attributes || []).find((a) => a.name === 'description');
      return description ? description.value : '';
    },
  },
]);

async function _onDeleteMapping() {
  try {
    await identifiersStore.deleteMapping(props.identifierTable.name, selectedMapping.value.name);
    selectedMapping.value = {} as VariableDto;
    confirm.value = { title: '', text: '', onCallback: () => ({}) };
    emits('update');
  } catch (error) {
    notifyError(error);
  }
}

function onAddMapping() {
  showAddMapping.value = true;
  selectedMapping.value = {} as VariableDto;
}

function onEditMapping(row: VariableDto) {
  showAddMapping.value = true;
  selectedMapping.value = row;
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

function onGenerateIdentifiers(row: VariableDto) {
  selectedMapping.value = row;
  showGenerateIdentifiers.value = true;
}

function onCloseMappingDialog() {
  selectedMapping.value = {} as VariableDto;
}

async function onMappingAdded() {
  onCloseMappingDialog();
  emits('update');
}

function onExportMappingIdentifiers(mappingName: string) {
  window.open(`${baseUrl}/identifiers/mapping/${mappingName}/_export?type=${props.identifierTable.entityType}`);
}

function onOverRow(row: VariableDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: VariableDto) {
  toolsVisible.value[row.name] = false;
}
</script>
