<template>
  <div>
    <div class="text-h5 q-pt-lg">
      <q-icon name="groups" size="sm" class="q-mb-xs"></q-icon
      ><span class="on-right">{{ $t('groups') }}</span>
    </div>
    <p>{{ $t('groups_info') }}</p>

    <q-table
      flat
      bordered
      :rows="groups"
      :columns="columns"
      row-key="name"
      :pagination="initialPagination"
      :hide-pagination="groups.length <= initialPagination.rowsPerPage"
      :loading="loading"
    >
    </q-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';

const { t } = useI18n();
import { SubjectCredentialsDto } from 'src/models/Opal';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
const groupsStore = useGroupsStore();
const groups = computed(() => groupsStore.groups || []);

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
    name: 'users',
    label: t('users'),
    align: 'left',
    field: 'subjectCredentials',
    format: (val: string[]) => (val || []).join(', '),
  },
];

const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});

const loading = ref(false);

onMounted(async () => {
  loading.value = true;
  await groupsStore.initGroups();
  loading.value = false;
});
</script>
