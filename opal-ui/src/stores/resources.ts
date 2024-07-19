import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { ResourceProvidersDto } from 'src/models/Resources';
import { ResourceReferenceDto } from 'src/models/Projects';

export const useResourcesStore = defineStore('resources', () => {

  const resourceProviders = ref<ResourceProvidersDto>();
  const project = ref<string>();
  const resourceReferences = ref<ResourceReferenceDto[]>([]);

  function reset() {
    project.value = undefined;
    resourceReferences.value = [];
  }

  async function initResourceReferences(pName: string) {
    if (project.value === pName) return initResourceProviders();
    return Promise.all([initResourceProviders(), loadResourceReferences(pName)]);
  }

  async function initResourceProviders() {
    if (resourceProviders.value) return Promise.resolve();
    return loadResourceProviders();
  }

  async function loadResourceProviders() {
    return api.get('/resource-providers').then((response) => resourceProviders.value = response.data);
  }

  async function loadResourceReferences(pName: string) {
    project.value = pName;
    return api.get(`/project/${project.value}/resources`).then((response) => resourceReferences.value = response.data);
  }

  function getResourceReference(name: string) {
    return resourceReferences.value?.find((reference) => reference.name === name);
  }

  function getResourceFactory(reference: ResourceReferenceDto) {
    return resourceProviders.value?.providers.find((provider) => provider.name === reference.provider)?.resourceFactories.find((factory) => factory.name === reference.factory);
  }

  function getResourceProvider(reference: ResourceReferenceDto) {
    return resourceProviders.value?.providers.find((provider) => provider.name === reference.provider);
  }

  return {
    project,
    resourceReferences,
    reset,
    initResourceProviders,
    initResourceReferences,
    loadResourceReferences,
    getResourceFactory,
    getResourceReference,
    getResourceProvider,
  }

});