import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { DatabaseDto } from 'src/models/Database';
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

  async function getDatabases(usage: string): Promise<DatabaseDto[]> {
    return api.get('/system/databases', { params: { usage } }).then((response) => response.data);
  }

  async function getDatabasesWithSettings() {
    return api.get('/system/databases', { params: { settings: true } }).then((response) => response.data);
  }

  async function getIdentifiersDatabase() {
    return api.get('/system/databases/identifiers').then((response) => response.data);
  }

  async function testDatabase(name: string) {
    return api.post(`/system/database/${name}/connections`);
  }

  async function deleteDatabase(name: string) {
    return api.delete(`/system/database/${name}`);
  }

  async function saveDatabase(database: DatabaseDto, update: boolean) {
    if (update) {
      return api.put(`/system/database/${database.name}`, database);
    } else {
      return api.post('/system/databases', database);
    }
  }

  return {
    generalConf,
    initGeneralConf,
    saveGeneralConf,
    getDatabases,
    getDatabasesWithSettings,
    getIdentifiersDatabase,
    testDatabase,
    deleteDatabase,
    saveDatabase,
  };
});
