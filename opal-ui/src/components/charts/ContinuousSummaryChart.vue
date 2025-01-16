<template>
  <div>
    <div>
      <q-btn-toggle
        v-model="valueType"
        size="sm"
        toggle-color="secondary"
        :options="[
          { label: t('frequency'), value: 'freq' },
          { label: t('density'), value: 'density' },
        ]"
      />
      <q-badge color="positive" :label="`N:  ${data.summary?.n || 0}`" class="on-right" />
    </div>
    <div class="row q-mt-md">
      <div v-if="hasIntervalFrequencies">
        <vue-plotly :data="histoChartData" :layout="histoLayout" :config="config" />
      </div>
      <div v-if="hasNormal">
        <vue-plotly :data="normalChartData" :layout="normalLayout" :config="config" />
      </div>
    </div>
    <div class="row q-col-gutter-md">
      <div class="col-md-6 col-xs-12">
        <div class="text-bold q-mb-md">{{ t('frequencies') }}</div>
        <frequencies-table
          :variable="variable"
          :nonMissingFreq="nonMissingFreq"
          :missingFreq="missingFreq"
          :totalFreq="totalFreq"
          :totalPct="totalPct"
        />
      </div>
      <div class="col-md-6 col-xs-12">
        <div class="text-bold q-mb-md">{{ t('descriptive_statistics') }}</div>
        <fields-list :items="items" :dbobject="data.summary" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { VariableDto } from 'src/models/Magma';
import type { ContinuousSummaryDto, IntervalFrequencyDto } from 'src/models/Math';
import FieldsList from 'src/components/FieldsList.vue';
import FrequenciesTable from 'src/components/datasource/FrequenciesTable.vue';
import VuePlotly from 'src/components/charts/VuePlotly.vue';

const { t } = useI18n();

interface ContinuousSummaryChartProps {
  variable: VariableDto;
  data: ContinuousSummaryDto;
}

const props = defineProps<ContinuousSummaryChartProps>();

const valueType = ref('freq');

const config = {
  displayModeBar: false,
  responsive: true,
};

const items = [
  { field: 'n', label: 'N' },
  { field: 'mean', label: t('mean') },
  { field: 'stdDev', label: t('std_dev') },
  { field: 'median', label: t('median') },
  { field: 'min', label: t('min') },
  { field: 'max', label: t('max') },
  { field: 'sum', label: t('sum') },
  { field: 'sumsq', label: t('sumsq') },
  { field: 'variance', label: t('variance') },
  { field: 'skewness', label: t('skewness') },
  { field: 'kurtosis', label: t('kurtosis') },
];

const isFreq = computed(() => valueType.value === 'freq');

const intervalFrequencies = computed(() => {
  if (!props.data?.intervalFrequency) return [];
  const freqs = props.data.intervalFrequency;
  return freqs;
});

const hasIntervalFrequencies = computed(
  () => intervalFrequencies.value.filter((f: IntervalFrequencyDto) => f.freq > 0).length > 0
);

const histoLayout = computed(() => {
  if (!props.data) return {};
  return {
    title: t('histogram'),
    xaxis: {
      title: t('intervals'),
      type: 'category',
    },
    yaxis: {
      title: isFreq.value ? t('frequency') : t('density'),
    },
    bargap: 0.01,
    margin: {
      l: 50, // left margin
      r: 80, // right margin
      t: 50, // top margin
      b: 100, // bottom margin
      pad: 4, // padding around the plot area
    },
  };
});

const histoChartData = computed(() => {
  if (!props.data) return [];
  const labels = intervalFrequencies.value.map(
    (f: IntervalFrequencyDto) => `${f.lower.toFixed(2)} - ${f.upper.toFixed(2)}`
  );
  const values = intervalFrequencies.value.map((f: IntervalFrequencyDto) => (isFreq.value ? f.freq : f.density * 100));

  return [
    {
      type: 'bar',
      x: labels,
      y: values,
    },
  ];
});

const hasNormal = computed(() => props.data?.summary && props.data?.summary.min !== props.data?.summary.max);

const normalLayout = computed(() => {
  if (!props.data) return {};
  return {
    title: t('normal_distribution'),
    xaxis: {
      title: t('theoretical_quantiles'),
    },
    yaxis: {
      title: t('sample_quantiles'),
    },
    margin: {
      l: 50, // left margin
      r: 80, // right margin
      t: 50, // top margin
      b: 100, // bottom margin
      pad: 4, // padding around the plot area
    },
  };
});

const normalChartData = computed(() => {
  if (!props.data || !props.data.summary) return [];
  const min = props.data.summary.min;
  const max = props.data.summary.max;
  return [
    {
      mode: 'lines+markers',
      x: [min, max],
      y: [min, max],
      name: t('normal'),
    },
    {
      mode: 'markers',
      x: props.data.summary.percentiles,
      y: props.data.distributionPercentiles,
      name: t('sample'),
      marker: {
        size: 8,
      },
    },
  ];
});

const nonMissingFreq = computed(() => props.data?.frequencies?.filter((f) => !f.missing));
const missingFreq = computed(() => props.data?.frequencies?.filter((f) => f.missing));
const totalFreq = computed(() => props.data?.frequencies?.reduce((acc, f) => acc + f.freq, 0));
const totalPct = computed(() => props.data?.frequencies?.reduce((acc, f) => acc + f.pct, 0));
</script>
