<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('attributes') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-input
          v-model="namespace"
          :label="t('namespace')"
          :hint="t('attribute_namespace_hint')"
          dense
          class="q-mb-md"
        />
        <q-input v-model="name" :label="t('name')" :hint="t('attribute_name_hint')" dense class="q-mb-md" />
        <div>
          <div>{{ t('value') }}</div>
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
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('apply')" color="primary" @click="onApply" :disable="name === ''" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { TableDto, VariableDto, AttributeDto } from 'src/models/Magma';
import type { AttributesBundle } from 'src/components/models';

interface DialogProps {
  modelValue: boolean;
  table: TableDto;
  variable: VariableDto;
  bundle?: AttributesBundle | undefined;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const NO_LOCALE = 'default';

const { t } = useI18n();
const datasourceStore = useDatasourceStore();
const systemStore = useSystemStore();

const namespace = ref('');
const name = ref('');
const texts = ref<{ [key: string]: string | undefined }>({});
const previews = ref<{ [key: string]: boolean | undefined }>({});
const tab = ref(NO_LOCALE);

const locales = computed(() => {
  const availableLocales = [...systemStore.generalConf.languages];
  if (props.bundle && props.bundle.attributes) {
    props.bundle.attributes.forEach((attr) => {
      if (attr.locale && !availableLocales.includes(attr.locale)) {
        availableLocales.push(attr.locale);
      }
    });
  }
  return [NO_LOCALE, ...availableLocales];
});

const showDialog = ref(props.modelValue);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      if (props.bundle && props.bundle.attributes.length > 0) {
        namespace.value = props.bundle.attributes[0]?.namespace || '';
        name.value = props.bundle.attributes[0]?.name || '';
        texts.value = {};
        previews.value = {};
        props.bundle.attributes.forEach((attr) => {
          texts.value[attr.locale || NO_LOCALE] = attr.value;
        });
      } else {
        namespace.value = '';
        name.value = '';
        texts.value = {};
        previews.value = {};
      }
      tab.value = NO_LOCALE;
    }
    showDialog.value = value;
  }
);

function onHide() {
  emit('update:modelValue', false);
}

async function onApply() {
  if (props.bundle && props.bundle.attributes.length > 0 && props.bundle.attributes[0]?.name) {
    // provided are to be modified
    await datasourceStore.deleteAttributes(
      props.variable,
      props.bundle.attributes[0]?.namespace,
      props.bundle.attributes[0].name
    );
  }
  // any existing is to be updated
  await datasourceStore.deleteAttributes(
    props.variable,
    namespace.value === '' ? undefined : namespace.value,
    name.value
  );
  const attributes: AttributeDto[] = [];
  for (const [locale, value] of Object.entries(texts.value)) {
    if (value) {
      attributes.push({
        namespace: namespace.value === '' ? undefined : namespace.value,
        name: name.value,
        locale: locale === NO_LOCALE ? undefined : locale,
        value,
      });
    }
  }
  if (attributes.length > 0) {
    await datasourceStore.applyAttributes(props.variable, attributes);
    datasourceStore.loadTableVariables();
  }
  onHide();
}

function onMarkdownGuide() {
  window.open('https://www.markdownguide.org/basic-syntax/', '_blank');
}
</script>
