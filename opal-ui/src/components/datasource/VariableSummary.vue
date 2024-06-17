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
      @update:model-value="init"
      style="width: 200px;">
      <template v-slot:after>
        <span class="on-right text-body1">/ {{ total }}</span>
      </template>
    </q-input>
    <q-btn
      color="secondary"
      icon="refresh"
      :label="$t('refresh')"
      size="sm"
      @click="init"
      class="q-mt-lg"
      style="height: 2.5em;"
    />
    <q-btn
      color="primary"
      icon="analytics"
      :label="$t('full_summary')"
      size="sm"
      @click="onFullSummary"
      class="q-mt-lg"
      style="height: 2.5em;"
    />
  </div>
  <q-separator class="q-mt-md q-mb-md" />
  <div v-if="summary['Math.CategoricalSummaryDto.categorical']">
    <categorical-summary-chart :data="summary['Math.CategoricalSummaryDto.categorical']" class="q-mt-md"/>
  </div>
  <div v-else-if="summary['Math.TextSummaryDto.textSummary']">
    <categorical-summary-chart :data="summary['Math.TextSummaryDto.textSummary']" class="q-mt-md"/>
  </div>
  <div v-else-if="summary['Math.ContinuousSummaryDto.continuous']">
    <continuous-summary-chart :data="summary['Math.ContinuousSummaryDto.continuous']" class="q-mt-md"/>
  </div>
  <div v-else>
    <pre>{{ summary }}</pre>
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'VariableSummary',
});
</script>
<script setup lang="ts">
import { VariableDto } from 'src/models/Magma';
import CategoricalSummaryChart from 'src/components/charts/CategoricalSummaryChart.vue';
import ContinuousSummaryChart from 'src/components/charts/ContinuousSummaryChart.vue';
const datasourceStore = useDatasourceStore();

interface VariableSummaryProps {
  variable: VariableDto;
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
    .loadVariableSummary(props.variable, true, limit.value > props.total ? props.total : limit.value)
    .then((data) => {
      summary.value = data;
    });
}

function onFullSummary() {
  limit.value = undefined;
  init();
}

</script>
