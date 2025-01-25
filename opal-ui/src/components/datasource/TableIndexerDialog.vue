<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card flat class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('schedule') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-select
          v-model="schedule.type"
          :options="typeOptions"
          :label="t('type')"
          dense
          emit-value
          map-options
          class="q-mb-md"
        />
        <div v-if="schedule.type === ScheduleType.HOURLY">
          <div class="row q-gutter-md">
            <div class="col-1 text-help q-pt-sm">{{ t('at') }}</div>
            <div class="col">
              <q-select
                v-model="schedule.minutes"
                :options="minutesOptions"
                :label="t('minutes')"
                dense
                emit-value
                map-options
              />
            </div>
          </div>
        </div>
        <div v-if="schedule.type === ScheduleType.WEEKLY">
          <div class="row q-gutter-md q-mb-md">
            <div class="col-1 text-help q-pt-sm">{{ t('on') }}</div>
            <div class="col">
              <q-select v-model="schedule.day" :options="dayOptions" :label="t('day')" dense emit-value map-options />
            </div>
          </div>
        </div>
        <div v-if="schedule.type === ScheduleType.DAILY || schedule.type === ScheduleType.WEEKLY">
          <div class="row q-gutter-md">
            <div class="col-1 text-help q-pt-sm">{{ t('at') }}</div>
            <div class="col">
              <q-select
                v-model="schedule.hours"
                :options="hoursOptions"
                :label="t('hour')"
                dense
                emit-value
                map-options
              />
            </div>
            <div class="col">
              <q-select
                v-model="schedule.minutes"
                :options="minutesOptions"
                :label="t('minutes')"
                dense
                emit-value
                map-options
              />
            </div>
          </div>
        </div>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('save')" color="primary" @click="onSchedule" v-close-popup :disable="!isValid" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { type ScheduleDto, ScheduleType, Day } from 'src/models/Opal';

interface DialogProps {
  modelValue: boolean;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const datasourceStore = useDatasourceStore();
const { t } = useI18n();

const schedule = ref<ScheduleDto>({ type: ScheduleType.NOT_SCHEDULED });
const showDialog = ref(props.modelValue);

const typeOptions = computed(() =>
  Object.values(ScheduleType)
    .filter((value) => value !== ScheduleType.UNRECOGNIZED)
    .map((value) => ({ label: t(`schedule_type.${value}`), value }))
);

const dayOptions = computed(() =>
  Object.values(Day)
    .filter((value) => value !== Day.UNRECOGNIZED)
    .map((value) => ({ label: t(`weekdays.${value}`), value }))
);

const minutesOptions = computed(() => {
  // every 5 minutes
  const options = Array.from({ length: 60 / 5 }, (_, i) => i * 5);
  return options.map((value) => ({ label: value.toString().padStart(2, '0'), value }));
});

const hoursOptions = computed(() => {
  // every hour
  const options = Array.from({ length: 24 }, (_, i) => i);
  return options.map((value) => ({ label: value.toString().padStart(2, '0'), value }));
});

const isValid = computed(() => {
  if (schedule.value.type === ScheduleType.HOURLY) {
    return schedule.value.minutes !== undefined;
  }
  if (schedule.value.type === ScheduleType.DAILY) {
    return schedule.value.hours !== undefined && schedule.value.minutes !== undefined;
  }
  if (schedule.value.type === ScheduleType.WEEKLY) {
    return (
      schedule.value.day !== undefined && schedule.value.hours !== undefined && schedule.value.minutes !== undefined
    );
  }
  return true;
});

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      schedule.value = datasourceStore.tableIndex.schedule
        ? { ...datasourceStore.tableIndex.schedule }
        : { type: ScheduleType.NOT_SCHEDULED };
    }
    showDialog.value = value;
  }
);

function onHide() {
  emit('update:modelValue', false);
}

function onSchedule() {
  if (schedule.value.type === ScheduleType.NOT_SCHEDULED) {
    delete schedule.value.day;
    delete schedule.value.hours;
    delete schedule.value.minutes;
  } else if (schedule.value.type === ScheduleType.HOURLY) {
    delete schedule.value.day;
    delete schedule.value.hours;
  } else if (schedule.value.type === ScheduleType.DAILY) {
    delete schedule.value.day;
  }
  datasourceStore.scheduleTableIndex(schedule.value);
}
</script>
