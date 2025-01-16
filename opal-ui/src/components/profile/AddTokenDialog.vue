<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ t(`user_profile.token_dialog.add_${type}_token`) }}</div>
      </q-card-section>

      <q-separator />
      <q-card-section style="max-height: 75vh" class="scroll">
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-input
            dense
            type="text"
            :label="t('name') + ' *'"
            :hint="t('user_profile.token_dialog.name_hint')"
            v-model="token.name"
            color="grey-10"
            lazy-rules
            :rules="[validateRequiredName]"
          >
          </q-input>

          <q-select
            dense
            v-model="token.projects"
            v-if="projectsFilterOptions.length > 0"
            use-input
            use-chips
            multiple
            input-debounce="0"
            :label="t('projects')"
            :hint="t('user_profile.token_dialog.projects_hint')"
            :options="projectFilters"
            @new-value="addProject"
            @filter="filterProjects"
          >
          </q-select>

          <add-token-dialog-access-section
            v-if="showAccessTasks"
            v-model="token.access"
          ></add-token-dialog-access-section>

          <add-token-dialog-option-groups
            v-if="showAccessTasks"
            v-model="token.commands"
            :group-options="taskGroupOptions"
            title="user_profile.token_dialog.project_tasks"
            hint="user_profile.token_dialog.project_tasks_hint"
          ></add-token-dialog-option-groups>

          <add-token-dialog-option-groups
            v-if="showAdminOptions"
            v-model="tokenAdmin"
            :group-options="adminGroupOptions"
            title="user_profile.token_dialog.project_administration"
            hint="user_profile.token_dialog.project_administration_hint"
            @update:model-value="onTokenAdminUpdate"
          ></add-token-dialog-option-groups>

          <add-token-dialog-option-groups
            v-if="showAdminOptions"
            v-model="tokenServices"
            :group-options="servicesGroupOptions"
            title="user_profile.token_dialog.services"
            hint="user_profile.token_dialog.services_hint"
            @update:model-value="onTokenServicesUpdate"
          ></add-token-dialog-option-groups>
        </q-form>
      </q-card-section>
      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('add')" type="submit" color="primary" @click="onAddToken" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { SubjectTokenDto /*, ProjectDto*/ } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';
import { generateName } from 'src/utils/strings';
import { TOKEN_TYPES } from 'src/stores/tokens';
import AddTokenDialogAccessSection from './AddTokenDialogAccessSection.vue';
import AddTokenDialogOptionGroups from './AddTokenDialogOptionGroups.vue';

interface DialogProps {
  modelValue: boolean;
  type: string;
  names: string[];
}

const props = defineProps<DialogProps>();

const showDialog = ref(props.modelValue);
const formRef = ref();
const emit = defineEmits(['update:modelValue', 'added']);
const tokensStore = useTokensStore();
const projectsStore = useProjectsStore();
let projectsFilterOptions = Array<string>();
const projectFilters = ref(Array<string>());
const tokenAdmin = ref<string[]>([]);
const tokenServices = ref<string[]>([]);
const showAccessTasks = computed(() => [TOKEN_TYPES.R, TOKEN_TYPES.CUSTOM].includes(props.type as TOKEN_TYPES));
const showAdminOptions = computed(() => TOKEN_TYPES.CUSTOM === props.type);

const emptyToken: SubjectTokenDto = {
  name: '',
  projects: [],
  commands: [],
} as SubjectTokenDto;
const token = ref({ ...emptyToken } as SubjectTokenDto);

const { t } = useI18n();

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      projectsStore.initProjects().then(() => {
        projectsFilterOptions = projectsStore.projects.map((p) => p.name);
        projectFilters.value = [...projectsFilterOptions];
      });

      token.value.token = undefined;
      token.value.name = generateName(props.type, props.names);
      updateTokenType();
      showDialog.value = value;
      initializeTaskGroupOptions();
      initializeAdminGroupOptions();
      initializeServicesGroupOptions();
    }
  }
);

