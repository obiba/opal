<template>
  <div>
    <q-table
      flat
      :filter="filter"
      :filter-method="onFilter"
      :rows="users"
      :columns="columns"
      row-key="name"
      :pagination="initialPagination"
      :hide-pagination="users.length <= initialPagination.rowsPerPage"
      :loading="loading"
    >
      <template v-slot:top-left>
        <q-btn-dropdown color="primary" :label="t('add')" icon="add" size="sm">
          <q-list>
            <q-item clickable v-close-popup @click.prevent="onAddWithPassword">
              <q-item-section>
                <q-item-label>{{ t('user_add_with_pwd') }}</q-item-label>
              </q-item-section>
            </q-item>

            <q-item clickable v-close-popup @click.prevent="onAddWithCertificate">
              <q-item-section>
                <q-item-label>{{ t('user_add_with_crt') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>
      </template>
      <template v-slot:top-right>
        <q-input dense clearable debounce="400" color="primary" v-model="filter">
          <template v-slot:append>
            <q-icon name="search" />
          </template>
        </q-input>
      </template>
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
              :title="t('edit')"
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
              :title="t('delete')"
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
              :title="props.row.enabled ? t('disable') : t('enable')"
              :icon="toolsVisible[props.row.name] ? (props.row.enabled ? 'block' : 'check_circle') : 'none'"
              class="q-ml-xs"
              @click="onEnableUser(props.row)"
            />
          </div>
        </q-td>
      </template>
      <template v-slot:body-cell-groups="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-chip class="q-ml-none" v-for="group in props.col.format(props.row.groups)" :key="group.name">
            {{ group }}
          </q-chip>
        </q-td>
      </template>
      <template v-slot:body-cell-authentication="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <span class="text-caption">{{ props.value }}</span>
        </q-td>
      </template>
      <template v-slot:body-cell-enabled="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-icon :name="props.value ? 'check' : 'close'" size="sm" />
        </q-td>
      </template>
    </q-table>

    <confirm-dialog
      v-if="selectedUser"
      v-model="showDelete"
      :title="t('delete')"
      :text="t('delete_user_confirm', { user: selectedUser.name })"
      @confirm="doDeleteUser"
    />

    <add-user-dialog
      v-model="showAddUser"
      :user="selectedUser"
      :authentication-type="authenticationType"
      @update:modelValue="onUserAdded"
    ></add-user-dialog>
  </div>
</template>

<script setup lang="ts">
import { type SubjectCredentialsDto, SubjectCredentialsDto_AuthenticationType } from 'src/models/Opal';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import AddUserDialog from 'src/components/admin/users/AddUserDialog.vue';
import { notifyError } from 'src/utils/notify';
import { DefaultAlignment } from 'src/components/models';

const usersStore = useUsersStore();
const groupsStore = useGroupsStore();
const users = computed(() => usersStore.users || []);
const { t } = useI18n();
const filter = ref('');
const loading = ref(false);
const showAddUser = ref(false);
const showDelete = ref(false);
const authenticationType = ref(SubjectCredentialsDto_AuthenticationType.PASSWORD);
const selectedUser = ref<SubjectCredentialsDto | null>(null);

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
    name: 'groups',
    label: t('groups'),
    align: DefaultAlignment,
    field: 'groups',
    format: (val: string[]) => (val || []).filter((val) => val && val.length > 0),
  },
  {
    name: 'authentication',
    label: t('authentication'),
    align: DefaultAlignment,
    field: 'authenticationType',
    format: (val: string) => t(`auth_types.${val}`),
  },
  {
    name: 'enabled',
    label: t('enabled'),
    align: DefaultAlignment,
    field: 'enabled',
  },
]);

const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});

const toolsVisible = ref<{ [key: string]: boolean }>({});

function onFilter() {
  if (filter.value.length === 0) {
    return users.value;
  }
  const query = filter.value && filter.value.length > 0 ? filter.value.toLowerCase() : '';
  const result = users.value.filter((row) => {
    return Object.values(row).some((val) => {
      return String(val).toLowerCase().includes(query);
    });
  });

  return result;
}

function onOverRow(row: SubjectCredentialsDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: SubjectCredentialsDto) {
  toolsVisible.value[row.name] = false;
}

function onEditUser(user: SubjectCredentialsDto) {
  showAddUser.value = true;
  selectedUser.value = user;
  authenticationType.value = user.authenticationType;
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
    await groupsStore.initGroups();
  } catch (err) {
    notifyError(err);
  }
}

async function onEnableUser(user: SubjectCredentialsDto) {
  user.enabled = !user.enabled;
  usersStore.updateUser(user).catch(notifyError);
}

function onAddWithPassword() {
  showAddUser.value = true;
  authenticationType.value = SubjectCredentialsDto_AuthenticationType.PASSWORD;
}

function onAddWithCertificate() {
  showAddUser.value = true;
  authenticationType.value = SubjectCredentialsDto_AuthenticationType.CERTIFICATE;
}

async function onUserAdded() {
  groupsStore.initGroups();
  selectedUser.value = null;
  showAddUser.value = false;
}

onMounted(async () => {
  loading.value = true;
  usersStore.initUsers().then(() => {
    loading.value = false;
  });
});
</script>
