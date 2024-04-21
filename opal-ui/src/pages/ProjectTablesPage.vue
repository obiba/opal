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
        <q-icon name="table_chart" size="sm" class="q-mb-xs"></q-icon
        ><span class="on-right">{{ $t('tables') }}</span>
      </div>
      <q-card flat bordered class="bg-info text-white q-mt-md q-mb-md">
        <q-card-section>
          Lorem ipsum dolor sit amet consectetur adipisicing elit. Et quos
          officiis labore, illo odit sit voluptatibus atque modi quae tempore
          soluta velit voluptatem adipisci nostrum repudiandae, ea molestiae
          maiores quam.
        </q-card-section>
      </q-card>
      <q-tabs
        v-model="tab"
        dense
        class="text-grey"
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
        </q-tab-panel>
      </q-tab-panels>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import DatasourceTables from 'src/components/DatasourceTables.vue';

const route = useRoute();
const projectsStore = useProjectsStore();
const datasourceStore = useDatasourceStore();

const tab = ref('dictionary');

const name = computed(() => route.params.id as string);

onMounted(() => {
  projectsStore.initProject(name.value);
});
</script>
