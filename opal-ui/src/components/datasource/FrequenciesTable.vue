<template>
  <q-markup-table flat bordered>
    <thead>
      <tr class="bg-blue-grey-1">
        <th class="text-left">{{ $t('value') }}</th>
        <th class="text-left">{{ $t('frequency') }}</th>
        <th class="text-left">{{ $t('percentage') }}</th>
      </tr>
    </thead>
    <tbody>
      <template v-if="nonMissingTotalFreq>0">
        <tr class="bg-grey-2 text-bold">
          <td colspan="3">{{  $t('non_missings') }}</td>
        </tr>
        <tr v-for="f in nonMissingFreq" :key="f.value">
          <td>{{ getLabel(f.value) }}</td>
          <td>{{ f.freq }}</td>
          <td class="text-grey-6">
            {{ (f.pct * 100).toFixed(2) }}%
            <span v-if="nonMissingFreq.length>1">({{ getGroupPct(f.freq, nonMissingTotalFreq).toFixed(2) }}%)</span>
          </td>
        </tr>
        <tr class="text-italic" v-if="nonMissingFreq.length>1">
          <td>{{ $t('sub_total') }}</td>
          <td>{{ nonMissingTotalFreq }}</td>
          <td class="text-grey-6">{{ (nonMissingTotalPct * 100).toFixed(2) }}% (100%)</td>
        </tr>
      </template>

      <template v-if="missingTotalFreq>0">
        <tr class="bg-grey-2 text-bold">
          <td colspan="3">{{  $t('missings') }}</td>
        </tr>
        <tr v-for="f in missingFreq" :key="f.value">
          <td>{{ getLabel(f.value) }}</td>
          <td>{{ f.freq }}</td>
          <td class="text-grey-6">
            {{ (f.pct * 100).toFixed(2) }}%
            <span v-if="missingFreq.length>1">({{ getGroupPct(f.freq, missingTotalFreq).toFixed(2) }}%)</span>
          </td>
        </tr>
        <tr class="text-italic" v-if="missingFreq.length>1">
          <td>{{ $t('sub_total') }}</td>
          <td>{{ missingTotalFreq }}</td>
          <td class="text-grey-6">{{ (missingTotalPct * 100).toFixed(2) }}% (100%)</td>
        </tr>
      </template>

      <tr class="bg-grey-2 text-bold">
        <td>{{ $t('total') }}</td>
        <td>{{ totalFreq }}</td>
        <td class="text-grey-6">{{ (totalPct * 100).toFixed(0) }}%</td>
      </tr>
    </tbody>
  </q-markup-table>
</template>

<script lang="ts">
export default defineComponent({
  name: 'FrequenciesTable',
});
</script>
<script setup lang="ts">
import { FrequencyDto } from 'src/models/Math';

const { t } = useI18n();

interface FrequenciesTableProps {
  nonMissingFreq: FrequencyDto[];
  missingFreq: FrequencyDto[];
  totalFreq: number;
  totalPct: number;
}

const props = withDefaults(defineProps<FrequenciesTableProps>(), {
  nonMissingFreq: [] as FrequencyDto[],
  missingFreq: [] as FrequencyDto[],
  totalFreq: 0,
  totalPct: 0,
});

const nonMissingTotalFreq = computed(() => props.nonMissingFreq.reduce((acc, f) => acc + f.freq, 0));
const nonMissingTotalPct = computed(() => props.nonMissingFreq.reduce((acc, f) => acc + f.pct, 0));
const missingTotalFreq = computed(() => props.missingFreq.reduce((acc, f) => acc + f.freq, 0));
const missingTotalPct = computed(() => props.missingFreq.reduce((acc, f) => acc + f.pct, 0));

function getLabel(value: string): string {
  return value === 'NOT_NULL' ? t('not_empty') : value;
}

function getGroupPct(freq: number, total: number): number {
  return total === 0 ? 0 : (freq / total) * 100;
}
</script>
