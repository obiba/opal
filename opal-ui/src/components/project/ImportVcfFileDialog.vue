<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ t('vcf_store.import_vcf_file') }}</div>
      </q-card-section>

      <q-separator />
      <q-card-section style="max-height: 75vh" class="scroll">
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <file-select
            v-model="importData"
            :label="t('vcf_store.import_vcf_file_label')"
            :hint="t('vcf_store.import_vcf_file_hint')"
            :folder="filesStore.current"
            selection="multiple"
            type="folder"
            :extensions="['.vcf', '.gz', '.bcf']"
            @select="onImportFileSelected"
          >
            <template v-slot:error>
              <div v-if="folderError" class="text-negative text-caption">{{ folderError }}</div>
            </template>
          </file-select>
        </q-form>
      </q-card-section>
      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn
          flat
          :label="t('import')"
          type="submit"
          color="primary"
          :disable="importFiles.length == 0"
          @click="onImport"
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { ProjectDto } from 'src/models/Projects';
import { notifyError, notifySuccess } from 'src/utils/notify';
import FileSelect from 'src/components/files/FileSelect.vue';
import type { FileDto, SubjectProfileDto } from 'src/models/Opal';
import type { ImportVCFCommandOptionsDto } from 'src/models/Commands';

interface DialogProps {
  modelValue: boolean;
  project: ProjectDto;
}

const emit = defineEmits(['update:modelValue']);
const { t } = useI18n();
const projectsStore = useProjectsStore();
const profilesStore = useProfilesStore();
const filesStore = useFilesStore();
const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const formRef = ref();
const importData = ref({} as FileDto);
const importFiles = ref<string[]>([]);
const folderError = ref('');
const profile = computed(() => profilesStore.profile || ({} as SubjectProfileDto));

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      showDialog.value = value;
    }
  }
);

function onHide() {
  showDialog.value = false;
  folderError.value = '';
  importData.value = {} as FileDto;
  importFiles.value = [];
  emit('update:modelValue', false);
}

async function onImportFileSelected(files: FileDto[]) {
  importFiles.value = (files || []).map((file) => file.path);
}

async function onImport() {
  try {
    const importOptions: ImportVCFCommandOptionsDto = {
      project: props.project.name,
      files: importFiles.value,
    };

    const taskId = await projectsStore.importVcfFiles(props.project.name, importOptions);
    notifySuccess(t('vcf_store.import_vcf_command_created', { id: taskId }));
    onHide();
  } catch (error) {
    notifyError(error);
  }
}

onMounted(() =>
  profilesStore.initProfile().then(() =>
    filesStore.refreshFiles(`/home/${profile.value.principal}`).then(() => {
      importData.value = filesStore.current;
    })
  )
);
</script>
