<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="pName" :to="`/project/${pName}`" />
        <q-breadcrumbs-el :label="t('resources')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        {{ t('resources') }}
      </div>
      <div class="text-help">
        {{ t('resources_info') }}
      </div>
      <q-tabs
        v-model="tab"
        dense
        class="text-grey q-mt-md"
        active-color="primary"
        indicator-color="primary"
        align="justify"
      >
        <q-tab name="references" :label="t('references')" />
        <q-tab
          name="permissions"
          :label="t('permissions')"
          v-if="resourcesStore.perms.resourcesPermissions?.canRead()"
        />
      </q-tabs>

      <q-separator />

      <q-tab-panels v-model="tab">
        <q-tab-panel name="references">
          <resource-references />
        </q-tab-panel>

        <q-tab-panel name="permissions" v-if="resourcesStore.perms.resourcesPermissions?.canRead()">
          <access-control-list
            :resource="`/project/${pName}/permissions/resources`"
            :options="['RESOURCES_VIEW', 'RESOURCES_ALL']"
          />
        </q-tab-panel>
      </q-tab-panels>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import ResourceReferences from 'src/components/resources/ResourceReferences.vue';
import AccessControlList from 'src/components/permissions/AccessControlList.vue';

const { t } = useI18n();
const route = useRoute();
const projectsStore = useProjectsStore();
const resourcesStore = useResourcesStore();

const pName = computed(() => route.params.id as string);
const tab = ref('references');

onMounted(() => {
  projectsStore.initProject(pName.value);
});
</script>
