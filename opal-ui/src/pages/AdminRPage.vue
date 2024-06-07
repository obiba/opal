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
      <div class="text-h5 q-mb-md">{{ $t('r_workspaces') }}</div>
      <div class="text-help q-mb-md">{{ $t('r_workspaces_info') }}</div>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import RServers from 'src/components/admin/RServers.vue';

const rStore = useRStore();

onMounted(() => {
  rStore.init();
});
</script>
