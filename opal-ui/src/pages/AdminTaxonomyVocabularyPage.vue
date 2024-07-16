<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('taxonomies')" to="/admin/taxonomies"/>
        <q-breadcrumbs-el :label="name" :to="`/admin/taxonomies/${name}`" />
        <q-breadcrumbs-el :label="vocabularyName" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page v-if="vocabulary" class="q-pa-md">
      <vocabulary-content :vocabulary="vocabulary"></vocabulary-content>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import VocabularyContent from 'src/components/admin/taxonomies/VocabularyContent.vue';
import { notifyError } from 'src/utils/notify';

const route = useRoute();
const taxonomiesStore = useTaxonomiesStore();
const vocabulary = computed(() => taxonomiesStore.vocabulary || null);
const name = computed(() => route.params.name as string);
const vocabularyName = computed(() => route.params.vocabulary as string);

watch(vocabularyName, (newVocabulary) => {
  if (newVocabulary) {
    taxonomiesStore.getVocabulary(name.value, newVocabulary).catch(notifyError);
  }
});

onMounted(() => {
  console.log('TaxonomiesPage mounted', taxonomiesStore.summaries);
  taxonomiesStore.initSummaries().catch(notifyError);
  taxonomiesStore.getVocabulary(name.value, vocabularyName.value).catch(notifyError);
});
</script>
