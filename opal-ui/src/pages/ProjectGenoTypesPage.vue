<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="name" :to="`/project/${name}`" />
        <q-breadcrumbs-el :label="$t('project_genotypes.title')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        {{ $t('project_genotypes.title') }}
      </div>

      <q-tabs
        v-model="tab"
        dense
        class="text-grey q-mt-md"
        active-color="primary"
        indicator-color="primary"
        align="justify"
      >
        <q-tab name="vcf" :label="$t('vcf')" />
        <q-tab name="permissions" :label="$t('permissions')" />
      </q-tabs>

      <q-separator />

      <q-tab-panels v-model="tab">
        <q-tab-panel name="vcf">
          <q-card flat v-if="projectsStore.perms.vcfstore?.canRead()">
            <q-card-section class="q-px-none">
              <div data-v-3ead6d89="" class="text-h6">
                {{ $t('summary') }}
              </div>

              <fields-list class="col-6" :items="summaryProperties" :dbobject="summary" />
            </q-card-section>
          </q-card>

          <q-card flat v-if="projectsStore.perms.samples?.canRead()">
            <q-card-section class="q-px-none">
              <div class="text-h6">
                <span>{{ $t('vcf_store.sample_participants_mapping') }}</span>
                <q-btn outline color="primary" icon="edit" size="sm" class="on-right" @click="onAddMapping"></q-btn>
                <q-btn
                  outline
                  color="negative"
                  icon="delete"
                  size="sm"
                  class="on-right"
                  @click="onDeleteMapping"
                ></q-btn>
              </div>
              <fields-list class="col-6" :items="sampleMappingProperties" :dbobject="samplesMapping" />
            </q-card-section>
          </q-card>

          <q-card flat v-if="projectsStore.perms.samples?.canRead()">
            <q-card-section class="q-px-none">
              <div class="text-h6">{{ $t('vcf_store.vcf_files') }}</div>
              <q-table
                flat
                :filter="filter"
                :filter-method="onFilter"
                :rows="vcfs"
                :columns="columns"
                :visible-columns="visibleColumns"
                :hide-pagination="vcfs.length <= initialPagination.rowsPerPage"
                row-key="name"
                :pagination="initialPagination"
                :loading="loading"
              >
                <template v-slot:top-left>
                  <q-btn size="sm" icon="cached" color="primary" :label="$t('reload')" @click="onRefreshVcfs"></q-btn>
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
                    <span class="text-primary">{{ props.value }}</span>
                    <div class="float-right">
                      <q-btn
                        rounded
                        dense
                        flat
                        size="sm"
                        color="secondary"
                        :title="$t('statistics')"
                        :icon="toolsVisible[props.row.name] ? 'download' : 'none'"
                        class="q-ml-xs"
                        @click="onGetStats()"
                      />
                      <q-btn
                        rounded
                        dense
                        flat
                        size="sm"
                        color="secondary"
                        :title="$t('statistics')"
                        :icon="toolsVisible[props.row.name] ? 'delete' : 'none'"
                        class="q-ml-xs"
                        @click="onDeleteVcf(props.row)"
                      />
                    </div>
                  </q-td>
                </template>
              </q-table>
            </q-card-section>
          </q-card>
        </q-tab-panel>

        <q-tab-panel name="permissions">
          <div class="text-h6">{{ $t('permissions') }}</div>
          <access-control-list
            :resource="`/project/${name}/permissions/vcf-store`"
            :options="['VCF_STORE_VIEW', 'VCF_STORE_VALUES', 'DATASOURCE_ALL']"
          />
        </q-tab-panel>
      </q-tab-panels>

      <confirm-dialog v-model="showDelete" :title="$t('delete')" :text="confirmText" @confirm="onConfirmed" />

      <add-vcf-mapping-table-dialog
        v-model="showMapping"
        :mapping="samplesMapping"
        :project="project.name || ''"
        @update="onMappingAdded"
      />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import { notifyError } from 'src/utils/notify';
import AccessControlList from 'src/components/permissions/AccessControlList.vue';
import { VCFStoreDto, VCFSummaryDto, VCFSamplesMappingDto } from 'src/models/Plugins';
import FieldsList, { FieldItem } from 'src/components/FieldsList.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import AddVcfMappingTableDialog from 'src/components/project/AddVcfMappingTableDialog.vue';
import { getSizeLabel } from 'src/utils/files';
import { baseUrl } from 'src/boot/api';

const { t } = useI18n();
const route = useRoute();
const projectsStore = useProjectsStore();
const tab = ref('vcf');
const showMapping = ref(false);
const showDelete = ref(false);
const loading = ref(false);
const confirmText = ref('');
const onConfirmed = ref(() => ({}));
const name = computed(() => route.params.id as string);
const summary = ref({} as VCFStoreDto);
const samplesMapping = ref({} as VCFSamplesMappingDto);
const vcfs = ref([] as VCFSummaryDto[]);
const filter = ref('');
const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});
const toolsVisible = ref<{ [key: string]: boolean }>({});
const summaryProperties: FieldItem<VCFStoreDto>[] = [
  {
    field: 'totalSamplesCount',
    label: 'samples',
  },
  {
    field: 'participantsCount',
    label: 'participants',
  },
  {
    field: 'identifiedSamplesCount',
    label: 'vcf_store.identified_samples',
  },
  {
    field: 'controlSamplesCount',
    label: 'vcf_store.controls',
  },
];

