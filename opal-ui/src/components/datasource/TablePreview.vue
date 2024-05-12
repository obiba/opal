<template>
  <div v-if="props.table">
    <q-tabs
      v-model="tableTab"
      class="text-primary"
    >
      <q-tab name="variables" :label="$t('dictionary')">
        <q-badge v-if="props.table.variableCount !== undefined" color="red">{{ props.table.variableCount }}</q-badge>
      </q-tab>
      <q-tab name="values" :label="$t('values')">
        <q-badge v-if="props.table.valueSetCount !== undefined" color="red">{{ props.table.valueSetCount }}</q-badge>
      </q-tab>
    </q-tabs>
    <q-separator />
    <q-tab-panels v-model="tableTab">
      <q-tab-panel name="variables">
        <variables-list
          :variables="props.variables" :loading="props.loading" />
      </q-tab-panel>
      <q-tab-panel name="values">
        <pre>{{ props.table }}</pre>
      </q-tab-panel>
    </q-tab-panels>
  </div>
</template>


<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'TablePreviewList',
});
</script>
<script setup lang="ts">
import VariablesList from 'src/components/datasource/VariablesList.vue';
import { TableDto, VariableDto } from 'src/models/Magma';

interface TablePreviewProps {
  table: TableDto | undefined;
  variables: VariableDto[];
  loading: boolean;
}

const props = defineProps<TablePreviewProps>();

const tableTab = ref('variables');

watch(() => props.table, (value) => {
  if (value) {
    tableTab.value = 'variables';
  }
});

</script>
