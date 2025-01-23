<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ pkg?.name }}</div>
      </q-card-section>
      <q-separator />
      <q-card-section>
        <fields-list :dbobject="dbobject" :items="fields" />
      </q-card-section>
      <q-separator />
      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('close')" color="primary" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { RPackageDto } from 'src/models/OpalR';
import type { StringMap } from 'src/components/models';
import FieldsList, { type FieldItem } from 'src/components/FieldsList.vue';

const { t } = useI18n();

interface DialogProps {
  modelValue: boolean;
  pkg?: RPackageDto;
}

const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const emit = defineEmits(['update:modelValue']);

const dbobject = computed(() => {
  if (!props.pkg) return undefined;
  const dbobject = {} as StringMap;
  props.pkg.description.forEach((d) => {
    dbobject[d.key] = d.value;
  });
  return dbobject;
});

const fields = computed(() => {
  if (!props.pkg) return [];
  return props.pkg.description.map((d) => ({
    field: d.key,
    label: d.key,
    format: (obj: StringMap) => obj[d.key],
  } as FieldItem));
});

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
  }
);

function onHide() {
  emit('update:modelValue', false);
}
</script>
