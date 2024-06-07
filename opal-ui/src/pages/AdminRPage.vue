<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('r')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">{{ $t('r_servers') }}</div>
      <div class="text-help q-mb-md">{{ $t('r_servers_info') }}</div>
      <r-servers />
      <div class="text-h5 q-mb-md">{{ $t('r_sessions') }}</div>
      <div class="text-help q-mb-md">{{ $t('r_sessions_info') }}</div>
      <r-sessions class="q-mb-md" />
      <div class="text-h5 q-mb-md">{{ $t('r_workspaces') }}</div>
      <div class="text-help q-mb-md">{{ $t('r_workspaces_info') }}</div>
      <r-workspaces />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import RServers from 'src/components/admin/r/RServers.vue';
import RSessions from 'src/components/admin/r/RSessions.vue';
import RWorkspaces from 'src/components/admin/r/RWorkspaces.vue';

const rStore = useRStore();

onMounted(() => {
  rStore.init();
});
</script>
