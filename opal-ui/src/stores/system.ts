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

  function getLogoutURL() {
    return generalConf.value.logoutURL;
  }

  return {
    generalConf,
    initGeneralConf,
    getLogoutURL,
  };
});
