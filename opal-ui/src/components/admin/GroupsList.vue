<template>
  <div>
    <div class="text-h5 q-pt-lg">
      <q-icon name="groups" size="sm" class="q-mb-xs"></q-icon><span class="on-right">{{ $t('groups') }}</span>
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
      <template v-slot:body-cell-users="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <span>{{ props.col.format(props.row.subjectCredentials) }}</span>
          <div class="float-right">
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="$t('delete')"
              :icon="toolsVisible[props.row.name] ? 'delete' : 'none'"
              class="q-ml-xs"
              @click="onDeleteGroup(props.row)"
            />
          </div>
        </q-td>
      </template>
    </q-table>

    <confirm-dialog
      v-if="selectedGroup"
      v-model="showDelete"
      :title="$t('delete')"
      :text="$t('delete_group_confirm', { group: selectedGroup.name })"
      @confirm="doDeleteGroup"
    />

  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';

const { t } = useI18n();
import { GroupDto } from 'src/models/Opal';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { notifyError } from 'src/utils/notify';

const groupsStore = useGroupsStore();
const usersStore = useUsersStore();
const groups = computed(() => groupsStore.groups || []);
const selectedGroup = ref<GroupDto | null>(null);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const showDelete = ref(false);

const columns = [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'name',
    format: (val: string) => val,
    sortable: true,
    style: 'width: 25%',
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
