<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('projects')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        <q-icon name="dashboard" size="sm" class="q-mb-xs"></q-icon
        ><span class="on-right">{{ $t('projects') }}</span>
      </div>
      <div v-if="tags.length" class="q-mb-md">
        <q-tabs
          v-model="tab"
          dense
          class="text-grey"
          active-color="primary"
          indicator-color="primary"
          align="justify"
          narrow-indicator
          @update:model-value="onTabChange"
        >
          <q-tab name="__all" :label="$t('all_projects')" />
          <q-tab v-for="tag in tags" :key="tag" :name="tag" :label="tag" />
        </q-tabs>
        <q-separator />
      </div>
      <q-table
        ref="tableRef"
        flat
        :rows="projects"
        :columns="columns"
        row-key="name"
        :pagination="initialPagination"
        :loading="loading"
        @row-click="onRowClick"
      >
        <template v-slot:body-cell-name="props">
          <q-td :props="props">
            <vue-router-link
              :to="`/project/${props.value}`"
              class="text-primary"
              >{{ props.value }}</vue-router-link
            >
          </q-td>
        </template>
        <template v-slot:body-cell-tags="props">
          <q-td :props="props">
            <q-badge
              color="primary"
              v-for="tag in props.value"
              :label="tag"
              :key="tag"
              class="on-left"
            />
          </q-td>
        </template>
        <template v-slot:body-cell-status="props">
          <q-td :props="props">
            <q-icon
              name="circle"
              size="sm"
              :color="projectStatusColor(props.value)"
            />
          </q-td>
        </template>
      </q-table>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import { Timestamps } from 'src/components/models';
import { getDateLabel } from 'src/utils/dates';
import { projectStatusColor } from 'src/utils/colors';


const { t } = useI18n();
const router = useRouter();
const projectsStore = useProjectsStore();

const tab = ref('__all');
const tableRef = ref();
const loading = ref(false);
const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 20,
});

const columns = [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'name',
    format: (val: string) => val,
    sortable: true,
  },
  {
    name: 'title',
    label: t('title'),
    align: 'left',
    field: 'title',
    format: (val: string) => val,
  },
  {
    name: 'tags',
    label: t('tags'),
    align: 'left',
    field: 'tags',
  },
  {
    name: 'lastUpdate',
    required: true,
    label: t('last_update'),
    align: 'left',
    field: 'timestamps',
    format: (val: Timestamps) => getDateLabel(val.lastUpdate),
  },
  {
    name: 'status',
    required: true,
    label: t('status'),
    align: 'left',
    field: 'datasourceStatus',
  },
];

const projects = computed(() => {
  if (!projectsStore.projects) {
    return [];
  }
  if (tab.value === '__all') {
    return projectsStore.projects;
  }
  return projectsStore.projects.filter((p) => p.tags && p.tags.includes(tab.value));
});

const tags = computed(() => {
  if (!projectsStore.projects) {
    return [];
  }
  const all: string[] = [];
  projectsStore.projects.filter((p) => p.tags).map((p) => p.tags).forEach((t) => {
    t.forEach((tag) => {
      if (!all.includes(tag)) {
        all.push(tag);
      }
    });
  });

  return all.sort();
});

onMounted(() => {
  init();
});

function init() {
  loading.value = true;
  projectsStore.initProjects().then(() => {
    loading.value = false;
  });
}

function onRowClick(evt: unknown, row: { name: string }) {
  projectsStore.initProject(row.name).then(() => {
    router.push(`/project/${row.name}`);
  });
}

function onTabChange() {
  tableRef.value.pagination.page = 1;
}
</script>
