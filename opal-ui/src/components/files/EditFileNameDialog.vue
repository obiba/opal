<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card>
      <q-card-section>
        <div class="text-h6">{{ t('edit') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-input v-model="newName" dense autofocus type="text" :label="t('name')" style="width: 300px" class="q-mb-md">
        </q-input>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('save')" color="primary" :disable="newName === ''" @click="onSave" />
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

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();
const filesStore = useFilesStore();

const showDialog = ref(props.modelValue);
const newName = ref<string>(props.file.name);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      newName.value = props.file.name;
    }
    showDialog.value = value;
  }
);

function onHide() {
  emit('update:modelValue', false);
}

function onSave() {
  filesStore
    .renameFile(props.file.path, newName.value)
    .then(() => {
      showDialog.value = false;
      filesStore.loadFiles(filesStore.getParentFolder(props.file.path));
    })
    .catch((error) => {
      notifyError(error);
    });
}
</script>
