<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t(editMode ? 'edit_variable' : 'add_variable') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-input
          v-model="selected.name"
          dense
          :label="t('name')"
          :hint="t('variable_name_hint')"
          class="q-mb-md"
          :disable="editMode && !isView"
        />
        <q-select
          v-model="selected.valueType"
          :options="ValueTypes"
          :label="t('value_type')"
          :hint="t('value_type_hint')"
          :disable="!canEditTypeFields"
          dense
          class="q-mb-sm"
        />
        <div class="row q-mb-md">
          <div class="col">
            <q-checkbox
              v-model="selected.isRepeatable"
              :label="t('repeatable')"
              :disable="!canEditTypeFields"
              dense
              class="q-mt-md q-mb-sm"
            />
            <div class="text-hint">
              {{ t('repeatable_hint') }}
            </div>
          </div>
          <div class="col">
            <q-input
              v-model="selected.occurrenceGroup"
              :label="t('occurrence_group')"
              :hint="t('occurrence_group_hint')"
              :disable="!selected.isRepeatable"
              dense
              class="q-mb-md"
            />
          </div>
        </div>
        <q-input v-model="selected.unit" dense :label="t('unit')" :hint="t('unit_hint')" class="q-mb-md" />
        <q-input
          v-model="selected.referencedEntityType"
          dense
          :label="t('referenced_entity_type')"
          :hint="t('referenced_entity_type_hint')"
          class="q-mb-md"
        />
        <q-input
          v-model="selected.mimeType"
          dense
          :label="t('mime_type')"
          :hint="t('mime_type_hint')"
          class="q-mb-md"
        />
        <q-input
          v-model.number="selected.index"
          dense
          type="number"
          :label="t('index')"
          :hint="t('index_hint')"
          class="q-mb-md"
        />
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('save')" color="primary" :disable="!isValid" @click="onSave" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { VariableDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';
import { ValueTypes } from 'src/utils/magma';

interface DialogProps {
  modelValue: boolean;
  variable: VariableDto;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'save']);

const { t } = useI18n();
const datasourceStore = useDatasourceStore();

const showDialog = ref(props.modelValue);
const editMode = ref(false);
const selected = ref({ ...props.variable });

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      selected.value = { ...props.variable };
      editMode.value = selected.value.name !== '' && selected.value.name !== undefined;
    }
    showDialog.value = value;
  }
);

function onHide() {
  emit('update:modelValue', false);
}

const isView = computed(() => datasourceStore.table.viewType !== undefined);
const hasValues = computed(() => datasourceStore.table.valueSetCount !== undefined && datasourceStore.table.valueSetCount > 0);
const canEditTypeFields = computed(() => isView.value || !editMode.value || !hasValues.value);
const isValid = computed(
  () =>
    selected.value.name &&
    (editMode.value ||
      datasourceStore.table.variableCount === 0 ||
      !datasourceStore.variables.some((v) => v.name === selected.value.name))
);

function onSave() {
  if (editMode.value) {
    datasourceStore
      .updateVariable(selected.value)
      .then(() => emit('save', selected.value))
      .catch((err) => {
        notifyError(err);
      });
    return;
  } else {
    datasourceStore
      .addVariable(selected.value)
      .then(() => emit('save', selected.value))
      .catch((err) => {
        notifyError(err);
      });
  }
}
</script>
