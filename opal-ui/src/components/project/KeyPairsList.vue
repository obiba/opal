<template>
  <slot name="title"></slot>

  <!-- TODO: instead of disabling Add button, put a message and a link to admin/id mappings page if has permission -->

  <q-table
    flat
    :rows="keyPairs"
    :columns="columns"
    row-key="name"
    :pagination="initialPagination"
    :hide-pagination="keyPairs.length <= initialPagination.rowsPerPage"
    :loading="loading"
  >
    <template v-slot:top-left>
      <q-btn color="primary" :label="t('add')" icon="add" size="sm" @click.prevent="onAdd" />
    </template>
    <template v-slot:body-cell-name="props">
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
            :icon="toolsVisible[props.row.alias] ? 'delete' : 'none'"
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

  <add-key-pair-dialog v-model="showAddDialog" :project="project" @update="$emit('update')" />
</template>

<script setup lang="ts">
const { t } = useI18n();
import type { ProjectDto } from 'src/models/Projects';
import type { KeyForm, KeyType } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';
import AddKeyPairDialog from 'src/components/project/AddKeyPairDialog.vue';
import { DefaultAlignment } from 'src/components/models';

interface Props {
  project: ProjectDto;
}

const emit = defineEmits(['update']);
const props = defineProps<Props>();
const projectsStore = useProjectsStore();
const showAddDialog = ref(false);
const keyPairs = ref([]);
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
    name: 'name',
    required: true,
    label: t('name'),
    align: DefaultAlignment,
    field: 'alias',
    headerStyle: 'width: 30%; white-space: normal;',
    style: 'width: 30%; white-space: normal;',
  },
  {
    name: 'type',
    label: t('type'),
    align: DefaultAlignment,
    field: 'keyType',
    format: (val: KeyType) => t(`key_type.${val}`),
  },
]);

// Handlers

function onOverRow(row: KeyForm) {
  toolsVisible.value[row.alias] = true;
}

function onLeaveRow(row: KeyForm) {
  toolsVisible.value[row.alias] = false;
}

function onAdd() {
  showAddDialog.value = true;
}

async function onDelete(row: KeyForm) {
  try {
    await projectsStore.deleteKeyPair(props.project.name, row.alias);
    emit('update');
  } catch (error) {
    notifyError(error);
  }
}

onMounted(() => {
  projectsStore.getKeyPairs(props.project.name).then((response) => {
    keyPairs.value = response;
  });
});
</script>