const sampleMappingProperties: FieldItem<VCFSamplesMappingDto>[] = [
  {
    field: 'tableReference',
    label: 'table',
  },
  {
    field: 'participantIdVariable',
    label: 'participant_id',
  },
  {
    field: 'sampleRoleVariable',
    label: 'role',
  },
];

const project = computed(() => projectsStore.project);

const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'name',
    format: (val: string, row: VCFSummaryDto) => `${val}.${row.format.toLowerCase()}.gz`,
    sortable: true,
    style: 'width: 25%',
  },
  {
    name: 'samples',
    label: t('samples'),
    align: 'left',
    field: 'totalSamplesCount',
  },
  {
    name: 'participants',
    label: t('participants'),
    align: 'left',
    field: 'participantsCount',
    required: false,
  },
  {
    name: 'identified',
    label: t('vcf_store.identified_samples'),
    align: 'left',
    field: 'identifiedSamplesCount',
  },
  {
    name: 'controls',
    label: t('vcf_store.controls'),
    align: 'left',
    field: 'controlSamplesCount',
  },
  {
    name: 'vcf_store.variants',
    label: t('vcf_store.variants'),
    align: 'left',
    field: 'variantsCount',
  },
  {
    name: 'genotypes',
    label: t('vcf_store.genotypes'),
    align: 'center  ',
    field: 'genotypesCount',
    format: (val: number) => `${getSizeLabel(val)}`,
  },
  {
    name: 'size',
    label: t('size'),
    align: 'center  ',
    field: 'size',
    format: (val: number) => `${getSizeLabel(val)}`,
  },
]);

const visibleColumns = ref(columns.value.map((c) => c.name));

function updateVisibleColumns() {
  if (!!!samplesMapping.value.projectName) {
    visibleColumns.value = columns.value
      .filter((c) => !['participants', 'identified', 'controls'].includes(c.name))
      .map((c) => c.name);
  } else {
    visibleColumns.value = columns.value.map((c) => c.name);
  }
}

async function getVcfSummary() {
  if (projectsStore.perms.vcfstore?.canRead()) {
    summary.value = {} as VCFStoreDto;
    projectsStore.getVcfStore(project.value.name).then((result) => (summary.value = result));
  }
}

async function getVcfSamplesMapping() {
  if (projectsStore.perms.samples?.canRead()) {
    samplesMapping.value = {} as VCFSamplesMappingDto;
    projectsStore
      .getVcfSamplesMapping(project.value.name)
      .then((result) => {
        samplesMapping.value = result;
      })
      .finally(() => {
        updateVisibleColumns();
      });
  }
}

async function getVcfs() {
  if (projectsStore.perms.vcfs?.canRead()) {
    vcfs.value = [];
    loading.value = true;
    return projectsStore.getVcfs(project.value.name).then((result) => {
      vcfs.value = result;
      loading.value = false;
    });
  }

  Promise.resolve();
}

async function initialize() {
  projectsStore
    .initProject(name.value)
    .then(() => {
      projectsStore
        .loadVcfPermissions(project.value.name)
        .then(() => {
          getVcfSummary();
          getVcfSamplesMapping();
          getVcfs();
        })
        .catch(notifyError);
    })
    .catch(notifyError);
}

async function _onDeleteMapping() {
  try {
    showDelete.value = false;
    await projectsStore.deleteVcfSamplesMapping(project.value.name);
    confirmText.value = '';
    onConfirmed.value = () => ({});
    initialize();
  } catch (error) {
    notifyError(error);
  }
}

async function _onDeleteVcf(vcf: VCFSummaryDto) {
  try {
    showDelete.value = false;
    await projectsStore.deleteVcf(project.value.name, vcf.name);
    confirmText.value = '';
    onConfirmed.value = () => ({});
    initialize();
  } catch (error) {
    notifyError(error);
  }
}

// Handlers

function onOverRow(row: VCFSummaryDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: VCFSummaryDto) {
  toolsVisible.value[row.name] = false;
}

function onAddMapping() {
  showMapping.value = true;
}

function onDeleteMapping() {
  confirmText.value = t('delete_vcf_mapping_confirm');
  onConfirmed.value = async () => await _onDeleteMapping();
  showDelete.value = true;
}

function onDeleteVcf(row: VCFSummaryDto) {
  confirmText.value = t('delete_vcf_confirm');
  onConfirmed.value = async () => await _onDeleteVcf(row);
  showDelete.value = true;
}

function onMappingAdded() {
  showMapping.value = false;
  initialize();
}

async function onRefreshVcfs() {
  await getVcfs();
  updateVisibleColumns();
}

function onFilter() {
  if (filter.value.length === 0) {
    return vcfs.value;
  }
  const query = !!filter.value && filter.value.length > 0 ? filter.value.toLowerCase() : '';
  const result = vcfs.value.filter((row) => {
    return Object.values(row).some((val) => {
      return String(val).toLowerCase().includes(query);
    });
  });

  return result;
}

async function onGetStats() {
  window.open(`${baseUrl}/project/${projectsStore.project.name}/vcf-store/vcf/plate/_statistics`);
}

onMounted(() => {
  initialize();
});
</script>
