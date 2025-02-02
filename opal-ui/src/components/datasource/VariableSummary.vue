<template>
  <div class="row q-gutter-md">
    <q-input
      v-model="limit"
      flat
      dense
      :label="t('limit')"
      type="number"
      min="0"
      :max="total"
      :step="STEP_COUNT"
      debounce="500"
      @update:model-value="init"
      style="width: 200px"
    >
      <template v-slot:after>
        <span class="on-right text-body1">/ {{ total }}</span>
      </template>
    </q-input>
    <q-btn
      color="secondary"
      icon="refresh"
      :title="t('refresh')"
      outline
      size="sm"
      @click="init"
      class="q-mt-lg"
      style="height: 2.5em"
      :disable="loading"
    />
    <q-btn
      color="primary"
      icon="analytics"
      :label="t('full_summary')"
      size="sm"
      @click="onFullSummary"
      class="q-mt-lg"
      style="height: 2.5em"
      :disable="loading"
    />
  </div>
  <q-separator class="q-mt-md q-mb-md" />
  <div v-if="loading">
    <q-spinner-dots size="lg" class="q-mt-md" />
  </div>
  <div v-else-if="categoricalData">
    <categorical-summary-chart :variable="variable" :data="categoricalData" class="q-mt-md" />
  </div>
  <div v-else-if="textData">
    <categorical-summary-chart :variable="variable" :data="textData" class="q-mt-md" />
  </div>
  <div v-else-if="continuousData">
    <continuous-summary-chart :variable="variable" :data="continuousData" class="q-mt-md" />
  </div>
  <div v-else-if="defaultData">
    <default-summary-chart :variable="variable" :data="defaultData" class="q-mt-md" />
  </div>
  <div v-else-if="total > 0">
    <pre>{{ summary }}</pre>
  </div>
</template>

<script setup lang="ts">
import type { VariableDto } from 'src/models/Magma';
import DefaultSummaryChart from 'src/components/charts/DefaultSummaryChart.vue';
import CategoricalSummaryChart from 'src/components/charts/CategoricalSummaryChart.vue';
import ContinuousSummaryChart from 'src/components/charts/ContinuousSummaryChart.vue';

const { t } = useI18n();
const route = useRoute();
const datasourceStore = useDatasourceStore();

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const categoricalData = computed(() => (summary.value as any)['Math.CategoricalSummaryDto.categorical'])
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const textData = computed(() => (summary.value as any)['Math.TextSummaryDto.textSummary'])
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const continuousData = computed(() => (summary.value as any)['Math.ContinuousSummaryDto.continuous'])
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const defaultData = computed(() => (summary.value as any)['Math.DefaultSummaryDto.defaultSummary'])

interface VariableSummaryProps {
  variable: VariableDto;
  total: number;
}

const props = defineProps<VariableSummaryProps>();

const STEP_COUNT = 1000;

const summary = ref({});
const limit = ref<number | undefined>(STEP_COUNT);
const loading = ref(false);

const dsName = computed(() => route.params.id as string);
const tName = computed(() => route.params.tid as string);

onMounted(() => {
  init();
});

watch([dsName, tName, () => props.variable], () => {
  init();
});

function init() {
  const fullIfCached = !limit.value || limit.value >= props.total;

  summary.value = {};
  if (!props.variable || !props.variable.name) {
    return;
  }
  loading.value = true;
  datasourceStore
    .loadVariableSummary(props.variable, fullIfCached, (limit.value && limit.value > props.total) ? props.total : limit.value)
    .then((data) => {
      summary.value = data;
    })
    .finally(() => {
      loading.value = false;
    });
}

function onFullSummary() {
  limit.value = undefined;
  init();
}
</script>
