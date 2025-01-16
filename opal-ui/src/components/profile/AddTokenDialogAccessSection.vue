<template>
  <div>
    <q-select
      v-model="accessMapping"
      :options="accessMappingOptions"
      dense
      :label="t('user_profile.token_dialog.project_access')"
      :hint="t('user_profile.token_dialog.project_access_hint')"
      class="q-mb-md q-pt-md"
      emit-value
      map-options
    />
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n();

interface AccessProps {
  modelValue: string | undefined;
}

const props = defineProps<AccessProps>();
const emit = defineEmits(['update:modelValue']);

const accessMappingOptions = [
  { label: t('access.DEFAULT'), value: 'DEFAULT' },
  { label: t('access.READ'), value: 'READ' },
  { label: t('access.READ_NO_VALUES'), value: 'READ_NO_VALUES' },
];

const accessMapping = computed({
  get: () => props.modelValue || 'DEFAULT',
  set: (value) => emit('update:modelValue', value === 'DEFAULT' ? undefined : value),
});
</script>
