<template>
  <div class="row q-gutter-md">
    <q-input
      v-model="limit"
      flat
      dense
      size="sm"
      :label="$t('limit')"
      type="number"
      min="0"
      :max="total"
      :step="STEP_COUNT"
      debounce="500"
      @update:model-value="init">
      <template v-slot:after>
        <span class="on-right on-left text-body1">/ {{ total }}</span>
        <q-btn
          flat
          class="bg-grey-6 text-white on-left"
          icon="refresh"
          :label="$t('refresh')"
          size="sm"
          @click="init"
        />
        <q-btn
          flat
          class="bg-primary text-white"
          icon="analytics"
          :label="$t('full_summary')"
          size="sm"
          @click="onFullSummary"
        />
      </template>
    </q-input>
  </div>
  <q-separator class="q-mt-md q-mb-md" />
  <div v-if="summary['Math.CategoricalSummaryDto.categorical']">
    <div class="row">
      <categorical-summary-chart :data="summary['Math.CategoricalSummaryDto.categorical']" class="q-mt-md"/>
    </div>
  </div>
  <div v-if="summary['Math.TextSummaryDto.textSummary']">
    <div class="row">
      <categorical-summary-chart :data="summary['Math.TextSummaryDto.textSummary']" class="q-mt-md"/>
    </div>
  </div>
  <div v-if="summary['Math.ContinuousSummaryDto.continuous']">
    <div>
      <continuous-summary-chart :data="summary['Math.ContinuousSummaryDto.continuous']" class="q-mt-md"/>
    </div>
  </div>
  <div v-else>
    <pre>{{ summary }}</pre>
  </div>
  <pre>{{ summary }}</pre>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'VariableSummary',
});
</script>
<script setup lang="ts">
import { Variable } from 'src/components/models';
import CategoricalSummaryChart from 'src/components/charts/CategoricalSummaryChart.vue';
import ContinuousSummaryChart from 'src/components/charts/ContinuousSummaryChart.vue';
const datasourceStore = useDatasourceStore();

interface VariableSummaryProps {
  variable: Variable;
  total: number;
}

const props = defineProps<VariableSummaryProps>();

const STEP_COUNT = 1000;

const summary = ref({});
const limit = ref(STEP_COUNT);

onMounted(() => {
  init();
});

function init() {
  datasourceStore
    .loadVariableSummary(props.variable, false, limit.value > props.total ? props.total : limit.value)
    .then((data) => {
      summary.value = data;
    });
}

function onFullSummary() {
  limit.value = undefined;
  init();
}

</script>
