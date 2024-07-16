<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('taxonomies')" to="/admin/taxonomies"/>
        <q-breadcrumbs-el :label="name" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page v-if="taxonomy" class="q-pa-md">
      <taxonomy-content :taxonomy="taxonomy"></taxonomy-content>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import TaxonomyContent from 'src/components/admin/taxonomies/TaxonomyContent.vue';
import { notifyError } from 'src/utils/notify';

const route = useRoute();
const taxonomiesStore = useTaxonomiesStore();
const taxonomy = computed(() => taxonomiesStore.taxonomy || null);
const name = computed(() => route.params.name as string);

watch(name, (newName) => {
  if (newName) {
    taxonomiesStore.getTaxonomy(name.value).catch(notifyError);
  }
});

onMounted(() => {
  console.log('TaxonomiesPage mounted', taxonomiesStore.summaries);
  taxonomiesStore.initSummaries().catch(notifyError);
  taxonomiesStore.getTaxonomy(name.value).catch(notifyError);
});
</script>
