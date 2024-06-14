import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { RPackageDto } from 'src/models/OpalR';
import { DataShieldProfileDto, DataShieldMethodDto, DataShieldROptionDto } from 'src/models/DataShield';

export const useDatashieldStore = defineStore('datashield', () => {

  const profiles = ref<DataShieldProfileDto[]>([]);
  const profile = ref<DataShieldProfileDto>({} as DataShieldProfileDto);
  const methods = ref<{ [key: string]: DataShieldMethodDto[]}>({
    aggregate: [],
    assign: [],
  }); // key: 'aggregate' | 'assign'
  const options = ref<DataShieldROptionDto[]>([]);

  function reset() {
    profiles.value = [];
    profile.value = {} as DataShieldProfileDto;
    methods.value = {
      aggregate: [],
      assign: [],
    };
    options.value = [];
  }

  async function initProfiles() {
    return api.get('/datashield/profiles').then((response) => {
      if (response.status === 200) {
        profiles.value = response.data;
      }
      return response;
    });
  }

  async function getPackages(): Promise<RPackageDto[]> {
    return api.get('/datashield/packages', { params: { profile: profile.value.cluster } }).then((response) => response.data);
  }

  async function loadMethods(env: string): Promise<DataShieldMethodDto[]> {
    return api.get(`/datashield/env/${env}/methods`, { params: { profile: profile.value.name } }).then((response) => response.data);
  }

  async function loadOptions(): Promise<DataShieldROptionDto[]> {
    return api.get('/datashield/options', { params: { profile: profile.value.name } }).then((response) => response.data);
  }

  async function deleteMethods(env: string, methodNames: string[]) {
    return api.delete(`/datashield/env/${env}/methods`, {
      params: { profile: profile.value.name, name: methodNames },
      paramsSerializer: {
        indexes: null, // no brackets at all
      }
    }).then(() => loadProfileSettings());
  }

  async function addMethod(env: string, method: DataShieldMethodDto) {
    return api.post(`/datashield/env/${env}/methods`, method, { params: { profile: profile.value.name } }).then(() => loadProfileSettings());
  }

  async function updateMethod(env: string, method: DataShieldMethodDto) {
    return api.put(`/datashield/env/${env}/method/${method.name}`, method, { params: { profile: profile.value.name } }).then(() => loadProfileSettings());
  }

  async function deleteOptions(optionNames: string[]) {
    return api.delete('/datashield/options', {
      params: { profile: profile.value.name, name: optionNames },
      paramsSerializer: {
        indexes: null, // no brackets at all
      }
    }).then(() => loadProfileSettings());
  }

  async function setOption(option: DataShieldROptionDto) {
    return api.post('/datashield/option', option, { params: { profile: profile.value.name } }).then(() => loadProfileSettings());
  }

  async function initProfileSettings(prof: DataShieldProfileDto) {
    profile.value = prof;
    return loadProfileSettings();
  }

  async function updateProfileStatus(status: boolean) {
    return status ?
      api.put(`/datashield/profile/${profile.value.name}/_enable`).then(() => profile.value.enabled = true)
      : api.delete(`/datashield/profile/${profile.value.name}/_enable`).then(() => profile.value.enabled = false);
  }

  async function loadProfileSettings() {
    return Promise.all(
      [
        loadOptions().then((response) => options.value = response),
        ...['aggregate', 'assign']
          .map((env) => loadMethods(env).then((response) => methods.value[env] = response))
      ]
    );
  }

  async function applyProfileSettings(packageNames: string[]) {
    return api.put('/datashield/packages/_publish', {}, {
      params: { profile: profile.value.name, name: packageNames },
      paramsSerializer: {
        indexes: null, // no brackets at all
      }
    }).then(() => loadProfileSettings());
  }

  return {
    profiles,
    profile,
    methods,
    options,
    reset,
    initProfiles,
    getPackages,
    deleteMethods,
    deleteOptions,
    initProfileSettings,
    loadProfileSettings,
    applyProfileSettings,
    updateProfileStatus,
    setOption,
    addMethod,
    updateMethod,
  };
});
