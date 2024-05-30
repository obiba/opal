<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="name" />
      </q-breadcrumbs>
      <q-icon
        name="circle"
        :color="projectStatusColor(projectsStore.summary?.datasourceStatus)"
        :title="projectsStore.summary?.datasourceStatus"
        size="sm"
        class="on-right"
      />
      <bookmark-icon :resource="`/project/${name}`" />
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        <q-icon name="dashboard" size="sm" class="q-mb-xs"></q-icon
        ><span class="on-right">{{ name }}</span>
        <q-badge v-for="tag in tags" :key="tag" class="on-right">{{ tag }}</q-badge>
      </div>
      <div class="row">
        <div class="col-md-auto col-sm-auto col-xs-12">
          <q-card flat bordered class="on-left bg-grey-2 q-mb-md o-card">
            <q-card-section>
              <div class="text-h4 text-center">
                {{ projectsStore.summary.tableCount }}
                <span v-if="projectsStore.summary.viewCount"
                  >({{ projectsStore.summary.viewCount }})</span
                >
              </div>
            </q-card-section>
            <q-separator />
            <q-card-section>
              <div class="text-subtitle2">
                {{ $t('tables_views') }}
                <q-btn
                  flat
                  rounded
                  icon="arrow_forward"
                  size="sm"
                  class="q-pa-sm bg-grey-2 text-grey-10 q-ml-sm"
                  :to="`/project/${name}/tables`"
                ></q-btn>
              </div>
            </q-card-section>
          </q-card>
        </div>
        <div class="col-md-auto col-sm-auto col-xs-12">
          <q-card flat bordered class="on-left bg-grey-2 q-mb-md o-card">
            <q-card-section>
              <div class="text-h4 text-center">
                {{ projectsStore.summary.resourceCount }}
              </div>
            </q-card-section>
            <q-separator />
            <q-card-section>
              <div class="text-subtitle2">
                {{ $t('resources') }}
                <q-btn
                  flat
                  rounded
                  icon="arrow_forward"
                  size="sm"
                  class="q-pa-sm bg-grey-2 text-grey-10 q-ml-sm"
                  :to="`/project/${name}/resources`"
                ></q-btn>
              </div>
            </q-card-section>
          </q-card>
        </div>
      </div>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import BookmarkIcon from 'src/components/BookmarkIcon.vue';
import { projectStatusColor } from 'src/utils/colors';

const route = useRoute();
const projectsStore = useProjectsStore();

const name = computed(() => route.params.id as string);
const tags = computed(() => projectsStore.project.tags ? projectsStore.project.tags : []);

onMounted(() => {
  projectsStore.initProject(name.value).then(() => {
    projectsStore.loadSummary(name.value);
  });
});
</script>
