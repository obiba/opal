<template>
  <div>
    <q-input
      v-model="url"
      :label="t('amber.url')"
      :hint="t('amber.url_hint')"
      placeholder="https://"
      dense
      class="q-mb-md"
      :debounce="500"
      @update:model-value="onUpdate"
    />
    <q-input
      v-model="token"
      :label="t('amber.api_key')"
      :hint="t('amber.api_key_hint')"
      autocomplete="new-password"
      type="password"
      dense
      class="q-mb-md"
      :debounce="500"
      @update:model-value="onUpdate"
    />
    <q-option-group
      v-model="source"
      :options="sourceOptions"
      inline
      dense
      class="q-mb-md"
      @update:model-value="onUpdate"
    />
    <q-input
      v-model="study"
      :label="t('amber.study')"
      dense
      class="q-mb-md"
      :debounce="500"
      @update:model-value="onUpdate"
    />
    <q-input
      v-model="form"
      :label="t(source === 'interview' ? 'amber.interview_design' : 'amber.case_report_form')"
      dense
      class="q-mb-md"
      :debounce="500"
      @update:model-value="onUpdate"
    />
    <q-input
      v-if="source === 'interview'"
      v-model="campaign"
      :label="t('amber.campaign')"
      :hint="t('amber.campaign_hint')"
      dense
      class="q-mb-md"
      :debounce="500"
      @update:model-value="onUpdate"
    />
    <q-select
      v-model="state"
      :options="stateOptions"
      :label="t('amber.state')"
      emit-value
      map-options
      clearable
      dense
      class="q-mb-md"
      @update:model-value="onUpdate"
    />
    <div class="row q-col-gutter-md q-mb-md">
      <div v-for="bound in bounds" :key="bound.key" class="col">
        <q-input
          v-model="bound.value.value"
          :label="t(bound.key)"
          :hint="t('amber.updated_hint')"
          mask="####-##-##"
          clearable
          dense
          @update:model-value="onUpdate"
        >
          <template v-slot:prepend>
            <q-icon name="event" class="cursor-pointer">
              <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                <q-date v-model="bound.value.value" mask="YYYY-MM-DD" @update:model-value="onUpdate">
                  <div class="row items-center justify-end">
                    <q-btn v-close-popup :label="t('close')" color="primary" flat />
                  </div>
                </q-date>
              </q-popup-proxy>
            </q-icon>
          </template>
        </q-input>
      </div>
    </div>
    <q-input
      v-model="entityType"
      :label="t('entity_type')"
      :hint="t('amber.entity_type_hint')"
      dense
      class="q-mb-md"
      :debounce="500"
      @update:model-value="onUpdate"
    />
  </div>
</template>

<script setup lang="ts">
import type { DatasourceFactory } from 'src/components/models';

interface ImportAmberFormProps {
  modelValue: DatasourceFactory | undefined;
}

const props = defineProps<ImportAmberFormProps>();
const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();

const params = props.modelValue?.['Magma.AmberDatasourceFactoryDto.params'];
const url = ref<string>(params?.url ?? '');
const token = ref<string>(params?.token ?? '');
const source = ref<string>(params?.source ?? 'case_report');
const study = ref<string>(params?.study ?? '');
const form = ref<string>(params?.form ?? '');
const state = ref<string | null>(params?.state ?? 'completed');
const from = ref<string | null>(params?.from ?? null);
const to = ref<string | null>(params?.to ?? null);
const entityType = ref<string>(params?.entityType ?? '');
const campaign = ref<string>(params?.campaign ?? '');

const bounds = [
  { key: 'from', value: from },
  { key: 'to', value: to },
];

const sourceOptions = [
  { label: t('amber.case_reports'), value: 'case_report' },
  { label: t('amber.interviews'), value: 'interview' },
];

const stateOptions = computed(() =>
  (source.value === 'interview' ? ['initiated', 'in_progress', 'completed'] : ['in_progress', 'completed']).map(
    (value) => ({ label: t(`amber.states.${value}`), value }),
  ),
);

function isValid() {
  return !!(url.value && token.value && study.value && form.value);
}

function onUpdate() {
  if (!isValid()) {
    emit('update:modelValue', undefined);
    return;
  }
  emit('update:modelValue', {
    'Magma.AmberDatasourceFactoryDto.params': {
      url: url.value,
      token: token.value,
      source: source.value,
      study: study.value,
      form: form.value,
      state: state.value || undefined,
      // whole days, in UTC
      from: from.value ? `${from.value}T00:00:00.000Z` : undefined,
      to: to.value ? `${to.value}T23:59:59.999Z` : undefined,
      entityType: entityType.value || undefined,
      campaign: (source.value === 'interview' && campaign.value) || undefined,
    },
  } as DatasourceFactory);
}
</script>
