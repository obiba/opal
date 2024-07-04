import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { IDProviderDto } from 'src/models/Opal';

export const useIdentityProvidersStore = defineStore('identityProviders', () => {
  const providers = ref([] as IDProviderDto[]);

  function reset() {
    providers.value = [];
  }

  async function initProviders() {
    reset();
    return loadProviders();
  }

  async function loadProviders() {
    return api.get('/system/idproviders').then((response) => {
      providers.value = response.data
    })
  }

  async function getProvider(provider: IDProviderDto) {
    return api.delete(`/system/idprovider/${provider.name}`);
  }

  async function deleteProvider(provider: IDProviderDto) {
    return api.delete(`/system/idprovider/${provider.name}`);
  }

  return {
    providers,
    reset,
    initProviders,
    getProvider,
    deleteProvider,
  };

});
