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
                <q-btn outline color="negative" icon="delete" size="sm" class="on-right" @click="onDeleteMapping"></q-btn>
              </div>
              <fields-list class="col-6" :items="sampleMappingProperties" :dbobject="samplesMapping" />
            </q-card-section>
          </q-card>
        </q-tab-panel>

        <q-tab-panel name="permissions">
          <span>PERMISSIONS</span>
        </q-tab-panel>
      </q-tab-panels>

      <confirm-dialog
          v-model="showDelete"
          :title="$t('delete')"
          :text=confirmText
          @confirm="onConfirmed"
        />

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
import { VCFSummaryDto, VCFSamplesMappingDto } from 'src/models/Plugins';
import FieldsList, { FieldItem } from 'src/components/FieldsList.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import AddVcfMappingTableDialog from 'src/components/project/AddVcfMappingTableDialog.vue';

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
const summary = ref({} as VCFSummaryDto);
const samplesMapping = ref({} as VCFSamplesMappingDto);
const summaryProperties: FieldItem<VCFSummaryDto>[] = [
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

async function getVcfSummary() {
  if (projectsStore.perms.vcfstore?.canRead()) {
    summary.value = {} as VCFSummaryDto;
    projectsStore.getVcfStore(project.value.name).then((result) => (summary.value = result));
  }
}

async function getVcfSamplesMapping() {
  if (projectsStore.perms.samples?.canRead()) {
    samplesMapping.value = {} as VCFSamplesMappingDto;
    projectsStore.getVcfSamplesMapping(project.value.name).then((result) => {
      console.log(result);
      samplesMapping.value = result;
    });
  }
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
        })
        .catch(notifyError);
    })
    .catch(notifyError);
}

// Handlers

function onAddMapping() {
  showMapping.value = true;
}

function onDeleteMapping() {
  confirmText.value = t('delete_vcf_mapping_confirm');
  onConfirmed.value = async () => await _onDeleteMapping();
  showDelete.value = true;
}

async function _onDeleteMapping() {
  try {
    showDelete.value = false;
    await projectsStore.deleteVcfSamplesMapping(project.value.name)
    confirmText.value = '';
    onConfirmed.value = () => ({});
    initialize();
  } catch (error) {
    notifyError(error);
  }
}

function onMappingAdded() {
  showMapping.value = false;
  initialize();
}

onMounted(() => {
  initialize();
});
</script>
