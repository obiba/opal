<template>
  <div>
    <div v-if="hasLocale">
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
      <q-tab-panels v-model="tab">
        <template v-for="loc in locales" :key="loc">
          <q-tab-panel :name="loc" style="padding-top: 0px; padding-bottom: 0px;">
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
          <q-markdown :src="bundle?.attributes[0]?.value" no-heading-anchor-links />
        </q-card-section>
      </q-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { AttributesBundle } from 'src/components/models';

interface Props {
  bundle?: AttributesBundle | undefined;
}
const props = defineProps<Props>();

const { locale } = useI18n({ useScope: 'global' });

const NO_LOCALE = 'default';

const locales = computed(() => props.bundle?.attributes?.map((attr) => attr.locale || NO_LOCALE).sort() || []);

const hasLocale = computed(() => locales.value.length > 1 || locales.value[0] !== NO_LOCALE);

const tab = ref(locales.value[0] || NO_LOCALE);

watch(
  () => props.bundle,
  () => {
    tab.value = locales.value.includes(locale.value) ? locale.value : locales.value[0] || NO_LOCALE;
  }
);

function getValue(locale: string) {
  return (
    props.bundle?.attributes?.find((attr) => attr.locale === (locale === NO_LOCALE ? undefined : locale))?.value || ''
  );
}
</script>
