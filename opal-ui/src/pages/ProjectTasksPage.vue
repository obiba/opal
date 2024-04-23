<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('projects')" to="/projects" />
        <q-breadcrumbs-el
          :label="projectsStore.project?.name"
          :to="`/project/${projectsStore.project.name}`"
        />
        <q-breadcrumbs-el :label="$t('tasks')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <command-states
        :commands="projectsStore.commandStates ? projectsStore.commandStates : []"
        :project="projectsStore.project.name"
        @refresh="onRefresh"
        @clear="onClear" />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import CommandStates from 'src/components/CommandStates.vue';
const route = useRoute();
const projectsStore = useProjectsStore();

onMounted(() => {
  const name = route.params.id as string;
  if (projectsStore.project.name === name) {
    projectsStore.loadCommandStates();
    return;
  }
  projectsStore.initProject(name).then(() => {
    projectsStore.loadCommandStates();
  })
});

const onRefresh = () => {
  projectsStore.loadCommandStates();
};

const onClear = () => {
  projectsStore.clearCommandStates().then(() => {
    projectsStore.loadCommandStates();
  });
};
</script>
