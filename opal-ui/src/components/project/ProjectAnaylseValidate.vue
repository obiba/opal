<template>
  <div class="text-help">{{ $t('analyse_validate.info') }}</div>
  <q-table
    flat
    :filter="filter"
    :filter-method="onFilter"
    :rows="analyses"
    :columns="columns"
    row-key="name"
    :pagination="initialPagination"
    :hide-pagination="analyses.length <= initialPagination.rowsPerPage"
    :loading="loading"
  >
    <template v-slot:top-left>
      <div class="q-gutter-sm">
        <q-btn
          no-caps
          color="primary"
          icon="add"
          size="sm"
          :label="$t('analyse_validate.add')"
          @click="onAddAnalysis"
        />
        <q-btn no-caps color="secondary" icon="refresh" size="sm" :label="$t('refresh')" @click="onRefresh" />
      </div>
    </template>
    <template v-slot:top-right>
      <q-input dense clearable debounce="400" color="primary" v-model="filter">
        <template v-slot:append>
          <q-icon name="search" />
        </template>
      </q-input>
    </template>
    <template v-slot:body-cell-name="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        {{ props.value }}
        <div class="float-right">
          <q-btn
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="$t('run')"
            :icon="toolsVisible[props.row.name] ? 'play_arrow' : 'none'"
            class="q-ml-xs"
            @click="onRunAnalysis(props.row)"
          />
          <q-btn
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="$t('view')"
            :icon="toolsVisible[props.row.name] ? 'visibility' : 'none'"
            class="q-ml-xs"
            @click="onViewAnalysis(props.row)"
          />
          <q-btn
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="$t('duplicate')"
            :icon="toolsVisible[props.row.name] ? 'content_copy' : 'none'"
            class="q-ml-xs"
          />
          <q-btn
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="$t('delete')"
            :icon="toolsVisible[props.row.name] ? 'delete' : 'none'"
            class="q-ml-xs"
            @click="onRemoveAnalysis(props.row)"
          />
        </div>
      </q-td>
    </template>
    <template v-slot:body-cell-type="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        {{ props.value }}
      </q-td>
    </template>
    <template v-slot:body-cell-total="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        {{ props.value }}
      </q-td>
    </template>
    <template v-slot:body-cell-status="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <q-icon
          name="circle"
          size="sm"
          :color="analysisColor(props.value)"
          :title="$t(`analysis_status.${props.value}`)"
        />
      </q-td>
    </template>
    <template v-slot:body-cell-date="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        {{ props.value }}
      </q-td>
    </template>
  </q-table>

  <confirm-dialog
    v-model="showRemove"
    :title="$t('remove')"
    :text="$t('analyse_validate.delete_analysis_confirm', { name: selectedAnalysis?.name })"
    @confirm="doRemoveAnalysis"
  />

  <add-project-analysis-dialog v-model="showDialog" :project-name="projectName" :table-name="tableName" />
</template>

<script lang="ts">
export default defineComponent({
  name: 'ProjectAnaylseValidate',
});
</script>

<script setup lang="ts">
import AddProjectAnalysisDialog from './AddProjectAnalysisDialog.vue';
import { OpalAnalysisDto, OpalAnalysisResultDto, AnalysisStatusDto } from 'src/models/Projects';
import { AnalyseCommandOptionsDto, AnalyseCommandOptionsDto_AnalyseDto } from 'src/models/Commands';
import { PluginPackageDto } from 'src/models/Plugins';
import { analysisColor } from 'src/utils/colors';
import { getDateLabel } from 'src/utils/dates';
import { notifyError, notifySuccess } from 'src/utils/notify';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';

interface Props {
  projectName: string;
  tableName: string;
  analysisPackages: PluginPackageDto[]
}

interface TableRow extends OpalAnalysisDto {
  total: string;
}

const { t } = useI18n();
const projectsStore = useProjectsStore();
const props = defineProps<Props>();
const showRemove = ref(false);
const showDialog = ref(false);
const loading = ref(false);
const selectedAnalysis = ref<OpalAnalysisDto | null>(null);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});
let pluginsTrPrefix = 'plugins.';
const filter = ref('');
const analyses = ref<OpalAnalysisDto[]>([]);
const hasFilter = computed(() => (filter.value || '').length > 0);
const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'name',
    sortable: true,
    style: 'width: 25%',
  },
  {
    name: 'type',
    label: t('type'),
    align: 'left',
    field: 'templateName',
    format: (value: string) => t(`${pluginsTrPrefix}.${value}.title`),
  },
  {
    name: 'total',
    label: t('total'),
    align: 'left',
    field: (row: OpalAnalysisDto) => getSuccessCount(row.lastResult || {} as OpalAnalysisResultDto),
  },
  {
    name: 'status',
    label: t('status'),
    align: 'left',
    field: (row: OpalAnalysisDto) => (row.lastResult || {}).status || AnalysisStatusDto.ERROR,
  },
  {
    name: 'date',
    label: t('date'),
    align: 'left',
    field: (row: OpalAnalysisDto) => getDateLabel(row.lastResult ? row.lastResult.startDate : row.updated),
  },
]);

function getSuccessCount(dto: OpalAnalysisResultDto) {
  let successCount = 0;

  dto.resultItems.forEach((item) => {
    successCount += item.status === 'PASSED' ? 1 : 0;
  });

  return successCount + ' / ' + dto.resultItems.length;
}

// Handlers

function onOverRow(row: TableRow) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: TableRow) {
  toolsVisible.value[row.name] = false;
}

function onFilter(tableRows: OpalAnalysisDto[], filter: string) {
  if (filter.length === 0) {
    return tableRows;
  }
  const query = !!filter && filter.length > 0 ? filter.toLowerCase() : '';
  const result = tableRows.filter((row) => {
    const rowString = `${row.name.toLowerCase()}`;
    return rowString.includes(query);
  });

  return result;
}

async function onAddAnalysis() {
  showDialog.value = true;
}

async function onRunAnalysis(row: OpalAnalysisDto) {
  const analysis: AnalyseCommandOptionsDto_AnalyseDto = {
    name: row.name,
    plugin: row.pluginName,
    template: row.templateName,
    table: row.table,
    params: row.parameters,
  };

  if ((row.variables ?? []).length > 0) analysis.variables = row.variables.join(',');

  const commandOptions: AnalyseCommandOptionsDto = {
    project: props.projectName,
    analyses: [analysis],
  };

  projectsStore.runAnalysis(props.projectName, commandOptions).then((id) => {
    notifySuccess(t('analyse_validate.analyse_command_created', { id }));
  });
}

function onViewAnalysis(row: OpalAnalysisDto) {
  console.log('View analysis', row);
}

function onRemoveAnalysis(row: OpalAnalysisDto) {
  selectedAnalysis.value = row;
  showRemove.value = true;
}

async function doRemoveAnalysis() {
  try {
    await projectsStore.removeAnalysis(props.projectName, props.tableName, selectedAnalysis.value?.name || '');
    showRemove.value = false;
    return onRefresh();
  } catch (error) {
    notifyError(error);
  }
}

async function onRefresh() {
  loading.value = true;
  return projectsStore
    .getAnalyses(props.projectName, props.tableName)
    .then((response) => {
      analyses.value = (response || {}).analyses || [];
    })
    .catch((error) => {
      notifyError(error);
    })
    .finally(() => {
      loading.value = false;
    });
}

onMounted(() => {
  // NOTE: assume only one package
  pluginsTrPrefix = `plugins.${props.analysisPackages[0].name}`;
  onRefresh();
});
</script>
