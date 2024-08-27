<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ $t('vcf_store.import_vcf_file') }}</div>
      </q-card-section>

      <q-separator />
      <q-card-section style="max-height: 75vh" class="scroll">
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <file-select
            v-model="importData"
            :label="$t('vcf_store.export_vcf_file_label')"
            :folder="filesStore.current"
            selection="single"
            type="folder"
            @select="onExportFolderSelected"
          >
            <template v-slot:error>
              <div v-if="folderError" class="text-negative text-caption">{{ folderError }}</div>
            </template>
          </file-select>

          <q-card-section>
            <pre>{{  selectedTable }}</pre>
            <q-select
              v-model="selectedTable"
              :options="filterOptions"
              :label="$t('table')"
              :hint="$t('vcf_store.mapping_table_hint')"
              dense
              map-options
              use-chips
              use-input
              input-debounce="0"
              @filter="onFilterFn"
            >
              <template v-slot:option="scope">
                <q-item v-show="!!!scope.opt.value" class="text-help" dense clickable disable :label="scope.opt.label">
                  <q-item-section class="q-pa-none">
                    {{ scope.opt.label }}
                  </q-item-section>
                </q-item>
                <q-item v-show="!!scope.opt.value" dense clickable v-close-popup @click="selectedTable = scope.opt.value">
                  <q-item-section class="q-pl-md">
                    {{ scope.opt.label }}
                  </q-item-section>
                </q-item>
              </template>
            </q-select>
          </q-card-section>
        </q-form>
      </q-card-section>
      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
        <q-btn
          flat
          :label="$t('import')"
          type="submit"
          color="primary"
          :disable="importFiles.length == 0"
          @click="onImport"
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'ExportVcfFileDialog',
});
</script>

<script setup lang="ts">
import { ProjectDto } from 'src/models/Projects';
import { notifyError, notifySuccess } from 'src/utils/notify';
import FileSelect from 'src/components/files/FileSelect.vue';
import { FileDto, SubjectProfileDto } from 'src/models/Opal';
import { TableDto } from 'src/models/Magma';
import { VCFSummaryDto } from 'src/models/Plugins';

interface DialogProps {
  modelValue: boolean;
  project: ProjectDto;
  vcfs: VCFSummaryDto[];
}

// type GroupOption = { group: string } | { label: string; value: TableDto };
type GroupOption = { label: string; value: TableDto | undefined };

const projectsStore = useProjectsStore();
const datasourceStore = useDatasourceStore();
const profilesStore = useProfilesStore();
const filesStore = useFilesStore();
const profile = computed(() => profilesStore.profile || ({} as SubjectProfileDto));
const { t } = useI18n();
const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const formRef = ref();
const emit = defineEmits(['update:modelValue']);
const importData = ref({} as FileDto);
const importFiles = ref<string[]>([]);
const folderError = ref('');
const selectedTable = ref<TableDto | null>(null);
let participantsOptions = [] as GroupOption[];
const filterOptions = ref([] as GroupOption[]);

function initMappingOptions(tables: TableDto[]) {
  if (tables.length > 0) {
    let lastGroup = '';
    tables.forEach((table) => {
      if (!!table.datasourceName && table.datasourceName !== lastGroup) {
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
      datasourceStore.getAllTables('Participant').then((response) => {
        initMappingOptions(response);
      });
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
      filterOptions.value = [...participantsOptions.filter((v: GroupOption) => 'label' in v && v.label.toLowerCase().indexOf(needle) > -1)];
    }
  });
}

function onHide() {
  showDialog.value = false;
  folderError.value = '';
  importData.value = {} as FileDto;
  importFiles.value = [];
  filterOptions.value = [];
  participantsOptions = [];
  selectedTable.value = null;
  emit('update:modelValue', false);
}

async function onExportFolderSelected(files: FileDto[]) {
  importFiles.value = (files || []).map((file) => file.path);
}



async function onImport() {
  try {
    // const taskId = await projectsStore.exportVcfFiles(props.project.name, importFiles.value);
    // notifySuccess(t('vcf_store.import_vcf_command_created', { id: taskId }));
    onHide();
  } catch (error) {
    notifyError(error);
  }
}

onMounted(() =>
  profilesStore.initProfile().then(() =>
    filesStore.initFiles(`/home/${profile.value.principal}`).then(() => {
      importData.value = filesStore.current;
    })
  )
);
</script>
