<template>
  <div>
    <div>
      <q-btn-toggle
        v-model="valueType"
        size="sm"
        toggle-color="secondary"
        :options="[
          {label: $t('frequency'), value: 'freq'},
          {label: $t('density'), value: 'density'},
        ]"
      />
      <q-badge color="positive" :label="`N:  ${data.summary.n}`"
        class="on-right"/>
    </div>
    <div class="row">
      <div class="col-6">
        <div v-if="hasIntervalFrequencies" class="q-mt-md">
          <vue-plotly :data="chartData" :layout="layout" :config="config"/>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col-6">
        <div class="text-bold">{{ $t('descriptive_statistics') }}</div>
        <fields-list
            :items="items"
            :dbobject="data.summary"
          />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  components: { VuePlotly },
  name: 'ContinuousSummaryChart',
});
</script>
<script setup lang="ts">
import { ContinuousSummary, IntervalFrequency } from 'src/components/models';
import FieldsList from 'src/components/FieldsList.vue';
import VuePlotly from 'src/components/charts/VuePlotly.vue';

const { t } = useI18n();

interface ContinuousSummaryChartProps {
  data: ContinuousSummary;
}

const props = defineProps<ContinuousSummaryChartProps>();

const valueType = ref('freq');

const layout = computed(() => {
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
      pad: 4 // padding around the plot area
    }
  }
});

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

const hasIntervalFrequencies = computed(() => intervalFrequencies.value.filter((f: IntervalFrequency) => f.freq>0).length > 0);

const chartData = computed(() => {
  if (!props.data) return [];
  const labels = intervalFrequencies.value.map((f: IntervalFrequency) => `${f.lower.toFixed(2)} - ${f.upper.toFixed(2)}`);
  const values = intervalFrequencies.value.map((f: IntervalFrequency) => isFreq.value ? f.freq : f.density * 100);

  return [
    {
      type: 'bar',
      x: labels,
      y: values,
    },
  ];
});
</script>
