<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('restore_views') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-file
          v-model="newFiles"
          dense
          multiple
          append
          :label="t('restore_views_files')"
          :hint="t('restore_views_files_hint')"
          class="q-mb-md"
          accept="json"
        />
        <q-checkbox v-model="override" class="q-ml-none" :label="t('restore_views_override')" />
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-spinner-dots v-if="processing" class="on-left" />
        <q-btn flat :label="t('cancel')" color="secondary" :disable="processing" v-close-popup />
        <q-btn
          flat
          :label="t('restore')"
          color="primary"
          @click="onRestore"
          :disable="newFiles.length === 0 || processing"
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { FileObject } from 'src/components/models';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const datasourceStore = useDatasourceStore();
const { t } = useI18n();

const showDialog = ref(props.modelValue);
const newFiles = ref<FileObject[]>([]);
const override = ref(false);
const processed = ref(0);
const processing = ref(false);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      newFiles.value = [];
      override.value = false;
      processed.value = 0;
      processing.value = false;
    }
    showDialog.value = value;
  }
);

function onHide() {
  emit('update:modelValue', false);
}

function onRestore() {
  // read each file and restore the view
  processed.value = 0;
  processing.value = true;
  newFiles.value.forEach((file) => {
    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const view = JSON.parse(event.target?.result as string);
        datasourceStore
          .getView(datasourceStore.datasource.name, view.name)
          .then(() => {
            if (!override.value) {
              notifyError(t('restore_views_override_error', { name: view.name }));
              incrementProcessed();
            } else {
              datasourceStore
                .updateView(datasourceStore.datasource.name, view.name, view, 'Restore')
                .finally(incrementProcessed);
            }
          })
          .catch(() => {
            datasourceStore.createView(datasourceStore.datasource.name, view).finally(incrementProcessed);
          });
      } catch (error) {
        notifyError(error);
        incrementProcessed();
      }
    };
    reader.readAsText(file);
  });
}

function incrementProcessed() {
  processed.value = processed.value + 1;
  if (processed.value === newFiles.value.length) {
    processing.value = false;
    datasourceStore.initDatasourceTables(datasourceStore.datasource.name);
    onHide();
  }
}
</script>
