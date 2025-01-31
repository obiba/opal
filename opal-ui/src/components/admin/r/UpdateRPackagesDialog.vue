<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('update') }}</div>
      </q-card-section>
      <q-separator />
      <q-card-section class="bg-warning">
        <q-icon name="warning" />
        {{ t('r.packages_warn') }}
      </q-card-section>
      <q-card-section>
        {{ t('update_all_r_packages_note') }}
      </q-card-section>
      <q-separator />
      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('update_action')" color="primary" @click="onUpdate" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { RServerClusterDto } from 'src/models/OpalR';
import { notifyError, notifySuccess } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  cluster: RServerClusterDto;
}

const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();
const rStore = useRStore();

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
  }
);

function onHide() {
  emit('update:modelValue', false);
}

function onUpdate() {
  rStore
    .updateRPackages(props.cluster.name)
    .then((response) => {
      notifySuccess(t('update_all_r_packages_task_created', { id: response.data.id }));
    })
    .catch((err) => {
      console.error(err);
      notifyError(err);
    });
}
</script>
