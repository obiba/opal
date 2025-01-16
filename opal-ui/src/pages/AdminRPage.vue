<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('administration')" to="/admin" />
        <q-breadcrumbs-el label="R" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">{{ t('r.servers') }}</div>
      <div class="text-help q-mb-md">{{ t('r.servers_info') }}</div>
      <r-clusters />
      <div class="text-h5 q-mb-md">{{ t('r.sessions') }}</div>
      <div class="text-help q-mb-md">{{ t('r.sessions_info') }}</div>
      <r-sessions class="q-mb-md" />
      <div class="text-h5 q-mb-md">{{ t('r.workspaces') }}</div>
      <div class="text-help q-mb-md">{{ t('r.workspaces_info') }}</div>
      <r-workspaces class="q-mb-md"/>
      <div class="text-h5 q-mb-md">{{ t('permissions') }}</div>
      <access-control-list resource="/system/permissions/r" :options="['R_USE']" class="q-mb-lg" />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import RClusters from 'src/components/admin/r/RClusters.vue';
import RSessions from 'src/components/admin/r/RSessions.vue';
import RWorkspaces from 'src/components/admin/r/RWorkspaces.vue';
import AccessControlList from 'src/components/permissions/AccessControlList.vue';

const { t } = useI18n();
const rStore = useRStore();

onMounted(() => {
  rStore.initR();
});
</script>
