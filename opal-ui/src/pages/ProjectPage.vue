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
      <div v-if="canViewSummary" class="row q-gutter-lg">
        <q-card flat bordered class="q-mb-md card bg-blue-12">
          <q-card-section @click="router.push(`/project/${name}/tables`)">
            <q-item class="q-pb-none">
              <q-item-section>
                <q-item-label>
                  <div class="text-subtitle2 text-caption text-grey-2 text-uppercase">
                    {{ t('tables_views') }}
                  </div>
                  <div class="text-white">
                    <q-spinner-dots v-if="loadingSummary" size="36px" color="white" />
                    <div v-else>
                      <span class="text-h3">{{ formatNumber(projectsStore.summary.tableCount, locale) }}</span>
                      <span class="text-h5 q-ml-sm" v-if="projectsStore.summary.viewCount">({{ formatNumber(projectsStore.summary.viewCount, locale) }})</span>
                    </div>
                  </div>
                </q-item-label>
              </q-item-section>
              <q-item-section avatar>
                <q-icon
                  name="table_chart"
                  size="64px"
                  color="blue-3" />
              </q-item-section>
            </q-item>
          </q-card-section>
          <q-card-section v-if="!loadingSummary" class="q-pt-none">
            <q-item-section class="q-px-md">
              <q-item-label>
                <div class="text-caption text-white">
                  {{ t('variables') }} <b>{{ formatNumber(projectsStore.summary.variableCount, locale) }}</b>
                </div>
                <div class="text-caption text-white">
                  <span v-for="(entityCount, index) in projectsStore.summary.entityTypeCounts" :key="entityCount.type">
                    {{ entityCount.type }} <b>{{ formatNumber(entityCount.count, locale) }}</b> <span v-if="index < entityTypeCountsSize - 1"> / </span>
                  </span>
                </div>
              </q-item-label>
            </q-item-section>
          </q-card-section>
          <q-card-actions class="footer">
            <q-btn
              flat
              rounded
              :label="t('go_to_tables')"
              icon-right="arrow_forward"
              size="sm"
              color="white"
              class="q-pa-sm q-ml-md"
              :to="`/project/${name}/tables`"
            ></q-btn>
          </q-card-actions>
        </q-card>
        <q-card flat bordered class="q-mb-md card bg-green-6">
          <q-card-section @click="router.push(`/project/${name}/resources`)">
            <q-item class="q-pb-none">
              <q-item-section>
                <q-item-label>
                  <div class="text-subtitle2 text-caption text-grey-2 text-uppercase">
                    {{ t('resources') }}
                  </div>
                  <div class="text-h3 text-white">
                    <q-spinner-dots v-if="loadingSummary" size="36px" color="white" />
                    <div v-else>
                      {{ formatNumber(projectsStore.summary.resourceCount, locale) }}
                    </div>
                  </div>
                </q-item-label>
              </q-item-section>
              <q-item-section avatar>
                <q-icon
                  name="link"
                  size="64px"
                  color="green-3" />
              </q-item-section>
            </q-item>
          </q-card-section>
          <q-card-actions class="footer">
            <q-btn
              flat
              rounded
              :label="t('go_to_resources')"
              icon-right="arrow_forward"
              size="sm"
              color="white"
              class="q-pa-sm q-ml-md"
              :to="`/project/${name}/resources`"
            ></q-btn>
          </q-card-actions>
        </q-card>
        <q-card flat bordered class="q-mb-md card bg-amber-9">
          <q-card-section @click="router.push(`/project/${name}/files`)">
            <q-item class="q-pb-none">
              <q-item-section>
                <q-item-label>
                  <div class="text-subtitle2 text-caption text-grey-2 text-uppercase">
                    {{ t('files') }}
                  </div>
                  <div class="text-h3 text-white">
                    <q-spinner-dots v-if="loadingSummary" size="36px" color="white" />
                    <div v-else>
                      {{ formatNumber(projectsStore.summary.filesCount, locale) }}
                    </div>
                  </div>
                </q-item-label>
              </q-item-section>
              <q-item-section avatar>
                <q-icon
                  name="folder"
                  size="64px"
                  color="amber-2" />
              </q-item-section>
            </q-item>
          </q-card-section>
          <q-card-actions class="footer">
            <q-btn
              flat
              rounded
              :label="t('go_to_files')"
              icon-right="arrow_forward"
              size="sm"
              color="white"
              class="q-pa-sm q-ml-md"
              :to="`/project/${name}/files`"
            ></q-btn>
          </q-card-actions>
        </q-card>
      </div>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import BookmarkIcon from 'src/components/BookmarkIcon.vue';
import { projectStatusColor } from 'src/utils/colors';
import { notifyError } from 'src/utils/notify';
import { formatNumber } from 'src/utils/numbers';

const { t, locale } = useI18n();
const route = useRoute();
const router = useRouter();
const projectsStore = useProjectsStore();

const loadingSummary = ref(false);
const name = computed(() => route.params.id as string);
const tags = computed(() => (projectsStore.project.tags ? projectsStore.project.tags : []));
const canViewSummary = ref(false);

const entityTypeCountsSize = computed(() => {
  return projectsStore.summary?.entityTypeCounts ? projectsStore.summary.entityTypeCounts.length : 0;
});

onMounted(() => {
  projectsStore
    .initProject(name.value)
    .then(() => {
      canViewSummary.value = projectsStore.perms.summary ? projectsStore.perms.summary.canRead() : false;
      if (canViewSummary.value) {
        loadingSummary.value = true;
        projectsStore
          .loadSummary()
          .finally(() => {
            loadingSummary.value = false;
          });
      }
    })
    .catch((error) => {
      notifyError(error);
      router.replace('/projects');
    });
});
</script>

<style scoped>
  .card {
    width: 100%;
    max-width: 400px;
    display: flex;
    flex-direction: column;
  }

  .footer {
    background-color: rgba(30, 58, 138, 0.2);
    margin-top: auto;
  }
</style>

