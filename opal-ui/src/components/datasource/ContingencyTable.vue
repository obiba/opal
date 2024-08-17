<template>
  <div>
    <div class="text-h6 q-mb-md">{{ $t('contingency_table') }}</div>
    <div class="text-help q-mb-md">{{ $t('contingency_table_info') }}</div>

    <div class="row">
      <q-input
        v-model="varCat"
        dense
        filled
        :label="$t('categorical_variable')"
        :hint="$t('categorical_variable_hint')"
        :disable="loading"
        debounce="500"
        @update:model-value="onSearchVarCat">
        <q-menu
          v-model="showVarCatSuggestions"
          no-parent-event
          auto-close>
          <q-list style="min-width: 100px">
            <q-item clickable v-close-popup v-for="sugg in varCatSuggestions" :key="sugg.name" @click="varCat = sugg.name">
              <q-item-section class="text-caption">
                <span>{{ sugg.name }}</span>
                <div v-for="attr in getLabels(sugg.attributes)" :key="attr.locale" class="text-hint">
                  <q-badge
                    v-if="attr.locale"
                    color="grey-3"
                    :label="attr.locale"
                    class="q-mr-xs text-grey-6"
                  />
                  <span>{{ attr.value }}</span>
                </div>
              </q-item-section>
            </q-item>
          </q-list>
        </q-menu>
      </q-input>
      <q-icon name="close" size="sm" class="q-mt-sm on-right"/>
      <q-input
        v-model="varAlt"
        dense
        filled
        :label="$t('other_variable')"
        :hint="$t('other_variable_hint')"
        :disable="loading"
        debounce="500"
        @update:model-value="onSearchVarAlt"
        class="on-right">
        <q-menu
          v-model="showVarAltSuggestions"
          no-parent-event
          auto-close>
          <q-list style="min-width: 100px">
            <q-item clickable v-close-popup v-for="sugg in varAltSuggestions" :key="sugg.name" @click="varAlt = sugg.name">
              <q-item-section>
                <span>{{ sugg.name }}</span>
                <div v-for="attr in getLabels(sugg.attributes)" :key="attr.locale" class="text-hint">
                  <q-badge
                    v-if="attr.locale"
                    color="grey-3"
                    :label="attr.locale"
                    class="q-mr-xs text-grey-6"
                  />
                  <span>{{ attr.value }}</span>
                </div>
            </q-item-section>
          </q-item>
          </q-list>
        </q-menu>
      </q-input>
      <q-btn
        color="primary"
        :label="$t('submit')"
        size="sm"
        style="height: 2.5em;"
        @click="onSubmit"
        :disable="!varCat || !varAlt || loading || variableCatCategories.length === 0"
        class="q-mt-sm on-right" />
      <q-btn
        outline
        color="secondary"
        icon="cleaning_services"
        :label="$t('clear')"
        size="sm"
        style="height: 2.5em;"
        @click="onClear"
        class="q-mt-sm on-right" />
    </div>

    <div v-if="loading">
      <q-spinner-dots size="lg" class="q-mt-md" />
    </div>
    <div v-else-if="contingency">
      <q-markup-table flat bordered class="q-mt-lg">
        <thead>
          <tr class="bg-grey-3">
            <th rowspan="2">
              <div class="text-bold">{{ varAlt }}</div>
              <div v-for="attr in getLabels(variableAlt?.attributes)" :key="attr.locale" class="text-hint">
                <q-badge
                  v-if="attr.locale"
                  color="grey-3"
                  :label="attr.locale"
                  class="q-mr-xs text-grey-6"
                />
                <span>{{ attr.value }}</span>
              </div>
            </th>
            <th :colspan="(variableCatCategories.length || 0) + 1">
              <div class="text-bold">{{ varCat }}</div>
              <div v-for="attr in getLabels(variableCat?.attributes)" :key="attr.locale" class="text-hint">
                <q-badge
                  v-if="attr.locale"
                  color="grey-3"
                  :label="attr.locale"
                  class="q-mr-xs text-grey-6"
                />
                <span>{{ attr.value }}</span>
              </div>
            </th>
          </tr>
          <tr>
            <th v-for="cat in variableCatCategories" :key="cat.name" class="bg-grey-2">
              <div class="text-bold">{{ cat.name }}</div>
              <div v-for="attr in getLabels(cat.attributes)" :key="attr.locale" class="text-hint">
                <q-badge
                  v-if="attr.locale"
                  color="grey-3"
                  :label="attr.locale"
                  class="q-mr-xs text-grey-6"
                />
                <span>{{ attr.value }}</span>
              </div>
            </th>
            <th>{{ $t('total') }}</th>
          </tr>
        </thead>
        <tbody v-if="withFrequencies">
          <tr v-for="catAlt in variableAltCategories" :key="catAlt.name">
            <td class="bg-grey-2">
              <div class="text-bold">{{ catAlt.name }}</div>
              <div v-for="attr in getLabels(catAlt.attributes)" :key="attr.locale" class="text-hint">
                <q-badge
                  v-if="attr.locale"
                  color="grey-3"
                  :label="attr.locale"
                  class="q-mr-xs text-grey-6"
                />
                <span>{{ attr.value }}</span>
              </div>
            </td>
            <td v-for="cat in variableCatCategories" :key="cat.name" class="text-caption">
              {{ getFrequency(cat.name, catAlt.name) }}
            </td>
            <td class="text-caption">
              {{ getFrequency("_total", catAlt.name) }}
            </td>
          </tr>
          <tr>
            <td class="text-bold">{{ $t('total') }}</td>
            <td v-for="cat in variableCatCategories" :key="cat.name" class="text-caption">
              {{ getFrequency(cat.name, undefined) }}
            </td>
            <td class="text-caption">
              {{ getFrequency("_total", undefined) }}
            </td>
          </tr>
        </tbody>
        <tbody v-if="withStatistics">
          <tr v-for="measure in measures" :key="measure">
            <td class="bg-grey-2">
              <div class="text-bold">{{ $t(`stats.${measure}`) }}</div>
            </td>
            <td v-for="cat in variableCatCategories" :key="cat.name" class="text-caption">
              {{ getStatistic(cat.name, measure) }}
            </td>
            <td class="text-caption">
              {{ getStatistic("_total", measure) }}
            </td>
          </tr>
        </tbody>
      </q-markup-table>
    </div>

  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'ContingencyTable',
});
</script>
<script setup lang="ts">
import { valueTypesMap } from 'src/utils/magma';
import { getLabels } from 'src/utils/attributes';
import { VariableDto } from 'src/models/Magma';
import { QueryResultDto } from 'src/models/Search';
import { notifyError } from 'src/utils/notify';

