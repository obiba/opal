<template>
  <div>
    <q-table
      ref="tableRef"
      flat
      :rows="rows"
      :columns="columns"
      row-key="name"
      :loading="props.loading"
      :pagination="{ page: 1, rowsPerPage: 5 }"
    >
      <template v-slot:body-cell-name="props">
        <q-td :props="props">
          <span class="text-primary">{{ props.value }}</span>
        </q-td>
      </template>
      <template v-slot:body-cell-valueType="props">
        <q-td :props="props">
          <pre class="q-ma-none">{{ props.value }}</pre>
        </q-td>
      </template>
      <template v-slot:body-cell-label="props">
        <q-td :props="props">
          <div v-for="(attr, idx) in getLabels(props.value)" :key="idx">
            <q-badge v-if="attr.locale" color="grey-6" :label="attr.locale" class="on-left" />
            <span>{{ attr.value }}</span>
          </div>
        </q-td>
      </template>
      <template v-slot:body-cell-categories="props">
        <q-td :props="props">
          <span>{{ getCategoryNames(props.value) }}</span>
        </q-td>
      </template>
    </q-table>
  </div>
</template>

<script setup lang="ts">
import type { VariableDto, CategoryDto } from 'src/models/Magma';
import { getLabels } from 'src/utils/attributes';
import { DefaultAlignment } from 'src/components/models';

interface VariablesListProps {
  variables: VariableDto[];
  loading: boolean;
}

const props = defineProps<VariablesListProps>();

const { t } = useI18n();

const tableRef = ref();

const rows = ref(props.variables ? props.variables : []);

watch(
  () => props.variables,
  (value) => {
    rows.value = value ? value : [];
  }
);

const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: DefaultAlignment,
    field: 'name',
    format: (val: string) => val,
    sortable: true,
  },
  {
    name: 'label',
    required: true,
    label: t('label'),
    align: DefaultAlignment,
    field: 'attributes',
  },
  {
    name: 'valueType',
    required: true,
    label: t('value_type'),
    align: DefaultAlignment,
    field: 'valueType',
    format: (val: string) => t(val),
    sortable: true,
  },
  {
    name: 'categories',
    required: true,
    label: t('categories'),
    align: DefaultAlignment,
    field: 'categories',
  },
]);

function getCategoryNames(categories: CategoryDto[]) {
  return categories ? categories.map((c) => c.name).join(', ') : undefined;
}
</script>
