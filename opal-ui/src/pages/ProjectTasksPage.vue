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
      <pre>{{ projectsStore.project }}</pre>
    </q-page>
  </div>
</template>

<script setup lang="ts">
const route = useRoute();
const projectsStore = useProjectsStore();

onMounted(() => {
  const name = route.params.id as string;
  if (projectsStore.project.name === name) {
    return;
  }
  projectsStore.initProject(name);
});
</script>