const route = useRoute();
const datasourceStore = useDatasourceStore();

const varCat = ref('');
const showVarCatSuggestions = ref(false);
const varCatSuggestions = ref<VariableDto[]>([]);
const varAlt = ref('');
const showVarAltSuggestions = ref(false);
const varAltSuggestions = ref<VariableDto[]>([]);
const loading = ref(false);
const contingency = ref<QueryResultDto | null>(null);

const measures = ['min', 'max', 'mean', 'stdDeviation', 'total', 'variance', 'count'];
const booleanCategories = [ { name: 'true', attributes: [] }, { name: 'false', attributes: [] } ];
const dsName = computed(() => route.params.id as string);
const tName = computed(() => route.params.tid as string);

const variableCat = computed(() => datasourceStore.variables.find((v) => v.name === varCat.value));
const variableCatCategories = computed(() => (variableCat.value?.valueType === 'boolean' ? booleanCategories : variableCat.value?.categories) || []);
const variableAlt = computed(() => datasourceStore.variables.find((v) => v.name === varAlt.value));
const variableAltCategories = computed(() => (variableAlt.value?.valueType === 'boolean' ? booleanCategories : variableAlt.value?.categories) || []);
const withFrequencies = computed(() => contingency.value?.facets.some((f) => f.frequencies !== undefined));
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
      loading.value = false;
    });
});

function onSearchVarCat(value: string) {
  loading.value = false;
  contingency.value = null;
  if (value.length < 2) {
    varCatSuggestions.value = [];
    showVarCatSuggestions.value = false;
    return;
  }
  varCatSuggestions.value = datasourceStore.variables
    .filter((v) => v.categories?.length > 0 || v.valueType === 'boolean')
    .filter((v) => v.name.toLowerCase().includes(value.toLowerCase()));
  showVarCatSuggestions.value = varCatSuggestions.value.length > 0;
}

function onSearchVarAlt(value: string) {
  loading.value = false;
  contingency.value = null;
  if (value.length < 2) {
    varAltSuggestions.value = [];
    showVarAltSuggestions.value = false;
    return;
  }
  varAltSuggestions.value = datasourceStore.variables
    .filter((v) => v.categories?.length > 0 || v.valueType === 'boolean' || valueTypesMap.numerical.includes(v.valueType))
    .filter((v) => v.name.toLowerCase().includes(value.toLowerCase()))
    .filter((v) => v.name !== varCat.value);
  showVarAltSuggestions.value = varAltSuggestions.value.length > 0;
}

function onSubmit() {
  loading.value = true;
  datasourceStore.getContingencyTable(varCat.value, varAlt.value)
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
  varCat.value = '';
  showVarCatSuggestions.value = false;
  varAlt.value = '';
  showVarAltSuggestions.value = false;
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
  if (catAlt === undefined)
    return facet.frequencies.reduce((acc, f) => acc + f.count, 0);
  return facet.frequencies.find((f) => f.term === catAlt)?.count || 0;
}

function getStatistic(cat: string, measure: string) {
  if (!contingency.value) return '-';
  const facet = getFacet(cat);
  if (!facet || !facet.statistics) return '-';
  return facet.statistics[measure] === undefined ? '-' : facet.statistics[measure];
}
</script>