// Validations
const validateRequiredName = (val: string) => (val && val.trim().length > 0) || t('validation.name_required');

// Group options
const taskGroupOptions: { label: string; value: string }[] = [];
const adminGroupOptions: { label: string; value: string }[] = [];
const servicesGroupOptions: { label: string; value: string }[] = [];

function initializeTaskGroupOptions() {
  taskGroupOptions.push(
    { label: t('command_types.import'), value: 'import' },
    { label: t('command_types.export'), value: 'export' }
  );

  if (props.type === TOKEN_TYPES.CUSTOM) {
    taskGroupOptions.push(
      { label: t('command_types.copy'), value: 'copy' },
      { label: t('command_types.analyse'), value: 'analyse' },
      { label: t('command_types.import_vcf'), value: 'import_vcf' },
      { label: t('command_types.export_vcf'), value: 'export_vcf' },
      { label: t('command_types.backup'), value: 'backup' },
      { label: t('command_types.restore'), value: 'restore' }
    );
  }
}

function initializeAdminGroupOptions() {
  adminGroupOptions.push(
    { label: t('token_administration.createProject'), value: 'createProject' },
    { label: t('token_administration.updateProject'), value: 'updateProject' },
    { label: t('token_administration.deleteProject'), value: 'deleteProject' }
  );
}

function initializeServicesGroupOptions() {
  servicesGroupOptions.push(
    { label: t('token_services.useR'), value: 'useR' },
    { label: t('token_services.useDatashield'), value: 'useDatashield' },
    { label: t('token_services.useSQL'), value: 'useSQL' },
    { label: t('token_services.sysAdmin'), value: 'sysAdmin' }
  );
}

function updateTokenType() {
  switch (props.type) {
    case TOKEN_TYPES.DATASHIELD:
      token.value.useDatashield = true;
      break;
    case TOKEN_TYPES.R:
      token.value.useR = true;
      break;
    case TOKEN_TYPES.SQL:
      token.value.useSQL = true;
      break;
  }
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function filterProjects(val: string, update: any) {
  update(() => {
    if (val.trim().length === 0) {
      projectFilters.value = [...projectsFilterOptions];
    } else {
      const needle = val.toLowerCase();
      projectFilters.value = projectFilters.value.filter((v) => v.toLowerCase().indexOf(needle) > -1);
    }
  });
}

function onTokenAdminUpdate() {
  token.value.createProject = tokenAdmin.value.includes('createProject');
  token.value.updateProject = tokenAdmin.value.includes('updateProject');
  token.value.deleteProject = tokenAdmin.value.includes('deleteProject');
}

function onTokenServicesUpdate() {
  token.value.useR = tokenServices.value.includes('useR');
  token.value.useDatashield = tokenServices.value.includes('useDatashield');
  token.value.useSQL = tokenServices.value.includes('useSQL');
  token.value.sysAdmin = tokenServices.value.includes('sysAdmin');
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function addProject(val: string, done: any) {
  if (val.trim().length > 0) {
    const modelValue = (token.value.projects || []).slice();
    if (modelValue.includes(val) === false) {
      modelValue.push(val);
    }

    done(null);
    token.value.projects = modelValue;
  }
}

function onHide() {
  token.value = { ...emptyToken };
  showDialog.value = false;
  projectsFilterOptions = [];
  projectFilters.value = [];
  taskGroupOptions.splice(0, taskGroupOptions.length);
  adminGroupOptions.splice(0, adminGroupOptions.length);
  tokenAdmin.value = [];
  servicesGroupOptions.splice(0, servicesGroupOptions.length);
  tokenServices.value = [];

  emit('update:modelValue', false);
}

async function onAddToken() {
  const valid = await formRef.value.validate();
  if (valid) {
    try {
      const created = await tokensStore.addToken(token.value);
      showDialog.value = false;
      emit('added', created);
    } catch (error) {
      notifyError(error);
    }
  }
}
</script>
