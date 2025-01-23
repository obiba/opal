<template>
  <div>
    <div class="text-h6 q-mb-md">{{ t('contingency_table') }}</div>
    <div class="text-help q-mb-md">{{ t('contingency_table_info') }}</div>

    <div class="row">
      <q-select
        filled
        dense
        :label="t('categorical_variable')"
        :hint="t('categorical_variable_hint')"
        v-model="varCat"
        use-input
        hide-selected
        fill-input
        input-debounce="0"
        :options="varCatOptions"
        @filter="onFilterVarCat"
        @update:model-value="onClearResults"
      >
        <template v-slot:option="scope">
          <q-item v-bind="scope.itemProps">
            <q-item-section class="text-caption">
              <span>{{ scope.opt.label }}</span>
              <div v-for="(attr, idx) in getLabels(scope.opt.variable.attributes)" :key="idx" class="text-hint">
                <q-badge v-if="attr.locale" color="grey-3" :label="attr.locale" class="q-mr-xs text-grey-6" />
                <span>{{ attr.value }}</span>
              </div>
            </q-item-section>
          </q-item>
        </template>
        <template v-slot:no-option>
          <q-item>
            <q-item-section class="text-grey">
              {{ t('no_options') }}
            </q-item-section>
          </q-item>
        </template>
      </q-select>
      <q-icon name="close" size="sm" class="q-mt-sm on-right on-left" />
      <q-select
        filled
        dense
        :label="t('other_variable')"
        :hint="t('other_variable_hint')"
        v-model="varAlt"
        use-input
        hide-selected
        fill-input
        input-debounce="0"
        :options="varAltOptions"
        @filter="onFilterVarAlt"
        @update:model-value="onClearResults"
      >
        <template v-slot:option="scope">
          <q-item v-bind="scope.itemProps">
            <q-item-section class="text-caption">
              <span>{{ scope.opt.label }}</span>
              <div v-for="(attr, idx) in getLabels(scope.opt.variable.attributes)" :key="idx" class="text-hint">
                <q-badge v-if="attr.locale" color="grey-3" :label="attr.locale" class="q-mr-xs text-grey-6" />
                <span>{{ attr.value }}</span>
              </div>
            </q-item-section>
          </q-item>
        </template>
        <template v-slot:no-option>
          <q-item>
            <q-item-section class="text-grey">
              {{ t('no_options') }}
            </q-item-section>
          </q-item>
        </template>
      </q-select>
      <q-btn
        color="primary"
        :label="t('submit')"
        size="sm"
        style="height: 2.5em"
        @click="onSubmit"
        :disable="!varCat || !varAlt || loading || variableCatCategories.length === 0"
        class="q-mt-sm on-right"
      />
      <q-btn
        outline
        color="secondary"
        icon="cleaning_services"
        :label="t('clear')"
        size="sm"
        style="height: 2.5em"
        @click="onClear"
        class="q-mt-sm on-right"
      />
    </div>

    <div v-if="loading">
      <q-spinner-dots size="lg" class="q-mt-md" />
    </div>
    <div v-else-if="contingency">
      <q-markup-table flat bordered class="q-mt-lg">
        <thead>
          <tr class="bg-grey-3">
            <th rowspan="2">
              <div class="text-bold">{{ variableAlt?.name }}</div>
              <div v-for="(attr, idx) in getLabels(variableAlt?.attributes)" :key="idx" class="text-hint">
                <q-badge v-if="attr.locale" color="grey-3" :label="attr.locale" class="q-mr-xs text-grey-6" />
                <span>{{ attr.value }}</span>
              </div>
            </th>
            <th :colspan="(variableCatCategories.length || 0) + 1">
              <div class="text-bold">{{ variableCat?.name }}</div>
              <div v-for="(attr, idx) in getLabels(variableCat?.attributes)" :key="idx" class="text-hint">
                <q-badge v-if="attr.locale" color="grey-3" :label="attr.locale" class="q-mr-xs text-grey-6" />
                <span>{{ attr.value }}</span>
              </div>
            </th>
          </tr>
          <tr>
            <th v-for="cat in variableCatCategories" :key="cat.name" class="bg-grey-1">
              <div class="text-bold">{{ cat.name }}</div>
              <div v-for="(attr, idx) in getLabels(cat.attributes)" :key="idx" class="text-hint">
                <q-badge v-if="attr.locale" color="grey-3" :label="attr.locale" class="q-mr-xs text-grey-6" />
                <span>{{ attr.value }}</span>
              </div>
            </th>
            <th>{{ t('total') }}</th>
          </tr>
        </thead>
        <tbody v-if="withFrequencies">
          <tr v-for="catAlt in variableAltCategories" :key="catAlt.name">
            <td class="bg-grey-2">
              <div class="text-bold">{{ catAlt.name }}</div>
              <div v-for="(attr, idx) in getLabels(catAlt.attributes)" :key="idx" class="text-hint">
                <q-badge v-if="attr.locale" color="grey-3" :label="attr.locale" class="q-mr-xs text-grey-6" />
                <span>{{ attr.value }}</span>
              </div>
            </td>
            <td v-for="cat in variableCatCategories" :key="cat.name" class="text-caption">
              {{ getFrequency(cat.name, catAlt.name) }}
            </td>
            <td class="text-caption">
              {{ getFrequency('_total', catAlt.name) }}
            </td>
          </tr>
          <tr>
            <td class="text-bold">{{ t('total') }}</td>
            <td v-for="cat in variableCatCategories" :key="cat.name" class="text-caption">
              {{ getFrequency(cat.name, undefined) }}
            </td>
            <td class="text-caption">
              {{ getFrequency('_total', undefined) }}
            </td>
          </tr>
        </tbody>
        <tbody v-if="withStatistics">
          <tr v-for="measure in measures" :key="measure">
            <td class="bg-grey-2">
              <div class="text-bold">{{ t(`stats.${measure}`) }}</div>
            </td>
            <td v-for="cat in variableCatCategories" :key="cat.name" class="text-caption">
              {{ getStatistic(cat.name, measure) }}
            </td>
            <td class="text-caption">
              {{ getStatistic('_total', measure) }}
            </td>
          </tr>
        </tbody>
      </q-markup-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getVariableNature, VariableNatures } from 'src/utils/magma';
