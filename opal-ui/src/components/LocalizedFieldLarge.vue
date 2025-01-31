<template>
  <div>
    <slot name="header"></slot>
    <span
      v-if="!$slots.header && title"
      class="text-body2"
      :class="{ 'text-primary': dirty, 'text-secondary': !dirty }"
      >{{ title }}</span
    >
    <q-card flat>
      <q-tabs inline-label v-model="tab" dense class="text-grey" active-color="primary" active-bg-color="grey-1">
        <q-tab v-for="label in labels" :key="label.locale" :name="label.locale" :label="label.locale">
          <template v-slot:default>
            <q-icon @click.prevent="onDeleteLocale(label)" name="close" color="red" class="q-pl-xs" />
          </template>
        </q-tab>
      </q-tabs>
      <q-separator></q-separator>
      <q-tab-panels v-model="tab" animated>
        <q-tab-panel v-for="label in labels" :key="label.locale" :name="label.locale">
          <q-input
            v-model="label.text"
            ref="input"
            :hint="hint"
            dense
            :disable="canAddLocale"
            type="textarea"
            lazy-rules
            :rules="[validateRequiredField]"
          />
        </q-tab-panel>
      </q-tab-panels>
      <q-btn
        v-if="canAddLocale"
        class="q-my-md"
        size="sm"
        color="primary"
        icon="add"
        :label="labels.length ? '' : t('add')"
        @click="onAddLocale"
      />
    </q-card>
  </div>
</template>

<script setup lang="ts">
import type { LocaleTextDto } from 'src/models/Opal';
const { t } = useI18n();

interface Props {
  modelValue: LocaleTextDto[] | undefined;
  title?: string;
  hint?: string;
}

const emit = defineEmits(['update:modelValue']);
const props = defineProps<Props>();
const systemStore = useSystemStore();
const locales = [...systemStore.generalConf.languages];
const tab = ref(props.modelValue?.[0]?.locale || locales[0]);
const dirty = ref(false);
const canAddLocale = computed(() => getMissingLocales().length > 0);
const labels = computed({
  get: () => props.modelValue || [],
  set: (value: LocaleTextDto[]) => {
    emit('update:modelValue', value);
  },
});

function getMissingLocales() {
  const currentLocales = labels.value.map((label: LocaleTextDto) => label.locale);
  return locales.filter((locale: string) => !currentLocales.includes(locale));
}

const validateRequiredField = (val: string) => (val && val.trim().length > 0) || t('validation.text_required');

// Handlers

function onAddLocale() {
  const missingLocales = getMissingLocales();

  if (missingLocales.length > 0) {
    labels.value = labels.value.concat({ locale: missingLocales[0] || 'en', text: '' });
    tab.value = missingLocales[0];
  }
}

function onDeleteLocale(label: LocaleTextDto) {
  labels.value = labels.value.filter((l: LocaleTextDto) => l.locale !== label.locale);
}

watch(
  labels,
  () => {
    dirty.value = labels.value.some((l) => l.text || l.locale);
  },
  { deep: true }
);
</script>
