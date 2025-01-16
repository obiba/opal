<template>
  <div>
    <div>
      <q-btn-toggle
        v-model="chartType"
        size="sm"
        toggle-color="primary"
        :options="[
          { value: 'pie', slot: 'pie' },
          { value: 'bar', slot: 'bar' },
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
          { label: t('frequency'), value: 'freq' },
          { label: t('percentage'), value: 'pct' },
        ]"
        class="on-right"
      />
      <q-badge color="positive" :label="`N:  ${data.n}`" class="on-right" />
      <q-toggle toggle-indeterminate v-model="nonMissingsSelection" :label="missings" class="on-right" />
    </div>
    <div class="row q-col-gutter-md">
      <div class="col-md-6 col-xs-12">
        <div v-if="hasFrequencies" class="q-mt-md">
          <vue-plotly :data="chartData" :layout="layout" :config="config" />
        </div>
      </div>
      <div class="col-md-6 col-xs-12">
        <frequencies-table
          :variable="variable"
          :nonMissingFreq="nonMissingFreq"
          :missingFreq="missingFreq"
          :totalFreq="totalFreq"
          :totalPct="totalPct"
          class="q-mt-md"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { VariableDto } from 'src/models/Magma';
import type { CategoricalSummaryDto, FrequencyDto } from 'src/models/Math';
import FrequenciesTable from 'src/components/datasource/FrequenciesTable.vue';
import VuePlotly from 'src/components/charts/VuePlotly.vue';

const { t } = useI18n();

interface Props {
  variable: VariableDto;
  data: CategoricalSummaryDto;
}

const props = defineProps<Props>();

const chartType = ref('pie');
const valueType = ref('freq');
const nonMissingsSelection = ref<boolean | null>(null);

const missings = computed(() =>
  nonMissingsSelection.value === null
    ? t('all_categories')
    : nonMissingsSelection.value
    ? t('non_missings')
    : t('missings')
);

const layout = computed(() => {
  if (!props.data) return {};
  if (isBar.value)
    return {
      //title: 'Frequencies',
      xaxis: {
        title: isFreq.value ? t('frequency') : t('percentage') + ' (%)',
      },
      yaxis: {
        title: t('categories'),
        type: 'category',
      },
      margin: {
        l: 80, // left margin
        r: 80, // right margin
        t: 50, // top margin
        b: 50, // bottom margin
        pad: 4, // padding around the plot area
      },
    };
  return {
    //title: 'Frequencies',
    margin: {
      l: 80, // left margin
      r: 80, // right margin
      t: 80, // top margin
      b: 50, // bottom margin
      pad: 4, // padding around the plot area
    },
  };
});

const config = {
  displayModeBar: false,
  responsive: true,
};

const isFreq = computed(() => valueType.value === 'freq');
const isBar = computed(() => chartType.value === 'bar');

const frequencies = computed(() => {
  if (!props.data) return [];
  const freqs = props.data.frequencies?.filter(
    (f: FrequencyDto) =>
      nonMissingsSelection.value === null ||
      (!nonMissingsSelection.value && f.missing) ||
      (nonMissingsSelection.value && !f.missing)
  );

  if (props.data.otherFrequency && props.data.otherFrequency > 0 && nonMissingsSelection.value !== false) {
    freqs.push({
      value: t('other'),
      freq: props.data.otherFrequency,
      pct: props.data.otherFrequency / props.data.n,
      missing: false,
    });
  }

  return freqs;
});

const hasFrequencies = computed(() => frequencies.value.filter((f: FrequencyDto) => f.freq > 0).length > 0);

const chartData = computed(() => {
  if (!props.data) return [];
  const labels = frequencies.value.map((f: FrequencyDto) => getLabel(f.value));
  const values = frequencies.value.map((f: FrequencyDto) => (isFreq.value ? f.freq : f.pct * 100));

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
      hole: 0.4,
      values: values,
      labels: labels,
      hoverinfo: 'label+value+percent',
      textinfo: 'label',
    },
  ];
});

const nonMissingFreq = computed(() => frequencies.value.filter((f: FrequencyDto) => !f.missing && f.freq > 0));
const missingFreq = computed(() => frequencies.value.filter((f: FrequencyDto) => f.missing));
const totalFreq = computed(() => frequencies.value.reduce((acc, f: FrequencyDto) => acc + f.freq, 0));
const totalPct = computed(() => frequencies.value.reduce((acc, f: FrequencyDto) => acc + f.pct, 0));

function getLabel(value: string): string {
  return value === 'NOT_NULL' ? t('not_empty') : value;
}
</script>
