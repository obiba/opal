import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { ItemFieldsDto, ItemResultDto } from 'src/models/Search';

export interface ItemFieldsResultDto extends ItemResultDto {
  'Search.ItemFieldsDto.item': ItemFieldsDto;
}

export interface SearchQuery {
  query: string;
  criteria: SearchCriteria;
}

export interface SearchCriteria {
  [key: string]: string[];
}

export const useSearchStore = defineStore('search', () => {
  const variablesQuery = ref({
    query: '',
    criteria: {
      project: [],
      table: [],
    } as SearchCriteria,
  });

  function reset() {
    variablesQuery.value = {
      query: '',
      criteria: {
        project: [],
        table: [],
      } as SearchCriteria,
    };
  }

  async function searchVariables(limit: number, lastDoc: string | undefined) {
    let fullQuery = variablesQuery.value.query?.trim() || '';
    Object.keys(variablesQuery.value.criteria).forEach((key) => {
      const terms = variablesQuery.value.criteria[key];
      if (terms && terms.length > 0) {
        const statement = `(${terms.map((t) => `${key}:"${t}"`).join(' OR ')})`;
        fullQuery = fullQuery.length === 0 ? statement : `${fullQuery} AND ${statement}`;
      }
    });
    return search(fullQuery, limit, ['label', 'label-en'], lastDoc);
  }

  async function countVariables() {
    let fullQuery = variablesQuery.value.query?.trim() || '';
    Object.keys(variablesQuery.value.criteria).forEach((key) => {
      const terms = variablesQuery.value.criteria[key];
      if (terms && terms.length > 0) {
        const statement = `(${terms.map((t) => `${key}:"${t}"`).join(' OR ')})`;
        fullQuery = fullQuery.length === 0 ? statement : `${fullQuery} AND ${statement}`;
      }
    });
    return count(fullQuery);
  }

  async function search(query: string, limit: number, fields: string[] | undefined, lastDoc: string | undefined) {
    return api
      .get('/datasources/variables/_search', {
        params: { query, lastDoc: lastDoc, limit, field: fields },
        paramsSerializer: {
          indexes: null, // no brackets at all
        },
      })
      .then((response) => response.data);
  }

  async function count(query: string) {
    return api
      .get('/datasources/variables/_count', {
        params: { query },
        paramsSerializer: {
          indexes: null, // no brackets at all
        },
      })
      .then((response) => response.data);
  }

  async function clearIndex(type: string) {
    return api.delete('/service/search/indices', { params: { type } });
  }

  async function getEntityTables(type: string, entity: string) {
    return api.get(`/entity/${entity}/type/${type}/tables`).then((response) => response.data);
  }

  async function getTables() {
    return api.get('/datasources/tables').then((response) => response.data);
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
    variablesQuery,
    reset,
    count,
    search,
    countVariables,
    searchVariables,
    clearIndex,
    getEntityTables,
    getTables,
    getLabels,
    getField,
  };
});
