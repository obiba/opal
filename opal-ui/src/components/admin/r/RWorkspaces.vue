<template>
  <div>
    <q-table
      flat
      :rows="rStore.workspaces"
      :columns="columns"
      row-key="name"
      :pagination="initialPagination"
      wrap-cells
      selection="multiple"
      v-model:selected="selected"
    >
      <template v-slot:top-left>
        <q-btn outline color="secondary" icon="refresh" :title="t('refresh')" size="sm" @click="updateRWorkspaces" />
        <q-btn
          outline
          color="red"
          icon="delete"
          size="sm"
          class="on-right"
          :disable="selected.length === 0"
          @click="onShowDeleteWorkspaces"
        />
      </template>
      <template v-slot:body-cell-name="props">
        <q-td :props="props">
          <code>{{ props.value }}</code>
        </q-td>
      </template>
      <template v-slot:body-cell-user="props">
        <q-td :props="props">
          <q-chip>{{ props.value }}</q-chip>
        </q-td>
      </template>
    </q-table>
    <confirm-dialog
      v-model="showDelete"
      :title="t('delete')"
      :text="t('delete_r_workspaces_confirm', { count: selected.length })"
      @confirm="onDeleteWorkspaces"
    />
  </div>
</template>

<script setup lang="ts">
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import type { RWorkspaceDto } from 'src/models/OpalR';
import { getDateLabel } from 'src/utils/dates';
import { getSizeLabel } from 'src/utils/files';
import { DefaultAlignment } from 'src/components/models';

const rStore = useRStore();
const { t } = useI18n();

const showDelete = ref(false);
const selected = ref<RWorkspaceDto[]>([]);
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});

const columns = computed(() => [
  { name: 'name', label: t('name'), align: DefaultAlignment, field: 'name', sortable: true },
  { name: 'context', label: t('context'), align: DefaultAlignment, field: 'context', sortable: true, classes: 'text-caption' },
  { name: 'user', label: t('user'), align: DefaultAlignment, field: 'user', sortable: true },
  {
    name: 'lastAccessDate',
    label: t('last_access'),
    align: DefaultAlignment,
    field: 'lastAccessDate',
    sortable: true,
    format: getDateLabel,
  },
  { name: 'size', label: t('size'), align: DefaultAlignment, field: 'size', sortable: true, format: getSizeLabel },
]);

function updateRWorkspaces() {
  rStore.initWorkspaces();
}

function onShowDeleteWorkspaces() {
  showDelete.value = true;
}

function onDeleteWorkspaces() {
  rStore.deleteWorkspaces(selected.value).finally(() => {
    selected.value = [];
  });
}
</script>
