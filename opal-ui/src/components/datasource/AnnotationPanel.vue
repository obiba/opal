<template>
  <q-card flat>
    <q-card-section v-if="header" class="q-pa-none q-mb-sm">
      <q-breadcrumbs separator=">" class="float-left on-left">
        <q-breadcrumbs-el :label="taxonomiesStore.getLabel(annotation.taxonomy.title, locale)" />
        <q-breadcrumbs-el :label="taxonomiesStore.getLabel(annotation.vocabulary.title, locale)" />
      </q-breadcrumbs>
      <q-btn
        :icon="showDetails ? 'expand_less' : 'expand_more'"
        flat
        dense
        color="primary"
        size="sm"
        no-caps
        :label="t(showDetails ? 'less' : 'more')"
        @click="showDetails = !showDetails"
      />
    </q-card-section>
    <q-card-section v-if="!header || showDetails" class="q-pa-none q-mb-md">
      <div class="q-mb-md">
        <div class="text-bold">{{ taxonomiesStore.getLabel(annotation.taxonomy.title, locale) }}</div>
        <div :style="maxWidth ? `max-width: ${maxWidth}` : ''">
          <q-markdown
            :src="taxonomiesStore.getLabel(annotation.taxonomy.description, locale)"
            no-heading-anchor-links
          />
        </div>
      </div>
      <div>
        <div class="text-bold">{{ taxonomiesStore.getLabel(annotation.vocabulary.title, locale) }}</div>
        <div :style="maxWidth ? `max-width: ${maxWidth}` : ''">
          <q-markdown
            :src="taxonomiesStore.getLabel(annotation.vocabulary.description, locale)"
            no-heading-anchor-links
          />
        </div>
      </div>
    </q-card-section>
    <q-card-section v-if="annotation.term" class="q-pa-none q-mb-sm">
      <div class="text-bold">{{ taxonomiesStore.getLabel(annotation.term.title, locale) }}</div>
      <div v-if="!header || showDetails" :style="maxWidth ? `max-width: ${maxWidth}` : ''">
        <q-markdown :src="taxonomiesStore.getLabel(annotation.term.description, locale)" no-heading-anchor-links />
      </div>
    </q-card-section>
    <q-card-section v-else-if="annotation.attributes" class="q-pa-none q-mb-sm">
      <attributes-bundle-panel :bundle="annotation" />
    </q-card-section>
  </q-card>
</template>

<script setup lang="ts">
import type { Annotation } from 'src/components/models';
import AttributesBundlePanel from 'src/components/datasource/AttributesBundlePanel.vue';

const taxonomiesStore = useTaxonomiesStore();
const { t, locale } = useI18n({ useScope: 'global' });

interface Props {
  annotation: Annotation;
  maxWidth?: string;
  header?: boolean;
}

defineProps<Props>();

const showDetails = ref(false);
</script>
