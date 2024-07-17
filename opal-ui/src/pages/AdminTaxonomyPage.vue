<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('taxonomies')" to="/admin/taxonomies"/>
        <q-breadcrumbs-el :label="taxonomyName" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page v-if="taxonomy" class="q-pa-md">
      <taxonomy-content :taxonomy="taxonomy" @update="onUpdate" @refresh="onRefresh"></taxonomy-content>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import TaxonomyContent from 'src/components/admin/taxonomies/TaxonomyContent.vue';
import { notifyError } from 'src/utils/notify';
import { TaxonomyDto } from 'src/models/Opal';

const route = useRoute();
const taxonomiesStore = useTaxonomiesStore();
const taxonomy = computed(() => taxonomiesStore.taxonomy || null);
const taxonomyName = computed(() => route.params.name as string);

watch(taxonomyName, (newName) => {
  if (newName) {
    taxonomiesStore.initSummaries().catch(notifyError);
    taxonomiesStore.getTaxonomy(taxonomyName.value).catch(notifyError);
  }
});

async function onUpdate(updated: TaxonomyDto) {
  try {
    await taxonomiesStore.updateTaxonomy(updated);
    await taxonomiesStore.getTaxonomy(taxonomyName.value);
  } catch (error) {
    notifyError(error);
  }
}

async function onRefresh() {
  try {
    await taxonomiesStore.getTaxonomy(taxonomyName.value);
  } catch (error) {
    notifyError(error);
  }
}

onMounted(() => {
  console.log('TaxonomyPage mounted', taxonomiesStore.summaries);
  taxonomiesStore.initSummaries().catch(notifyError);
  taxonomiesStore.getTaxonomy(taxonomyName.value).catch(notifyError);
});
</script>
