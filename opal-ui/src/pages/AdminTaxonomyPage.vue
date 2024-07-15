<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('taxonomies')" to="/admin/taxonomies"/>
        <template v-if="vocabularyName">
          <q-breadcrumbs-el :label="name" :to="`/admin/taxonomies/${name}`" />
          <q-breadcrumbs-el :label="vocabularyName" />
        </template>
        <template v-else>
          <q-breadcrumbs-el :label="name" />
        </template>
      </q-breadcrumbs>
    </q-toolbar>
    <q-page v-if="taxonomy" class="q-pa-md">
      <taxonomy-content :taxonomy="taxonomy" :vocabulary="vocabulary"></taxonomy-content>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import TaxonomyContent from 'src/components/admin/taxonomies/TaxonomyContent.vue';
import { notifyError } from 'src/utils/notify';

const route = useRoute();
const taxonomiesStore = useTaxonomiesStore();
const taxonomy = computed(() => taxonomiesStore.taxonomy || null);
const vocabulary = computed(() => taxonomiesStore.vocabulary || null);
const name = computed(() => route.params.name as string);
const vocabularyName = computed(() => route.params.vocabulary as string || null);

watch(name, (newName) => {
  if (newName) {
    taxonomiesStore.getTaxonomy(name.value).catch(notifyError);
  }
});

watch(vocabularyName, (newVocabulary) => {
  if (newVocabulary) {
    taxonomiesStore.getVocabulary(name.value, newVocabulary).catch(notifyError);
  } else {
    taxonomiesStore.getTaxonomy(name.value).catch(notifyError);
  }
});

onMounted(() => {
  console.log('TaxonomiesPage mounted', taxonomiesStore.summaries);
  taxonomiesStore.initSummaries().catch(notifyError);
  if (!!vocabularyName.value) {
    taxonomiesStore.getVocabulary(name.value, vocabularyName.value).catch(notifyError);
  } else {
    taxonomiesStore.getTaxonomy(name.value).catch(notifyError);
  }
});
</script>
