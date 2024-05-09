import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { PluginPackage } from 'src/components/models';

export const usePluginsStore = defineStore('plugins', () => {

  const datasourceImportPlugins = ref([] as PluginPackage[]);
  const datasourceExportPlugins = ref([] as PluginPackage[]);

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

  return {
    datasourceImportPlugins,
    reset,
    initDatasourcePlugins,
  };

});
