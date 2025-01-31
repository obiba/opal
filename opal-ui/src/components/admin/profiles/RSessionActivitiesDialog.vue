<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ t('r_sessions_activity') }}</div>
      </q-card-section>
      <q-separator />
      <q-card-section>
        <div class="q-mb-sm">
          <span class="text-help">{{ t('profile') }}:</span>
          <span class="text-caption q-ml-xs">{{ props.profile }}</span>
          <span class="text-help on-right">{{ t('context') }}:</span>
          <span class="text-caption q-ml-xs">{{ props.context }}</span>
        </div>
        <div class="q-mb-md">
          <span class="text-hint">{{ t('total_duration') }}:</span>
          <span class="text-caption q-ml-xs">{{ getMillisLabel(totalDuration) }}</span>
          <span class="text-hint on-right">{{ t('total_execution_time') }}:</span>
          <span class="text-caption q-ml-xs">{{ getMillisLabel(totalExecutionTime) }}</span>
        </div>
        <q-table
          flat
          :rows="activities"
          :columns="columns"
          >
        </q-table>
      </q-card-section>
      <q-separator />
      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('close')" color="primary" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { RSessionActivityDto } from 'src/models/OpalR';
import { getDate, getDateLabel, getMillisLabel } from 'src/utils/dates';
import { DefaultAlignment } from 'src/components/models';

interface DialogProps {
  modelValue: boolean;
  principal: string;
  profile: string;
  context: string;
}

const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const emit = defineEmits(['update:modelValue']);

const profileActivityStore = useProfileActivityStore();
const { t } = useI18n();

const activities = ref<RSessionActivityDto[]>([]);

const totalDuration = computed(() => activities.value.map((act) => getDuration(act)).reduce((acc, val) => acc + val, 0));
const totalExecutionTime = computed(() => activities.value.map((act) => act.executionTimeMillis).reduce((acc, val) => acc + val, 0));

const columns = computed(() => {
  return [
    {
      name: 'createdDate',
      required: true,
      label: t('start_time'),
      align: DefaultAlignment,
      field: 'createdDate',
      format: (val: string) => getDateLabel(val),
      sortable: true,
    },
    {
      name: 'updatedDate',
      required: true,
      label: t('end_time'),
      align: DefaultAlignment,
      field: 'updatedDate',
      format: (val: string) => getDateLabel(val),
      sortable: true,
    },
    {
      name: 'duration',
      required: true,
      label: t('duration'),
      align: DefaultAlignment,
      field: (row: RSessionActivityDto) => getMillisLabel(Math.ceil(Math.max(getDuration(row), row.executionTimeMillis)/1000)*1000),
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

watch(
  () => props.modelValue,
  () => {
    showDialog.value = props.modelValue;
    init();
  }
);

onMounted(init);

function init() {
  if (props.modelValue) {
    profileActivityStore.getRSessionActivities(props.principal, props.context, props.profile).then((data: RSessionActivityDto[]) => {
      activities.value = data;
    });
  }
}

function onHide() {
  emit('update:modelValue', false);
}

function getDuration(activity: RSessionActivityDto) {
  const updated = getDate(activity.updatedDate);
  const created = getDate(activity.createdDate);
  return created && updated ? updated.getTime() - created.getTime() : 0;
}
</script>
