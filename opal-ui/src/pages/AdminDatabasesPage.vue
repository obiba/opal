<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="t('databases')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">{{ t('db.data_databases') }}</div>
      <div class="text-help q-mb-md">{{ t('db.data_databases_info') }}</div>
      <data-databases />
      <div class="text-h5 q-mb-md">{{ t('db.id_database') }}</div>
      <div class="text-help q-mb-md">{{ t('db.id_database_info') }}</div>
      <identifiers-database />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import DataDatabases from 'src/components/admin/databases/DataDatabases.vue';
import IdentifiersDatabase from 'src/components/admin/databases/IdentifiersDatabase.vue';

const { t } = useI18n();
</script>
