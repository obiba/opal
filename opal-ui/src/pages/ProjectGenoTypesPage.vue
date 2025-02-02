<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="name" :to="`/project/${name}`" />
        <q-breadcrumbs-el :label="t('genotypes')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        {{ t('genotypes') }}
      </div>

      <q-tabs
        v-if="projectsStore.perms.permissions_vcfstore?.canRead()"
        v-model="tab"
        dense
        class="text-grey q-mt-md"
        active-color="primary"
        indicator-color="primary"
        align="justify"
      >
        <q-tab name="vcf" :label="t('vcf')" />
        <q-tab name="permissions" :label="t('permissions')" />
      </q-tabs>

      <q-separator />

      <q-tab-panels v-model="tab">
        <q-tab-panel name="vcf">
          <q-card flat v-if="projectsStore.perms.vcfstore?.canRead()">
            <q-card-section class="q-px-none">
              <div data-v-3ead6d89="" class="text-h6">
                {{ t('summary') }}
              </div>

              <fields-list class="col-6" :items="summaryProperties" :dbobject="summary" />
            </q-card-section>
          </q-card>

          <q-card flat v-if="projectsStore.perms.samples?.canRead()">
            <q-card-section class="q-px-none">
              <div class="text-h6">
                <span>{{ t('vcf_store.sample_participants_mapping') }}</span>
                <q-btn
                  v-if="canUpdateMapping"
                  outline
                  color="primary"
                  icon="edit"
                  size="sm"
                  class="on-right"
                  @click="onAddMapping"
                ></q-btn>
                <q-btn
                  v-if="canDeleteMapping"
                  outline
                  color="negative"
                  icon="delete"
                  size="sm"
                  class="on-right"
                  @click="onDeleteMapping"
                ></q-btn>
              </div>
              <div class="text-help q-mb-sm">{{ t('vcf_store.sample_participants_mapping_info') }}</div>
              <fields-list class="col-6" :items="sampleMappingProperties" :dbobject="samplesMapping" />
            </q-card-section>
          </q-card>

          <q-card flat v-if="projectsStore.perms.samples?.canRead()">
            <q-card-section class="q-px-none">
              <div class="text-h6">{{ t('vcf_store.vcf_files') }}</div>
              <q-table
                flat
                :filter="filter"
                :filter-method="onFilter"
                :rows="vcfs"
                :columns="columns"
                :visible-columns="visibleColumns"
                :hide-pagination="vcfs.length <= initialPagination.rowsPerPage"
                row-key="name"
                selection="multiple"
                v-model:selected="selectedVcfs"
                :pagination="initialPagination"
                :loading="loading"
              >
                <template v-slot:top-left>
                  <div class="q-gutter-sm">
                    <q-btn
                      outline
                      size="sm"
                      icon="refresh"
                      color="secondary"
                      :title="t('refresh')"
                      @click="onRefreshVcfs"
                    ></q-btn>
                    <q-btn
                      v-if="canImportVcfs"
                      size="sm"
                      icon="input"
                      color="secondary"
                      :label="t('import')"
                      @click="onImportVcfFile"
                    ></q-btn>
                    <q-btn
                      v-if="canExportVcfs"
                      size="sm"
                      icon="output"
                      color="secondary"
                      :label="t('export')"
                      :disable="selectedVcfs.length === 0"
                      @click="onExportVcfFile"
                    ></q-btn>
                    <q-btn
                      v-if="canDeleteVcf"
                      outline
                      icon="delete"
                      size="sm"
                      color="negative"
                      :title="t('delete')"
                      :disable="selectedVcfs.length === 0"
                      class="q-ml-xs"
                      @click="onDeleteVcf()"
                    />
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
                    <span class="text-primary">{{ props.value }}</span>
                    <div class="float-right">
                      <q-btn
                        rounded
                        dense
                        flat
                        size="sm"
                        color="secondary"
                        :title="t('statistics')"
                        :icon="toolsVisible[props.row.name] ? 'description' : 'none'"
                        class="q-ml-xs"
                        @click="onGetStats()"
                      />
                    </div>
                  </q-td>
                </template>
              </q-table>
            </q-card-section>
          </q-card>
        </q-tab-panel>

        <q-tab-panel name="permissions">
          <div class="text-h6 q-mb-md">{{ t('permissions') }}</div>
          <access-control-list
            :resource="`/project/${name}/permissions/vcf-store`"
            :options="['VCF_STORE_VIEW', 'VCF_STORE_VALUES', 'VCF_STORE_ALL']"
          />
        </q-tab-panel>
      </q-tab-panels>

      <confirm-dialog v-model="showDelete" :title="t('delete')" :text="confirmText" @confirm="onConfirmed" />

      <add-vcf-mapping-table-dialog
        v-model="showMapping"
        :mapping="samplesMapping"
        :project="project.name || ''"
        @update:modelValue="onMappingAdded"
      />

      <import-vcf-file-dialog v-model="showImport" :project="project" @update="onImportedVcfs" />

      <export-vcf-file-dialog
        v-model="showExport"
        :project="project"
        :vcfs="selectedVcfs"
        :show-mapping="samplesMapping.projectName !== undefined"
        @update:modelValue="onExportedVcfs"
      />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import { notifyError } from 'src/utils/notify';
