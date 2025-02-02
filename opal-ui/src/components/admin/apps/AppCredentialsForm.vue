<template>
  <div>
    <q-input
      v-model="name"
      dense
      type="text"
      :label="t('name') + ' *'"
      :hint="t('apps.name_hint', { type: type })"
      class="q-mb-md"
      lazy-rules
      :rules="[validateRequiredField('name')]"
    >
    </q-input>

    <q-input
      autocomplete="off"
      type="password"
      :label="t('password') + ' *'"
      :hint="t('apps.name_hint', { type: type })"
      v-model="password"
      color="grey-10"
      lazy-rules
      :rules="[validateRequiredField('password'), validateRequiredPassword]"
    ></q-input>
  </div>
</template>

<script setup lang="ts">
import type { AppCredentialsDto } from 'src/models/Apps';

interface Props {
  modelValue: AppCredentialsDto | undefined;
  type: string;
}

const { t } = useI18n();
const props = defineProps<Props>();
const emit = defineEmits(['update:modelValue']);

const name = computed({
  get: () => props.modelValue?.name || '',
  set: (value: string) => {
    emit('update:modelValue', { ...props.modelValue, name: value });
  },
});

const password = computed({
  get: () => props.modelValue?.password || '',
  set: (value: string) => {
    emit('update:modelValue', { ...props.modelValue, password: value });
  },
});

const validateRequiredField = (id: string) => (val: string) => (val && val.trim().length > 0) || t(id);
const validateRequiredPassword = (val: string) =>
  /*editMode.value &&*/ !val || val.length === 0 || (val && val.length >= 8) || t('password_hint');
</script>
