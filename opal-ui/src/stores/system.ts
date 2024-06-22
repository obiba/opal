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

  return {
    generalConf,
    initGeneralConf,
    saveGeneralConf,
  };
});
