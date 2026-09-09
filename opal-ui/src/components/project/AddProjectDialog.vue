<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
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
            :label="t('name')"
            :hint="t('unique_name_hint')"
            :disable="editMode"
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
            :label="t('title')"
            :hint="t('title_hint')"
            style="min-width: 300px"
            class="q-mb-md"
          >
          </q-input>
          <q-select
            v-model="storage"
            :options="storageOptions"
            dense
            :label="t('database')"
            :hint="storageHint"
            :disable="hasTables"
            class="q-mb-md q-pt-md"
            emit-value
            map-options
          />
          <q-select
            v-if="hasVcfStores"
            v-model="newProject.vcfStoreService"
            :options="vcfStores"
            dense
            :label="t('vcf_store.label')"
            class="q-mb-md q-pt-md"
            emit-value
            map-options
          />
          <q-input
            v-model="newProject.description"
            ref="input"
            :label="t('description')"
            :hint="t('project_description_hint')"
            dense
            type="textarea"
            lazy-rules
          />
          <file-select
            v-model="exportFolder"
            :label="t('export_folder')"
            :folder="filesStore.current"
            selection="single"
            @select="onUpdateFolder"
            type="folder"
          />
          <q-select
            v-model="newProject.tags"
            use-input
            use-chips
            multiple
            input-debounce="0"
            :label="t('tags')"
            :hint="t('project_tag_hint')"
            @new-value="addTag"
            :options="tagsFilters"
            @filter="onFilterTags"
          ></q-select>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="submitCaption" type="submit" color="primary" @click="onAddProject" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { ProjectDto, ProjectDto_IdentifiersMappingDto } from 'src/models/Projects';
import FileSelect from 'src/components/files/FileSelect.vue';
import { notifyError } from 'src/utils/notify';
import { DatabaseDto_Usage, type DatabaseDto } from 'src/models/Database';
import type { FileDto } from 'src/models/Opal';
import type { PluginPackageDto } from 'src/models/Plugins';

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

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'update']);
const projectsStore = useProjectsStore();
const systemStore = useSystemStore();
const filesStore = useFilesStore();
const profilesStore = useProfilesStore();
const pluginsStore = usePluginsStore();
const { t } = useI18n();

const formRef = ref();
// The value of the internal storage option. The server reserves names beginning with an underscore for its own
// databases, so this cannot be one an operator registered.
const INTERNAL_STORAGE = '_project_';
const storage = ref<string>(INTERNAL_STORAGE);
const databases = ref<{ label: string; value: string; defaultStorage: boolean }[]>([]);
const vcfStores = ref<{ label: string; value: string }[]>([]);
const showDialog = ref(props.modelValue);
const newProject = ref<Project>({} as Project);
let tagsFilterOptions = Array<string>();
const tagsFilters = ref(Array<string>());

const exportFolder = ref<FileDto>();
const editMode = computed(() => props.project && props.project.name !== undefined && props.project.name !== '');
const submitCaption = computed(() => (editMode.value ? t('update') : t('add')));
const dialogTitle = computed(() => (editMode.value ? t('edit_project') : t('add_project')));
const hasTables = computed(() => (newProject.value?.datasource?.table ?? []).length > 0);
const hasVcfStores = computed(() => pluginsStore.vcfStorePlugins.length > 0);
const storageOptions = computed(() => [
  { label: t('internal_database'), value: INTERNAL_STORAGE, defaultStorage: false },
  ...databases.value,
  { label: t('none_value'), value: '', defaultStorage: false },
]);
// What deletion means is the whole difference between the two, and worth saying out loud
const storageHint = computed(() => {
  if (storage.value === INTERNAL_STORAGE) return t('internal_database_hint');
  return storage.value === '' ? '' : t('registered_database_hint');
});

// Validators
const validateRequiredField = (val: string) => (val && val.trim().length > 0) || t('validation.name_required');
const validateName = (val: string) => val.match(/^[\w _-]*$/) !== null || t('validation.name_invalid_chars');

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
  exportFolder.value = undefined;
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
        storage.value = props.project.internalDatabase ? INTERNAL_STORAGE : (props.project.database ?? '');
        if (props.project.exportFolder) {
          filesStore.getFile(props.project.exportFolder).then((file) => {
            exportFolder.value = file;
          });
        }
      } else {
        newProject.value = { ...emptyProject };
        // an operator marking a database as the default storage says where project data is meant to go on this
        // server, and internal storage does not overrule that
        const defaultDb = databases.value.find((db) => db.defaultStorage);
        storage.value = defaultDb ? defaultDb.value : INTERNAL_STORAGE;
        const defaultVcfStore = vcfStores.value.find((vcf) => vcf.value === '');
        if (hasVcfStores) newProject.value.vcfStoreService = (defaultVcfStore || vcfStores.value[0] || {}).value;
      }

      tagsFilterOptions = (newProject.value.tags || []).slice();
      tagsFilters.value = [...tagsFilterOptions];
      showDialog.value = value;
    }
  },
);

/**
 * An internal database is asked for by name of kind, not by name of database. No storage at all is asked for with an
 * empty database name: leaving the field out is what a client that cannot name an internal database does, and the
 * server reads it as "leave the storage alone".
 */
function applyStorage() {
  if (storage.value === INTERNAL_STORAGE) {
    newProject.value.internalDatabase = true;
    delete newProject.value.database;
  } else {
    newProject.value.internalDatabase = false;
    newProject.value.database = storage.value;
  }
}

function onUpdateFolder() {
  newProject.value.exportFolder = exportFolder.value?.path;
}

async function onAddProject() {
  const valid = await formRef.value.validate();
  if (valid) {
    try {
      if (newProject.value.title === '') {
        newProject.value.title = newProject.value.name;
      }
      applyStorage();

      if (editMode.value) await projectsStore.updateProject(newProject.value as ProjectDto);
      else await projectsStore.addProject(newProject.value as ProjectDto);
      emit('update', newProject.value);
      onHide();
    } catch (err) {
      notifyError(err);
    }
  }
}

onMounted(() => {
  profilesStore.initProfile().then(() => {
    if (!filesStore.current?.path) {
      filesStore.loadFiles(`/home/${profilesStore.profile.principal}`);
    }

    systemStore.getDatabases(DatabaseDto_Usage.STORAGE).then((dbs: DatabaseDto[]) => {
      // a database another project owns is not a choice; the server refuses it too
      databases.value = (dbs || [])
        .filter((db) => !db.ownerProject)
        .map((db) => {
          return {
            label: db.defaultStorage ? `${db.name} (${t('default_storage').toLocaleLowerCase()})` : db.name,
            value: db.name,
            defaultStorage: db.defaultStorage,
          };
        });
    });

    pluginsStore.initVcfStorePlugins().then(() => {
      if (pluginsStore.vcfStorePlugins.length > 0) {
        vcfStores.value = pluginsStore.vcfStorePlugins.map((pkg: PluginPackageDto) => {
          return {
            label: pkg.name,
            value: pkg.name,
          };
        });

        vcfStores.value.push({ label: t('none_value'), value: '' });
      }
    });
  });
});
</script>
