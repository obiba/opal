<template>
  <div>
    <div>
      <q-btn-toggle
        v-model="chartType"
        size="sm"
        toggle-color="primary"
        :options="[
          {value: 'pie', slot: 'pie'},
          {value: 'bar', slot: 'bar'},
        ]"
      >
        <template v-slot:bar>
          <q-icon name="bar_chart" />
        </template>
        <template v-slot:pie>
          <q-icon name="pie_chart" />
        </template>
      </q-btn-toggle>
      <q-btn-toggle
        v-model="valueType"
        size="sm"
        toggle-color="secondary"
        :options="[
          {label: $t('frequency'), value: 'freq'},
          {label: $t('percentage'), value: 'pct'},
        ]"
        class="on-right"
      />
      <q-badge color="positive" :label="`N:  ${data.n}`"
        class="on-right"/>
      <q-toggle toggle-indeterminate v-model="nonMissingsSelection" :label="missings"
        class="on-right"/>
    </div>
    <div v-if="hasFrequencies" class="q-mt-md">
      <vue-plotly :data="chartData" :layout="layout" :config="config"/>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  components: { VuePlotly },
  name: 'CategoricalSummaryChart',
});
</script>
<script setup lang="ts">
import { CategoricalSummary, Frequency } from 'src/components/models';
import VuePlotly from './VuePlotly.vue';

const { t } = useI18n();

interface FrequenciesChartProps {
  data: CategoricalSummary;
}

const props = defineProps<FrequenciesChartProps>();

const chartType = ref('pie');
const valueType = ref('freq');
const nonMissingsSelection = ref<boolean>(null);

const missings = computed(() => nonMissingsSelection.value === null ? t('all_categories') : nonMissingsSelection.value ? t('non_missings') : t('missings'));

const layout = computed(() => {
  return {
    //title: 'Frequencies',
    xaxis: {
      title: isFreq.value ? t('frequency') : t('percentage') + ' (%)',
    },
    yaxis: {
      title: t('categories'),
      tickvals: props.data.frequencies.map((f: Frequency) => f.value),
      ticktext: props.data.frequencies.map((f: Frequency) => f.value),
    },
    margin: {
    l: 80, // left margin
    r: 80, // right margin
    t: 80, // top margin
    b: 50, // bottom margin
    pad: 4 // padding around the plot area
  }
  }
});

const config = {
  displayModeBar: false,
  responsive: true,
};

const isFreq = computed(() => valueType.value === 'freq');
const isBar = computed(() => chartType.value === 'bar');

const frequencies = computed(() => {
  if (!props.data) return [];
  return props.data.frequencies
    .filter((f: Frequency) => nonMissingsSelection.value === null || (!nonMissingsSelection.value && f.missing) || (nonMissingsSelection.value && !f.missing));
});


const hasFrequencies = computed(() => frequencies.value.filter((f: Frequency) => f.freq>0).length > 0);

const chartData = computed(() => {
  if (!props.data) return [];
  const labels = frequencies.value.map((f: Frequency) => f.value);
  const values = frequencies.value.map((f: Frequency) => isFreq.value ? f.freq : f.pct * 100);

  if (props.data.otherFrequency > 0 && nonMissingsSelection.value !== false) {
    labels.push(t('other'));
    values.push(isFreq.value ? props.data.otherFrequency : ((props.data.otherFrequency / props.data.n) * 100));
  }

  if (isBar.value) {
    return [
      {
        type: 'bar',
        orientation: 'h',
        x: values,
        y: labels,
      },
    ];
  }
  return [
    {
      type: 'pie',
      hole: .4,
      values: values,
      labels: labels,
      hoverinfo: 'label+value+percent',
      textinfo: 'label'
    },
  ];
});
</script>
