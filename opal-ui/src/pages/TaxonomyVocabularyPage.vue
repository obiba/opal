<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('taxonomies')" to="/taxonomies" />
        <q-breadcrumbs-el :label="taxonomyName" :to="`/taxonomy/${taxonomyName}`" />
        <q-breadcrumbs-el :label="vocabularyName" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page v-if="vocabulary" class="q-pa-md">
      <vocabulary-content
        :taxonomy="taxonomyName"
        :vocabulary="vocabulary"
        @update="onUpdate"
        @refresh="onRefresh"
      ></vocabulary-content>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import VocabularyContent from 'src/components/taxonomies/VocabularyContent.vue';
import { notifyError } from 'src/utils/notify';
import type { VocabularyDto } from 'src/models/Opal';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const taxonomiesStore = useTaxonomiesStore();
const systemStore = useSystemStore();
const vocabulary = computed(() => taxonomiesStore.vocabulary || null);
const taxonomyName = computed(() => route.params.name as string);
const vocabularyName = computed(() => route.params.vocabulary as string);

watch(vocabularyName, (newVocabulary) => {
  if (newVocabulary) {
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

async function onRefresh(newName?: string) {
  try {
    if (newName !== undefined) {
      await taxonomiesStore.getVocabulary(taxonomyName.value, newName);
      router.replace(`/taxonomy/${taxonomyName.value}/vocabulary/${newName}`);
    } else {
      await taxonomiesStore.getVocabulary(taxonomyName.value, vocabularyName.value);
    }
  } catch (error) {
    notifyError(error);
  }
}

onMounted(() => {
  systemStore.initGeneralConf();
  taxonomiesStore.initSummaries().catch(notifyError);
  taxonomiesStore.getVocabulary(taxonomyName.value, vocabularyName.value).catch((error) => {
    notifyError(error);
    router.replace(`/taxonomy/${taxonomyName}`);
  });
});
</script>
