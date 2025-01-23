<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ datashieldStore.profile.name }}</div>
      </q-card-section>
      <q-separator />
      <q-card-section>
        <div class="text-help q-mb-md">
          {{ t('datashield.settings_init_help') }}
        </div>
        <q-spinner-dots v-if="loading" />
        <q-option-group v-model="selected" :options="options" type="checkbox" />
      </q-card-section>
      <q-separator />
      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn
          flat
          :label="t('initialize')"
          :disable="loading || selected.length === 0"
          color="primary"
          @click="onInitSettings"
          v-close-popup
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { RPackageDto } from 'src/models/OpalR';

const { t } = useI18n();

interface DialogProps {
  modelValue: boolean;
}

const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const emit = defineEmits(['update:modelValue', 'beforeInit', 'afterInit']);

const selected = ref<string[]>([]);
const options = ref<{ label: string; value: string }[]>([]);
const loading = ref(false);

const datashieldStore = useDatashieldStore();

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
    if (value) {
      selected.value = [];
      options.value = [];
      loading.value = true;
      datashieldStore.getPackages().then((packages: RPackageDto[]) => {
        const uniquePackages = Array.from(new Set(packages.map((p) => p.name)));
        options.value = uniquePackages.map((name) => ({ label: name, value: name }));
        selected.value = uniquePackages;
        loading.value = false;
      });
    }
  }
);

function onHide() {
  emit('update:modelValue', false);
}

function onInitSettings() {
  if (selected.value.length > 0) {
    emit('beforeInit');
    datashieldStore.applyProfileSettings(selected.value).finally(() => {
      emit('afterInit');
    });
  }
}
</script>
