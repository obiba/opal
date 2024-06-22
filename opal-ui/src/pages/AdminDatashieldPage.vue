<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el label="DataSHIELD" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">{{ $t('packages') }}</div>
      <div class="text-help q-mb-md">{{ $t('datashield.packages_info') }}</div>
      <datashield-clusters />
      <div class="text-h5 q-mb-md">{{ $t('permissions') }}</div>
      <access-control-list
        resource="/system/permissions/datashield"
        :options="['DATASHIELD_USE', 'DATASHIELD_ALL']"
      />
      <div class="text-h5 q-mb-md">{{ $t('profiles') }}</div>
      <div class="text-help q-mb-md">{{ $t('datashield.profiles_info') }}</div>
      <datashield-profiles />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import DatashieldClusters from 'src/components/admin/datashield/DatashieldClusters.vue';
import DatashieldProfiles from 'src/components/admin/datashield/DatashieldProfiles.vue';
import AccessControlList from 'src/components/permissions/AccessControlList.vue';

const rStore = useRStore();
const datashieldStore = useDatashieldStore();

onMounted(() => {
  rStore.initR();
  datashieldStore.initProfiles();
});
</script>
