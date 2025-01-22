<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="name" :to="`/project/${name}`" />
        <q-breadcrumbs-el :label="t('administration')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5">
        <span>{{ t('administration') }} </span>
        <q-btn
          v-if="hasAdminPermission"
          outline
          color="primary"
          icon="edit"
          size="sm"
          @click="onEdit"
          class="on-right"
        ></q-btn>
      </div>

      <q-card flat>
        <q-card-section class="q-px-none">
          <fields-list class="col-6" :items="properties" :dbobject="project" />
        </q-card-section>
      </q-card>

      <template v-if="hasAdminPermission">
        <q-card flat>
          <q-card-section v-if="hasDatabase" class="q-px-none">
            <span class="text-help">{{ t('project_admin.db_hint') }}</span>
            <fields-list class="col-6" :items="dsProperties" :dbobject="project || {}" />
          </q-card-section>
          <q-card-section v-else class="q-px-none">
            <q-banner inline-actions rounded class="bg-orange text-white">
              {{ t('project_admin.no_database_warning') }}

              <template v-slot:action>
                <q-btn no-caps flat :label="t('project_admin.edit')" @click="onEdit" />
              </template>
            </q-banner>
          </q-card-section>
        </q-card>

        <q-card flat>
          <q-card-section v-if="hasVcfStores && project.vcfStoreService" class="q-px-none">
            <span class="text-help">{{ t('project_admin.vcf_store_hint') }}</span>
            <fields-list class="col-6" :items="vcfProperties" :dbobject="project || {}" />
          </q-card-section>
        </q-card>
        <q-card-section v-if="hasVcfStores && !project.vcfStoreService" class="q-px-none">
          <div class="box-warning">
            {{ t('project_admin.no_vcf_store_warning') }}
          </div>
        </q-card-section>
      </template>

      <template v-if="hasKeystorePermission">
        <q-card flat>
          <q-card-section class="q-px-none">
            <div class="text-h6">{{ t('project_admin.encryption_keys') }}</div>
            <div class="text-help q-mb-sm">{{ t('project_admin.encryption_keys_info') }}</div>
            <KeyPairsList :project="project" @update="onProjectUpdate" />
          </q-card-section>
        </q-card>
      </template>

      <template v-if="hasAdminPermission">
        <q-card flat>
          <q-card-section class="q-px-none">
            <div class="text-h6">{{ t('id_mappings.title') }}</div>
            <div class="text-help q-mb-sm">{{ t('project_admin.id_mappings_info') }}</div>
            <id-mappings-list :project="project" @update="onProjectUpdate" />
          </q-card-section>
        </q-card>
      </template>

      <!-- FIXME use /project/PROJ/permissions/project when fixed -->
      <template v-if="hasAdminPermission">
        <q-card flat>
          <q-card-section class="q-px-none">
            <span class="text-h6 q-mb-md">{{ t('permissions') }}</span>
            <access-control-list
              :resource="`/project/${name}/permissions/project`"
              :options="[AclAction.PROJECT_ALL]"
            />
          </q-card-section>
        </q-card>
      </template>

      <template v-if="hasReloadPermission">
        <q-card flat>
          <q-card-section class="q-px-none">
            <span class="text-h6"
              >{{ t('database') }} <q-icon name="circle" size="sm" :color="tableStatusColor(state)"
            /></span>
            <div class="text-help">{{ t('project_admin.db_reload_hint') }}</div>
          </q-card-section>
          <q-card-section class="q-px-none q-pt-none q-gutter-sm">
            <q-btn
              size="sm"
              icon="cached"
              color="warning"
              text-color="black"
              :label="t('reload')"
              @click="onReloadDatabase"
            ></q-btn>
            <q-btn size="sm" icon="cached" color="primary" :label="t('state')" @click="getState"></q-btn>
          </q-card-section>
        </q-card>
      </template>

      <template v-if="hasAdminPermission">
        <q-card flat>
          <q-card-section class="q-px-none q-pb-none">
            <span class="text-h6">{{ t('project_admin.backup_restore') }}</span>
          </q-card-section>

          <q-card-section class="q-pa-none">
            <div class="text-help">{{ t('project_admin.backup_hint') }}</div>
            <q-btn
              size="sm"
              class="q-mt-xs"
              icon="download"
              color="primary"
              :label="t('backup')"
              @click="onBackup"
            ></q-btn>

            <q-card-section class="q-px-none">
              <div class="text-help">{{ t('project_admin.restore_hint') }}</div>
              <q-btn
                size="sm"
                class="q-mt-xs"
                icon="upload"
                color="primary"
                :label="t('restore')"
                @click="onRestore"
              ></q-btn>
            </q-card-section>
          </q-card-section>
        </q-card>

        <div class="q-my-md">
          <q-card flat class="q-mt-md o-border-negative rounded-borders">
            <q-card-section>
              <span class="text-h6">{{ t('project_admin.danger_zone') }} </span>
            </q-card-section>
            <q-separator />
            <q-card-section>
              <div class="text-help">{{ t('project_admin.danger_zone_info') }}</div>
              <q-btn
                size="sm"
                class="q-mt-xs"
                icon="archive"
                color="negative"
                :label="t('project_admin.archive')"
                @click="onArchive"
              ></q-btn>
            </q-card-section>
            <q-card-section>
              <div class="text-help">{{ t('project_admin.remove_info') }}</div>
              <q-btn
                size="sm"
                class="q-mt-xs"
                icon="delete"
                color="negative"
                :label="t('project_admin.remove')"
                @click="onDelete"
              ></q-btn>
            </q-card-section>
          </q-card>
        </div>
      </template>

      <!-- Dialogs -->

      <confirm-dialog v-model="showConfirm" :title="t('delete')" :text="confirmText" @confirm="onConfirmed" />

      <backup-project-dialog v-model="showBackup" :project="project" @update:model-value="onBackedUp" />

      <restore-project-dialog v-model="showRestore" :project="project" @update:model-value="onRestored" />

      <add-project-dialog v-model="showEditProject" :project="project" @update="onProjectEdited" />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import FieldsList, { type FieldItem } from 'src/components/FieldsList.vue';
