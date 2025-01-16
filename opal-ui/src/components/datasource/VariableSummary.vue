<template>
  <div class="row q-gutter-md">
    <q-input
      v-model="limit"
      flat
      dense
      size="sm"
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
  <div v-else-if="summary['Math.CategoricalSummaryDto.categorical']">
    <categorical-summary-chart :variable="variable" :data="summary['Math.CategoricalSummaryDto.categorical']" class="q-mt-md" />
  </div>
  <div v-else-if="summary['Math.TextSummaryDto.textSummary']">
    <categorical-summary-chart :variable="variable" :data="summary['Math.TextSummaryDto.textSummary']" class="q-mt-md" />
  </div>
  <div v-else-if="summary['Math.ContinuousSummaryDto.continuous']">
    <continuous-summary-chart :variable="variable" :data="summary['Math.ContinuousSummaryDto.continuous']" class="q-mt-md" />
  </div>
  <div v-else-if="summary['Math.DefaultSummaryDto.defaultSummary']">
    <default-summary-chart :variable="variable" :data="summary['Math.DefaultSummaryDto.defaultSummary']" class="q-mt-md" />
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

interface VariableSummaryProps {
  variable: VariableDto;
  total: number;
}

const props = defineProps<VariableSummaryProps>();

const STEP_COUNT = 1000;

const summary = ref({});
const limit = ref(STEP_COUNT);
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
    .loadVariableSummary(props.variable, fullIfCached, limit.value > props.total ? props.total : limit.value)
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
