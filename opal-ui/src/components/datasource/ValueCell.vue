<template>
  <div :title="occurrence ? `[${occurrence}] ${variable.occurrenceGroup ? variable.occurrenceGroup : ''}` : ''">
    <div v-if="value === undefined">
      <span class="text-caption text-grey-5">null</span>
    </div>
    <div v-else-if="value.link">
      <q-btn
        flat
        dense
        no-caps
        icon="download"
        size="sm"
        :label="`[${getSizeLabel(value.length)}]`"
        class="q-mr-sm"
        @click="onDownload()"
      />
    </div>
    <div v-else-if="value.value">
      <a v-if="value.value.startsWith('http')" :href="value.value" target="_blank">{{ value.value }}</a>
      <span v-else>{{ value.value }}</span>
    </div>
    <div v-else-if="value.values">
      <value-cell
        v-for="(val, idx) in value.values"
        :key="idx"
        :value="val"
        :variable="variable"
        :occurrence="idx + 1"
      ></value-cell>
    </div>
    <div v-else>
      <span class="text-caption text-grey-5">null</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { baseUrl } from 'src/boot/api';
import { getSizeLabel } from 'src/utils/files';
import type { ValueSetsDto_ValueDto, VariableDto } from 'src/models/Magma';

interface ValueCellProps {
  value?: ValueSetsDto_ValueDto;
  variable: VariableDto;
  occurrence?: number;
}

const props = defineProps<ValueCellProps>();

function onDownload() {
  if (props.value?.link) {
    window.open(`${baseUrl}${props.value.link.replace('/entity/', '/')}`, '_blank');
  }
}
</script>
