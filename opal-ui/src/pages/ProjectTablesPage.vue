<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="name" :to="`/project/${name}`" />
        <q-breadcrumbs-el :label="$t('tables')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5">
        {{ $t('tables') }}
      </div>
      <q-tabs
        v-model="tab"
        dense
        class="text-grey q-mt-md"
        active-color="primary"
        indicator-color="primary"
        align="justify"
        narrow-indicator
      >
        <q-tab name="dictionary" :label="$t('dictionary')" />
        <q-tab name="sql" :label="$t('sql')" />
        <q-tab name="permissions" :label="$t('permissions')" v-if="datasourceStore.perms.datasourcePermissions?.canRead()"/>
      </q-tabs>

      <q-separator />

      <q-tab-panels v-model="tab">
        <q-tab-panel name="dictionary">
          <datasource-tables />
        </q-tab-panel>

        <q-tab-panel name="sql">
          <div class="text-h6">{{ $t('sql') }}</div>
        </q-tab-panel>

        <q-tab-panel name="permissions" v-if="datasourceStore.perms.datasourcePermissions?.canRead()">
          <div class="text-h6">{{ $t('permissions') }}</div>
          <access-control-list
            :resource="`/project/${name}/permissions/datasource`"
            :options="['DATASOURCE_VIEW', 'TABLE_ADD', 'DATASOURCE_ALL']"
          />
        </q-tab-panel>
      </q-tab-panels>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import DatasourceTables from 'src/components/datasource/DatasourceTables.vue';
import AccessControlList from 'src/components/permissions/AccessControlList.vue';

const route = useRoute();
const projectsStore = useProjectsStore();
const datasourceStore = useDatasourceStore();

const tab = ref('dictionary');

const name = computed(() => route.params.id as string);

onMounted(() => {
  projectsStore.initProject(name.value);
});
</script>
