<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t(isDeleteOperation ? 'delete_annotation' : 'apply_annotation') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <div class="q-mb-md box-info">
          <q-icon name="info" size="1.2rem" />
          <span class="on-right">
            {{ t('annotate_info', { count: props.variables.length }) }}
          </span>
        </div>

        <q-select
          v-model="taxonomyName"
          :options="taxonomiesOptions"
          :label="t('taxonomy.title')"
          dense
          emit-value
          map-options
          class="q-mb-sm"
          @update:model-value="onTaxonomyChange"
        />
        <div class="text-hint q-mb-md">{{ taxonomyHint }}</div>

        <q-select
          v-model="vocabularyName"
          :options="vocabulariesOptions"
          :label="t('vocabulary')"
          dense
          emit-value
          map-options
          class="q-mb-sm"
          @update:model-value="onVocabularyChange"
        />
        <div class="text-hint q-mb-md">{{ vocabularyHint }}</div>

        <div v-if="!isDeleteOperation">
          <div v-if="termsOptions.length > 0">
            <q-select
              v-model="termName"
              :options="termsOptions"
              :label="t('term')"
              dense
              emit-value
              map-options
              class="q-mb-sm"
            />
            <div class="text-hint q-mb-md">{{ termHint }}</div>
          </div>
          <div v-else>
            <q-tabs
              v-model="tab"
              dense
              class="text-grey"
              active-color="primary"
              indicator-color="primary"
              align="left"
              no-caps
            >
              <q-tab v-for="loc in locales" :key="loc" :name="loc" :label="loc" />
            </q-tabs>
            <q-separator />
            <q-tab-panels v-model="tab">
              <template v-for="loc in locales" :key="loc">
                <q-tab-panel v-if="tab === loc" :name="loc">
                  <q-input
                    v-if="!previews[loc]"
                    v-model="texts[loc]"
                    :label="t('text')"
                    type="textarea"
                    auto-grow
                    dense
                  />
                  <q-card v-if="previews[loc]" bordered flat>
                    <q-card-section>
                      <q-markdown :src="texts[loc]" no-heading-anchor-links />
                    </q-card-section>
                  </q-card>
                  <div class="q-mt-sm">
                    <q-btn
                      v-if="!previews[loc]"
                      flat
                      no-caps
                      size="sm"
                      :label="t('preview')"
                      color="secondary"
                      class="q-pl-none q-pr-none"
                      @click="previews[loc] = true"
                    />
                    <q-btn
                      v-else
                      flat
                      no-caps
                      size="sm"
                      :label="t('edit')"
                      color="secondary"
                      class="q-pl-none q-pr-none"
                      @click="previews[loc] = false"
                    />
                    <q-btn
                      flat
                      no-caps
                      size="sm"
                      icon="help_outline"
                      :label="t('markdown_guide')"
                      color="secondary"
                      class="float-right q-pl-none q-pr-none"
                      @click="onMarkdownGuide"
                    />
                  </div>
                </q-tab-panel>
              </template>
            </q-tab-panels>
            <div class="text-hint">
              {{ t('annotation_texts_hint') }}
            </div>
          </div>
        </div>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn
          flat
          :label="t(isDeleteOperation ? 'delete' : 'apply')"
          color="primary"
          @click="onSubmit"
          v-close-popup
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { TableDto, VariableDto } from 'src/models/Magma';
import type { Annotation } from 'src/components/models';
import type { TaxonomyDto, TermDto, VocabularyDto } from 'src/models/Opal';

