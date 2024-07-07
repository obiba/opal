import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { TaxonomyDto } from 'src/models/Opal';

export const useTaxonomiesStore = defineStore('taxonomies', () => {
  const taxonomies = ref<TaxonomyDto[]>([]);

  function reset() {
    taxonomies.value = [];
  }

  function refresh() {
    taxonomies.value = [];
    loadTaxonomies();
  }

  async function init() {
    if (taxonomies.value.length === 0)
      return loadTaxonomies();
    return Promise.resolve();
  }

  async function loadTaxonomies() {
    return api.get('/system/conf/taxonomies').then((response) => {
      taxonomies.value = response.data;
      return response;
    });
  }

  return {
    taxonomies,
    reset,
    refresh,
    init,
  };
});
