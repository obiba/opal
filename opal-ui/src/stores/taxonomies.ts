import { defineStore } from 'pinia';
import { api, baseUrl } from 'src/boot/api';
import type { TaxonomyDto, TaxonomiesDto_TaxonomySummaryDto, LocaleTextDto, VocabularyDto, TermDto } from 'src/models/Opal';
import type { AttributeDto } from 'src/models/Magma';
import type { Annotation } from 'src/components/models';

export interface TaxonomiesImportOptions {
  user: string;
  repo: string;
  override: boolean | false;
  ref?: string;
  file?: string;
}

export const useTaxonomiesStore = defineStore('taxonomies', () => {
  const taxonomies = ref<TaxonomyDto[]>([]);
  const taxonomy = ref<TaxonomyDto>({} as TaxonomyDto);
  const vocabulary = ref<VocabularyDto>({} as VocabularyDto);
  const summaries = ref<TaxonomiesDto_TaxonomySummaryDto[]>([]);
  const canAdd = ref(false);
  const canEdit = ref(false);

  function reset() {
    taxonomies.value = [];
    summaries.value = [];
    taxonomy.value = {} as TaxonomyDto;
    vocabulary.value = {} as VocabularyDto;
  }

  function refresh() {
    taxonomies.value = [];
    loadTaxonomies();
  }

  async function refreshSummaries() {
    summaries.value = [];
    return loadSummaries();
  }

  async function init() {
    if (taxonomies.value.length === 0) return loadTaxonomies();
    return Promise.resolve();
  }

  async function initSummaries() {
    loadTaxonomiesPermissions();
    if (summaries.value.length === 0) {
      return loadSummaries();
    }
    return Promise.resolve();
  }

  async function loadTaxonomiesPermissions() {
    return api.options('/system/conf/taxonomies').then((response) => {
      canAdd.value = response.headers['allow'].split(',').map((m: string) => m.trim()).includes('POST');
      return response;
    });
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

  async function getTaxonomy(name: string) {
    taxonomy.value = {} as TaxonomyDto;
    vocabulary.value = {} as VocabularyDto;
    canEdit.value = false;
    return api.get(`/system/conf/taxonomy/${name}`).then((response) => {
      canEdit.value = response.headers['allow'].split(',').map((m: string) => m.trim()).includes('DELETE');
      taxonomy.value = response.data;
      return response;
    });
  }

  async function addTaxonomy(taxonomy: TaxonomyDto) {
    return api.post('/system/conf/taxonomies', taxonomy).then((response) => response.data);
  }

  async function updateTaxonomy(taxonomy: TaxonomyDto, oldName?: string) {
    return api
      .put(`/system/conf/taxonomy/${oldName ? oldName : taxonomy.name}`, taxonomy)
      .then((response) => response.data);
  }

  async function deleteTaxonomy(toDelete: TaxonomyDto) {
    taxonomy.value = {} as TaxonomyDto;
    return api.delete(`/system/conf/taxonomy/${toDelete.name}`);
  }

  async function addVocabulary(taxonomyName: string, vocabulary: VocabularyDto) {
    return api.post(`/system/conf/taxonomy/${taxonomyName}/vocabularies`, vocabulary).then((response) => response.data);
  }

  async function updateVocabulary(taxonomyName: string, vocabulary: VocabularyDto, oldName?: string) {
    return api
      .put(`/system/conf/taxonomy/${taxonomyName}/vocabulary/${oldName ? oldName : vocabulary.name}`, vocabulary)
      .then((response) => response.data);
  }

  async function deleteVocabulary(taxonomyName: string, vocabulary: VocabularyDto) {
    return api.delete(`/system/conf/taxonomy/${taxonomyName}/vocabulary/${vocabulary.name}`);
  }

  async function getVocabulary(taxonomyName: string, vocabularyName: string) {
    vocabulary.value = {} as VocabularyDto;
    return api.get(`/system/conf/taxonomy/${taxonomyName}/vocabulary/${vocabularyName}`).then((response) => {
      vocabulary.value = response.data;
      return response;
    });
  }

  async function addTerm(taxonomyName: string, vocabularyName: string, term: TermDto) {
    return api
      .post(`/system/conf/taxonomy/${taxonomyName}/vocabulary/${vocabularyName}/terms`, term)
      .then((response) => response.data);
  }

  async function updateTerm(taxonomyName: string, vocabularyName: string, term: TermDto, oldName?: string) {
    return api
      .put(
        `/system/conf/taxonomy/${taxonomyName}/vocabulary/${vocabularyName}/term/${oldName ? oldName : term.name}`,
        term
      )
      .then((response) => response.data);
  }

  async function deleteTerm(taxonomyName: string, vocabularyName: string, term: TermDto) {
    return api.delete(`/system/conf/taxonomy/${taxonomyName}/vocabulary/${vocabularyName}/term/${term.name}`);
  }

  async function gitCommits(name: string) {
    return api.get(`/system/conf/taxonomy/${name}/commits`).then((response) => response.data);
  }

  async function gitCompare(name: string, id: string) {
    return api.get(`/system/conf/taxonomy/${name}/commit/${id}`).then((response) => response.data);
  }

  async function gitCompareWith(name: string, id: string, withId = 'head') {
    return api.get(`/system/conf/taxonomy/${name}/commit/${withId}/${id}`).then((response) => response.data);
  }

  async function gitRestore(name: string, id: string) {
    return api.put(`/system/conf/taxonomy/${name}/restore/${id}`).then((response) => response.data);
  }

  async function getMlstrTaxonomies() {
    return api
      .get('/system/conf/taxonomies/tags/_github', {
        params: {
          user: 'maelstrom-research',
          repo: 'maelstrom-taxonomies',
        },
      })
      .then((response) => response.data);
  }

  async function importGithubTaxonomies(options: TaxonomiesImportOptions) {
    return api
      .post('/system/conf/taxonomies/import/_github', null, {
        params: options,
      })
      .then((response) => response.data);
  }

  async function importMlstrTaxonomies(ref: string, override = true) {
    const params: TaxonomiesImportOptions = {
      user: 'maelstrom-research',
      repo: 'maelstrom-taxonomies',
      override,
      ref,
    };

    return importGithubTaxonomies(params);
  }

  async function importFileTaxonomy(file: string, override = false) {
    return api
      .post('/system/conf/taxonomies/import/_file', null, {
        params: {
          file,
          override,
        },
      })
      .then((response) => response.data);
  }

  function getLabel(messages: LocaleTextDto[], locale: string): string {
    if (!messages || messages.length === 0) return '';
    let msg = messages.find((msg) => msg.locale === locale);
    if (msg) return msg.text;
    msg = messages.find((msg) => msg.locale === 'en');
    if (msg) return msg.text;
    return messages[0]?.text || '';
  }

  function getAnnotations(attributes: AttributeDto[], withTerms: boolean): Annotation[] {
    if (!attributes) return [];
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
        if (
          annotations[i]?.taxonomy === annotations[j]?.taxonomy &&
          annotations[i]?.vocabulary === annotations[j]?.vocabulary
        ) {
          if (annotations[j]?.attributes)
            annotations[i]?.attributes.push(...annotations[j]?.attributes || []);
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

  function downloadTaxonomy(taxonomy: TaxonomyDto) {
    window.open(`${baseUrl}/system/conf/taxonomy/${taxonomy.name}/_download`, '_self');
  }

  return {
    taxonomies,
    summaries,
    taxonomy,
    vocabulary,
    canAdd,
    canEdit,
    reset,
    refresh,
    refreshSummaries,
    init,
    initSummaries,
    addTaxonomy,
    getTaxonomy,
    updateTaxonomy,
    deleteTaxonomy,
    addVocabulary,
    updateVocabulary,
    deleteVocabulary,
    getVocabulary,
    addTerm,
    updateTerm,
    deleteTerm,
    gitCommits,
    gitCompare,
    gitCompareWith,
    gitRestore,
    getMlstrTaxonomies,
    importMlstrTaxonomies,
    importGithubTaxonomies,
    importFileTaxonomy,
    getLabel,
    getAnnotations,
    downloadTaxonomy,
  };
});
