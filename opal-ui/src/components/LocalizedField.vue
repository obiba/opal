<template>
  <div>
    <slot name="header"></slot>
    <span
      v-if="!$slots.header && title"
      class="text-subtitle1"
      :class="{ 'text-primary': dirty, 'text-secondary': !dirty }"
      >{{ title }}</span
    >

    <q-card flat class="bg-grey-2">
      <q-card-section>
        <div v-for="label in labels" :key="label.locale" class="row q-col-gutter-md q-mb-md">
          <q-input v-model="label.locale" dense type="text" :label="t('locale')" :debounce="500" style="width: 80px" />
          <q-input
            v-model="label.text"
            dense
            type="text"
            :label="t('value')"
            style="min-width: 290px"
            lazy-rules
            :rules="[validateRequiredField]"
          />
          <span class="q-mt-md">
            <q-btn flat size="sm" color="negative" icon="delete" @click="onDeleteLocale(label)" class="on-right" />
          </span>
        </div>
        <q-btn
          v-if="canAddLocale"
          size="sm"
          color="primary"
          icon="add"
          :label="labels.length ? '' : t('add')"
          @click="onAddLocale"
        />
      </q-card-section>
    </q-card>
    <span v-if="hint" class="text-caption text-secondary q-pa-none">{{ hint }}</span>
  </div>
</template>

<script setup lang="ts">
import type { LocaleTextDto } from 'src/models/Opal';
import { locales } from 'boot/i18n';

const { t } = useI18n();

interface Props {
  modelValue: LocaleTextDto[] | undefined;
  title?: string;
  hint?: string;
}

const emit = defineEmits(['update:modelValue']);
const props = defineProps<Props>();
const dirty = ref(false);
const canAddLocale = ref(true);
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
  }

  canAddLocale.value = missingLocales.length === 0;
}

function onDeleteLocale(label: LocaleTextDto) {
  labels.value = labels.value.filter((l: LocaleTextDto) => l.locale !== label.locale);
  canAddLocale.value = getMissingLocales().length === 0;
}

watch(
  labels,
  () => {
    dirty.value = labels.value.some((l) => l.text || l.locale);
  },
  { deep: true }
);
</script>
