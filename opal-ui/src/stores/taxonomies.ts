import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { TaxonomyDto, TaxonomiesDto, TaxonomiesDto_TaxonomySummaryDto, LocaleTextDto } from 'src/models/Opal';
import { AttributeDto } from 'src/models/Magma';
import { Annotation } from 'src/components/models';

export const useTaxonomiesStore = defineStore('taxonomies', () => {
  const taxonomies = ref<TaxonomyDto[]>([]);
  const summaries = ref<TaxonomiesDto_TaxonomySummaryDto[]>([]);

  function reset() {
    taxonomies.value = [];
    summaries.value = [];
  }

  function refresh() {
    taxonomies.value = [];
    loadTaxonomies();
  }

  function refreshSummaries() {
    summaries.value = [];
    loadSummaries();
  }

  async function init() {
    if (taxonomies.value.length === 0)
      return loadTaxonomies();
    return Promise.resolve();
  }

  async function initSummaries() {
    if (taxonomies.value.length === 0)
      return loadSummaries();
    return Promise.resolve();
  }

  async function loadTaxonomies() {
    return api.get('/system/conf/taxonomies').then((response) => {
      taxonomies.value = response.data;
      return response;
    });
  }

  async function loadSummaries() {
    return api.get('/system/conf/taxonomies/summaries').then((response) => {
      summaries.value = response.data.summaries || [];
      return response;
    });
  }

  function getLabel(messages: LocaleTextDto[], locale: string): string {
    if (!messages || messages.length === 0) return '';
    let msg = messages.find((msg) => msg.locale === locale);
    if (msg) return msg.text;
    msg = messages.find((msg) => msg.locale === 'en');
    if (msg) return msg.text;
    return messages[0].text;
  }

  function getAnnotations(attributes: AttributeDto[], withTerms: boolean): Annotation[] {
    const annotations = attributes
      .filter((a) => a.namespace && a.name && a.value)
      .map((a) => {
        const taxo = taxonomies.value.find((t) => t.name === a.namespace);
        const voc = taxo?.vocabularies.find((v) => v.name === a.name);
        const trm = voc?.terms?.find((t) => t.name === a.value);
        return {
          id: `${taxo?.name}::${voc?.name}:${trm?.name}`,
          taxonomy: taxo,
          vocabulary: voc,
          term: trm,
          attributes: [a],
        };
      })
      .filter((a) => a.taxonomy && a.vocabulary && (!withTerms || a.term)) as Annotation[];
    // merge annotations with same taxonomy and vocabulary
    for (let i = 0; i < annotations.length; i++) {
      for (let j = i + 1; j < annotations.length; j++) {
        if (annotations[i].taxonomy === annotations[j].taxonomy && annotations[i].vocabulary === annotations[j].vocabulary) {
          annotations[i].attributes.push(...annotations[j].attributes);
          annotations.splice(j, 1);
          j--;
        }
      }
    }
    // sort by taxonomy and vocabulary
    annotations.sort((a, b) => {
      if (a.taxonomy.name < b.taxonomy.name) return -1;
      if (a.taxonomy.name > b.taxonomy.name) return 1;
      if (a.vocabulary.name < b.vocabulary.name) return -1;
      if (a.vocabulary.name > b.vocabulary.name) return 1;
      return 0;
    });
    return annotations;
  }

  return {
    taxonomies,
    summaries,
    reset,
    refresh,
    refreshSummaries,
    init,
    initSummaries,
    getLabel,
    getAnnotations,
  };
});
