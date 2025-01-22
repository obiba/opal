<template>
  <div>
    <div v-if="profileActivityStore.summaries.length">
      <div class="q-mb-md">
        <span class="text-hint">{{ t('total_execution_time') }}:</span>
        <span class="text-caption q-ml-xs">{{ getMillisLabel(totalExecutionTime) }}</span>
      </div>
      <q-table
        flat
        :rows="profileActivityStore.summaries"
        :columns="columns"
        :row-key="(row) => `${row.context}:${row.profile}`"
        >
        <template v-slot:body-cell-profile="props">
          <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
            <q-chip :label="props.value" class="q-ma-none" />
            <div class="float-right">
              <q-btn
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :icon="toolsVisible[getKey(props.row)] ? 'visibility' : 'none'"
                class="q-ml-xs"
                @click="onShowActivities(props.row)"
              />
            </div>
          </q-td>
        </template>
        <template v-slot:body-cell-context="props">
          <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
            <span class="text-caption">{{ props.value }}</span>
          </q-td>
        </template>
      </q-table>
    </div>
    <div v-else class="text-hint">
      {{ t('no_r_activity') }}
    </div>

    <r-session-activities-dialog
      v-if="props.principal"
      v-model:model-value="showDialog"
      :principal="props.principal"
      :profile="selectedProfile"
      :context="selectedContext" />
  </div>
</template>

<script setup lang="ts">
import RSessionActivitiesDialog from 'src/components/admin/profiles/RSessionActivitiesDialog.vue';
import type { RActivitySummaryDto } from 'src/models/OpalR';
import { getDateLabel, getMillisLabel } from 'src/utils/dates';
import { DefaultAlignment } from 'src/components/models';

interface Props {
  principal: string | undefined;
}

const props = defineProps<Props>();

const profileActivityStore = useProfileActivityStore();
const { t } = useI18n();

const showDialog = ref<boolean>(false);
const selectedProfile = ref<string>('');
const selectedContext = ref<string>('');
const toolsVisible = ref<{ [key: string]: boolean }>({});

const totalExecutionTime = computed(() => profileActivityStore.summaries.map((sum) => sum.executionTimeMillis).reduce((acc, val) => acc + val, 0));

const columns = computed(() => {
  return [
    {
      name: 'profile',
      required: true,
      label: t('profile'),
      align: DefaultAlignment,
      field: 'profile',
      sortable: true,
    },
    {
      name: 'context',
      required: true,
      label: t('context'),
      align: DefaultAlignment,
      field: 'context',
      sortable: true,
    },
    {
      name: 'startDate',
      required: true,
      label: t('from'),
      align: DefaultAlignment,
      field: 'startDate',
      format: (val: string) => getDateLabel(val),
      sortable: true,
    },
    {
      name: 'endDate',
      required: true,
      label: t('to'),
      align: DefaultAlignment,
      field: 'endDate',
      format: (val: string) => getDateLabel(val),
      sortable: true,
    },
    {
      name: 'sessionsCount',
      required: true,
      label: t('r_sessions_count'),
      align: DefaultAlignment,
      field: 'sessionsCount',
      sortable: true,
    },
    {
      name: 'executionTimeMillis',
      required: true,
      label: t('r_execution_time'),
      align: DefaultAlignment,
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

function onShowActivities(row: RActivitySummaryDto) {
  selectedProfile.value = row.profile;
  selectedContext.value = row.context;
  showDialog.value = true;
}

function getKey(row: RActivitySummaryDto) {
  return `${row.context}:${row.profile}`;
}

function onOverRow(row: RActivitySummaryDto) {
  toolsVisible.value[getKey(row)] = true;
}

function onLeaveRow(row: RActivitySummaryDto) {
  toolsVisible.value[getKey(row)] = false;
}
</script>