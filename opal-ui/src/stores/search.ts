import { defineStore } from 'pinia';
import { api } from 'src/boot/api';

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

  return {
    search,
    clearIndex,
    getEntityTables,
  };

});
