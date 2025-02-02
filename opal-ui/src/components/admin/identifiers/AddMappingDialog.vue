<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ dialogTitle }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-input
            v-model="newMapping.name"
            dense
            type="text"
            :label="t('name') + '*'"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredName]"
            :disable="editMode"
          >
          </q-input>
          <q-input
            v-model="description"
            dense
            type="text"
            :label="t('description')"
            class="q-mb-md"
            lazy-rules
          >
          </q-input>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="submitCaption" type="submit" color="primary" @click="onAddMapping" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { TableDto, VariableDto, AttributeDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  identifier: TableDto;
  mapping: VariableDto | null;
}

const { t } = useI18n();
const identifiersStore = useIdentifiersStore();
const formRef = ref();
const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'update']);
const showDialog = ref(props.modelValue);
const newMapping = ref<VariableDto>({} as VariableDto);
const description = ref('');
const editMode = computed(() => props.mapping?.name !== undefined);
const submitCaption = computed(() => (editMode.value ? t('update') : t('add')));
const dialogTitle = computed(() => (editMode.value ? t('id_mappings.add_mapping') : t('id_mappings.edit_mapping')));

// Validation rules
const validateRequiredName = (val: string) => (val && val.trim().length > 0) || t('validation.name_required');

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      const emptyMapping: VariableDto = {
        isNewVariable: true,
        name: '',
        entityType: props.identifier.entityType,
        valueType: 'text',
        isRepeatable: false,
        attributes: [{ name: 'description', value: '' } as AttributeDto],
      } as VariableDto;

      if (props.mapping) {
        newMapping.value = { ...emptyMapping, ...props.mapping };
      } else {
        newMapping.value = emptyMapping;
      }

      description.value = newMapping.value.attributes?.find((a) => a.name === 'description')?.value || '';

      showDialog.value = value;
    }
  }
);

function onHide() {
  newMapping.value = {} as VariableDto;
  showDialog.value = false;
  emit('update:modelValue', false);
}

async function onAddMapping() {
  const valid = await formRef.value.validate();
  if (valid) {
    newMapping.value.attributes = [{ name: 'description', value: description.value }];
    (editMode.value
      ? identifiersStore.updateMapping(props.identifier.name, newMapping.value)
      : identifiersStore.addMapping(props.identifier.name, newMapping.value)
    )
      .then(() => {
        emit('update');
        onHide();
      })
      .catch(notifyError);
  }
}
</script>
