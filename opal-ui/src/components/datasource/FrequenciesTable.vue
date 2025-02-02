<template>
  <q-markup-table flat bordered>
    <thead>
      <tr class="bg-blue-grey-1">
        <th class="text-left">{{ t('value') }}</th>
        <th class="text-left">{{ t('frequency') }}</th>
        <th class="text-left">{{ t('percentage') }}</th>
      </tr>
    </thead>
    <tbody>
      <template v-if="nonMissingTotalFreq > 0">
        <tr class="bg-grey-2 text-bold">
          <td colspan="3">{{ t('non_missings') }}</td>
        </tr>
        <tr v-for="f in nonMissingFreq" :key="f.value">
          <td>
            <div>{{ getLabel(f.value) }}</div>
            <div class="text-hint">{{ getLabels(f.value) }}</div>
          </td>
          <td>{{ f.freq }}</td>
          <td class="text-grey-6">
            {{ (f.pct * 100).toFixed(2) }}%
            <span v-if="nonMissingFreq.length > 1">({{ getGroupPct(f.freq, nonMissingTotalFreq).toFixed(2) }}%)</span>
          </td>
        </tr>
        <tr class="text-italic" v-if="nonMissingFreq.length > 1">
          <td>{{ t('sub_total') }}</td>
          <td>{{ nonMissingTotalFreq }}</td>
          <td class="text-grey-6">{{ (nonMissingTotalPct * 100).toFixed(2) }}% (100%)</td>
        </tr>
      </template>

      <template v-if="missingTotalFreq > 0">
        <tr class="bg-grey-2 text-bold">
          <td colspan="3">{{ t('missings') }}</td>
        </tr>
        <tr v-for="f in missingFreq" :key="f.value">
          <td>
            <div>{{ getLabel(f.value) }}</div>
            <div class="text-hint">{{ getLabels(f.value) }}</div>
          </td>
          <td>{{ f.freq }}</td>
          <td class="text-grey-6">
            {{ (f.pct * 100).toFixed(2) }}%
            <span v-if="missingFreq.length > 1">({{ getGroupPct(f.freq, missingTotalFreq).toFixed(2) }}%)</span>
          </td>
        </tr>
        <tr class="text-italic" v-if="missingFreq.length > 1">
          <td>{{ t('sub_total') }}</td>
          <td>{{ missingTotalFreq }}</td>
          <td class="text-grey-6">{{ (missingTotalPct * 100).toFixed(2) }}% (100%)</td>
        </tr>
      </template>

      <tr class="bg-grey-2 text-bold">
        <td>{{ t('total') }}</td>
        <td>{{ totalFreq }}</td>
        <td class="text-grey-6">{{ (totalPct * 100).toFixed(0) }}%</td>
      </tr>
    </tbody>
  </q-markup-table>
</template>

<script setup lang="ts">
import type { VariableDto } from 'src/models/Magma';
import type { FrequencyDto } from 'src/models/Math';
import { getLabelsString } from 'src/utils/attributes';

const { t } = useI18n();

interface FrequenciesTableProps {
  variable: VariableDto;
  nonMissingFreq: FrequencyDto[];
  missingFreq: FrequencyDto[];
  totalFreq: number;
  totalPct: number;
}

const props = withDefaults(defineProps<FrequenciesTableProps>(), {
  totalFreq: 0,
  totalPct: 0,
});

const categorical = computed(() => props.variable.categories && props.variable.categories.length > 0);
const nonMissingTotalFreq = computed(() => props.nonMissingFreq.reduce((acc, f) => acc + f.freq, 0));
const nonMissingTotalPct = computed(() => props.nonMissingFreq.reduce((acc, f) => acc + f.pct, 0));
const missingTotalFreq = computed(() => props.missingFreq.reduce((acc, f) => acc + f.freq, 0));
const missingTotalPct = computed(() => props.missingFreq.reduce((acc, f) => acc + f.pct, 0));

function getLabels(value: string): string | undefined {
  if (categorical.value) {
    const cat = props.variable.categories.find((c) => c.name === value);
    if (cat && cat.attributes) {
      const labels = getLabelsString(cat.attributes);
      if (labels) {
        return labels;
      }
    }
  }
  return undefined;
}

function getLabel(value: string): string {
  return value === 'NOT_NULL' ? t('not_empty') : value;
}

function getGroupPct(freq: number, total: number): number {
  return total === 0 ? 0 : (freq / total) * 100;
}
</script>
