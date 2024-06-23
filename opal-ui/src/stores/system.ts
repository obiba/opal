import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { GeneralConf } from 'src/models/Opal';


export const useSystemStore = defineStore('system', () => {

  const generalConf = ref<GeneralConf>({} as GeneralConf);

  async function initGeneralConf() {
    return api.get('/system/conf/general').then((response) => {
      if (response.status === 200) {
        generalConf.value = response.data;
      }
      return response;
    });
  }

  async function saveGeneralConf(data: GeneralConf) {
    return api.put('/system/conf/general', data).then(initGeneralConf);
  }

  async function getDatabases(usage: string) {
    return api.get('/system/databases', { params: { usage } });
  }

  async function getDatabasesWithSettings() {
    return api.get('/system/databases', { params: { settings: true } }).then((response) => response.data);
  }

  async function getIdentifiersDatabase() {
    return api.get('/system/databases/identifiers').then((response) => response.data);
  }

  return {
    generalConf,
    initGeneralConf,
    saveGeneralConf,
    getDatabases,
    getDatabasesWithSettings,
    getIdentifiersDatabase,
  };
});
