<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="t('search')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">{{ t('variables_index') }}</div>
      <div class="text-help q-mb-md">{{ t('variables_index_info') }}</div>
      <q-btn
        color="secondary"
        icon="cleaning_services"
        :label="t('clear')"
        size="sm"
        @click="onVariablesClear"
        class="q-mb-sm"
      />
      <div class="text-h5 q-mb-md q-mt-md">{{ t('values_index') }}</div>
      <div class="text-help q-mb-md">{{ t('values_index_info') }}</div>
      <q-btn
        color="secondary"
        icon="cleaning_services"
        :label="t('clear')"
        size="sm"
        @click="onValuesClear"
        class="q-mb-sm"
      />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import { notifySuccess, notifyError } from 'src/utils/notify';

const searchStore = useSearchStore();
const { t } = useI18n();

const onVariablesClear = () => {
  searchStore
    .clearIndex('variables')
    .then(() => notifySuccess(t('variables_index_cleared')))
    .catch(() => notifyError(t('variables_index_clear_error')));
};

const onValuesClear = () => {
  searchStore
    .clearIndex('values')
    .then(() => notifySuccess(t('values_index_cleared')))
    .catch(() => notifyError(t('values_index_clear_error')));
};
</script>
