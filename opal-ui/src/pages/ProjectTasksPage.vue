<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="projectsStore.project?.name" :to="`/project/${projectsStore.project.name}`" />
        <q-breadcrumbs-el :label="t('tasks')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        {{ t('tasks') }}
      </div>
      <command-states
        :commands="commands"
        :project="projectsStore.project.name"
        @refresh="onRefresh"
        @clear="onClear"
        @cancel="onCancel"
      />
    </q-page>
    <confirm-dialog
      v-model="showConfirmClear"
      :title="t('clear')"
      :text="t('clear_tasks_confirm', { count: selectedToClear.length })"
      @confirm="onClearConfirmed"
      @cancel="onClearCancel"
    />
    <confirm-dialog
      v-model="showConfirmCancel"
      :title="t('cancel')"
      :text="t('cancel_task_confirm')"
      @confirm="onCancelConfirmed"
      @cancel="onCancelCancel"
    />
  </div>
</template>

<script setup lang="ts">
import CommandStates from 'src/components/CommandStates.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import type { CommandStateDto } from 'src/models/Commands';

const { t } = useI18n();
const route = useRoute();
const projectsStore = useProjectsStore();

const showConfirmClear = ref(false);
const showConfirmCancel = ref(false);
const selectedToClear = ref<CommandStateDto[]>([]);
const selectedToCancel = ref<CommandStateDto | undefined>();

onMounted(() => {
  const name = route.params.id as string;
  if (projectsStore.project.name === name) {
    projectsStore.loadCommandStates();
    return;
  }
  projectsStore.initProject(name).then(() => {
    projectsStore.loadCommandStates();
  });
});

const commands = computed(() => (projectsStore.commandStates ? projectsStore.commandStates : []));

function onRefresh() {
  projectsStore.loadCommandStates();
}

function onClear(command: CommandStateDto) {
  selectedToClear.value = command ? [command] : commands.value;
  showConfirmClear.value = true;
}

function onClearConfirmed() {
  projectsStore
    .clearCommandStates(selectedToClear.value)
    .then(() => {
      projectsStore.loadCommandStates();
    })
    .catch(() => {
      projectsStore.loadCommandStates();
    });
}

function onClearCancel() {
  selectedToClear.value = [];
  onRefresh();
}

function onCancel(command: CommandStateDto) {
  selectedToCancel.value = command;
  showConfirmCancel.value = true;
}

function onCancelConfirmed() {
  if (!selectedToCancel.value) {
    return;
  }
  projectsStore
    .cancelCommandState(selectedToCancel.value)
    .then(() => {
      projectsStore.loadCommandStates();
    })
    .catch(() => {
      projectsStore.loadCommandStates();
    });
}

function onCancelCancel() {
  selectedToCancel.value = undefined;
  onRefresh();
}
</script>
