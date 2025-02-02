<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('taxonomies')" to="/taxonomies" />
        <q-breadcrumbs-el :label="taxonomyName" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page v-if="taxonomy" class="q-pa-md">
      <taxonomy-content :taxonomy="taxonomy" @update="onUpdate" @refresh="onRefresh"></taxonomy-content>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import TaxonomyContent from 'src/components/taxonomies/TaxonomyContent.vue';
import { notifyError } from 'src/utils/notify';
import type { TaxonomyDto } from 'src/models/Opal';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const taxonomiesStore = useTaxonomiesStore();
const systemStore = useSystemStore();
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

async function onRefresh(newName?: string) {
  try {
    if (newName) {
      await taxonomiesStore.refreshSummaries();
      router.replace(`/taxonomy/${newName}`);
    } else {
      await taxonomiesStore.getTaxonomy(taxonomyName.value);
    }
  } catch (error) {
    notifyError(error);
  }
}

onMounted(() => {
  systemStore.initGeneralConf();

  taxonomiesStore.initSummaries().catch(notifyError);
  taxonomiesStore.getTaxonomy(taxonomyName.value).catch((error) => {
    notifyError(error);
    router.replace('/taxonomies');
  });
});
</script>
