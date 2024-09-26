<template>
  <div>
    <q-table
      v-if="profileActivityStore.summaries.length"
      flat
      :rows="profileActivityStore.summaries"
      :columns="columns"
      :row-key="(row) => `${row.context}:${row.profile}`"
      >
    <template v-slot:body-cell-profile="props">
      <q-td :props="props">
        <q-chip :label="props.value" class="q-ma-none" />
      </q-td>
    </template>
    </q-table>
    <div v-else class="text-hint">
      {{ $t('no_r_activity') }}
    </div>
  </div>
</template>


<script lang="ts">
export default defineComponent({
  name: 'RActivity',
});
</script>

<script setup lang="ts">
import { getDateLabel, getMillisLabel } from 'src/utils/dates';

interface Props {
  principal: string | undefined;
}

const props = defineProps<Props>();

const profileActivityStore = useProfileActivityStore();
const { t } = useI18n();

const columns = computed(() => {
  return [
    {
      name: 'profile',
      required: true,
      label: t('profile'),
      align: 'left',
      field: 'profile',
      sortable: true,
    },
    {
      name: 'context',
      required: true,
      label: t('context'),
      align: 'left',
      field: 'context',
      sortable: true,
    },
    {
      name: 'startDate',
      required: true,
      label: t('from'),
      align: 'left',
      field: 'startDate',
      format: (val: string) => getDateLabel(val),
      sortable: true,
    },
    {
      name: 'endDate',
      required: true,
      label: t('to'),
      align: 'left',
      field: 'endDate',
      format: (val: string) => getDateLabel(val),
      sortable: true,
    },
    {
      name: 'sessionsCount',
      required: true,
      label: t('r_sessions_count'),
      align: 'left',
      field: 'sessionsCount',
      sortable: true,
    },
    {
      name: 'executionTimeMillis',
      required: true,
      label: t('r_execution_time'),
      align: 'left',
      field: 'executionTimeMillis',
      format: (val: number) => getMillisLabel(val),
      sortable: true,
    },
  ];
});

onMounted(init);

watch(() => props.principal, init);

function init() {
  if (props.principal) {
    profileActivityStore.initSummaries(props.principal);
  }
}

</script>