import { getLabels } from 'src/utils/attributes';
import type { VariableDto } from 'src/models/Magma';
import type { QueryResultDto } from 'src/models/Search';
import { notifyError } from 'src/utils/notify';

const { t } = useI18n();
const route = useRoute();
const datasourceStore = useDatasourceStore();

interface VariableOption {
  label: string;
  value: string;
  variable: VariableDto;
}

const varCat = ref<VariableOption>();
const varCatOptions = ref<VariableOption[]>([]);
const varAlt = ref<VariableOption>();
const varAltOptions = ref<VariableOption[]>([]);
const loading = ref(false);
const contingency = ref<QueryResultDto | null>(null);

const measures = ['min', 'max', 'mean', 'stdDeviation', 'total', 'variance', 'count'];
const booleanCategories = [
  { name: 'true', attributes: [] },
  { name: 'false', attributes: [] },
];

const dsName = computed(() => route.params.id as string);
const tName = computed(() => route.params.tid as string);

const allVarCatOptions = computed(() =>
  datasourceStore.variables
    .filter((v) => getVariableNature(v) === VariableNatures.CATEGORICAL)
    .map((v) => ({ label: v.name, value: v.name, variable: v }))
);
const allVarAltOptions = computed(() =>
  datasourceStore.variables
    .filter(
      (v) => getVariableNature(v) === VariableNatures.CATEGORICAL || getVariableNature(v) === VariableNatures.CONTINUOUS
    )
    .map((v) => ({ label: v.name, value: v.name, variable: v }))
);

const variableCat = computed(() => varCat.value?.variable);
const variableCatCategories = computed(
  () => (variableCat.value?.valueType === 'boolean' ? booleanCategories : variableCat.value?.categories) || []
);
const variableAlt = computed(() => varAlt.value?.variable);
const variableAltCategories = computed(
  () => (variableAlt.value?.valueType === 'boolean' ? booleanCategories : variableAlt.value?.categories) || []
);
const withFrequencies = computed(() => varAlt.value ? getVariableNature(varAlt.value.variable) === VariableNatures.CATEGORICAL : contingency.value?.facets.some((f) => f.frequencies !== undefined));
const withStatistics = computed(() => contingency.value?.facets.some((f) => f.statistics !== undefined));

watch([dsName, tName], () => {
  onClear();
  loading.value = true;
  datasourceStore
    .initDatasourceTableVariables(dsName.value, tName.value)
    .catch((err) => {
      notifyError(err);
    })
    .finally(() => {
      varCatOptions.value = { ...allVarCatOptions.value };
      varAltOptions.value = { ...allVarAltOptions.value };
      loading.value = false;
    });
});

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function onFilterVarCat(val: string, update: any) {
  update(() => {
    const needle = val.toLowerCase().trim();
    varCatOptions.value =
      needle === ''
        ? [...allVarCatOptions.value]
        : allVarCatOptions.value.filter((v) => v.value.toLowerCase().indexOf(needle) > -1);
  });
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function onFilterVarAlt(val: string, update: any) {
  update(() => {
    const needle = val.toLowerCase().trim();
    varAltOptions.value =
      needle === ''
        ? [...allVarAltOptions.value]
        : allVarAltOptions.value.filter((v) => v.value.toLowerCase().indexOf(needle) > -1);
  });
}

function onSubmit() {
  if (!varCat.value?.value || !varAlt.value?.value) return;
  loading.value = true;
  datasourceStore
    .getContingencyTable(varCat.value?.value, varAlt.value?.value)
    .then((res) => {
      contingency.value = res;
    })
    .catch((err) => {
      notifyError(err);
    })
    .finally(() => {
      loading.value = false;
    });
}

function onClear() {
  varCat.value = undefined;
  varAlt.value = undefined;
  onClearResults();
}

function onClearResults() {
  loading.value = false;
  contingency.value = null;
}

function getFacet(cat: string) {
  return contingency.value?.facets.find((f) => f.facet === cat);
}

function getFrequency(cat: string, catAlt: string | undefined) {
  if (!contingency.value) return 0;
  const facet = getFacet(cat);
  if (!facet) return 0;
  if (catAlt === undefined) return facet.frequencies?.reduce((acc, f) => acc + f.count, 0) || 0;
  return facet.frequencies?.find((f) => f.term === catAlt)?.count || 0;
}

function getStatistic(cat: string, measure: string) {
  if (!contingency.value) return '-';
  const facet = getFacet(cat);
  if (!facet || !facet.statistics) return '-';
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  return (facet.statistics as any)[measure] === undefined ? '-' : (facet.statistics as any)[measure];
}
</script>
