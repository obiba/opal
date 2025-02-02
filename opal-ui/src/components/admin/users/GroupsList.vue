<template>
  <div>
    <q-table
      flat
      :rows="groups"
      :columns="columns"
      row-key="name"
      :pagination="initialPagination"
      :hide-pagination="groups.length <= initialPagination.rowsPerPage"
      :loading="loading"
    >
      <template v-slot:body-cell-name="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <span class="text-primary">{{ props.value }}</span>
          <div class="float-right">
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="t('delete')"
              :icon="toolsVisible[props.row.name] ? 'delete' : 'none'"
              class="q-ml-xs"
              @click="onDeleteGroup(props.row)"
            />
          </div>
        </q-td>
      </template>
      <template v-slot:body-cell-users="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-chip class="q-ml-none" v-for="user in props.col.format(props.row.subjectCredentials)" :key="user.name">
            {{ user }}
          </q-chip>
        </q-td>
      </template>
    </q-table>

    <confirm-dialog
      v-if="selectedGroup"
      v-model="showDelete"
      :title="t('delete')"
      :text="t('delete_group_confirm', { group: selectedGroup.name })"
      @confirm="doDeleteGroup"
    />
  </div>
</template>

<script setup lang="ts">
import type { GroupDto } from 'src/models/Opal';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { notifyError } from 'src/utils/notify';
import { DefaultAlignment } from 'src/components/models';

const { t } = useI18n();

const groupsStore = useGroupsStore();
const usersStore = useUsersStore();
const groups = computed(() => groupsStore.groups || []);
const selectedGroup = ref<GroupDto | null>(null);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const showDelete = ref(false);

const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: DefaultAlignment,
    field: 'name',
    format: (val: string) => val,
    sortable: true,
    style: 'width: 25%',
  },
  {
    name: 'users',
    label: t('users'),
    align: DefaultAlignment,
    field: 'subjectCredentials',
    format: (val: string[]) => (val || []).filter((val) => val && val.length > 0),
  },
]);

const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});

function onOverRow(row: GroupDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: GroupDto) {
  toolsVisible.value[row.name] = false;
}

async function onDeleteGroup(user: GroupDto) {
  selectedGroup.value = user;
  showDelete.value = true;
}

async function doDeleteGroup() {
  showDelete.value = false;
  if (selectedGroup.value == null) {
    return;
  }

  const toDelete: GroupDto | null = selectedGroup.value;
  selectedGroup.value = null;

  try {
    await groupsStore.deleteGroup(toDelete);
    await usersStore.initUsers();
  } catch (err) {
    notifyError(err);
  }
}

const loading = ref(false);

onMounted(async () => {
  loading.value = true;
  groupsStore.initGroups().then(() => {
    loading.value = false;
  });
});
</script>
