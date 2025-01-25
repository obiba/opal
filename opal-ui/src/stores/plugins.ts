import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { PluginPackage } from 'src/components/models';
import type { PluginPackagesDto } from 'src/models/Plugins';
import { mergeAnalysesTranslations } from 'src/utils/plugins';

export const usePluginsStore = defineStore('plugins', () => {
  const plugins = ref({} as PluginPackagesDto);
  const analysisPlugins = ref({} as PluginPackagesDto);
  const datasourceImportPlugins = ref([] as PluginPackage[]);
  const datasourceExportPlugins = ref([] as PluginPackage[]);
  const vcfStorePlugins = ref([] as PluginPackage[]);

  function reset() {
    plugins.value = {} as PluginPackagesDto;
    analysisPlugins.value = {} as PluginPackagesDto;
    datasourceImportPlugins.value = [];
    datasourceExportPlugins.value = [];
  }

  async function initDatasourcePlugins(usage: 'import' | 'export') {
    if (
      (usage === 'import' && datasourceImportPlugins.value.length === 0) ||
      (usage === 'export' && datasourceExportPlugins.value.length === 0)
    ) {
      return loadDatasourcePlugins(usage);
    }
    return Promise.resolve();
  }

  async function getDatasourcePluginForm(pluginId: string, usage: 'import' | 'export') {
    return api.get(`datasource-plugin/${pluginId}/form`, { params: { usage } }).then((response) => response.data);
  }

  async function loadDatasourcePlugins(usage: 'import' | 'export') {
    if (usage === 'import') {
      datasourceImportPlugins.value = [];
    } else {
      datasourceExportPlugins.value = [];
    }
    return api.get('datasource-plugins', { params: { usage } }).then((response) => {
      const plugins = response.data.packages ? (response.data.packages as PluginPackage[]) : [];
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
    return getPlugins('vcf-store').then((plugins) => {
      vcfStorePlugins.value = plugins.packages ?? [];
    });
  }

  async function loadPlugins() {
    return api.get('plugins').then((response) => (plugins.value = response.data));
  }

  async function initAnalysisPlugins() {
    if (analysisPlugins.value.site) return Promise.resolve();
    return loadAnalysisPlugins();
  }

  async function loadAnalysisPlugins() {
    analysisPlugins.value = {} as PluginPackagesDto;
    return api.get('analysis-plugins').then((response) => {
      analysisPlugins.value = response.data;
      mergeAnalysesTranslations(response.data);
    });
  }

  async function getPlugins(type: string) {
    return api.get('plugins', { params: { type } }).then((response) => response.data);
  }

  async function hasPlugin(type: string) {
    return api.get('plugins', { params: { type } }).then((response) => {
      return (response.data.packages || []).length > 0;
    });
  }

  async function getPlugin(name: string) {
    return api.get(`plugin/${name}`).then((response) => response.data);
  }

  async function configurePlugin(name: string, config: string | undefined) {
    if (!config) return Promise.resolve();
    return api.put(`plugin/${name}/cfg`, config, { headers: { 'Content-Type': 'text/plain' } });
  }

  async function restartPlugin(name: string) {
    return api.delete(`plugin/${name}/service`).then(() => api.put(`plugin/${name}/service`));
  }

  async function installPlugin(name: string, version: string) {
    return api.post('plugins', {}, { params: { name, version } }).then(() => loadPlugins());
  }

  async function installPluginFile(file: string) {
    return api.post('plugins', {}, { params: { file } }).then(() => loadPlugins());
  }

  async function uninstallPlugin(name: string) {
    return api.delete(`plugin/${name}`).then(() => loadPlugins());
  }

  async function cancelUninstallPlugin(name: string) {
    return api.put(`plugin/${name}`).then(() => loadPlugins());
  }

  async function getPluginsUpdates() {
    return api.get('plugins/_updates').then((response) => response.data);
  }

  async function getPluginsAvailable() {
    return api.get('plugins/_available').then((response) => response.data);
  }

  return {
    plugins,
    analysisPlugins,
    datasourceImportPlugins,
    datasourceExportPlugins,
    vcfStorePlugins,
    reset,
    loadPlugins,
    initAnalysisPlugins,
    initDatasourcePlugins,
    getDatasourcePluginForm,
    initVcfStorePlugins,
    hasPlugin,
    getPlugin,
    configurePlugin,
    uninstallPlugin,
    cancelUninstallPlugin,
    restartPlugin,
    getPluginsUpdates,
    getPluginsAvailable,
    installPlugin,
    installPluginFile,
  };
});
