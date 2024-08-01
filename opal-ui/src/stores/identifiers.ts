import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { TableDto, VariableDto } from 'src/models/Magma';

export const useIdentifiersStore = defineStore('identifiers', () => {
  const identifiers = ref([] as TableDto[]);
  const mappings = ref([] as VariableDto[]);

  function reset() {
    identifiers.value = [];
    mappings.value = [];
  }

  async function initIdentifiers() {
    identifiers.value = [];
    return loadIdentifiers();
  }

  async function loadIdentifiers() {
    return api.get('/identifiers/tables', { params: { counts: true } }).then((response) => {
      identifiers.value = response.data;
      return response;
    });
  }

  async function addIdentifier(identifier: TableDto) {
    return api.post('/identifiers/tables', identifier).then((response) => response.data);
  }

  async function deleteIdentifier(identifier: TableDto) {
    return api.delete(`/identifiers/table/${identifier.name}`);
  }

  async function initMappings(idName: string) {
    mappings.value = [];
    return loadMappings(idName);
  }

  async function loadMappings(idName: string) {
    return api.get(`/identifiers/table/${idName}/variables`).then((response) => {
      mappings.value = response.data;
      return response;
    });
  }

  async function addMappings(identifiers: VariableDto[]) {
    return api.post('/table/keys/variables', identifiers);
  }

  async function deleteMapping(idName: string, mappingName: string) {
    return api.delete(`/identifiers/table/${idName}/variable/${mappingName}`);
  }

  return {
    identifiers,
    mappings,
    reset,
    initIdentifiers,
    addIdentifier,
    deleteIdentifier,
    initMappings,
    addMappings,
    deleteMapping,
  };
});