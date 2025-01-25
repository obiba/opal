<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('taxonomies')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        {{ t('taxonomies') }}
      </div>
      <div class="text-help q-mb-md">
        {{ t('taxonomies_info') }}
      </div>
      <div class="row" v-if="summaries.length">
        <template v-for="summary in summaries" :key="summary.name">
          <q-card flat bordered class="on-left q-mb-md o-card-sm bg-grey-1">
            <q-card-section class="q-pa-sm text-h6 text-center bg-grey-4">
              <router-link :to="`/taxonomy/${summary.name}`">{{ summary.name }}</router-link>
            </q-card-section>
            <q-separator />
            <q-card-section class="text-hint">
              {{ getTitle(summary) }}
              <q-btn flat rounded dense icon="arrow_forward" size="xs" color="primary" :to="`/taxonomy/${summary.name}`" />
            </q-card-section>
          </q-card>
        </template>
      </div>
      <div v-else class="text-hint">
        {{ t('no_taxonomies') }}
      </div>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import type { TaxonomiesDto_TaxonomySummaryDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';

const { t, locale } = useI18n();
const taxonomiesStore = useTaxonomiesStore();
const summaries = computed(() => taxonomiesStore.summaries || []);

onMounted(() => {
  taxonomiesStore
    .initSummaries()
    .catch(notifyError);
});

function getTitle(summary: TaxonomiesDto_TaxonomySummaryDto) {
  if (!summary.title) return '';
  let title = summary.title.find((att) => att.locale === locale.value)?.text;
  if (!title) {
    title = summary.title.find((att) => att.locale === 'en')?.text;
  }
  if (!title) {
    title = '';
  }
  return title;
}
</script>
