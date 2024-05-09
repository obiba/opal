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
      @row-click="onRowClick"
    >
      <template v-slot:top>
        <q-btn
          color="secondary"
          text-color="white"
          icon="refresh"
          :label="$t('refresh')"
          size="sm"
          @click="onRefresh"
          class="q-mb-sm"
        />
        <q-btn
          outline
          color="secondary"
          icon="delete"
          :label="$t('clear')"
          size="sm"
          @click="onClear"
          class="on-right q-mb-sm"
        />
      </template>
      <template v-slot:body="props">
        <q-tr :props="props" @mouseover="onOverCommand(props.row)" @mouseleave="onLeaveCommand(props.row)">
          <q-td key="id" :props="props">
            <span class="text-primary">{{ props.row.id }}</span>
          </q-td>
          <q-td key="name" :props="props">
            <q-badge :label="props.row.name"/>
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="$t('messages')"
              :icon="toolsVisible[props.row.id] ? 'visibility' : 'none'"
              @click="onShowMessages(props.row)"
              class="on-right"></q-btn>
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="$t('delete')"
              :icon="toolsVisible[props.row.id] ? 'delete' : 'none'"
              class="on-right"></q-btn>
          </q-td>

          <q-td key="project" :props="props">
            <span class="text-caption">{{ props.row.project }}</span>
          </q-td>

          <q-td key="owner" :props="props">
            <span class="text-caption">{{ props.row.owner }}</span>
          </q-td>

          <q-td key="startTime" :props="props">
            {{ getDateLabel(props.row.startTime) }}
          </q-td>

          <q-td key="endTime" :props="props">
            {{ getDateLabel(props.row.endTime) }}
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
                <span class="on-right  text-help">
                  {{ `${props.row.progress.percent}% (${props.row.progress.current}/${props.row.progress.end})` }}
                </span>
              </div>
              <q-linear-progress :value="props.row.progress.percent" />
            </div>
          </q-td>
        </q-tr>
      </template>
    </q-table>

    <q-dialog v-model="showMessages" persistent>
      <q-card flat style="width: 500px; max-width: 80vw;">
        <q-card-section align="right" class="q-pb-none">
          <q-btn
            flat
            round
            dense
            icon="close"
            v-close-popup
          />
        </q-card-section>
        <q-card-section class="q-pt-none">
          <q-list separator dense>
            <q-item>
              <q-item-section>
                <q-item-label class="text-caption text-bold">{{ $t('date') }}</q-item-label>
              </q-item-section>
              <q-item-section>
                <q-item-label class="text-bold">{{  $t('message') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item v-for="message in selected?.messages" :key="message.timestamp">
              <q-item-section>
                <q-item-label class="text-caption">{{ getDateLabel(message.timestamp) }}</q-item-label>
              </q-item-section>
              <q-item-section>
                <q-item-label>
                  <div v-html="message.msg.replace('\n', '<br/>')">
                  </div>
                </q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-card-section>
      </q-card>
    </q-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'CommandStates',
});
</script>
<script setup lang="ts">
import { CommandStateDto, CommandStateDto_Status } from 'src/models/Commands';
import { commandStatusColor } from 'src/utils/colors';
import { getDateLabel } from 'src/utils/dates';

interface CommandStatesProps {
  commands: CommandStateDto[];
  project?: string | undefined;
}

const props = defineProps<CommandStatesProps>();
const emit = defineEmits(['refresh', 'clear'])

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
      align: 'left',
      field: 'id',
      format: (val: string) => val,
      sortable: true,
    },
    {
      name: 'name',
      required: true,
      label: t('name'),
      align: 'left',
      field: 'name',
      sortable: true,
    },
    {
      name: 'owner',
      required: true,
      label: t('owner'),
      align: 'left',
      field: 'owner',
      sortable: true,
    },
    {
      name: 'startTime',
      required: true,
      label: t('start_time'),
      align: 'left',
      field: 'startTime',
      format: (val: string) => getDateLabel(val),
      sortable: true,
    },
    {
      name: 'endTime',
      required: true,
      label: t('end_time'),
      align: 'left',
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
      align: 'left',
      field: 'project',
      format: (val: string) => val,
      sortable: true,
    });
  }
  cols.push({
      name: 'status',
      required: false,
      label: t('status'),
      align: 'left',
      field: 'status',
      sortable: true,
    });
  return cols;
});

const rows = computed(() => props.commands ? props.commands : []);

watch(() => props.commands, (value) => {
  loading.value = false;
});

function onRowClick(evt: unknown, row: CommandStateDto) {
  console.log(row);
}

function onRefresh() {
  loading.value = true;
  emit('refresh');
}

function onClear() {
  loading.value = true;
  emit('clear');
}

function onOverCommand(row: CommandStateDto) {
  toolsVisible.value[row.id] = true;
}

function onLeaveCommand(row: CommandStateDto) {
  toolsVisible.value[row.id] = false;
}

function onShowMessages(row: CommandStateDto) {
  showMessages.value = !showMessages.value;
  selected.value = row;
}
</script>
