<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('users_and_groups')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div>
        <div class="q-py-md">
          <q-btn-dropdown
            color="primary"
            :label="$t('users_add')"
            icon="add"
            size="sm"
          >
            <q-list>
              <q-item clickable v-close-popup @click="onAddWithPassword">
                <q-item-section>
                  <q-item-label>Photos</q-item-label>
                </q-item-section>
              </q-item>

              <q-item clickable v-close-popup @click="onAddWithCertificate">
                <q-item-section>
                  <q-item-label>Videos</q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-btn-dropdown>
        </div>

        <div class="text-h5">
          <q-icon name="people" size="sm" class="q-mb-xs"></q-icon
          ><span class="on-right">{{ $t('users') }}</span>
        </div>
        <p>{{ $t('users_info') }}</p>

        <q-table
          ref=" usersTableRef"
          flat
          bordered
          :filter="() => filter"
          :filter-method="filterUsers"
          :rows="users"
          :columns="users_columns"
          row-key="name"
          :pagination="usersInitialPagination"
          :hide-pagination="
            filteredUserCount <= usersInitialPagination.rowsPerPage
          "
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
          <template v-slot:body-cell-enabled="props">
            <q-td :props="props">
              <q-icon :name="props.value ? 'check' : 'close'" size="1.5rem" />
            </q-td>
          </template>
          <template v-slot:body-cell-actions="props">
            <q-td :props="props">
              <q-btn
                flat
                dense
                size="xs"
                icon="edit"
                color="primary"
                @click="editRow(props.row)"
              >
                <q-tooltip>{{ $t('user_edit') }}</q-tooltip>
              </q-btn>
              <q-btn
                flat
                dense
                size="xs"
                icon="delete"
                color="negative"
                @click="deleteRow(props.row)"
              >
              <q-tooltip>{{ $t('user_delete') }}</q-tooltip>
              </q-btn>
              <q-btn
                flat
                dense
                size="xs"
                icon="block"
                color="secondary"
                @click="viewRow(props.row)"
              >
              <q-tooltip>{{ $t('user_disable') }}</q-tooltip>
              </q-btn>
            </q-td>
          </template>
        </q-table>
      </div>
      <div>
        <div class="text-h5 q-pt-lg">
          <q-icon name="groups" size="sm" class="q-mb-xs"></q-icon
          ><span class="on-right">{{ $t('groups') }}</span>
        </div>
        <p>{{ $t('groups_info') }}</p>

        <q-table
          ref="groupsTableRef"
          flat
          bordered
          :rows="groups"
          :columns="groups_columns"
          row-key="name"
          :pagination="groupsInitialPagination"
          :hide-pagination="
            groups.length <= groupsInitialPagination.rowsPerPage
          "
          :loading="loading"
        >
        </q-table>
      </div>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
const { t } = useI18n();

const usersGroupsStore = useUsersGroupsStore();
const users = computed(() => usersGroupsStore.users || []);
const groups = computed(() => usersGroupsStore.groups || []);
const usersTableRef = ref(null);
const groupsTableRef = ref(null);
const filteredUserCount = ref(0);
const filter = ref('');
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

const users_columns = [
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
    name: 'enabled',
    label: t('enabled'),
    align: 'center  ',
    field: 'enabled',
    format: (val: boolean) => (val ? t('yes') : t('no')),
  },
  {
    name: 'authentication',
    label: t('authentication'),
    align: 'left',
    field: 'authenticationType',
    format: (val: string) => t(`auth_types.${val}`),
  },
  {
    name: 'actions',
    label: t('actions'),
    align: 'center',
    style: 'width: 1rem',
  },
];

const groups_columns = [
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

const usersInitialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});

const groupsInitialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});

const loading = ref(false);

function onAddWithPassword() {
  console.log('onAddWithPassword');
}
function onAddWithCertificate() {
  console.log('onAddWithCertificate');
}

onMounted(async () => {
  loading.value = true;
  await usersGroupsStore.initUsersAndGroups();
  filteredUserCount.value = users.value.length;
  loading.value = false;
});
</script>
