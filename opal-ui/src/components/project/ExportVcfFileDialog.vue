<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ t('vcf_store.export_vcf_file') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section style="max-height: 75vh" class="scroll">
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-banner rounded class="bg-primary text-white">{{
            t('vcf_store.export_vcf_file_label', { count: vcfs.length })
          }}</q-banner>

          <file-select
            v-model="exportData"
            :folder="filesStore.current"
            selection="single"
            type="folder"
            @select="onExportFolderSelected"
          >
            <template v-slot:error>
              <div v-if="folderError" class="text-negative text-caption">{{ folderError }}</div>
            </template>
          </file-select>

          <template v-if="showMapping">
            <q-select
              v-model="selectedTable"
              :options="filterOptions"
              :label="t('vcf_store.export_participants_filter_label')"
              :hint="t('vcf_store.export_mapping_table_hint')"
              dense
              map-options
              use-chips
              use-input
              input-debounce="0"
              @filter="onFilterFn"
            >
              <template v-slot:option="scope">
                <q-item v-show="!scope.opt.value" class="text-help" dense clickable disable :label="scope.opt.label">
                  <q-item-section class="q-pa-none">
                    {{ scope.opt.label }}
                  </q-item-section>
                </q-item>
                <q-item
                  v-show="scope.opt.value"
                  dense
                  clickable
                  v-close-popup
                  @click="selectedTable = scope.opt.value"
                >
                  <q-item-section class="q-pl-md">
                    {{ scope.opt.label }}
                  </q-item-section>
                </q-item>
              </template>
            </q-select>

            <q-select
              v-model="exportOptions.participantIdentifiersMapping"
              :disable="!selectedTable?.name"
              :options="idMappings"
              :label="t('vcf_store.export_id_mappings_label')"
              :hint="t('vcf_store.export_id_mappings_hint')"
              dense
              map-options
              use-input
            >
            </q-select>

            <q-checkbox v-model="exportOptions.caseControl" :label="t('vcf_store.export_case_control_label')" />
          </template>
        </q-form>
      </q-card-section>
      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('export')" type="submit" color="primary" :disable="!canExport" @click="onExport" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { ProjectDto } from 'src/models/Projects';
import { notifyError, notifySuccess } from 'src/utils/notify';
import FileSelect from 'src/components/files/FileSelect.vue';
import type { FileDto, SubjectProfileDto } from 'src/models/Opal';
import type { TableDto } from 'src/models/Magma';
import type { VCFSummaryDto } from 'src/models/Plugins';
import type { ExportVCFCommandOptionsDto } from 'src/models/Commands';
import type { IdentifiersMappingDto } from 'src/models/Identifiers';

interface DialogProps {
  modelValue: boolean;
  project: ProjectDto;
  vcfs: VCFSummaryDto[];
  showMapping: boolean;
}

type GroupOption = { label: string; value: TableDto | undefined };

const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();
const projectsStore = useProjectsStore();
const identifiersStore = useIdentifiersStore();
const datasourceStore = useDatasourceStore();
const profilesStore = useProfilesStore();
const filesStore = useFilesStore();

const props = defineProps<DialogProps>();

const showDialog = ref(props.modelValue);
const formRef = ref();
const exportData = ref({} as FileDto);
const idMappings = ref<string[]>([]);
const folderError = ref('');
const selectedTable = ref<TableDto | null>(null);
const filterOptions = ref([] as GroupOption[]);
const exportOptions = ref({} as ExportVCFCommandOptionsDto);
let participantsOptions = [] as GroupOption[];
const profile = computed(() => profilesStore.profile || ({} as SubjectProfileDto));
const canExport = computed(() => exportData.value.path !== '' && exportData.value.path !== undefined);

function initMappingOptions(tables: TableDto[]) {
  if (tables.length > 0) {
    let lastGroup = '';
    tables.forEach((table) => {
      if (table.datasourceName && table.datasourceName !== lastGroup) {
        lastGroup = table.datasourceName;
        participantsOptions.push({ label: lastGroup } as GroupOption);
      }
      participantsOptions.push({ label: table.name, value: table } as GroupOption);
    });

    filterOptions.value = [...participantsOptions];
  }
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      exportOptions.value = {
        names: props.vcfs.map((vcf) => vcf.name),
        project: props.project.name,
        destination: '',
        caseControl: true,
      } as ExportVCFCommandOptionsDto;

      if (props.showMapping) {
        datasourceStore.getAllTables('Participant').then((response) => initMappingOptions(response));
        identifiersStore
          .getMappings()
          .then((mappings: IdentifiersMappingDto[]) => (idMappings.value = mappings.map((m) => m.name)))
          .catch((error) => console.error(error));
      }
      showDialog.value = value;
    }
  }
);

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function onFilterFn(val: string, update: any) {
  update(() => {
    if (val.trim().length === 0) {
      filterOptions.value = [...participantsOptions];
    } else {
      const needle = val.toLowerCase();
      filterOptions.value = [
        ...participantsOptions.filter((v: GroupOption) => 'label' in v && v.label.toLowerCase().indexOf(needle) > -1),
      ];
    }
  });
}

function onHide() {
  showDialog.value = false;
  folderError.value = '';
  exportData.value = {} as FileDto;
  filterOptions.value = [];
  participantsOptions = [];
  selectedTable.value = null;
  emit('update:modelValue', false);
}

async function onExportFolderSelected(folder: FileDto) {
  exportOptions.value.destination = folder.path;
}

async function onExport() {
  try {
    exportOptions.value.destination = exportData.value.path;
    if (selectedTable.value?.name)
      exportOptions.value.table = `${selectedTable.value.datasourceName}.${selectedTable.value.name}`;
    const taskId = await projectsStore.exportVcfFiles(props.project.name, exportOptions.value);
    notifySuccess(t('vcf_store.export_vcf_command_created', { id: taskId }));
    onHide();
  } catch (error) {
    notifyError(error);
  }
}

onMounted(() =>
  profilesStore.initProfile().then(() =>
    filesStore.initFiles(`/home/${profile.value.principal}`).then(() => {
      exportData.value = filesStore.current;
    })
  )
);
</script>
