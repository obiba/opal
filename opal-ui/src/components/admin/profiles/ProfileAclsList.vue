<template>
  <div>
    <div class="text-h5 q-mb-md">
      {{ $t('profile_acls') }}
    </div>
    <div class="text-help q-mb-md">{{ $t('profile_acls_info') }}</div>
    <q-table
      flat
      :rows="rows"
      :columns="columns"
      row-key="resource"
      :sort-method="sortRows"
      binary-state-sort
      :pagination="initialPagination"
      :hide-pagination="rows.length <= initialPagination.rowsPerPage"
      :loading="loading"
      selection="multiple"
      v-model:selected="selectedAcls"
    >
      <template v-slot:top>
        <q-btn outline color="red" icon="delete" size="sm" :disable="selectedAcls.length === 0" @click="onDeleteAcls"></q-btn>
      </template>
      <template v-slot:body-cell-resource="props">
        <q-td :props="props">
          <q-item-section>
            <q-item-label><router-link :to="props.row.url">{{ props.row.title }}</router-link></q-item-label>
            <q-item-label caption lines="2">{{ props.row.caption }}</q-item-label>
          </q-item-section>
        </q-td>
      </template>
      <template v-slot:body-cell-permissions="props">
        <q-td :props="props">
          <span class="q-ml-none" v-for="(permission, index) in props.row.actions" :key="index">
            {{ props.col.format(permission) }}
            <q-tooltip>{{ props.col.tooltip(permission) }}</q-tooltip>
          </span>
        </q-td>
      </template>
    </q-table>

    <confirm-dialog
      v-model="showDeletes"
      :title="$t('delete')"
      :text="$t('delete_profile_acl_confirm', {count: selectedAcls.length})"
      @confirm="doRemoveAcls"
    />
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'ProfileAclsList',
});
</script>

<script setup lang="ts">
import { onMounted } from 'vue';
import { notifyError } from 'src/utils/notify';
import { Acl } from 'src/models/Opal';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';

const { t } = useI18n();
const profileAclsStore = useProfileAclsStore();
const route = useRoute();
const selectedAcls = ref<Acl[]>([]);
const showDeletes = ref(false);

const principal = computed(() => route.params.principal);

const columns = [
  {
    name: 'resource',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'resource',
    sortable: true,
    style: 'width: 25%',
  },
  {
    name: 'permissions',
    label: t('permissions'),
    align: 'left',
    field: 'action',
    format: (val: string) => t(`acls.${val}.label`),
    tooltip: (val: string) => t(`acls.${val}.description`),
  }
];

const initialPagination = ref({
  sortBy: 'resource',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});

function onDeleteAcls() {
  showDeletes.value = true;
}

async function doRemoveAcls() {
  showDeletes.value = false;
  const toDelete: Acl[] = selectedAcls.value;
  selectedAcls.value = [];

  try {
    await profileAclsStore.deleteAcls(toDelete);
    await profileAclsStore.initAcls(`${route.params.principal}`);
  } catch (err) {
    notifyError(err);
  }
}

const cases = [
  { regex: new RegExp(`/files/home/${principal.value}$`), type: 'home_folder' },
  { regex: /\/files\/(.*)$/, type: 'folder' },
  { regex: /\/datasource\/([^\/]+)\/table\/([^\/]+)\/variable\/(.*)$/, type: 'variable' },
  { regex: /\/datasource\/([^\/]+)\/table\/(.*)$/, type: 'table' },
  { regex: /\/datasource\/(.*)$/, type: 'project' },
  { regex: /\/project\/(.*)\/resources$/, type: 'resources' },
  { regex: /\/project\/(.*)\/resource\/(.*)$/, type: 'resource' },
  { regex: /\/r$/, type: 'r_service', url: '/admin/rservers' },
  { regex: /\/datashield$/, type: 'datashield_service', url: '/admin/datashield' },
  { regex: /\/datashield\/profile\/(.*)$/, type: 'datashield_profile', url: '/admin/datashield' },
  { regex: /^\/$/, type: 'project|system', url: '/admin/settings' },
];

const rows = computed(() => {
  return (profileAclsStore.acls || []).map((acl) => {
    const result = {
      ...acl,
      ...{
        url: acl.resource.replace(/^\/datasource\//g, '/project/'),
        title: acl.resource,
        caption: '',
      }
    };

    cases.some((item) => {
      const match = item.regex.exec(acl.resource);
        if (match) {
          result.caption = t(item.type);
          switch (item.type) {
            case 'home_folder':
            case 'folder':
              result.title = result.url.replace(/^\/files\//, '/');
              break;
            case 'project':
              result.title = `${match[1]}`;
              break;
            case 'table':
              result.title = `${match[1]}.${match[2]}`;
              break;
            case 'variable':
              result.title = `${match[1]}.${match[2]}.${match[3]}`;
              break;
            case 'resources':
              result.title = `${match[1]}`;
              break;
            case 'resource':
              result.title = `${match[1]}.${match[2]}`;
              break;
            case 'r_service':
              result.title = 'R';
              result.caption = t(item.type);
              result.url = item.url || '';
              break;
            case 'datashield_service':
              result.title = 'DataSHIELD';
              result.caption = t(item.type);
              result.url = item.url || '';
              break;
            case 'datashield_profile':
              result.title = `${match[1]}`;
              result.caption = t(item.type);
              result.url = item.url || '';
              break;
            case 'project|system':
              result.url = item.url || '';
              const isSystem = acl.actions.includes('SYSTEM_ALL');
              result.title = isSystem ? t('system_settings') : t('project_settings');
              result.caption = isSystem ? t('system') : t('project');
              break;
          }
          return true;
        }
        return false;
      });

      return result;
    });

  }
);

function sortRows(rows: readonly any[], sortBy: string, descending: boolean) {
  const data = [...rows];

  data.sort((a: any, b: any): any => {
    if (sortBy) {
      const sortA = a['caption'];
      const sortB = b['caption'];
      if (sortA < sortB) {
        return descending ? 1 : -1;
      }
      if (sortA > sortB) {
        return descending ? -1 : 1;
      }
      return 0;
    }
  });

  return data;
}

const loading = ref(false);

onMounted(async () => {
  loading.value = true;
  profileAclsStore.initAcls(`${route.params.principal}`).then(() => {
    loading.value = false;
  });
});
</script>
