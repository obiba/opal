import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { ItemFieldsDto, ItemResultDto } from 'src/models/Search';

export interface ItemFieldsResultDto extends ItemResultDto {
  'Search.ItemFieldsDto.item': ItemFieldsDto;
}

export const useSearchStore = defineStore('search', () => {

  async function search(query: string, limit: number, fields: string[] | undefined) {
    return api.get('/datasources/variables/_search', {
      params: { query, limit, field: fields },
      paramsSerializer: {
        indexes: null, // no brackets at all
      }
    })
      .then(response => response.data);
  }

  async function clearIndex(type: string) {
    return api.delete('/service/search/indices', { params: { type } });
  }

  async function getEntityTables(type: string, entity: string) {
    return api.get(`/entity/${entity}/type/${type}/tables`)
      .then(response => response.data);
  }

  async function getTables() {
    return api.get('/datasources/tables')
      .then(response => response.data);
  }

  //
  // Results helpers
  //

  function getLabels(item: ItemFieldsResultDto) {
    const fields = item['Search.ItemFieldsDto.item'].fields;
    if (!fields) {
      return [];
    }
    const labels = [];
    for (const field of fields) {
      if (field.key.startsWith('label')) {
        const tokens = field.key.split('-');
        labels.push({ value: field.value, locale: tokens.length > 1 ? tokens[1] : undefined });
      }
    }
    return labels;
  }

  function getField(item: ItemFieldsResultDto, key: string) {
    const fields = item['Search.ItemFieldsDto.item'].fields;
    if (!fields) {
      return '';
    }
    return fields.find((field) => field.key === key)?.value;
  }

  return {
    search,
    clearIndex,
    getEntityTables,
    getTables,
    getLabels,
    getField,
  };

});
