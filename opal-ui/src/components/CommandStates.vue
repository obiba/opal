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
      <template v-slot:body-cell-id="props">
        <q-td :props="props">
          <span class="text-primary">{{ props.value }}</span>
        </q-td>
      </template>
      <template v-slot:body-cell-name="props">
        <q-td :props="props">
          <q-badge :label="props.value"/>
        </q-td>
      </template>
      <template v-slot:body-cell-owner="props">
        <q-td :props="props">
          <span class="text-caption">{{ props.value }}</span>
        </q-td>
      </template>
      <template v-slot:body-cell-progress="props">
        <q-td :props="props">
          <div v-if="props.value">
            <div class="text-caption">
              <span>
                {{ props.value.message }}
              </span>
              <span class="on-right  text-help">
                {{ `${props.value.percent}% (${props.value.current}/${props.value.end})` }}
              </span>
            </div>
            <q-linear-progress :value="props.value.percent" />
          </div>
        </q-td>
      </template>
      <template v-slot:body-cell-status="props">
        <q-td :props="props">
          <q-icon
            name="circle"
            size="sm"
            :color="commandStatusColor(props.value)"
            :title="props.value"
          />
        </q-td>
      </template>
    </q-table>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'CommandStates',
});
</script>
<script setup lang="ts">
import { CommandState } from 'src/components/models';
import { commandStatusColor } from 'src/utils/colors';
import { getDateLabel } from 'src/utils/dates';

interface CommandStatesProps {
  commands: CommandState[];
  project: string | undefined;
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
      name: 'progress',
      required: true,
      label: t('progress'),
      align: 'left',
      field: 'progress',
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
    cols.push({
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

function onRowClick(evt: unknown, row: CommandState) {
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
</script>
