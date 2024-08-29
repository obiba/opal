import { add } from 'date-fns';
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

  async function addMappings(idName: string, mappings: VariableDto[]) {
    return api.post(`/identifiers/table/${idName}/variables`, mappings);
  }

  async function addMapping(idName: string, mappings: VariableDto) {
    return addMappings(idName, [mappings]);
  }

  async function updateMapping(idName: string, mapping: VariableDto) {
    return api.put(`/identifiers/table/${idName}/variable/${mapping.name}`, mapping);
  }

  async function deleteMapping(idName: string, mappingName: string) {
    return api.delete(`/identifiers/table/${idName}/variable/${mappingName}`);
  }

  async function getMappingIdentifiersCount(idName: string, mappingName: string) {
    return api
      .get(`/identifiers/mapping/${mappingName}/_count`, { params: { type: idName } })
      .then((response) => response.data);
  }

  async function importSystemIdentifiers(idName: string, content: string, separator?: string) {
    return api.post('/identifiers/mapping/entities/_import', content, {
      params: separator ? { type: idName, separator } : { type: idName },
      headers: { 'Content-Type': 'text/plain' },
    });
  }

  async function importMappingSystemIdentifiers(idName: string, mappingName: string, content: string, separator?: string) {
    return api.post(`/identifiers/mapping/${mappingName}/_import`, content, {
      params: separator ? { type: idName, separator } : { type: idName },
      headers: { 'Content-Type': 'text/plain' },
    });
  }

  async function getMappings(type = 'Participant') {
    return api.get('/identifiers/mappings', { params: { type } }).then((response) => response.data);
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
    addMapping,
    updateMapping,
    deleteMapping,
    getMappingIdentifiersCount,
    importSystemIdentifiers,
    importMappingSystemIdentifiers,
    getMappings,
  };
});
