<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card>
      <q-card-section>
        <div class="text-h6">{{ dialogTitle }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-input
            v-model="newProject.name"
            dense
            type="text"
            :label="$t('name')"
            :hint="$t('unique_name_hint')"
            style="min-width: 300px"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredField, validateName]"
          >
          </q-input>
          <q-input
            v-model="newProject.title"
            dense
            type="text"
            :label="$t('title')"
            :hint="$t('title_hint')"
            style="min-width: 300px"
            class="q-mb-md"
          >
          </q-input>
          <q-select
            v-model="newProject.database"
            :options="databases"
            dense
            :label="$t('database')"
            class="q-mb-md q-pt-md"
            emit-value
            map-options
          />
          <q-input
            v-model="newProject.description"
            ref="input"
            :label="$t('description')"
            :hint="$t('project_description_hint')"
            dense
            type="textarea"
            lazy-rules
          />
          <file-select
            v-model="exportFolder"
            :label="$t('export_folder')"
            :folder="filesStore.current"
            selection="single"
            @select="onUpdateFolder"
            type="folder"
          />
        </q-form>
        <q-select
          v-model="newProject.tags"
          use-input
          use-chips
          multiple
          placeholder="(none)"
          input-debounce="0"
          :label="$t('tags')"
          :hint="$t('project_tag_hint')"
          @new-value="addTag"
          :options="tagsFilters"
          @filter="onFilterTags"
        ></q-select>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="submitCaption" type="submit" color="primary" @click="onAddProject" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'AddProjectDialog',
});
</script>
<script setup lang="ts">
import { ProjectDto, ProjectDto_IdentifiersMappingDto } from 'src/models/Projects';
import FileSelect from 'src/components/files/FileSelect.vue';
import { notifyError } from 'src/utils/notify';
import { DatabaseDto_Usage, DatabaseDto } from 'src/models/Database';
import { FileDto, FileDto_FileType, SubjectProfileDto } from 'src/models/Opal';

interface Project extends Omit<ProjectDto, 'idMappings'> {
  idMappings?: ProjectDto_IdentifiersMappingDto[];
}

interface DialogProps {
  modelValue: boolean;
  project?: Project;
}

const emptyProject = {
  name: '',
  title: '',
  description: '',
  exportFolder: '',
  tags: [],
} as Project;

const emptyFileDto = {
  name: '/',
  path: '/home',
  type: FileDto_FileType.FOLDER,
  readable: true,
  writable: true,
  children: [],
} as FileDto;

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'update']);
const projectsStore = useProjectsStore();
const systemStore = useSystemStore();
const filesStore = useFilesStore();
const profilesStore = useProfilesStore();
const { t } = useI18n();

const formRef = ref();
const databases = ref<{ label: string; value: string }[]>([]);
const showDialog = ref(props.modelValue);
const newProject = ref<Project>({} as Project);
let tagsFilterOptions = Array<string>();
const tagsFilters = ref(Array<string>());

const profile = computed(() => profilesStore.profile || ({} as SubjectProfileDto));
const exportFolder = ref({ ...emptyFileDto } as FileDto);
const editMode = computed(() => !!props.project && !!props.project.name);
const submitCaption = computed(() => (editMode.value ? t('update') : t('add')));
const dialogTitle = computed(() => (editMode.value ? t('edit_project') : t('add_project')));

// Validators
const validateRequiredField = (val: string) => (val && val.trim().length > 0) || t('validation.name_required');
const validateName = (val: string) => val.match(/^[\w _-]*$/) !== null || t('validation.invalid_chars');

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function addTag(val: string, done: any) {
  if (val.trim().length > 0) {
    const modelValue = (newProject.value.tags || []).slice();
    if (tagsFilterOptions.includes(val) === false) {
      tagsFilterOptions.push(val);
      tagsFilters.value = [...tagsFilterOptions];
    }
    if (modelValue.includes(val) === false) {
      modelValue.push(val);
    }

    done(null);
    newProject.value.tags = modelValue;
  }
}
// Handlers

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function onFilterTags(val: string, update: any) {
  update(() => {
    if (val.trim().length === 0) {
      tagsFilters.value = [...tagsFilterOptions];
    } else {
      const needle = val.toLowerCase();
      tagsFilters.value = tagsFilters.value.filter((v) => v.toLowerCase().indexOf(needle) > -1);
    }
  });
}

function onHide() {
  newProject.value = { ...emptyProject };
  exportFolder.value = { ...emptyFileDto };
  tagsFilterOptions = [];
  tagsFilters.value = [];
  showDialog.value = false;
  emit('update:modelValue', false);
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      if (props.project) {
        newProject.value = { ...props.project };
      } else {
        // TODO: check for VCF plugin
        systemStore.getDatabases(DatabaseDto_Usage.STORAGE).then((dbs: DatabaseDto[]) => {
          newProject.value = { ...emptyProject };
          databases.value = (dbs || []).map((db) => {
            return {
              label: db.defaultStorage ? `${db.name} (${t('default_storage').toLocaleLowerCase()})` : db.name,
              value: db.name,
            };
          });
          newProject.value.database = (databases.value[0] || {}).value;
          newProject.value.exportFolder = exportFolder.value.path;
        });
      }

      tagsFilterOptions = (newProject.value.tags || []).slice();
      tagsFilters.value = [...tagsFilterOptions];
      showDialog.value = value;
    }
  }
);

function onUpdateFolder() {
  newProject.value.exportFolder = exportFolder.value.path;
}

async function onAddProject() {
  const valid = await formRef.value.validate();
  if (valid) {
    try {
      if (newProject.value.title === '') {
        newProject.value.title = newProject.value.name;
      }

      editMode.value
        ? await projectsStore.updateProject(newProject.value as ProjectDto)
        : await projectsStore.addProject(newProject.value as ProjectDto),
        onHide();
        emit('update');
    } catch (err) {
      notifyError(err);
    }
  }
}

onMounted(() =>
  profilesStore.initProfile().then(() =>
    filesStore.initFiles(`/home/${profile.value.principal}/export`).then(() => {
      exportFolder.value = filesStore.current;
    })
  )
);
</script>
