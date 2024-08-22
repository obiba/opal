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

  return { search };

});
