import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { AppDto, AppsConfigDto } from 'src/models/Apps';

export const useAppsStore = defineStore('apps', () => {
  const apps = ref<AppDto[]>([]);
  const config = ref<AppsConfigDto>({} as AppsConfigDto);

  function reset() {
    apps.value = [];
    config.value = {} as AppsConfigDto;
  }

  async function initApps() {
    return loadApps();
  }

  async function loadApps() {
    apps.value = [];
    return api.get('/apps').then((response) => {
      apps.value = response.data;
      return response.data;
    });
  }

  async function unregisterApp(id: string) {
    return api.delete(`/app/${id}`);
  }

  async function initConfig() {
    config.value = {} as AppsConfigDto;
    return loadConfig();
  }

  async function loadConfig() {
    return api.get('/apps/config').then((response) => {
      config.value = response.data;
      return response.data;
    });
  }

  async function updateConfig(data: AppsConfigDto) {
    return api.put('/apps/config', data);
  }

  return {
    apps,
    config,
    reset,
    initApps,
    unregisterApp,
    initConfig,
    updateConfig,
  };
});
