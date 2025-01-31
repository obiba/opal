import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { TableDto, VariableDto } from 'src/models/Magma';
import type { IdentifiersMappingDto } from 'src/models/Identifiers';

export interface GenerateIdentifiersOptions {
  prefix: string | '';
  size: number;
  zeros: boolean | false;
  checksum: boolean | false;
}

export const useIdentifiersStore = defineStore('identifiers', () => {
  const identifiersTables = ref([] as TableDto[]);
  const mappings = ref([] as VariableDto[]);

  function reset() {
    identifiersTables.value = [];
    mappings.value = [];
  }

  async function initIdentifiersTables() {
    identifiersTables.value = [];
    return loadIdentifiersTables();
  }

  async function loadIdentifiersTables() {
    return api.get('/identifiers/tables', { params: { counts: true } }).then((response) => {
      identifiersTables.value = response.data;
      return response;
    });
  }

  async function addIdentifierTable(identifier: TableDto) {
    return api.post('/identifiers/tables', identifier).then((response) => response.data);
  }

  async function deleteIdentifierTable(identifier: TableDto) {
    return api.delete(`/identifiers/table/${identifier.name}`);
  }

  async function initMappings(idTableName: string) {
    mappings.value = [];
    return loadMappings(idTableName);
  }

  async function loadMappings(idTableName: string) {
    return api.get(`/identifiers/table/${idTableName}/variables`).then((response) => {
      mappings.value = response.data;
      return response;
    });
  }

  async function getAllMappings() {
    return api.get('/identifiers/mappings').then((response) => {
      return response.data;
    });
  }

  async function addMappings(idTableName: string, mappings: VariableDto[]) {
    return api.post(`/identifiers/table/${idTableName}/variables`, mappings);
  }

  async function addMapping(idTableName: string, mappings: VariableDto) {
    return addMappings(idTableName, [mappings]);
  }

  async function updateMapping(idTableName: string, mapping: VariableDto) {
    return api.put(`/identifiers/table/${idTableName}/variable/${mapping.name}`, mapping);
  }

  async function deleteMapping(idTableName: string, mappingName: string) {
    return api.delete(`/identifiers/table/${idTableName}/variable/${mappingName}`);
  }

  async function getMappingIdentifiersCount(entityType: string, mappingName: string) {
    return api
      .get(`/identifiers/mapping/${mappingName}/_count`, { params: { type: entityType } })
      .then((response) => response.data);
  }

  async function generateMapping(entityType: string, mappingName: string, options: GenerateIdentifiersOptions) {
    return api.post(`/identifiers/mapping/${mappingName}/_generate`, null, {
      params: { type: entityType, ...options },
    });
  }

  async function importSystemIdentifiers(entityType: string, content: string, separator?: string) {
    return api.post('/identifiers/mapping/entities/_import', content, {
      params: separator ? { type: entityType, separator } : { type: entityType },
      headers: { 'Content-Type': 'text/plain' },
    });
  }

  async function importTableSystemIdentifiers(datasource: string, table: string) {
    return api.post('/identifiers/mappings/entities/_sync', null, { params: { datasource, table } });
  }

  async function importMappingSystemIdentifiers(
    entityType: string,
    mappingName: string,
    content: string,
    separator?: string
  ) {
    return api.post(`/identifiers/mapping/${mappingName}/_import`, content, {
      params: separator ? { type: entityType, separator } : { type: entityType },
      headers: { 'Content-Type': 'text/plain' },
    });
  }

  async function getMappings(type = 'Participant'): Promise<IdentifiersMappingDto[]> {
    return api.get('/identifiers/mappings', { params: { type } }).then((response) => response.data);
  }

  async function loadIdentifiers(idTableName: string, offset = 0, limit = 20) {
    return api
      .get(`/identifiers/table/${idTableName}/valueSets`, { params: { select: true, offset, limit } })
      .then((response) => response.data);
  }

  return {
    identifiers: identifiersTables,
    mappings,
    reset,
    initIdentifiersTables,
    addIdentifierTable,
    deleteIdentifierTable,
    initMappings,
    getAllMappings,
    addMappings,
    addMapping,
    updateMapping,
    deleteMapping,
    getMappingIdentifiersCount,
    generateMapping,
    importSystemIdentifiers,
    importTableSystemIdentifiers,
    importMappingSystemIdentifiers,
    getMappings,
    loadIdentifiers,
  };
});
