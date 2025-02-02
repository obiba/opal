<template>
  <div>
    <q-input
      v-model="url"
      :label="t('opal_url')"
      placeholder="https://"
      dense
      class="q-mb-md"
      :debounce="500"
      @update:model-value="onUpdate"
    />
    <q-select
      v-model="authMethod"
      :options="authOptions"
      :label="t('auth_method')"
      dense
      class="q-mb-md"
      @update:model-value="onAuthSelection"
    />
    <q-input
      v-if="!isCredentials"
      v-model="token"
      :label="t('personal_access_token')"
      dense
      class="q-mb-md"
      :debounce="500"
      @update:model-value="onUpdate"
    />
    <q-input
      v-if="isCredentials"
      v-model="username"
      :label="t('username')"
      dense
      class="q-mb-md"
      :debounce="500"
      @update:model-value="onUpdate"
    />
    <q-input
      v-if="isCredentials"
      v-model="password"
      :label="t('password')"
      type="password"
      dense
      class="q-mb-md"
      :debounce="500"
      @update:model-value="onUpdate"
    />
    <q-input
      v-model="remoteDatasource"
      :label="t('project')"
      dense
      class="q-mb-md"
      :debounce="500"
      @update:model-value="onUpdate"
    />
  </div>
</template>

<script setup lang="ts">
import type { DatasourceFactory } from 'src/components/models';

interface ImportOpalFormProps {
  modelValue: DatasourceFactory | undefined;
}

const props = defineProps<ImportOpalFormProps>();
const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();

const url = ref<string>(props.modelValue?.['Magma.RestDatasourceFactoryDto.params']?.url ?? '');
const username = ref<string>(props.modelValue?.['Magma.RestDatasourceFactoryDto.params']?.username ?? '');
const password = ref<string>(props.modelValue?.['Magma.RestDatasourceFactoryDto.params']?.password ?? '');
const token = ref<string>(props.modelValue?.['Magma.RestDatasourceFactoryDto.params']?.token ?? '');
const remoteDatasource = ref<string>(
  props.modelValue?.['Magma.RestDatasourceFactoryDto.params']?.remoteDatasource ?? ''
);

const authOptions = [
  { label: t('token'), value: 'token' },
  { label: t('credentials'), value: 'credentials' },
];
const authMethod = ref(
  props.modelValue?.['Magma.RestDatasourceFactoryDto.params']?.username ? authOptions[1] : authOptions[0]
);

function onAuthSelection() {
  if (authMethod.value?.value === 'token') {
    username.value = '';
    password.value = '';
  } else {
    token.value = '';
  }
  onUpdate();
}

const isCredentials = computed(() => authMethod.value?.value === 'credentials');

function isValid() {
  return (
    (url.value && remoteDatasource.value && (!isCredentials.value || (username.value && password.value))) ||
    (token.value !== '' && token.value !== undefined)
  );
}

function onUpdate() {
  if (!isValid()) {
    emit('update:modelValue', undefined);
    return;
  }
  emit('update:modelValue', {
    'Magma.RestDatasourceFactoryDto.params': {
      url: url.value,
      username: isCredentials.value ? username.value : undefined,
      password: isCredentials.value ? password.value : undefined,
      token: !isCredentials.value ? token.value : undefined,
      remoteDatasource: remoteDatasource.value,
    },
  } as DatasourceFactory);
}
</script>
