import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { IDProviderDto } from 'src/models/Opal';

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
      providers.value = response.data;
    });
  }

  async function addProvider(provider: IDProviderDto) {
    return api.post('/system/idproviders', provider);
  }

  async function updateProvider(provider: IDProviderDto) {
    return api.put(`/system/idprovider/${provider.name}`, provider);
  }

  async function getProvider(provider: IDProviderDto) {
    return api.delete(`/system/idprovider/${provider.name}`);
  }

  async function toggleEnableProvider(provider: IDProviderDto) {
    return provider.enabled
      ? api.delete(`/system/idprovider/${provider.name}/_enable`)
      : api.put(`/system/idprovider/${provider.name}/_enable`);
  }

  async function deleteProvider(provider: IDProviderDto) {
    return api.delete(`/system/idprovider/${provider.name}`);
  }

  return {
    providers,
    reset,
    initProviders,
    getProvider,
    addProvider,
    updateProvider,
    deleteProvider,
    toggleEnableProvider,
  };
});
