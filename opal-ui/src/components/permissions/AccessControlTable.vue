<template>
  <div>
    <div v-if="rows.length === 0">
      <div class="text-hint q-mt-md">{{ t('no_permissions') }}</div>
    </div>
    <q-table
      v-else
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
      <template v-slot:top v-if="!hideDelete">
        <q-btn
          outline
          color="red"
          icon="delete"
          size="sm"
          :disable="selectedAcls.length === 0"
          @click="onDeleteAcls"
        ></q-btn>
      </template>
      <template v-slot:body-cell-resource="props">
        <q-td :props="props">
          <q-item-section>
            <q-item-label
              ><router-link :to="props.row.url">{{ props.row.title }}</router-link></q-item-label
            >
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
  </div>
  
</template>

<script setup lang="ts">
import type { Acl, Subject_SubjectType } from 'src/models/Opal';
import { DefaultAlignment } from 'src/components/models';

interface Props {
  modelValue: Acl[];
  acls: Acl[];
  principal?: string;
  type?: Subject_SubjectType;
  loading: boolean;
  hideDelete?: boolean;
  onDeleteAcls: () => void;
}

const props = defineProps<Props>();
const emit = defineEmits(['update:modelValue']);
const { t } = useI18n();

const initialPagination = ref({
  sortBy: 'resource',
  descending: false,
  page: 1,
  rowsPerPage: 5,
  minRowsForPagination: 5,
});

interface Row {
  resource: string;
  url: string;
  title: string;
  caption: string;
}

const selectedAcls = computed({
  get: () => props.modelValue,
  set: (value: Acl[]) => emit('update:modelValue', value),
});
const columns = computed(() => [
  {
    name: 'resource',
    required: true,
    label: t('name'),
    align: DefaultAlignment,
    field: 'resource',
    sortable: true,
    style: 'width: 25%',
  },
  {
    name: 'permissions',
    label: t('permissions'),
    align: DefaultAlignment,
    field: 'action',
    format: (val: string) => t(`acls.${val}.label`),
    tooltip: (val: string) => t(`acls.${val}.description`),
  },
]);

const cases = [
  { regex: /\/files\/(.*)$/, type: 'folder_file' },
  { regex: /\/datasource\/([^/]+)\/table\/([^/]+)\/variable\/(.*)$/, type: 'variable' },
  { regex: /\/datasource\/([^/]+)\/table\/(.*)$/, type: 'table' },
  { regex: /\/datasource\/([^/]+)\/view\/(.*)$/, type: 'view' },
  { regex: /\/datasource\/(.*)$/, type: 'project' },
  { regex: /\/project\/(.*)\/resources$/, type: 'resources' },
  { regex: /\/project\/(.*)\/resource\/(.*)$/, type: 'resource' },
  { regex: /\/project\/(.*)$/, type: 'project' },
  { regex: /\/r$/, type: 'r_service', url: '/admin/rservers' },
  { regex: /\/datashield$/, type: 'datashield_service', url: '/admin/datashield' },
  { regex: /\/datashield\/profile\/(.*)$/, type: 'datashield_profile', url: '/admin/datashield' },
  { regex: /^\/$/, type: 'project|system', url: '/admin/settings' },
];

if (props.principal) {
  cases.unshift({ regex: new RegExp(`/files/home/${props.principal}$`), type: 'home_folder' });
}

const rows = computed(() => {
  return (props.acls || []).map((acl) => {
    const result = {
      ...acl,
      ...{
        url: acl.resource.replace(/^\/datasource\//g, '/project/'),
        title: acl.resource,
        caption: '',
      },
    };

    cases.some((item) => {
      const match = item.regex.exec(acl.resource);
      if (match) {
        result.caption = t(item.type);
        switch (item.type) {
          case 'home_folder':
          case 'folder_file':
            result.title = result.url.replace(/^\/files\//, '/');
            break;
          case 'project':
            result.title = `${match[1]}`;
            break;
          case 'table':
            result.title = `${match[1]}.${match[2]}`;
            break;
          case 'view':
            result.title = `${match[1]}.${match[2]}`;
            result.caption = t(item.type);
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
            if (acl.actions.includes('SYSTEM_ALL')) {
              result.title = t('system_settings');
              result.caption = t('system');
            } else {
              result.title = t('project_settings');
              result.caption = t('project');
            }
            break;
        }
        return true;
      }
      return false;
    });

    return result;
  });
});

function sortRows(rows: readonly Row[], sortBy: string, descending: boolean) {
  const data = [...rows];

  data.sort((a: Row, b: Row): number => {
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
    return 0; // default when no sorting
  });

  return data;
}
</script>
