<template>
  <div>
    <div class="text-h5 q-mb-md">
      {{ $t('profile_acls') }}
    </div>
    <div class="text-help q-mb-md">{{ $t('profile_acls_info', {principal: $route.params.principal}) }}</div>
    <q-table
      flat
      :rows="acls"
      :columns="columns"
      row-key="resource"
      :pagination="initialPagination"
      :hide-pagination="acls.length <= initialPagination.rowsPerPage"
      :loading="loading"
    >
      <template v-slot:body-cell-resource="props">
        <q-td :props="props">
          <span>{{ props.value }} </span>
          <div class="float-right">
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="$t('delete')"
              :icon="toolsVisible[props.row.principal] ? 'delete' : 'none'"
              class="q-ml-xs"
            />
          </div>
        </q-td>
      </template>
      <template v-slot:body-cell-permissions="props">
        <q-td :props="props">
          <q-chip removable class="q-ml-none" v-for="(permission, index) in props.row.actions" :key="index" @remove="onRemovePermission(props.row, permission)">
            {{ props.col.format(permission) }}
            <q-tooltip>{{ props.col.tooltip(permission) }}</q-tooltip>
          </q-chip>
        </q-td>
      </template>
    </q-table>

    <confirm-dialog
      v-model="showDelete"
      :title="$t('delete')"
      :text="$t('delete_profile_acl_confirm', { permission: $t(`acls.${selectedAcl.permission}.label`), resource: selectedAcl.acl?.resource || ''})"
      @confirm="doRemovePermission"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { notifyError } from 'src/utils/notify';
import { Acl } from 'src/models/Opal';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';

const { t } = useI18n();

const profileAclsStore = useProfileAclsStore();
const route = useRoute();
const acls = computed(() => profileAclsStore.acls || []);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const selectedAcl = ref({permission: null as string | null, acl: null as Acl | null});
const showDelete = ref(false);

const columns = [
  {
    name: 'resource',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'resource',
    format: (val: string) => val,
    sortable: true,
    style: 'width: 25%',
  },
  {
    name: 'permissions',
    label: t('permissions'),
    align: 'left',
    field: 'actions',
    format: (val: string) => t(`acls.${val}.label`),
    tooltip: (val: string) => t(`acls.${val}.description`),
  }
];

const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});

function onRemovePermission(acl: Acl, permission: string) {
  console.log('onRemovePermission', acl, permission);
  selectedAcl.value = {permission, acl};
  showDelete.value = true;
}

async function doRemovePermission() {
  console.log('doRemovePermission');
  showDelete.value = false;
  const toDelete = selectedAcl.value;
  selectedAcl.value = {acl: null, permission: null};

  if (toDelete.acl && toDelete.permission) {
    try {
      await profileAclsStore.deleteAcl(`${route.params.principal}`, toDelete.permission, toDelete.acl.resource);
      await profileAclsStore.initAcls(`${route.params.principal}`);
    } catch (err) {
      notifyError(err);
    }
  }
}

const loading = ref(false);

onMounted(async () => {
  loading.value = true;
  profileAclsStore.initAcls(`${route.params.principal}`).then(() => {
    loading.value = false;
  });
});
</script>
