<template>
    <div>
      <q-toolbar class="bg-grey-3">
        <q-breadcrumbs>
          <q-breadcrumbs-el icon="home" to="/" />
          <q-breadcrumbs-el :label="$t('projects')" to="/projects" />
          <q-breadcrumbs-el :label="name" :to="`/project/${name}`" />
          <q-breadcrumbs-el :label="$t('project_genotypes.title')" />
        </q-breadcrumbs>
      </q-toolbar>
      <q-page class="q-pa-md">
        <div class="text-h5 q-mb-md">
          {{ $t('project_genotypes.title') }}
        </div>

        <!-- <confirm-dialog
          v-model="showDeletes"
          :title="$t('delete')"
          :text="$t('delete_profile_acl_confirm', { count: selectedAcls.length })"
          @confirm="doDeleteAcls"
        /> -->
      </q-page>
    </div>
  </template>

  <script setup lang="ts">
  import { notifyError } from 'src/utils/notify';
  import AccessControlTable from 'src/components/permissions/AccessControlTable.vue';
  import ConfirmDialog from 'src/components/ConfirmDialog.vue';


  const route = useRoute();
  const projectsStore = useProjectsStore();
  const loading = ref(false);
  const name = computed(() => route.params.id as string);

  // Handlers

  onMounted(() => {
    // projectsStore.initProject(name.value).then(() => loadSubjects());
  });
  </script>
