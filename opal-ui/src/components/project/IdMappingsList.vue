<template>
  <slot name="title"></slot>

  <!-- TODO: instead of disabling Add button, put a message and a link to admin/id mappings page if has permission -->
  <q-table
    flat
    :rows="idMappings"
    :columns="columns"
    row-key="name"
    :pagination="initialPagination"
    :hide-pagination="idMappings.length <= initialPagination.rowsPerPage"
    :loading="loading"
  >
    <template v-slot:top-left>
      <q-btn
        :disable="!canAddMappings"
        color="primary"
        :label="t('add')"
        icon="add"
        size="sm"
        @click.prevent="onAdd"
      />
    </template>
    <template v-slot:body-cell-type="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        {{ props.value }}
        <div class="float-right">
          <q-btn
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="t('delete')"
            :icon="toolsVisible[props.row.entityType + props.row.mapping] ? 'delete' : 'none'"
            class="q-ml-xs"
            @click="onDelete(props.row)"
          />
        </div>
      </q-td>
    </template>
    <template v-slot:body-cell-mapping="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        {{ props.value }}
      </q-td>
    </template>
  </q-table>

  <add-project-id-mappings-dialog v-model="showAddDialog" :project="project" @update="$emit('update')" />
</template>

<script setup lang="ts">
const { t } = useI18n();
import type { ProjectDto, ProjectDto_IdentifiersMappingDto } from 'src/models/Projects';
import { notifyError } from 'src/utils/notify';
import AddProjectIdMappingsDialog from 'src/components/project/AddProjectIdMappingsDialog.vue';
import { DefaultAlignment } from 'src/components/models';

interface Props {
  project: ProjectDto;
}

const emit = defineEmits(['update']);
const props = defineProps<Props>();
const projectsStore = useProjectsStore();
const identifiersStore = useIdentifiersStore();
const showAddDialog = ref(false);
const idMappings = ref([]);
const loading = ref(false);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const initialPagination = ref({
  sortBy: 'type',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});

const columns = computed(() => [
  {
    name: 'type',
    required: true,
    label: t('entity_type'),
    align: DefaultAlignment,
    field: 'entityType',
    headerStyle: 'width: 30%; white-space: normal;',
    style: 'width: 30%; white-space: normal;',
  },
  {
    name: 'mapping',
    label: t('project_admin.id_mapping'),
    align: DefaultAlignment,
    field: 'mapping',
  },
]);
const canAddMappings = computed(
  () => (identifiersStore.identifiers || []).filter((id) => id.variableCount && id.variableCount > 0).length > 0
);

// Handlers

function onOverRow(row: ProjectDto_IdentifiersMappingDto) {
  toolsVisible.value[row.entityType + row.mapping] = true;
}

function onLeaveRow(row: ProjectDto_IdentifiersMappingDto) {
  toolsVisible.value[row.entityType + row.mapping] = false;
}

function onAdd() {
  showAddDialog.value = true;
}

async function onDelete(row: ProjectDto_IdentifiersMappingDto) {
  try {
    await projectsStore.deleteIdMappings(props.project, row);
    emit('update');
  } catch (error) {
    notifyError(error);
  }
}

onMounted(() => {
  identifiersStore.initIdentifiersTables();

  projectsStore.getIdMappings(props.project.name).then((response) => {
    idMappings.value = response;
  });
});
</script>
