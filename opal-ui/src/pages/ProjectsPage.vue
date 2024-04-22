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
      <q-table
        ref="tableRef"
        flat
        :rows="projectsStore.projects"
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
      </q-table>
    </q-page>
  </div>
</template>

<script setup lang="ts">
const router = useRouter();
const projectsStore = useProjectsStore();

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
    label: 'Name',
    align: 'left',
    field: 'name',
    format: (val: string) => val,
    sortable: true,
  },
  {
    name: 'title',
    label: 'Title',
    align: 'left',
    field: 'title',
    format: (val: string) => val,
  },
  {
    name: 'tags',
    label: 'Tags',
    align: 'left',
    field: 'tags',
  },
];

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
</script>
