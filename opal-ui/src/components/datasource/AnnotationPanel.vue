<template>
  <q-card flat>
    <q-card-section v-if="header" class="q-pa-none q-mb-sm">
      <q-breadcrumbs
        separator=">"
        class="float-left on-left">
        <q-breadcrumbs-el
          :label="taxonomiesStore.getLabel(annotation.taxonomy.title, locale)"/>
        <q-breadcrumbs-el
          :label="taxonomiesStore.getLabel(annotation.vocabulary.title, locale)"/>
      </q-breadcrumbs>
      <q-btn
        :icon="showDetails ? 'expand_less' : 'expand_more'"
        flat
        dense
        color="primary"
        size="sm"
        no-caps
        :label="$t(showDetails ? 'less' : 'more')"
        @click="showDetails = !showDetails"
      />
    </q-card-section>
    <q-card-section v-if="!header || showDetails" class="q-pa-none q-mb-md">
      <div class="q-mb-md">
        <div class="text-bold">{{ taxonomiesStore.getLabel(annotation.taxonomy.title, locale) }}</div>
        <div :style="maxWidth ? `max-width: ${maxWidth}` : ''">
          <q-markdown :src="taxonomiesStore.getLabel(annotation.taxonomy.description, locale)" no-heading-anchor-links />
        </div>
      </div>
      <div>
        <div class="text-bold">{{ taxonomiesStore.getLabel(annotation.vocabulary.title, locale) }}</div>
        <div :style="maxWidth ? `max-width: ${maxWidth}` : ''">
          <q-markdown :src="taxonomiesStore.getLabel(annotation.vocabulary.description, locale)" no-heading-anchor-links />
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
      <div v-if="hasLocale">
        <q-tabs
          v-model="tab"
          dense
          class="text-grey"
          active-color="primary"
          indicator-color="primary"
          align="left"
          narrow-indicator
          no-caps
        >
          <q-tab v-for="loc in locales" :key="loc" :name="loc" :label="loc"/>
        </q-tabs>
        <q-tab-panels v-model="tab">
          <template v-for="loc in locales" :key="loc">
            <q-tab-panel :name="loc" style="padding-top: 0;">
              <q-card bordered flat>
                <q-card-section>
                  <q-markdown :src="getValue(loc)" no-heading-anchor-links />
                </q-card-section>
              </q-card>
            </q-tab-panel>
          </template>
        </q-tab-panels>
      </div>
      <div v-else>
        <q-card bordered flat>
          <q-card-section>
            <q-markdown :src="annotation.attributes[0].value" no-heading-anchor-links />
          </q-card-section>
        </q-card>
      </div>
    </q-card-section>
  </q-card>
</template>


<script lang="ts">
export default defineComponent({
  name: 'AnnotationPanel',
});
</script>
<script setup lang="ts">
import { Annotation } from 'src/components/models';
const taxonomiesStore = useTaxonomiesStore();
const { locale } = useI18n({ useScope: 'global' });

interface Props {
  annotation: Annotation;
  maxWidth?: string;
  header?: boolean;
}

const NO_LOCALE = 'default';

const props = defineProps<Props>();

const locales = computed(() => props.annotation.attributes?.map(attr => attr.locale || NO_LOCALE) || []);

const hasLocale = computed(() => locales.value.length > 1 || locales.value[0] !== NO_LOCALE);

const tab = ref(locales.value[0] || NO_LOCALE);
const showDetails = ref(false);

function getValue(locale: string) {
  return props.annotation.attributes?.find(attr => attr.locale === (locale === NO_LOCALE ? undefined : locale))?.value || '';
}
</script>
