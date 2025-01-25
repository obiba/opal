<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('add_categories_range') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-input
          v-model="rangeStr"
          dense
          type="text"
          :label="t('range')"
          :hint="t('categories_range_hint')"
          placeholder="ex: 1-5, 99"
          style="min-width: 300px"
          class="q-mb-xl"
        >
        </q-input>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('save')" color="primary" :disable="!isValid" @click="onSave" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { VariableDto } from 'src/models/Magma';

interface DialogProps {
  modelValue: boolean;
  variable: VariableDto;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'saved']);

const { t } = useI18n();
const datasourceStore = useDatasourceStore();

const showDialog = ref(props.modelValue);
const rangeStr = ref('');

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      rangeStr.value = '';
    }
    showDialog.value = value;
  }
);

const isValid = computed(() => rangeStr.value !== '');

function onHide() {
  emit('update:modelValue', false);
}

function onSave() {
  const newVariable = { ...props.variable } as VariableDto;
  newVariable.categories = props.variable.categories ? [...props.variable.categories] : [];
  const ranges: string[] = rangeStr.value
    .trim()
    .split(',')
    .map((r) => r.trim())
    .filter((r) => r.length > 0);
  ranges.forEach((r) => {
    const interval = r.split('-');
    if (interval.length === 1) {
      const n = interval[0];
      if (!newVariable.categories.find((c) => c.name === n)) {
        newVariable.categories.push({ name: n || '', attributes: [], isMissing: false });
      }
    } else if (interval.length === 2) {
      try {
        const from = interval[0] ? parseInt(interval[0].trim()) : 0;
        const to = interval[1] ? parseInt(interval[1].trim()) : 0;
        for (let i = from; i <= to; i++) {
          const n = i.toString();
          if (!newVariable.categories.find((c) => c.name === n)) {
            newVariable.categories.push({ name: n, attributes: [], isMissing: false });
          }
        }
      } catch (e) {
        // ignore
        console.error(e);
      }
    }
  });
  datasourceStore.updateVariable(newVariable).then(() => {
    emit('saved', newVariable);
  });
}
</script>
