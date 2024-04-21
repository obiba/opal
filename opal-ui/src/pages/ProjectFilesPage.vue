<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="name" :to="`/project/${name}`" />
        <q-breadcrumbs-el :label="$t('files')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <file-view :file="filesStore.current" />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import FileView from 'src/components/files/FileView.vue';

const route = useRoute();
const projectsStore = useProjectsStore();
const filesStore = useFilesStore();

const name = computed(() => route.params.id as string);

onMounted(() => {
  projectsStore.initProject(name.value).then(() => {
    filesStore.loadFiles(`/projects/${name.value}`);
  });
});
</script>
