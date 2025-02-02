<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('projects')" to="/projects" />
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
        <span>{{ name }}</span>
        <q-badge v-for="tag in tags" :key="tag" class="on-right">{{ tag }}</q-badge>
      </div>
      <div class="row q-gutter-md">
        <q-card v-if="canViewSummary" flat bordered class="on-left q-mb-md o-card-md">
          <q-card-section class="text-h4 text-center bg-grey-2">
            <div>
              {{ projectsStore.summary.tableCount }}
              <span v-if="projectsStore.summary.viewCount">({{ projectsStore.summary.viewCount }})</span>
            </div>
          </q-card-section>
          <q-separator />
          <q-card-section>
            <div class="text-subtitle2">
              {{ t('tables_views') }}
              <q-btn
                flat
                rounded
                icon="arrow_forward"
                size="sm"
                class="q-pa-sm bg-primary text-white q-ml-sm"
                :to="`/project/${name}/tables`"
              ></q-btn>
            </div>
          </q-card-section>
        </q-card>
        <q-card flat bordered class="on-left q-mb-md o-card-md">
          <q-card-section class="text-h4 text-center bg-grey-2">
            <div>
              {{ projectsStore.summary.resourceCount }}
            </div>
          </q-card-section>
          <q-separator />
          <q-card-section>
            <div class="text-subtitle2">
              {{ t('resources') }}
              <q-btn
                flat
                rounded
                icon="arrow_forward"
                size="sm"
                class="q-pa-sm bg-primary text-white q-ml-sm"
                :to="`/project/${name}/resources`"
              ></q-btn>
            </div>
          </q-card-section>
        </q-card>
      </div>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import BookmarkIcon from 'src/components/BookmarkIcon.vue';
import { projectStatusColor } from 'src/utils/colors';
import { notifyError } from 'src/utils/notify';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const projectsStore = useProjectsStore();

const name = computed(() => route.params.id as string);
const tags = computed(() => (projectsStore.project.tags ? projectsStore.project.tags : []));
const canViewSummary = ref(false);

onMounted(() => {
  projectsStore
    .initProject(name.value)
    .then(() => {
      canViewSummary.value = projectsStore.perms.summary ? projectsStore.perms.summary.canRead() : false;
      if (canViewSummary.value) projectsStore.loadSummary();
    })
    .catch((error) => {
      notifyError(error);
      router.replace('/projects');
    });
});
</script>
