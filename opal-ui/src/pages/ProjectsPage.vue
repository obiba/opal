<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('projects')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        {{ t('projects') }}
      </div>
      <div class="text-help">
        {{ t('projects_info') }}
      </div>
      <q-table
        ref="tableRef"
        flat
        :rows="projects"
        :columns="columns"
        row-key="name"
        binary-state-sort
        :pagination="initialPagination"
        :loading="loading"
        :filter="filter"
        :filter-method="onFilter"
        @row-click="onRowClick"
      >
        <template v-slot:top-left v-if="projectsStore.perms.projects?.canCreate()">
          <div class="q-gutter-sm">
            <q-btn no-caps color="primary " icon="add" size="sm" @click="onAddProject" />
          </div>
        </template>
        <template v-slot:top-right>
          <q-select
            dense
            multiple
            use-chips
            v-model="tagsFilter"
            :options="tags"
            :label="t('tags')"
            @update:model-value="onTabChange"
            class="on-left"
            style="min-width: 200px"
          />
          <q-input dense clearable debounce="400" color="primary" v-model="filter">
            <template v-slot:append>
              <q-icon name="search" />
            </template>
          </q-input>
        </template>
        <template v-slot:body-cell-name="props">
          <q-td :props="props">
            <router-link :to="`/project/${props.value}`" class="text-primary">{{ props.value }}</router-link>
          </q-td>
        </template>
        <template v-slot:body-cell-tags="props">
          <q-td :props="props">
            <q-badge color="primary" v-for="tag in props.value" :label="tag" :key="tag" class="on-left" />
          </q-td>
        </template>
        <template v-slot:body-cell-status="props">
          <q-td :props="props">
            <q-icon name="circle" size="sm" :color="projectStatusColor(props.value)" />
          </q-td>
        </template>
      </q-table>

      <add-project-dialog v-model="showAdd" @update="onProjectAdded" />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import type { ProjectDto } from 'src/models/Projects';
import { getDateLabel } from 'src/utils/dates';
import { projectStatusColor } from 'src/utils/colors';
import AddProjectDialog from 'src/components/project/AddProjectDialog.vue';
import { flattenObjectToString } from 'src/utils/strings';
import { DefaultAlignment } from 'src/components/models';

const { t } = useI18n();
const router = useRouter();
const projectsStore = useProjectsStore();

const tagsFilter = ref<string[]>([]);
const tableRef = ref();
const loading = ref(false);
const showAdd = ref(false);
const filter = ref('');
const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 20,
});

const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: DefaultAlignment,
    field: 'name',
    format: (val: string) => val,
    sortable: true,
    headerStyle: 'width: 20%; white-space: normal;',
    style: 'width: 20%; white-space: normal;',
    columnSortOrder: 'ad',
  },
  {
    name: 'title',
    label: t('title'),
    align: DefaultAlignment,
    field: 'title',
    format: (val: string) => val,
    headerStyle: 'width: 20%; white-space: normal;',
    style: 'width: 20%; white-space: normal;',
  },
  {
    name: 'tags',
    label: t('tags'),
    align: DefaultAlignment,
    field: 'tags',
    headerStyle: 'width: 20%; white-space: normal;',
    style: 'width: 20%; white-space: normal;',
  },
  {
    name: 'lastUpdate',
    required: true,
    label: t('last_update'),
    align: DefaultAlignment,
    field: (row: ProjectDto) => (row.timestamps || {}).lastUpdate,
    format: (val: string) => getDateLabel(val),
    sortable: true,
    headerStyle: 'width: 10%; white-space: normal;',
    style: 'width: 10%; white-space: normal;',
  },
  {
    name: 'status',
    required: true,
    label: t('status'),
    align: DefaultAlignment,
    field: 'datasourceStatus',
    headerStyle: 'width: 5%; white-space: normal;',
    style: 'width: 5%; white-space: normal;',
  },
]);

const projects = computed(() => {
  if (!projectsStore.projects) {
    return [];
  }
  if (tagsFilter.value.length === 0) {
    return projectsStore.projects;
  }
  return projectsStore.projects?.filter((p) => p.tags && p.tags.some((tg) => tagsFilter.value.includes(tg)));
});

const tags = computed(() => {
  if (!projectsStore.projects) {
    return [];
  }
  const all: string[] = [];
  projectsStore.projects
    .filter((p) => p.tags)
    .map((p) => p.tags)
    .forEach((t) => {
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
  return projectsStore.initProjects().then(() => {
    loading.value = false;
  });
}

function onRowClick(evt: unknown, row: { name: string }) {
  projectsStore.initProject(row.name).then(() => {
    router.push(`/project/${row.name}`);
  });
}

function onFilter() {
  if (filter.value.length === 0) {
    return projects.value;
  }
  const query = filter && filter.value.length > 0 ? filter.value.toLowerCase() : '';
  const result = projects.value.filter((row) => {
    const rowString = `${row.name.toLowerCase()} ${flattenObjectToString(row.title || {})} ${flattenObjectToString(
      row.description || {}
    )}`;
    return rowString.includes(query);
  });

  return result;
}

function onTabChange() {
  tableRef.value.pagination.page = 1;
}

function onAddProject() {
  showAdd.value = true;
}

async function onProjectAdded(newProject: ProjectDto) {
  projectsStore.refreshProject(newProject.name).then(() => router.push(`/project/${projectsStore.project.name}`));
}
</script>