interface DialogProps {
  modelValue: boolean;
  table?: TableDto;
  variables: VariableDto[];
  annotation?: Annotation | undefined;
  operation?: string;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const NO_LOCALE = 'default';

const taxonomiesStore = useTaxonomiesStore();
const datasourceStore = useDatasourceStore();
const systemStore = useSystemStore();
const { t, locale } = useI18n({ useScope: 'global' });

const taxonomyName = ref<string>('');
const vocabularyName = ref<string>('');
const termName = ref<string>('');
const texts = ref<{ [key: string]: string | undefined }>({});
const previews = ref<{ [key: string]: boolean | undefined }>({});
const tab = ref(NO_LOCALE);

const isDeleteOperation = computed(() => props.operation === 'delete');

const locales = computed(() => {
  const availableLocales = [...systemStore.generalConf.languages];
  if (props.annotation) {
    props.annotation.attributes.forEach((attr) => {
      if (attr.locale && !availableLocales.includes(attr.locale)) {
        availableLocales.push(attr.locale);
      }
    });
  }
  return [NO_LOCALE, ...availableLocales];
});

const taxonomiesOptions = computed(() => {
  return taxonomiesStore.taxonomies
    ? taxonomiesStore.taxonomies.map((item: TaxonomyDto) => {
        return {
          label: item.title ? taxonomiesStore.getLabel(item.title, locale.value) : item.name,
          value: item.name,
        };
      })
    : [];
});

const taxonomy = computed(() => {
  return taxonomiesStore.taxonomies.find((taxo: TaxonomyDto) => taxo.name === taxonomyName.value);
});

const taxonomyHint = computed(() => {
  return taxonomy.value?.description ? taxonomiesStore.getLabel(taxonomy.value.description, locale.value) : '';
});

const vocabulariesOptions = computed(() => {
  if (!taxonomy.value) {
    return [];
  }
  return taxonomy.value.vocabularies
    ? taxonomy.value.vocabularies.map((item: VocabularyDto) => {
        return {
          label: item.title ? taxonomiesStore.getLabel(item.title, locale.value) : item.name,
          value: item.name,
        };
      })
    : [];
});

const vocabulary = computed(() => {
  return taxonomiesStore.taxonomies
    .find((taxo: TaxonomyDto) => taxo.name === taxonomyName.value)
    ?.vocabularies.find((voc: VocabularyDto) => voc.name === vocabularyName.value);
});

const vocabularyHint = computed(() => {
  return vocabulary.value?.description ? taxonomiesStore.getLabel(vocabulary.value.description, locale.value) : '';
});

const termsOptions = computed(() => {
  if (!vocabulary.value) {
    return [];
  }
  return vocabulary.value.terms
    ? vocabulary.value.terms.map((item: TermDto) => {
        return {
          label: item.title ? taxonomiesStore.getLabel(item.title, locale.value) : item.name,
          value: item.name,
        };
      })
    : [];
});

const term = computed(() => {
  return taxonomiesStore.taxonomies
    .find((taxo: TaxonomyDto) => taxo.name === taxonomyName.value)
    ?.vocabularies.find((voc: VocabularyDto) => voc.name === vocabularyName.value)
    ?.terms?.find((trm: TermDto) => trm.name === termName.value);
});

const termHint = computed(() => {
  return term.value?.description ? taxonomiesStore.getLabel(term.value.description, locale.value) : '';
});

const showDialog = ref(props.modelValue);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      taxonomiesStore.init().then(() => {
        if (props.annotation) {
          taxonomyName.value = props.annotation.taxonomy.name;
          onTaxonomyChange();
          vocabularyName.value = props.annotation.vocabulary.name;
          onVocabularyChange();
          if (props.annotation.term) {
            termName.value = props.annotation.term.name;
          } else {
            texts.value = {};
            previews.value = {};
            props.annotation.attributes.forEach((attr) => {
              texts.value[attr.locale || NO_LOCALE] = attr.value;
            });
          }
        } else {
          taxonomyName.value = taxonomiesStore.taxonomies ? taxonomiesStore.taxonomies[0]?.name || '' : '';
          onTaxonomyChange();
        }
        tab.value = NO_LOCALE;
      });
    }
    showDialog.value = value;
  }
);

function onTaxonomyChange() {
  if (taxonomy.value) {
    vocabularyName.value = taxonomy.value.vocabularies ? taxonomy.value.vocabularies[0]?.name || '' : '';
  } else {
    vocabularyName.value = '';
  }
  onVocabularyChange();
}

function onVocabularyChange() {
  if (vocabulary.value) {
    termName.value = vocabulary.value.terms ? vocabulary.value.terms[0]?.name || '' : '';
  } else {
    termName.value = '';
  }
  texts.value = {};
}

function onHide() {
  emit('update:modelValue', false);
}

async function onSubmit() {
  if (props.annotation) {
    // case a specific annotation is to be replaced
    await datasourceStore.deleteAnnotation(
      props.variables,
      props.annotation.taxonomy.name,
      props.annotation.vocabulary.name
    );
  }
  if (isDeleteOperation.value) {
    await datasourceStore.deleteAnnotation(props.variables, taxonomyName.value, vocabularyName.value);
  } else {
    await datasourceStore.annotate(
      props.variables,
      taxonomyName.value,
      vocabularyName.value,
      termsOptions.value.length ? termName.value : texts.value
    );
  }
  if (props.table) datasourceStore.loadTableVariables();
}

function onMarkdownGuide() {
  window.open('https://www.markdownguide.org/basic-syntax/', '_blank');
}
</script>
