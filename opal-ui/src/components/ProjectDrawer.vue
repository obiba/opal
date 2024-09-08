<template>
  <div v-if="projectsStore.project">
    <h6 class="q-mt-none q-mb-none q-pa-md">
      {{ projectsStore.project.name }}
    </h6>
    <q-list>
      <q-item :to="`/project/${projectsStore.project.name}`">
        <q-item-section avatar>
          <q-icon name="dashboard" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $t('dashboard') }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-item-label header class="text-weight-bolder">{{ $t('content') }}</q-item-label>

      <q-item :to="`/project/${projectsStore.project.name}/tables`">
        <q-item-section avatar>
          <q-icon name="table_chart" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $t('tables') }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-item :to="`/project/${projectsStore.project.name}/resources`">
        <q-item-section avatar>
          <q-icon name="link" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $t('resources') }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-item v-show="hasVcfStorePermission" :to="`/project/${projectsStore.project.name}/genotypes`">
        <q-item-section avatar>
          <q-icon name="science" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $t('genotypes') }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-item :to="`/project/${projectsStore.project.name}/files`">
        <q-item-section avatar>
          <q-icon name="folder" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $t('files') }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-item-label header class="text-weight-bolder">{{ $t('administration') }}</q-item-label>

      <q-item :to="`/project/${projectsStore.project.name}/tasks`">
        <q-item-section avatar>
          <q-icon name="splitscreen" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $t('tasks') }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-item :to="`/project/${projectsStore.project.name}/perms`">
        <q-item-section avatar>
          <q-icon name="lock" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $t('permissions') }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-item v-show="hasAdminPermission" :to="`/project/${projectsStore.project.name}/admin`">
        <q-item-section avatar>
          <q-icon name="admin_panel_settings" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $t('administration') }}</q-item-label>
        </q-item-section>
      </q-item>
    </q-list>
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'ProjectDrawer',
});
</script>
<script setup lang="ts">
const projectsStore = useProjectsStore();
const pluginsStore = usePluginsStore();
const hasAdminPermission = ref(false);
const hasVcfStorePermission = ref(false);
const hasVcfPlugins = ref(false);

watchEffect(() => {
  hasAdminPermission.value =
    projectsStore.perms.project?.canCreate() ||
    projectsStore.perms.project?.canUpdate() ||
    projectsStore.perms.project?.canDelete() ||
    false;

  hasVcfStorePermission.value =
    hasVcfPlugins && projectsStore.perms.vcfstore?.canRead() && !!projectsStore.project.vcfStoreService ? true : false;
});

onMounted(() => {
  pluginsStore.hasPlugin('vcf-store').then((status) => (hasVcfPlugins.value = status));
});
</script>
