<template>
  <div>
    <div class="q-py-md">
      <q-btn-dropdown color="primary" :label="$t('user_add')" icon="add" size="sm">
        <q-list>
          <q-item dense clickable v-close-popup @click.prevent="onAddWithPassword">
            <q-item-section>
              <q-item-label>{{ $t('user_add_with_pwd') }}</q-item-label>
            </q-item-section>
          </q-item>

          <q-item dense clickable v-close-popup @click.prevent="onAddWithCertificate">
            <q-item-section>
              <q-item-label>{{ $t('user_add_with_crt') }}</q-item-label>
            </q-item-section>
          </q-item>
        </q-list>
      </q-btn-dropdown>
    </div>

    <div class="text-h5">
      <q-icon name="people" size="sm" class="q-mb-xs"></q-icon><span class="on-right">{{ $t('users') }}</span>
    </div>
    <p>{{ $t('users_info') }}</p>

    <q-table
      flat
      bordered
      :filter="filter"
      :filter-method="filterUsers"
      :rows="users"
      :columns="columns"
      row-key="name"
      :pagination="initialPagination"
      :hide-pagination="filteredUserCount <= initialPagination.rowsPerPage"
      :loading="loading"
    >
      <template v-slot:top v-if="users.length > 10">
        <div class="fit row wrap justify-start items-start content-start">
          <div class="col-6 self-end offset-6">
            <q-input
              dense
              debounce="400"
              color="primary"
              v-model="filter"
              :placeholder="$t('users_filter_placeholder')"
            >
              <template v-slot:append>
                <q-icon name="search" />
              </template>
            </q-input>
          </div>
        </div>
      </template>
      <template v-slot:body-cell-groups="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <span>{{ props.col.format(props.row.groups) }}</span>
          <div class="float-right">
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="$t('edit')"
              :icon="toolsVisible[props.row.name] ? 'edit' : 'none'"
              class="q-ml-xs"
              @click="onEditUser(props.row)"
            />
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="$t('delete')"
              :icon="toolsVisible[props.row.name] ? 'delete' : 'none'"
              class="q-ml-xs"
              @click="onDeleteUser(props.row)"
            />
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="props.row.enabled ? $t('disable') : $t('enable')"
              :icon="toolsVisible[props.row.name] ? (props.row.enabled ? 'block' : 'check_circle') : 'none'"
              class="q-ml-xs"
              @click="onEnableUser(props.row)"
            />
          </div>
        </q-td>
      </template>
      <template v-slot:body-cell-enabled="props">
        <q-td :props="props">
          <q-icon :name="props.value ? 'check' : 'close'" size="1.5rem" />
        </q-td>
      </template>
    </q-table>

    <confirm-dialog
      v-if="selectedUser"
      v-model="showDelete"
      :title="$t('delete')"
      :text="$t('delete_user_confirm', { user: selectedUser.name })"
      @confirm="doDeleteUser"
    />

    <add-user-dialog v-model="showAddUser" :user="selectedUser" @update:modelValue="onUserAdded"></add-user-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { SubjectCredentialsDto } from 'src/models/Opal';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import AddUserDialog from 'src/components/admin/AddUserDialog.vue';
import { notifyError } from 'src/utils/notify';

const usersStore = useUsersStore();
const users = computed(() => usersStore.users || []);
const { t } = useI18n();
const filteredUserCount = ref(0);
const filter = ref('');
const loading = ref(false);
const showAddUser = ref(false);
const showDelete = ref(false);
const selectedUser = ref<SubjectCredentialsDto | null>(null);

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
    name: 'groups',
    label: t('groups'),
    align: 'left',
    field: 'groups',
    format: (val: string[]) => (val || []).join(', '),
    style: 'width: 50%',
  },
  {
    name: 'authentication',
    label: t('authentication'),
    align: 'left',
    field: 'authenticationType',
    format: (val: string) => t(`auth_types.${val}`),
  },
  {
    name: 'enabled',
    label: t('enabled'),
    align: 'center  ',
    field: 'enabled',
  },
];

const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});

const toolsVisible = ref<{ [key: string]: boolean }>({});

const filterUsers = (rows: any[], terms: string) => {
  const query = filter.value.length > 0 ? filter.value.toLowerCase() : '';

  const result = rows.filter((row) => {
    return Object.values(row).some((val) => {
      return String(val).toLowerCase().includes(query);
    });
  });

  filteredUserCount.value = result.length;

  return result;
};

function onOverRow(row: SubjectCredentialsDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: SubjectCredentialsDto) {
  toolsVisible.value[row.name] = false;
}

function onEditUser(user: SubjectCredentialsDto) {
  showAddUser.value = true;
  selectedUser.value = user;
}

async function onDeleteUser(user: SubjectCredentialsDto) {
  selectedUser.value = user;
  showDelete.value = true;
}

async function doDeleteUser() {
  showDelete.value = false;
  if (selectedUser.value == null) {
    return;
  }

  const toDelete: SubjectCredentialsDto | null = selectedUser.value;
  selectedUser.value = null;

  try {
    await usersStore.deleteUser(toDelete);
  } catch (err) {
    notifyError(err);
  }
}

async function onEnableUser(user: SubjectCredentialsDto) {
  user.enabled = !user.enabled;
  try {
    await usersStore.updateUser(user);
  } catch (err) {
    notifyError(err);
  }
}

function onAddWithPassword() {
  console.log('Add user with password');
  showAddUser.value = true;
}

function onAddWithCertificate() {
  showAddUser.value = true;
}

function onUserAdded() {
  console.log('User added');
  selectedUser.value = null;
  showAddUser.value = false;
}

onMounted(async () => {
  loading.value = true;
  await usersStore.initUsers();
  loading.value = false;
});
</script>
