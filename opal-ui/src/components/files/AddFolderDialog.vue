<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card>
      <q-card-section>
        <div class="text-h6">{{ t('add_folder') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="formRef">
          <q-input
            v-model="newFolder"
            dense
            autofocus
            type="text"
            :label="t('name')"
            style="width: 300px"
            class="q-mb-md"
            lazy-rules
            :rules="[validateFolderName]"
          >
          </q-input>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('add')" color="primary" :disable="newFolder === ''" @click="onAddFolder" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { FileDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  file: FileDto;
}

const { t } = useI18n();
const INVALID_CHARS = ['%', '#'];
const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const filesStore = useFilesStore();

const showDialog = ref(props.modelValue);
const newFolder = ref<string>('');
const formRef = ref();

// Validators
const validateFolderName = (value: string) => {
  if (value) {
    if (INVALID_CHARS.some((char) => value.includes(char))) {
      return t('validation.folder.invalid_chars', { chars: INVALID_CHARS.join(', ') });
    } else if (['.', '..'].indexOf(value) !== -1) {
      return t('validation.folder.dot_name');
    }
  }

  return true;
};

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      newFolder.value = '';
    }
    showDialog.value = value;
  }
);

function onHide() {
  emit('update:modelValue', false);
}

async function onAddFolder() {
  const valid = await formRef.value.validate();

  if (valid) {
    try {
      await filesStore.addFolder(props.file.path, newFolder.value);
      filesStore.loadFiles(props.file.path);
      onHide();
    } catch (error) {
      notifyError(error);
    }
  }
}
</script>
