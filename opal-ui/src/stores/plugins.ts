import { get } from 'http';
import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { PluginPackage } from 'src/components/models';

export const usePluginsStore = defineStore('plugins', () => {

  const datasourceImportPlugins = ref([] as PluginPackage[]);
  const datasourceExportPlugins = ref([] as PluginPackage[]);
  const vcfStorePlugins = ref([] as PluginPackage[]);

  function reset() {
    datasourceImportPlugins.value = [];
    datasourceExportPlugins.value = [];
  }

  async function initDatasourcePlugins(usage: 'import' | 'export') {
    if ((usage === 'import' && datasourceImportPlugins.value.length === 0) || (usage === 'export' && datasourceExportPlugins.value.length === 0)) {
      return loadDatasourcePlugins(usage);
    }
    return Promise.resolve();
  }

  async function getDatasourcePluginForm(pluginId: string, usage: 'import' | 'export') {
    return api.get(`datasource-plugin/${pluginId}/form`, { params: { usage }}).then((response) => response.data);
  }

  async function loadDatasourcePlugins(usage: 'import' | 'export') {
    if (usage === 'import') {
      datasourceImportPlugins.value = [];
    } else {
      datasourceExportPlugins.value = [];
    }
    return api.get('datasource-plugins', { params: { usage }}).then((response) => {
      const plugins = response.data.packages ? response.data.packages as PluginPackage[] : [];
      if (usage === 'import') {
        datasourceImportPlugins.value = plugins;
      } else {
        datasourceExportPlugins.value = plugins;
      }
      return response;
    });
  }

  async function initVcfStorePlugins() {
    vcfStorePlugins.value = [];
    return loadPlugin('vcf-store').then((plugins) => {
      vcfStorePlugins.value = plugins.packages ?? [];
    });
  }

  async function loadPlugin(type: string) {
    return api.get('plugins', {params: {type}}).then((response) => response.data);
  }

  return {
    datasourceImportPlugins,
    datasourceExportPlugins,
    vcfStorePlugins,
    reset,
    initDatasourcePlugins,
    getDatasourcePluginForm,
    initVcfStorePlugins
  };

});
