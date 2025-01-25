<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="t('apps.title')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        {{ t('apps.title') }}
      </div>
      <div class="text-help">{{ t('apps.info') }}</div>
      <q-card flat>
        <q-card-section class="q-px-none">
          <apps-registry-list />
        </q-card-section>
      </q-card>

      <q-card flat>
        <q-card-section class="q-px-none">
          <apps-config />
        </q-card-section>
      </q-card>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import AppsRegistryList from 'src/components/admin/apps/AppsRegistryList.vue';
import AppsConfig from 'src/components/admin/apps/AppsConfig.vue';
const { t } = useI18n();
</script>
