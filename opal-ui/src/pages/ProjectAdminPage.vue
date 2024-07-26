<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="name" :to="`/project/${name}`" />
        <q-breadcrumbs-el :label="$t('administration')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5">
        <q-icon name="admin_panel_settings" size="sm" class="q-mb-xs"></q-icon
        ><span class="on-right">{{ $t('administration') }} </span>
      </div>

      <div class="q-mb-lg">
        <fields-list class="col-6" :items="properties" :dbobject="project" />
        <div class="text-help q-mt-md">
          {{ $t('project_admin.db_hint') }}
          <fields-list class="col-6" :items="dsProperties" :dbobject="project || {}" />
        </div>
      </div>

      <div class="q-my-lg">
        <span class="text-h5">{{ $t('permissions') }}</span>
        <access-control-list :resource="`/project/${name}/permissions/project`" :options="[AclAction.PROJECT_ALL]" />
      </div>

      <div class="q-gutter-md q-my-lg">
        <span class="text-h5"
          >{{ $t('database') }} <q-icon name="circle" size="sm" :color="tableStatusColor(state)"
        /></span>
        <div class="text-help">{{ $t('project_admin.db_reload_hint') }}</div>
        <q-btn
          size="sm"
          icon="cached"
          color="warning"
          text-color="black"
          :label="$t('reload')"
          @click="onReloadDatabase"
        ></q-btn>
        <q-btn size="sm" icon="cached" color="primary" :label="$t('state')" @click="getState"></q-btn>
      </div>

      <div class="q-gutter-md q-my-md">
        <span class="text-h5"
          >{{ $t('project_admin.backup_restore') }} <q-icon name="circle" size="sm" :color="tableStatusColor(state)"
        /></span>

        <div class="text-help">{{ $t('project_admin.backup_hint') }}</div>
        <q-btn size="sm" icon="download" color="primary" :label="$t('backup')" @click="onBackup"></q-btn>

        <div class="text-help">{{ $t('project_admin.restore_hint') }}</div>
        <q-btn size="sm" icon="upload" color="primary" :label="$t('restore')" @click="onRestore"></q-btn>
      </div>

      <confirm-dialog v-model="showConfirm" :title="$t('delete')" :text="confirmText" @confirm="onConfirmed" />

      <backup-project-dialog v-model="showBackup" :project="project" @update:model-value="onBackedUp"/>

      <restore-project-dialog v-model="showRestore" :project="project" @update:model-value="onRestored"/>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import { ProjectDto } from 'src/models/Projects';
import FieldsList, { FieldItem } from 'src/components/FieldsList.vue';
import AccessControlList from 'src/components/permissions/AccessControlList.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { AclAction } from 'src/models/Opal';
import { ProjectDatasourceStatusDto } from 'src/models/Projects';
import { notifyError } from 'src/utils/notify';
import { tableStatusColor } from 'src/utils/colors';
import BackupProjectDialog from 'src/components/project/BackupProjectDialog.vue';
import RestoreProjectDialog from 'src/components/project/RestoreProjectDialog.vue';

const route = useRoute();
const projectsStore = useProjectsStore();
const { t } = useI18n();
const showConfirm = ref(false);
const showBackup = ref(false);
const showRestore = ref(false);
const confirmText = ref('');
const onConfirmed = ref(() => ({}));
const state = ref(ProjectDatasourceStatusDto.NONE);
const project = computed(() => projectsStore.project);
const name = computed(() => route.params.id as string);

const properties: FieldItem<ProjectDto>[] = [
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

const dsProperties: FieldItem<ProjectDto>[] = [
  {
    field: 'database',
    label: 'database',
  },
  {
    field: 'datasource.type',
    label: 'type',
    html: (val) => t(val?.datasource?.type ?? '_'),
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

onMounted(() => {
  projectsStore.initProject(name.value).then(() => {
    getState();
  });
});
</script>
