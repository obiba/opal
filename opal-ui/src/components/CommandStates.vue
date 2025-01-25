<template>
  <div>
    <q-table
      ref="tableRef"
      flat
      :rows="rows"
      :columns="columns"
      row-key="name"
      :pagination="initialPagination"
      :loading="loading"
    >
      <template v-slot:top>
        <q-btn
          color="secondary"
          icon="refresh"
          :title="t('refresh')"
          outline
          size="sm"
          @click="onRefresh"
          class="q-mb-sm"
        />
        <q-btn
          outline
          color="secondary"
          icon="cleaning_services"
          :title="t('clear')"
          size="sm"
          @click="onClear(undefined)"
          class="on-right q-mb-sm"
        />
      </template>
      <template v-slot:body="props">
        <q-tr :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-td key="id" :props="props">
            <span class="text-primary">{{ props.row.id }}</span>
          </q-td>
          <q-td key="name" :props="props">
            <q-badge :label="props.row.name" />
            <div class="float-right">
              <q-btn
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="t('messages')"
                :icon="toolsVisible[props.row.id] ? 'visibility' : 'none'"
                @click="onShowMessages(props.row)"
                class="on-right"
              />
              <q-btn
                v-if="isDeletable(props.row)"
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="t('delete')"
                :icon="toolsVisible[props.row.id] ? 'delete' : 'none'"
                class="q-ml-xs"
                @click="onClear(props.row)"
              />
              <q-btn
                v-if="isCancelable(props.row)"
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="t('cancel')"
                :icon="toolsVisible[props.row.id] ? 'cancel' : 'none'"
                class="q-ml-xs"
                @click="onCancel(props.row)"
              />
            </div>
          </q-td>

          <q-td key="project" :props="props">
            <router-link v-if="props.row.project" :to="`/project/${props.row.project}`"
              >{{ props.row.project }}
            </router-link>
          </q-td>

          <q-td key="owner" :props="props">
            <span class="text-caption">{{ props.row.owner }}</span>
          </q-td>

          <q-td key="startTime" :props="props">
            {{ getDateLabel(props.row.startTime) }}
          </q-td>

          <q-td key="endTime" :props="props">
            {{ props.row.endTime ? getDateLabel(props.row.endTime) : '' }}
            <span class="text-help">({{ getDatesDistanceLabel(props.row.startTime, props.row.endTime, false) }})</span>
          </q-td>

          <q-td key="status" :props="props">
            <q-icon
              v-if="props.row.status !== CommandStateDto_Status.IN_PROGRESS"
              name="circle"
              size="sm"
              :color="commandStatusColor(props.row.status)"
              :title="props.row.status"
            />
            <div v-if="props.row.status === CommandStateDto_Status.IN_PROGRESS && props.row.progress">
              <div class="text-caption">
                <span>
                  {{ props.row.progress.message }}
                </span>
                <span class="on-right text-help">
                  {{ `${props.row.progress.percent}% (${props.row.progress.current}/${props.row.progress.end})` }}
                </span>
              </div>
              <q-linear-progress :value="props.row.progress.percent / 100" />
            </div>
          </q-td>
        </q-tr>
      </template>
    </q-table>

    <q-dialog v-model="showMessages">
      <q-card flat class="dialog-md">
        <q-card-section align="right" class="q-pb-none">
          <q-btn flat round dense icon="close" v-close-popup />
        </q-card-section>
        <q-card-section class="q-pt-none">
          <q-list separator dense>
            <q-item>
              <q-item-section style="max-width: 200px">
                <q-item-label class="text-bold">{{ t('date') }}</q-item-label>
              </q-item-section>
              <q-item-section>
                <q-item-label class="text-bold">{{ t('message') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item v-for="message in selected?.messages" :key="message.timestamp">
              <q-item-section style="max-width: 200px">
                <q-item-label class="text-caption">{{ getDateLabel(message.timestamp) }}</q-item-label>
              </q-item-section>
              <q-item-section>
                <q-item-label>
                  <div v-html="message.msg.replace('\n', '<br/>')"></div>
                </q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-card-section>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup lang="ts">
import { type CommandStateDto, CommandStateDto_Status } from 'src/models/Commands';
import { commandStatusColor } from 'src/utils/colors';
import { getDateLabel, getDatesDistanceLabel } from 'src/utils/dates';
import { DefaultAlignment } from 'src/components/models';

interface Props {
  commands: CommandStateDto[];
  project?: string | undefined;
}

const props = defineProps<Props>();
const emit = defineEmits(['refresh', 'clear', 'cancel']);

const { t } = useI18n();

const tableRef = ref();
const loading = ref(false);
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const toolsVisible = ref<{ [key: string]: boolean }>({});
const showMessages = ref(false);
const selected = ref<CommandStateDto | null>(null);

const columns = computed(() => {
  const cols = [
    {
      name: 'id',
      required: true,
      label: t('id'),
      align: DefaultAlignment,
      field: 'id',
      format: (val: string) => val,
      sortable: true,
    },
    {
      name: 'name',
      required: true,
      label: t('name'),
      align: DefaultAlignment,
      field: 'name',
      sortable: true,
    },
    {
      name: 'owner',
      required: true,
      label: t('user'),
      align: DefaultAlignment,
      field: 'owner',
      sortable: true,
    },
    {
      name: 'startTime',
      required: true,
      label: t('start_time'),
      align: DefaultAlignment,
      field: 'startTime',
      format: (val: string) => getDateLabel(val),
      sortable: true,
    },
    {
      name: 'endTime',
      required: true,
      label: t('end_time'),
      align: DefaultAlignment,
      field: 'endTime',
      format: (val: string) => getDateLabel(val),
      sortable: true,
    },
  ];
  if (!props.project) {
    cols.splice(2, 0, {
      name: 'project',
      required: false,
      label: t('project'),
      align: DefaultAlignment,
      field: 'project',
      format: (val: string) => val,
      sortable: true,
    });
  }
  cols.push({
    name: 'status',
    required: false,
    label: t('status'),
    align: DefaultAlignment,
    field: 'status',
    sortable: true,
  });
  return cols;
});

const rows = computed(() => (props.commands ? props.commands : []));

watch(
  () => props.commands,
  () => {
    loading.value = false;
  }
);

function isDeletable(row: CommandStateDto) {
  return row.status !== CommandStateDto_Status.IN_PROGRESS && row.status !== CommandStateDto_Status.CANCEL_PENDING;
}

function isCancelable(row: CommandStateDto) {
  return row.status === CommandStateDto_Status.IN_PROGRESS;
}

function onRefresh() {
  loading.value = true;
  emit('refresh');
}

function onClear(row: CommandStateDto | undefined) {
  loading.value = true;
  emit('clear', row);
}

function onCancel(row: CommandStateDto) {
  loading.value = true;
  emit('cancel', row);
}

function onOverRow(row: CommandStateDto) {
  toolsVisible.value[row.id] = true;
}

function onLeaveRow(row: CommandStateDto) {
  toolsVisible.value[row.id] = false;
}

function onShowMessages(row: CommandStateDto) {
  showMessages.value = !showMessages.value;
  selected.value = row;
}
</script>
