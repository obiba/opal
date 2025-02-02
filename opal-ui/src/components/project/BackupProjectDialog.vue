<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ t('project_admin.backup_project') }}</div>
      </q-card-section>

      <q-separator />
      <q-card-section style="max-height: 75vh" class="scroll">
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <file-select
            v-model="backupFolder"
            :label="t('project_admin.backup_folder')"
            :folder="filesStore.current"
            selection="single"
            @select="onUpdateFolder"
            type="folder"
          >
            <template v-slot:error>
              <div v-if="folderError" class="text-negative text-caption">{{ folderError }}</div>
            </template>
          </file-select>

          <q-checkbox v-model="backupOptions.viewsAsTables" :label="t('project_admin.views_as_table')" />
        </q-form>
      </q-card-section>
      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('backup')" type="submit" color="primary" @click="onBackup" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { ProjectDto } from 'src/models/Projects';
import { notifyError, notifySuccess } from 'src/utils/notify';
import FileSelect from 'src/components/files/FileSelect.vue';
import { type FileDto, type SubjectProfileDto, FileDto_FileType } from 'src/models/Opal';
import type { BackupCommandOptionsDto } from 'src/models/Commands';

interface DialogProps {
  modelValue: boolean;
  project: ProjectDto;
}

const projectsStore = useProjectsStore();
const profilesStore = useProfilesStore();
const filesStore = useFilesStore();
const { t } = useI18n();

const emptyFileDto = {
  name: '/',
  path: '/home',
  type: FileDto_FileType.FOLDER,
  readable: true,
  writable: true,
  children: [],
} as FileDto;

const profile = computed(() => profilesStore.profile || ({} as SubjectProfileDto));
const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const formRef = ref();
const emit = defineEmits(['update:modelValue']);
const backupFolder = ref({ ...emptyFileDto } as FileDto);
const emptyOption = { archive: '', viewsAsTables: false, override: true } as BackupCommandOptionsDto;
const backupOptions = ref(emptyOption);
const folderError = ref('');
const validateRequiredFolder = (val: string) => val && val.trim().length > 0;

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
  backupFolder.value = emptyFileDto;
  backupOptions.value = emptyOption;
  emit('update:modelValue', false);
}

async function onBackup() {
  formRef.value.validate().then((valid: boolean) => {
    if (valid) {
      if (!validateRequiredFolder(backupOptions.value.archive)) {
        folderError.value = t('validation.project_admin.backup_folder_required');
      } else {
        folderError.value = '';
        projectsStore
          .backup(props.project, backupOptions.value)
          .then(() => {
            notifySuccess(t('project_admin.backup_success'));
            onHide();
          })
          .catch(notifyError);
      }
    }
  });
}

function onUpdateFolder() {
  backupOptions.value.archive = backupFolder.value.path;
}

onMounted(() =>
  profilesStore.initProfile().then(() =>
    filesStore.initFiles(`/home/${profile.value.principal}`).then(() => {
      backupFolder.value = filesStore.current;
    })
  )
);
</script>
