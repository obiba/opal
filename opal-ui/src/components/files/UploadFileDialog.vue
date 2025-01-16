<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card>
      <q-card-section>
        <div class="text-h6">{{ t('upload') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-file
          v-model="newFiles"
          dense
          multiple
          append
          :label="t('select_files_to_upload')"
          style="width: 300px"
          :accept="accept"
          @update:model-value="onLocalFilesChange"
          class="q-mb-md"
        >
        </q-file>
        <div v-if="existingFiles.length">
          <q-checkbox dense v-model="overwrite" :label="t('select_files_overwrite')" />
        </div>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn
          flat
          :label="t('add')"
          color="primary"
          :disable="newFiles.length === 0 || (existingFiles.length > 0 && !overwrite)"
          @click="onUpload"
          v-close-popup
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { FileObject } from 'src/components/models';
import type { FileDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  file: FileDto;
  extensions: string[] | undefined;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();
const filesStore = useFilesStore();

const showDialog = ref(props.modelValue);
const newFiles = ref<FileObject[]>([]);
const overwrite = ref(false);

const accept = props.extensions ? props.extensions.join(',') : undefined;

const existingFiles = computed(() => {
  if (newFiles.value.length === 0 || !props.file.children || props.file.children.length === 0) {
    return [];
  }
  return props.file.children.filter((f) => newFiles.value.some((nf) => nf.name === f.name));
});

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      newFiles.value = [];
    }
    showDialog.value = value;
  }
);

function onHide() {
  emit('update:modelValue', false);
}

function onUpload() {
  filesStore
    .uploadFiles(props.file.path, newFiles.value as FileObject[])
    .then(() => filesStore.loadFiles(props.file.path))
    .catch(notifyError);
}

function onLocalFilesChange() {
  if (newFiles.value.length > 0) {
    // check files do not already exist
    
  }
}
</script>
