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
      <pre>{{ projectsStore.project }}</pre>
    </q-page>
  </div>
</template>

<script setup lang="ts">
const route = useRoute();
const projectsStore = useProjectsStore();

const name = computed(() => route.params.id as string);

onMounted(() => {
  projectsStore.initProject(name.value);
});
</script>
