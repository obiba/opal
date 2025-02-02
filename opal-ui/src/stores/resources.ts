import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { ResourceProvidersDto } from 'src/models/Resources';
import type { ResourceReferenceDto } from 'src/models/Projects';
import { Perms } from 'src/utils/authz';

interface ResourcePerms {
  resources: Perms | undefined;
  resourcesPermissions: Perms | undefined;
  resource: Perms | undefined;
  resourcePermissions: Perms | undefined;
}

export const useResourcesStore = defineStore('resources', () => {
  const resourceProviders = ref<ResourceProvidersDto>();
  const project = ref<string>();
  const resourceReferences = ref<ResourceReferenceDto[]>([]);
  const perms = ref({} as ResourcePerms);

  function reset() {
    project.value = undefined;
    resourceReferences.value = [];
    resourceProviders.value = undefined;
    perms.value = {} as ResourcePerms;
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
    return api.get('/resource-providers').then((response) => (resourceProviders.value = response.data));
  }

  async function loadResourceReferences(pName: string) {
    project.value = pName;
    perms.value.resources = undefined;
    perms.value.resourcesPermissions = undefined;
    return Promise.all([
      api.get(`/project/${project.value}/resources`).then((response) => {
        perms.value.resources = new Perms(response);
        resourceReferences.value = response.data;
        return response;
      }),
      api.options(`/project/${pName}/permissions/resources`).then((response) => {
        perms.value.resourcesPermissions = new Perms(response);
        return response;
      }),
    ]);
  }

  function getResourceReference(name: string) {
    return resourceReferences.value?.find((reference) => reference.name === name);
  }

  async function testResource(pName: string, name: string) {
    return api.put(`/project/${pName}/resource/${name}/_test`);
  }

  async function deleteResource(pName: string, name: string) {
    return api.delete(`/project/${pName}/resource/${name}`);
  }

  async function deleteResources(pName: string, names: string[]) {
    return api.delete(`/project/${pName}/resources`, {
      params: { names },
      paramsSerializer: {
        indexes: null,
      },
    });
  }

  function getResourceFactory(reference: ResourceReferenceDto) {
    return (resourceProviders.value?.providers ?? [])
      .find((provider) => provider.name === reference.provider)
      ?.resourceFactories.find((factory) => factory.name === reference.factory);
  }

  function getResourceProvider(reference: ResourceReferenceDto) {
    return (resourceProviders.value?.providers ?? []).find((provider) => provider.name === reference.provider);
  }

  async function addResource(resourceRef: ResourceReferenceDto) {
    return api.post(`/project/${resourceRef.project}/resources`, resourceRef);
  }

  async function saveResource(resourceRef: ResourceReferenceDto) {
    return api.put(`/project/${resourceRef.project}/resource/${resourceRef.name}`, resourceRef);
  }

  async function loadResourcePerms(pName: string, name: string) {
    perms.value.resource = undefined;
    perms.value.resourcePermissions = undefined;
    return Promise.all([
      api.options(`/project/${pName}/resource/${name}`).then((response) => {
        perms.value.resource = new Perms(response);
        return response;
      }),
      api.options(`/project/${pName}/permissions/resource/${name}`).then((response) => {
        perms.value.resourcePermissions = new Perms(response);
        return response;
      }),
    ]);
  }

  return {
    project,
    resourceReferences,
    resourceProviders,
    perms,
    reset,
    initResourceProviders,
    initResourceReferences,
    loadResourceReferences,
    getResourceFactory,
    getResourceReference,
    getResourceProvider,
    testResource,
    deleteResource,
    deleteResources,
    addResource,
    saveResource,
    loadResourcePerms,
  };
});
