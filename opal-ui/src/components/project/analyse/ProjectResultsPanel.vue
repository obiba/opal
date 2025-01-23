<template>
  <q-card flat>
    <q-card-section class="q-px-none">
      <fields-list :items="properties" :dbobject="firstResult" />

      <div class="q-py-md">
        <q-list>
          <q-expansion-item
            dense
            switch-toggle-side
            header-class="text-primary text-caption q-pl-none"
            :default-opened="details.length < 6"
            :label="t('details')"
          >
            <q-table
              flat
              :rows="details"
              :columns="detailsColumns"
              :pagination="initialDetailsPagination"
              :hide-pagination="details.length <= initialDetailsPagination.rowsPerPage"
              row-key="status"
              dense
              class="q-mt-sm"
            >
              <template v-slot:body-cell-message="props">
                <q-td :props="props">
                  {{ props.value }}
                </q-td>
              </template>
              <template v-slot:body-cell-status="props">
                <q-td :props="props">
                  <q-icon
                    name="circle"
                    size="sm"
                    :color="analysisColor(props.value)"
                    :title="t(`analysis_status.${props.value}`)"
                  />
                </q-td>
              </template>
            </q-table>
          </q-expansion-item>
        </q-list>
      </div>

      <div v-if="history.length > 0">
        <q-list>
          <q-expansion-item
            dense
            switch-toggle-side
            header-class="text-primary text-caption q-pl-none"
            :default-opened="history.length < 6"
            :label="t('history')"
          >
            <q-table
              flat
              :rows="history"
              :columns="historyColumns"
              row-key="status"
              :pagination="initialHistoryPagination"
              :hide-pagination="history.length <= initialHistoryPagination.rowsPerPage"
            >
              <template v-slot:body-cell-status="props">
                <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
                  <q-icon
                    name="circle"
                    size="sm"
                    :color="analysisColor(props.value)"
                    :title="t(`analysis_status.${props.value}`)"
                  />
                  <div class="float-right">
                    <q-btn
                      rounded
                      dense
                      flat
                      size="sm"
                      color="secondary"
                      :title="t('report')"
                      :icon="toolsVisible[props.row.status] ? 'insert_chart' : 'none'"
                      class="q-ml-xs"
                      @click="onViewReport(props.row)"
                    />
                    <q-btn
                      rounded
                      dense
                      flat
                      size="sm"
                      color="secondary"
                      :title="t('remove')"
                      :icon="toolsVisible[props.row.status] ? 'delete' : 'none'"
                      class="q-ml-xs"
                      @click="onRemoveHistory(props.row)"
                    />
                  </div>
                </q-td>
              </template>
              <template v-slot:body-cell-date="props">
                <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
                  {{ props.value }}
                </q-td>
              </template>
            </q-table>
          </q-expansion-item>
        </q-list>
      </div>
    </q-card-section>
  </q-card>
</template>

<script setup lang="ts">
import FieldsList, { type FieldItem } from 'src/components/FieldsList.vue';
import { type OpalAnalysisResultDto, AnalysisStatusDto } from 'src/models/Projects';
import { getDateLabel } from 'src/utils/dates';
import { analysisColor } from 'src/utils/colors';
import { notifyError } from 'src/utils/notify';
import { DefaultAlignment } from 'src/components/models';

interface Props {
  projectName: string;
  tableName: string;
  analysisName: string;
  results: OpalAnalysisResultDto[];
}

const props = defineProps<Props>();
const projectsStore = useProjectsStore();
const { t } = useI18n();

const firstResult = props.results[0];
const details = firstResult?.resultItems || [];
const history = ref(props.results.slice(1));
const initialDetailsPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 5,
  minRowsForPagination: 5,
});
const initialHistoryPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 5,
  minRowsForPagination: 5,
});
const toolsVisible = ref<{ [key: string]: boolean }>({});

const properties = computed<FieldItem[]>(() => [
  {
    field: 'status',
    label: 'status',
    html: (val) =>
      `<span><i class="q-icon text-h5 text-${analysisColor(
        val.status
      )} material-icons" aria-hidden="true" role="presentation">circle</i> ${t(
        'analysis_status.' + val.status
      )}</span>`,
  },
  {
    field: 'startDate',
    label: 'start',
    format: (val) => getDateLabel(val.startDate),
  },
  {
    field: 'endDate',
    label: 'end',
    format: (val) => getDateLabel(val.endDate),
  },
  {
    field: 'message',
    label: 'message',
  },
  {
    field: 'report',
    label: 'report',
    html: (val) =>
      `<a class="" href="${projectsStore.getAnalysisReportUrl(
        props.projectName,
        props.tableName,
        props.analysisName,
        val.id
      )}" target="_blank" >${t('view')}</a>`,
  },
]);

const detailsColumns = computed(() => [
  {
    name: 'status',
    label: t('status'),
    align: DefaultAlignment,
    field: 'status',
  },
  {
    name: 'message',
    label: t('message'),
    align: DefaultAlignment,
    field: 'message',
  },
]);

const historyColumns = computed(() => [
  {
    name: 'status',
    label: t('status'),
    align: DefaultAlignment,
    field: (row: OpalAnalysisResultDto) => row.status || AnalysisStatusDto.ERROR,
  },
  {
    name: 'date',
    label: t('date'),
    field: 'startDate',
    align: DefaultAlignment,
    format: (val: string) => getDateLabel(val),
    headerStyle: 'width: 65%; white-space: normal;',
    style: 'width: 65%; white-space: normal;',
  },
]);

function onOverRow(row: OpalAnalysisResultDto) {
  toolsVisible.value[row.status] = true;
}

function onLeaveRow(row: OpalAnalysisResultDto) {
  toolsVisible.value[row.status] = false;
}

function onViewReport(row: OpalAnalysisResultDto) {
  window.open(projectsStore.getAnalysisReportUrl(props.projectName, props.tableName, props.analysisName, row.id));
}

async function onRemoveHistory(row: OpalAnalysisResultDto) {
  try {
    await projectsStore.removeAnalysisResult(props.projectName, props.tableName, props.analysisName, row.id);
    history.value = history.value.filter((r) => r.id !== row.id);
  } catch (error) {
    notifyError(error);
  }
}
</script>
