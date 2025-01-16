<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="t('general_settings')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">{{ t('configuration') }}</div>
      <general-configuration />
      <div class="text-h5 q-mb-md q-mt-lg">{{ t('permissions') }}</div>
      <access-control-list resource="/system/permissions/administration" :options="['PROJECT_ADD', 'SYSTEM_ALL']" />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import GeneralConfiguration from 'src/components/admin/configuration/GeneralConfiguration.vue';
import AccessControlList from 'src/components/permissions/AccessControlList.vue';

const { t } = useI18n();
</script>
