<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="pName" :to="`/project/${pName}`" />
        <q-breadcrumbs-el :label="$t('resources')" :to="`/project/${pName}/resources`"/>
        <q-breadcrumbs-el :label="rName" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">

    </q-page>
  </div>
</template>

<script setup lang="ts">

const route = useRoute();
const projectsStore = useProjectsStore();

const pName = computed(() => route.params.id as string);
const rName = computed(() => route.params.rid as string);
//const tab = ref('references');

onMounted(() => {
  projectsStore.initProject(pName.value);
});
</script>
