<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="t('plugins')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        {{ t('plugins') }}
      </div>
      <div class="text-help q-mb-md">
        {{ t('plugins_info') }}
      </div>
      <div v-if="pluginsStore.plugins.restart" class="q-mb-md box-warning">
        <q-icon name="error" size="1.2rem" />
        <span class="on-right">
          {{ t('plugin.system_restart_required') }}
        </span>
      </div>
      <q-tabs
        v-model="tab"
        dense
        class="text-grey q-mt-md"
        active-color="primary"
        indicator-color="primary"
        align="justify"
      >
        <q-tab name="installed" :label="t('plugin.installed')" />
        <q-tab name="updates" :label="t('plugin.updates')" />
        <q-tab name="available" :label="t('plugin.available')" />
        <q-tab name="advanced" :label="t('plugin.advanced')" />
      </q-tabs>
      <q-separator />
      <q-tab-panels v-model="tab">
        <q-tab-panel name="installed">
          <installed-plugins />
        </q-tab-panel>
        <q-tab-panel name="updates">
          <updates-plugins />
        </q-tab-panel>
        <q-tab-panel name="available">
          <available-plugins />
        </q-tab-panel>
        <q-tab-panel name="advanced">
          <advanced-plugins />
        </q-tab-panel>
      </q-tab-panels>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import InstalledPlugins from 'src/components/admin/plugins/InstalledPlugins.vue';
import UpdatesPlugins from 'src/components/admin/plugins/UpdatesPlugins.vue';
import AvailablePlugins from 'src/components/admin/plugins/AvailablePlugins.vue';
import AdvancedPlugins from 'src/components/admin/plugins/AdvancedPlugins.vue';

const { t } = useI18n();
const pluginsStore = usePluginsStore();

const tab = ref('installed');
</script>
