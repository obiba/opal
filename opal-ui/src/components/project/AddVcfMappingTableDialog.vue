<template>
  <template>
    <q-dialog v-model="showDialog" persistent @hide="onHide">
      <q-card class="dialog-sm">
        <q-card-section>
          <div class="text-h6">{{ dialogTitle }}</div>
        </q-card-section>

        <q-separator />

        <q-card-section v-if="tables.length < 1">
          <q-banner inline-actions rounded class="bg-orange text-white">{{
            $t('vcf_store.no_mapping_tables')
          }}</q-banner>
        </q-card-section>

        <q-card-section v-else>
          <q-select
            v-model="selectedTable"
            :options="mappingOptions"
            :label="$t('table')"
            :hint="$t('vcf_store.mapping_table_hint')"
            dense
            emit-value
            map-options
            use-input
          >
            <template v-slot:option="scope">
              <q-item v-if="scope.opt.group" class="text-help" dense clickable disable :label="scope.opt.group">
                <q-item-section class="q-pa-none">
                  {{ scope.opt.group }}
                </q-item-section>
              </q-item>
              <q-item v-else dense clickable v-close-popup @click="onSelectTable(scope.opt.value)">
                <q-item-section class="q-pl-md">
                  {{ scope.opt.label }}
                </q-item-section>
              </q-item>
            </template>
          </q-select>

          <q-select
            v-model="selectedParticipantIdVariable"
            dense
            class="q-mb-md"
            :label="$t('vcf_store.participant_id')"
            :hint="$t('vcf_store.participant_id_hint')"
            :options="participantIdOptions"
            emit-value
            map-options
            @update:model-value="newMapping.participantIdVariable = $event.name"
          />

          <q-select
            v-model="selectedRoleVariable"
            dense
            class="q-mb-md"
            :label="$t('vcf_store.participant_id')"
            :hint="$t('vcf_store.participant_id_hint')"
            :options="roleOptions"
            emit-value
            map-options
            @update:model-value="newMapping.sampleRoleVariable = $event.name"
          />
        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="bg-grey-3">
          <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
          <q-btn flat :label="submitCaption" color="primary" @click="onAdd" v-close-popup />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </template>
</template>

<script lang="ts">
export default defineComponent({
  name: 'AddVcfMappingTableDialog',
});
</script>

<script setup lang="ts">
import { VCFSamplesMappingDto } from 'src/models/Plugins';
import { TableDto, VariableDto, CategoryDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  project: string
  mapping?: VCFSamplesMappingDto;
}

type GroupOption = { group: string } | { label: string; value: TableDto };

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'update']);
const projectsStore = useProjectsStore();
const datasourceStore = useDatasourceStore();
const tables = ref([] as TableDto[]);
const { t } = useI18n();
const showDialog = ref(props.modelValue);
const newMapping = ref({} as VCFSamplesMappingDto);
const editMode = computed(() => !!props.mapping && !!props.mapping.projectName);
const submitCaption = computed(() => (editMode.value ? t('update') : t('add')));
const dialogTitle = computed(() => (editMode.value ? t('vcf_store.edit_mapping') : t('vcf_store.add_mapping')));
const selectedTable = ref<TableDto | null>(null);
const selectedParticipantIdVariable = ref<VariableDto | null>(null);
const selectedRoleVariable = ref<VariableDto | null>(null);
const mappingOptions = ref([] as GroupOption[]);
const participantIdOptions = ref([] as { label: string; value: VariableDto }[]);
const roleOptions = ref([] as { label: string; value: VariableDto }[]);
let roleVariableSuggestion: VariableDto | undefined = undefined;
let participantIdVariableSuggestion: VariableDto | undefined = undefined;

function initMappingOptions() {
  if (tables.value.length > 0) {
    let lastGroup = '';
    tables.value.forEach((table) => {
      const tableRef = `${table.datasourceName}.${table.name}`;
      if (!!!selectedTable.value && newMapping.value.tableReference === tableRef) {
        selectedTable.value = table;
      }

      if (!!table.datasourceName && table.datasourceName !== lastGroup) {
        lastGroup = table.datasourceName;
        mappingOptions.value.push({ group: lastGroup });
      }
      mappingOptions.value.push({ label: table.name, value: table });
    });
  }
}

function initializeVariableOptions(variables: VariableDto[]) {
  (variables || []).forEach((variable) => {
    const variableName = variable.name;
    let roleCategory = null;
    const categories: CategoryDto[] = variable.categories || [];

    if (!!!roleVariableSuggestion) {
      if (categories.length > 0) {
        roleCategory = categories.find((category: CategoryDto) => {
          const categoryName = category.name.toLowerCase();
          return ['control', 'sample'].includes(categoryName);
        });
      }

      if (roleCategory || variableName.match(/role/i) != null) {
        roleVariableSuggestion = variable;
      }
    }

    if (!!!participantIdVariableSuggestion && variableName.match(/participant/i) != null) {
      participantIdVariableSuggestion = variable;
    }

    roleOptions.value.push({ label: variableName, value: variable });
    participantIdOptions.value.push({ label: variableName, value: variable });
  });

  if (!!participantIdVariableSuggestion) {
    selectedParticipantIdVariable.value = participantIdVariableSuggestion;
    newMapping.value.participantIdVariable = participantIdVariableSuggestion.name;
  }

  if (!!roleVariableSuggestion) {
    selectedRoleVariable.value = roleVariableSuggestion;
    newMapping.value.sampleRoleVariable = roleVariableSuggestion.name;
  }
}

async function getVariables() {
  if (selectedTable.value && selectedTable.value.datasourceName && selectedTable.value.name) {
    datasourceStore
      .getTableVariables(selectedTable.value.datasourceName, selectedTable.value.name)
      .then((variables) => {
        initializeVariableOptions(variables);
      });
  }
}

// Handlers

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      datasourceStore.getAllTables('Sample').then((response) => {
        tables.value = response;

        if (props.mapping && props.mapping.projectName) {
          newMapping.value = { ...props.mapping };
        } else {
          newMapping.value = {projectName: props.project} as VCFSamplesMappingDto;
        }

        initMappingOptions();
      });

      showDialog.value = true;
    }
  }
);

function onSelectTable(table: TableDto) {
  selectedTable.value = table;
  newMapping.value.tableReference = `${table.datasourceName}.${table.name}`;
  getVariables();
}

function onHide() {
  newMapping.value = {} as VCFSamplesMappingDto;
  selectedTable.value = null;
  mappingOptions.value = [];
  emit('update:modelValue', false);
}

async function onAdd() {
  try {
    await projectsStore.addVcfSamplesMapping(props.project, newMapping.value);
    emit('update');
    onHide();
  } catch (error) {
    notifyError(error);
  }
}
</script>
