<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('id_mappings.add_identifier') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-input
            v-model="entityType"
            dense
            type="text"
            :label="t('entity_type') + ' *'"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredField]"
          >
          </q-input>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('add')" type="submit" color="primary" @click="onAddMapping" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { TableDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
}

const { t } = useI18n();
const identifiersStore = useIdentifiersStore();
const formRef = ref();
const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'update']);
const showDialog = ref(props.modelValue);
const newIdentifier = ref<TableDto>({} as TableDto);
const entityType = computed({
  get: () => newIdentifier.value.entityType,
  set: (val) => {
    newIdentifier.value.name = val;
    newIdentifier.value.entityType = val;
  },
});

// Validation rules
const validateRequiredField = (val: string) => (val && val.trim().length > 0) || t('validation.entity_type_required');

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      newIdentifier.value = {
        name: '',
        entityType: '',
      } as TableDto;
      showDialog.value = value;
    }
  }
);

function onHide() {
  newIdentifier.value = {} as TableDto;
  showDialog.value = false;
  emit('update:modelValue', false);
}

async function onAddMapping() {
  const valid = await formRef.value.validate();
  if (valid) {
    identifiersStore
      .addIdentifierTable(newIdentifier.value)
      .then(() => {
        emit('update', newIdentifier.value);
        onHide();
      })
      .catch(notifyError);
  }
}
</script>