import AccessControlList from 'src/components/permissions/AccessControlList.vue';
import type { VCFStoreDto, VCFSummaryDto, VCFSamplesMappingDto } from 'src/models/Plugins';
import FieldsList, { type FieldItem } from 'src/components/FieldsList.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import AddVcfMappingTableDialog from 'src/components/project/AddVcfMappingTableDialog.vue';
import ImportVcfFileDialog from 'src/components/project/ImportVcfFileDialog.vue';
import ExportVcfFileDialog from 'src/components/project/ExportVcfFileDialog.vue';
import { getSizeLabel } from 'src/utils/files';
import { baseUrl } from 'src/boot/api';
import { DefaultAlignment } from 'src/components/models';

const { t } = useI18n();
const route = useRoute();
const projectsStore = useProjectsStore();
const tab = ref('vcf');
const showMapping = ref(false);
const showImport = ref(false);
const showExport = ref(false);
const showDelete = ref(false);
const loading = ref(false);
const confirmText = ref('');
const onConfirmed = ref(() => ({}));
const summary = ref({} as VCFStoreDto);
const samplesMapping = ref({} as VCFSamplesMappingDto);
const vcfs = ref([] as VCFSummaryDto[]);
const filter = ref('');
const selectedVcfs = ref([] as VCFSummaryDto[]);
const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});
const toolsVisible = ref<{ [key: string]: boolean }>({});
const summaryProperties: FieldItem[] = [
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
const sampleMappingProperties: FieldItem[] = [
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
const name = computed(() => route.params.id as string);
const project = computed(() => projectsStore.project);
const canDeleteVcf = computed(() => projectsStore.perms.vcfs?.canDelete());
const canImportVcfs = computed(() => projectsStore.perms.import_vcf?.canCreate());
const canExportVcfs = computed(() => projectsStore.perms.export_vcf?.canCreate());
const canDeleteMapping = computed(() => projectsStore.perms.samples?.canDelete());
const canUpdateMapping = computed(() => projectsStore.perms.samples?.canUpdate());
const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: DefaultAlignment,
    field: 'name',
    format: (val: string, row: VCFSummaryDto) => `${val}.${row.format.toLowerCase()}.gz`,
    sortable: true,
    style: 'width: 25%',
  },
  {
    name: 'samples',
    label: t('samples'),
    align: DefaultAlignment,
    field: 'totalSamplesCount',
  },
  {
    name: 'participants',
    label: t('participants'),
    align: DefaultAlignment,
    field: 'participantsCount',
    required: false,
  },
  {
    name: 'identified',
    label: t('vcf_store.identified_samples'),
    align: DefaultAlignment,
    field: 'identifiedSamplesCount',
  },
  {
    name: 'controls',
    label: t('vcf_store.controls'),
    align: DefaultAlignment,
    field: 'controlSamplesCount',
  },
  {
    name: 'vcf_store.variants',
    label: t('vcf_store.variants'),
    align: DefaultAlignment,
    field: 'variantsCount',
  },
  {
    name: 'genotypes',
    label: t('vcf_store.genotypes'),
    align: DefaultAlignment,
    field: 'genotypesCount',
    format: (val: number) => `${getSizeLabel(val)}`,
  },
  {
    name: 'size',
    label: t('size'),
    align: DefaultAlignment,
    field: 'size',
    format: (val: number) => `${getSizeLabel(val)}`,
  },
]);

const visibleColumns = ref(columns.value.map((c) => c.name));

function updateVisibleColumns() {
  if (!samplesMapping.value.projectName) {
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
    return projectsStore.getVcfStore(project.value.name).then((result) => (summary.value = result));
  }

  return Promise.resolve();
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

async function _onDeleteVcf() {
  try {
    const toDelete: string[] = selectedVcfs.value.map((v) => v.name);
    selectedVcfs.value = [];
    showDelete.value = false;
    await projectsStore.deleteVcf(project.value.name, toDelete);
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

function onDeleteVcf() {
  confirmText.value = t('delete_vcf_confirm', {
    count: selectedVcfs.value.length,
    profile: selectedVcfs.value[0]?.name,
  });
  onConfirmed.value = async () => await _onDeleteVcf();
  showDelete.value = true;
}

function onMappingAdded() {
  showMapping.value = false;
  initialize();
}

async function onRefreshVcfs() {
  await getVcfSummary();
  await getVcfs();
  updateVisibleColumns();
}

function onImportVcfFile() {
  showImport.value = true;
}

function onExportVcfFile() {
  showExport.value = true;
}

function onFilter() {
  if (filter.value.length === 0) {
    return vcfs.value;
  }
  const query = filter.value && filter.value.length > 0 ? filter.value.toLowerCase() : '';
  const result = vcfs.value.filter((row) => {
    return Object.values(row).some((val) => {
      return String(val).toLowerCase().includes(query);
    });
  });

  return result;
}

function onImportedVcfs() {
  showImport.value = false;
}

function onExportedVcfs() {
  showExport.value = false;
  selectedVcfs.value = [];
}

async function onGetStats() {
  window.open(`${baseUrl}/project/${projectsStore.project.name}/vcf-store/vcf/plate/_statistics`);
}

onMounted(() => {
  initialize();
});
</script>
