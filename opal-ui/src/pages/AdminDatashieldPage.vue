<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('administration')" to="/admin" />
        <q-breadcrumbs-el label="DataSHIELD" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">{{ t('packages') }}</div>
      <div class="text-help q-mb-md">{{ t('datashield.packages_info') }}</div>
      <datashield-clusters />
      <div class="row q-col-gutter-md q-mb-lg">
        <div class="col-12 col-md-6">
          <div class="text-h5 q-mb-md">{{ t('permissions') }}</div>
          <access-control-list
            resource="/system/permissions/datashield"
            :options="['DATASHIELD_USE', 'DATASHIELD_ALL']"
          />
        </div>
        <div class="col-12 col-md-6">
          <div class="text-h5 q-mb-md">{{ t('datashield.audit') }}</div>
          <div class="text-help q-mb-md">{{ t('datashield.audit_info') }}</div>
          <q-btn-dropdown :label="t('download')" icon="download" size="sm" color="primary">
            <q-list>
              <q-item clickable v-close-popup @click="onDownloadLogs(true)">
                <q-item-section>
                  <q-item-label>{{ t('datashield.download_all_logs') }}</q-item-label>
                </q-item-section>
              </q-item>
              <q-item clickable v-close-popup @click="onDownloadLogs(false)">
                <q-item-section>
                  <q-item-label>{{ t('datashield.download_latest_logs') }}</q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-btn-dropdown>
        </div>
      </div>

      <div class="text-h5 q-mb-md">{{ t('profiles') }}</div>
      <div class="text-help q-mb-md">{{ t('datashield.profiles_info') }}</div>
      <datashield-profiles />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import { baseUrl } from 'src/boot/api';
import DatashieldClusters from 'src/components/admin/datashield/DatashieldClusters.vue';
import DatashieldProfiles from 'src/components/admin/datashield/DatashieldProfiles.vue';
import AccessControlList from 'src/components/permissions/AccessControlList.vue';

const { t } = useI18n();
const rStore = useRStore();
const datashieldStore = useDatashieldStore();

onMounted(() => {
  rStore.initR();
  datashieldStore.initProfiles();
});

function onDownloadLogs(all: boolean) {
  window.open(`${baseUrl}/system/log/datashield.log?all=${all}`);
}
</script>
