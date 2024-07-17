<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('taxonomies')" to="/admin/taxonomies"/>
        <q-breadcrumbs-el :label="taxonomyName" :to="`/admin/taxonomies/${taxonomyName}`" />
        <q-breadcrumbs-el :label="vocabularyName" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page v-if="vocabulary" class="q-pa-md">
      <vocabulary-content :vocabulary="vocabulary" @update="onUpdate" @refresh="onRefresh"></vocabulary-content>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import VocabularyContent from 'src/components/admin/taxonomies/VocabularyContent.vue';
import { notifyError } from 'src/utils/notify';
import { VocabularyDto } from 'src/models/Opal';

const route = useRoute();
const taxonomiesStore = useTaxonomiesStore();
const vocabulary = computed(() => taxonomiesStore.vocabulary || null);
const taxonomyName = computed(() => route.params.name as string);
const vocabularyName = computed(() => route.params.vocabulary as string);

watch(vocabularyName, (newVocabulary) => {
  if (newVocabulary) {
    console.log('$$$$$ Vocabulary name changed');
    taxonomiesStore.getVocabulary(taxonomyName.value, newVocabulary).catch(notifyError);
  }
});


async function onUpdate(updated: VocabularyDto) {
  try {
    await taxonomiesStore.updateVocabulary(taxonomyName.value, updated);
    await taxonomiesStore.getVocabulary(taxonomyName.value, vocabularyName.value);
  } catch (error) {
    notifyError(error);
  }
}

async function onRefresh() {
  try {
    await taxonomiesStore.getVocabulary(taxonomyName.value, vocabularyName.value);
  } catch (error) {
    notifyError(error);
  }
}

onMounted(() => {
  console.log('TaxonomiesPage mounted', taxonomiesStore.summaries);
  taxonomiesStore.initSummaries().catch(notifyError);
  taxonomiesStore.getVocabulary(taxonomyName.value, vocabularyName.value).catch(notifyError);
});
</script>
