<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('project_admin.import_key') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section style="max-height: 75vh" class="scroll">
        <q-form ref="formRef" class="q-gutter-sm" persistent>
          <q-input
            v-model="keyPair.alias"
            dense
            type="text"
            :label="t('name')"
            lazy-rules
            :rules="[validateRequiredField('validation.name_required')]"
          >
          </q-input>
          <q-input
            v-model="keyPair.privateImport"
            dense
            type="textarea"
            rows="10"
            :label="t('project_admin.private_key')"
            lazy-rules
            :rules="[validateRequiredField('validation.project_admin.private_key_required')]"
          >
          </q-input>
          <q-input
            v-model="keyPair.publicImport"
            dense
            type="textarea"
            rows="10"
            :label="t('project_admin.public_key')"
            lazy-rules
            :rules="[validateRequiredField('validation.project_admin.public_key_required')]"
          >
          </q-input>
          <div class="text-help">{{ t('project_admin.import_key_info') }}</div>
        </q-form>
      </q-card-section>
      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('add')" type="submit" color="primary" @click="onAdd" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { ProjectDto } from 'src/models/Projects';
import { notifyError } from 'src/utils/notify';
import { type KeyForm, KeyType } from 'src/models/Opal';

interface DialogProps {
  modelValue: boolean;
  project: ProjectDto;
}

const projectsStore = useProjectsStore();
const emptyKeyForm = {
  alias: '',
  publicImport: '',
  privateImport: '',
  keyType: KeyType.KEY_PAIR,
} as KeyForm;

const { t } = useI18n();
const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const formRef = ref();
const emit = defineEmits(['update:modelValue', 'update']);
const keyPair = ref({ ...emptyKeyForm } as KeyForm);
const validateRequiredField = (id: string) => (val: string) => (val && val.trim().length > 0) || t(id);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      showDialog.value = value;
    }
  }
);

function onHide() {
  showDialog.value = false;
  keyPair.value = { ...emptyKeyForm };
  emit('update:modelValue', false);
}

async function onAdd() {
  const valid = await formRef.value.validate();
  if (valid) {
    try {
      await projectsStore.addKeyPair(props.project.name, keyPair.value);
      emit('update');
      onHide();
    } catch (error) {
      notifyError(error);
    }
  }
}
</script>
