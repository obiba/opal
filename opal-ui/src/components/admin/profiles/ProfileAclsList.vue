<template>
  <div>
    <div class="text-h5 q-mb-md">
      {{ $t('profile_acls') }}
    </div>
    <div class="text-help q-mb-md">{{ $t('profile_acls_info', {principal: $route.params.principal}) }}</div>
    <q-table
      flat
      :rows="rows"
      :columns="columns"
      row-key="resource"
      :pagination="initialPagination"
      :hide-pagination="rows.length <= initialPagination.rowsPerPage"
      :loading="loading"
      selection="multiple"
      v-model:selected="selectedAcls"
    >
      <template v-slot:top>
        <div class="row rows-center q-gutter-sm">
          <span :class="{'text-secondary': selectedAcls.length === 0}">{{ $t("delete_profiles_selected") }}</span>
          <q-btn outline color="red" icon="delete" size="sm" :disable="selectedAcls.length === 0" @click="onDeleteAcls"></q-btn>
        </div>
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
  sortBy: 'name',
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
  { regex: /\/files\/home\/(.*)$/, type: 'home_folder' },
  { regex: /\/files\/(.*)$/, type: 'folder' },
  { regex: /\/datasource\/([^\/]+)\/table\/([^\/]+)\/variable\/(.*)$/, type: 'variable' },
  { regex: /\/datasource\/([^\/]+)\/table\/(.*)$/, type: 'table' },
  { regex: /\/datasource\/(.*)$/, type: 'project' },
];

const rows = computed(() => {
  return (profileAclsStore.acls || []).map((acl) => {
    const result = {
      ...acl,
      ...{
        url: acl.resource.replace(/\/datasource\//g, '/project/'),
        title: acl.resource,
        caption: '',
      }
    };

    const input = acl.resource;

    cases.some((item) => {
      const match = item.regex.exec(input);
        if (match) {
          result.caption = t(item.type);
          switch (item.type) {
            case 'home_folder':
              result.title = `${match[1]}`;
              break;
            case 'folder':
              result.title = `${match[1]}`;
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
          }
          return true;
        }
        return false;
      });

      return result;
    });
  }
);

const loading = ref(false);

onMounted(async () => {
  loading.value = true;
  profileAclsStore.initAcls(`${route.params.principal}`).then(() => {
    loading.value = false;
  });
});
</script>
