<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('datashield')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">{{ $t('packages') }}</div>
      <div class="text-help q-mb-md">{{ $t('datashield_packages_info') }}</div>
      <datashield-clusters />
      <div class="text-h5 q-mb-md">{{ $t('profiles') }}</div>
      <div class="text-help q-mb-md">{{ $t('datashield_profiles_info') }}</div>
      <datashield-profiles />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import DatashieldClusters from 'src/components/admin/datashield/DatashieldClusters.vue';
import DatashieldProfiles from 'src/components/admin/datashield/DatashieldProfiles.vue';

const rStore = useRStore();
const datashieldStore = useDatashieldStore();

onMounted(() => {
  rStore.initR();
  datashieldStore.initProfiles();
});
</script>
