<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card>
      <q-card-section>
        <div class="text-h6">{{ t('id_mappings.title') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form class="q-gutter-md" persistent>
          <q-select
            v-model="selectedId"
            dense
            :options="idOptions"
            :label="t('entity_type')"
            :hint="t('project_admin.entity_type_hint')"
            class="q-mb-md q-pt-md"
            emit-value
            map-options
          />

          <q-select
            v-model="selectedMappings"
            dense
            :options="mappingOptions"
            :label="t('id_mappings.title')"
            :hint="t('project_admin.id_mappings_hint')"
            emit-value
            map-options
          ></q-select>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('add')" type="submit" color="primary" @click="onAddMapping" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { ProjectDto, ProjectDto_IdentifiersMappingDto } from 'src/models/Projects';
import type { TableDto, VariableDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  project: ProjectDto;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'update']);
const projectsStore = useProjectsStore();
const identifiersStore = useIdentifiersStore();
const { t } = useI18n();

const showDialog = ref(props.modelValue);
const mappingOptions = ref([] as { label: string; value: VariableDto }[]);
const idOptions = ref([] as { label: string; value: TableDto }[]);
const selectedId = ref<TableDto | null>(null);
const selectedMappings = ref<VariableDto | null>(null);

async function initMapping(identifier: string) {
  identifiersStore.initMappings(identifier).then(() => {
    mappingOptions.value = identifiersStore.mappings.map((mapping) => ({
      label: mapping.name,
      value: mapping,
    }));
    selectedMappings.value = mappingOptions.value[0]?.value || null;
  });
}

// Handlers

function onHide() {
  selectedId.value = null;
  selectedMappings.value = null;
  mappingOptions.value = [];
  idOptions.value = [];
  showDialog.value = false;
  emit('update:modelValue', false);
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      idOptions.value = identifiersStore.identifiers.map((id) => ({ label: id.entityType, value: id }));
      selectedId.value = idOptions.value[0]?.value || null;
      if (selectedId.value)
        initMapping(selectedId.value.name);
      showDialog.value = value;
    }
  }
);

async function onAddMapping() {
  try {
    await projectsStore.addIdMappings(props.project, {
      name: selectedId.value?.name,
      entityType: selectedId.value?.entityType,
      mapping: selectedMappings.value?.name,
    } as ProjectDto_IdentifiersMappingDto);
    emit('update');
    onHide();
  } catch (err) {
    notifyError(err);
  }
}
</script>
