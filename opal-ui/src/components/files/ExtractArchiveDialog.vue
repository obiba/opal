<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('extract') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <file-select
          v-model="destinationFolder"
          :label="t('destination_folder')"
          :folder="filesStore.current"
          selection="single"
          type="folder"
          class="q-mb-md"
        />
        <form autocomplete="off">
          <q-input
            v-model="password"
            :label="t('password')"
            :hint="t('extract_archive_password_hint')"
            type="password"
            autocomplete="new-password"
            dense
            class="q-mb-md"
          />
        </form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn
          flat
          :label="t('submit')"
          color="primary"
          :disable="!destinationFolder"
          @click="onSubmit"
          v-close-popup
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import FileSelect from 'src/components/files/FileSelect.vue';
import type { FileDto } from 'src/models/Opal';

interface DialogProps {
  modelValue: boolean;
  file: FileDto;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();
const filesStore = useFilesStore();

const showDialog = ref(props.modelValue);
const destinationFolder = ref<FileDto>();
const password = ref();

onMounted(init);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      init();
    }
    showDialog.value = value;
  }
);

function init() {
  destinationFolder.value = filesStore.current;
  password.value = '';
}

function onHide() {
  emit('update:modelValue', false);
}

function onSubmit() {
  if (!destinationFolder.value) {
    return;
  }
  filesStore
    .extractArchive(props.file.path, destinationFolder.value, password.value === '' ? undefined : password.value)
    .then(() => {
      if (destinationFolder.value) filesStore.loadFiles(destinationFolder.value.path);
    });
}
</script>