import AccessControlList from 'src/components/permissions/AccessControlList.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { AclAction } from 'src/models/Opal';
import { ProjectDatasourceStatusDto } from 'src/models/Projects';
import { notifyError } from 'src/utils/notify';
import { tableStatusColor } from 'src/utils/colors';
import BackupProjectDialog from 'src/components/project/BackupProjectDialog.vue';
import RestoreProjectDialog from 'src/components/project/RestoreProjectDialog.vue';
import AddProjectDialog from 'src/components/project/AddProjectDialog.vue';
import IdMappingsList from 'src/components/project/IdMappingsList.vue';
import KeyPairsList from 'src/components/project/KeyPairsList.vue';

const route = useRoute();
const router = useRouter();
const projectsStore = useProjectsStore();
const pluginsStore = usePluginsStore();
const { t } = useI18n();
const showConfirm = ref(false);
const showBackup = ref(false);
const showRestore = ref(false);
const showEditProject = ref(false);
const confirmText = ref('');
const onConfirmed = ref(() => ({}));
const state = ref(ProjectDatasourceStatusDto.NONE);
const project = computed(() => projectsStore.project);
const name = computed(() => route.params.id as string);
const hasAdminPermission = computed(
  () =>
    projectsStore.perms.project?.canCreate() ||
    projectsStore.perms.project?.canUpdate() ||
    projectsStore.perms.project?.canDelete()
);
const hasReloadPermission = computed(() => projectsStore.perms.reload?.canCreate() || false);
const hasKeystorePermission = computed(() => projectsStore.perms.keystore?.canCreate() || false);
const hasDatabase = computed(() => project.value.database !== undefined);
const hasVcfStores = computed(() => pluginsStore.vcfStorePlugins.length > 0);

const properties: FieldItem[] = [
  {
    field: 'name',
  },
  {
    field: 'title',
    label: 'title',
  },
  {
    field: 'description',
    label: 'description',
  },
  {
    field: 'exportFolder',
    label: 'export_folder',
  },
  {
    field: 'tags',
    label: 'tags',
    html: (val) => (val.tags || []).join(', '),
  },
];

const dsProperties: FieldItem[] = [
  {
    field: 'database',
    label: 'database',
  },
  {
    field: 'datasource.type',
    label: 'type',
    html: (val) => {
      if (val.datasource && val.datasource.type) return t(`${val.datasource.type}`);
      return t('none_value');
    },
  },
];

const vcfProperties: FieldItem[] = [
  {
    field: 'vcfStoreService',
    label: 'vcf_store.label',
  },
];

function resetConfirmedData() {
  onConfirmed.value = () => ({});
  confirmText.value = '';
}

async function getState() {
  return projectsStore.getState(name.value).then((response) => {
    state.value = response;
  });
}

// Handlers

async function _onReloadDatabase() {
  try {
    await projectsStore.reloadDbCommand(name.value);
    await getState();
    resetConfirmedData();
  } catch (error) {
    notifyError(error);
  }
}

async function _onArchive() {
  try {
    await projectsStore.archive(project.value);
    resetConfirmedData();
    router.replace('/projects');
  } catch (error) {
    notifyError(error);
  }
}

async function _onDelete() {
  try {
    await projectsStore.deleteProject(project.value);
    resetConfirmedData();
    router.replace('/projects');
  } catch (error) {
    notifyError(error);
  }
}

function onReloadDatabase() {
  confirmText.value = t('project_admin.db_reload_confirm');
  onConfirmed.value = async () => await _onReloadDatabase();
  showConfirm.value = true;
}

function onBackup() {
  showBackup.value = true;
}

function onBackedUp() {
  showBackup.value = false;
}

function onRestore() {
  showRestore.value = true;
}

function onRestored() {
  showRestore.value = false;
}

function onEdit() {
  showEditProject.value = true;
}

async function onProjectUpdate() {
  try {
    await projectsStore.refreshProject(name.value);
    await getState();
  } catch (error) {
    notifyError(error);
  }
}

async function onProjectEdited() {
  showEditProject.value = false;
  try {
    await projectsStore.refreshProject(name.value);
    await getState();
  } catch (error) {
    notifyError(error);
  }
}

function onArchive() {
  confirmText.value = t('project_admin.archive_confirm');
  onConfirmed.value = async () => await _onArchive();
  showConfirm.value = true;
}

function onDelete() {
  confirmText.value = t('project_admin.remove_confirm');
  onConfirmed.value = async () => await _onDelete();
  showConfirm.value = true;
}

onMounted(() => {
  projectsStore.refreshProject(name.value).then(() => {
    getState();
    pluginsStore.initVcfStorePlugins();
  });
});
</script>

<style lang="scss" scoped>
.o-border-negative {
  border: 1px solid $negative !important;
}
</style>
