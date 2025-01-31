<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t(isCreation ? 'add_option' : 'update_option') }}</div>
      </q-card-section>
      <q-separator />
      <q-card-section>
        <q-input
          v-model="optName"
          :label="t('name')"
          :hint="t('datashield.option_name_hint')"
          :disable="!isCreation"
          dense
          class="q-mb-md"
        />
        <q-input
          v-model="optValue"
          :label="t('value')"
          :hint="t('datashield.option_value_hint')"
          dense
          class="q-mb-md"
        />
      </q-card-section>
      <q-separator />
      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn
          flat
          :label="t(isCreation ? 'add' : 'update_action')"
          :disable="optName.length === 0 || optValue.length === 0"
          color="primary"
          @click="onSubmit"
          v-close-popup
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { DataShieldROptionDto } from 'src/models/DataShield';

const { t } = useI18n();

interface DialogProps {
  modelValue: boolean;
  option: DataShieldROptionDto | null;
}

const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const emit = defineEmits(['update:modelValue']);

const datashieldStore = useDatashieldStore();

const optName = ref('');
const optValue = ref('');

const isCreation = computed(() => props.option === null);

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
    if (value) {
      optName.value = props.option ? props.option.name : '';
      optValue.value = props.option ? props.option.value : '';
    }
  }
);

function onHide() {
  emit('update:modelValue', false);
}

function onSubmit() {
  datashieldStore.setOption({ name: optName.value, value: optValue.value });
}
</script>
