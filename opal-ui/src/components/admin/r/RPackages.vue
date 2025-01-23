<template>
  <div>
    <div v-if="!systemStore.generalConf.allowRPackageManagement" class="box-info">
      <q-icon name="info" />
      {{ t('r_packages_management_forbidden') }}
    </div>
    <q-table
      flat
      :rows="packages"
      :columns="columns"
      :row-key="getPackageKey"
      wrap-cells
      :pagination="initialPagination"
      :filter="filter"
      :filter-method="onFilter"
    >
      <template v-slot:top-left>
        <q-btn-dropdown color="primary" icon="add" :label="t('install')" size="sm" class="on-left" :disable="!systemStore.generalConf.allowRPackageManagement">
          <q-list>
            <q-item clickable v-close-popup @click="onShowInstallPackages">
              <q-item-section>
                <q-item-label>{{ t('install_r_package') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item clickable v-close-popup @click="onShowUpdatePackages">
              <q-item-section>
                <q-item-label>{{ t('update_all_r_packages') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>
        <q-btn outline color="secondary" icon="refresh" :title="t('refresh')" size="sm" @click="updateRPackages" />
      </template>
      <template v-slot:top-right>
        <q-input dense debounce="500" v-model="filter">
          <template v-slot:append>
            <q-icon name="search" />
          </template>
        </q-input>
      </template>
      <template v-slot:body="props">
        <q-tr :props="props" @mouseover="onOverPackage(props.row)" @mouseleave="onLeavePackage(props.row)">
          <q-td key="name" :props="props">
            <span class="text-primary">{{ props.row.name }}</span>
            <div class="float-right">
              <q-btn
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :icon="packageToolsVisible[getPackageKey(props.row)] ? 'visibility' : 'none'"
                class="q-ml-xs"
                @click="onShowViewPackage(props.row)"
              />
              <q-btn
                v-if="systemStore.generalConf.allowRPackageManagement"
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="t('delete')"
                :icon="packageToolsVisible[getPackageKey(props.row)] ? 'delete' : 'none'"
                class="q-ml-xs"
                @click="onShowDeletePackage(props.row)"
              />
            </div>
          </q-td>
          <q-td key="title" :props="props">
            {{ getDescriptionValue(props.row, 'Title') }}
          </q-td>
          <q-td key="version" :props="props">
            {{ getDescriptionValue(props.row, 'Version') }}
          </q-td>
          <q-td key="built" :props="props">
            {{ `R ${getDescriptionValue(props.row, 'Built')}` }}
          </q-td>
          <q-td key="libpath" :props="props">
            {{ getDescriptionValue(props.row, 'LibPath') }}
          </q-td>
          <q-td key="rserver" :props="props">
            <span>{{ props.row.rserver.split('~')[0] }}</span>
            <code class="on-right">{{ props.row.rserver.split('~')[1].split('-')[0] }}</code>
          </q-td>
        </q-tr>
      </template>
    </q-table>
    <confirm-dialog
      v-model="showDelete"
      :title="t('delete')"
      :text="t('delete_r_package_confirm', { name: pkg?.name })"
      @confirm="onDeletePackage"
    />
    <install-r-package-dialog v-model="showInstall" :cluster="cluster" />
    <update-r-packages-dialog v-model="showUpdate" :cluster="cluster" />
    <view-r-package-dialog v-if="pkg" v-model="showView" :pkg="pkg" />
  </div>
</template>

<script setup lang="ts">
import type { RServerDto, RServerClusterDto, RPackageDto } from 'src/models/OpalR';
import UpdateRPackagesDialog from 'src/components/admin/r/UpdateRPackagesDialog.vue';
import InstallRPackageDialog from 'src/components/admin/r/InstallRPackageDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import ViewRPackageDialog from 'src/components/admin/r/ViewRPackageDialog.vue';
import { getDescriptionValue, getPackageKey } from 'src/utils/r';
import { DefaultAlignment } from 'src/components/models';

interface Props {
  cluster: RServerClusterDto;
}

const props = defineProps<Props>();

const systemStore = useSystemStore();
const rStore = useRStore();
const { t } = useI18n();

const packages = ref<RPackageDto[]>([]);
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const filter = ref('');
const pkg = ref<RPackageDto>();
const packageToolsVisible = ref<{ [key: string]: boolean }>({});
const showInstall = ref(false);
const showUpdate = ref(false);
const showDelete = ref(false);
const showView = ref(false);

const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: DefaultAlignment,
    field: 'name',
    sortable: true,
  },
  {
    name: 'title',
    required: true,
    label: t('title'),
    align: DefaultAlignment,
    field: 'title',
    sortable: true,
  },
  {
    name: 'version',
    required: true,
    label: t('version'),
    align: DefaultAlignment,
    classes: 'text-caption',
    field: 'version',
    sortable: true,
  },
  {
    name: 'built',
    required: true,
    label: t('built'),
    align: DefaultAlignment,
    classes: 'text-caption',
    field: 'built',
    sortable: true,
  },
  {
    name: 'libpath',
    required: true,
    label: t('libpath'),
    align: DefaultAlignment,
    classes: 'text-caption',
    field: 'libpath',
    sortable: true,
  },
  {
    name: 'rserver',
    required: true,
    label: t('server'),
    align: DefaultAlignment,
    field: 'rserver',
    sortable: true,
  },
]);

function onFilter() {
  if (filter.value.length === 0) {
    return packages.value;
  }
  const query = filter.value && filter.value.length > 0 ? filter.value.toLowerCase() : '';
  return packages.value.filter((pkg) => {
    const description = ['Title', 'Version', 'Built', 'LibPath'].map((key) => getDescriptionValue(pkg, key)).join(' ');
    return `${pkg.name} ${pkg.rserver} ${description}`.toLowerCase().includes(query);
  });
}

onMounted(() => {
  updateRPackages();
});

watch(
  () => props.cluster,
  () => {
    updateRPackages();
  }
);

function updateRPackages() {
  if (props.cluster.servers.length) {
    if (props.cluster.servers.some((server: RServerDto) => server.running)) {
      rStore.getRPackages(props.cluster.name).then((res: RPackageDto[]) => {
        packages.value = res;
      });
    } else {
      packages.value = [];
    }
  }
}

function onShowInstallPackages() {
  showInstall.value = true;
}

function onShowUpdatePackages() {
  showUpdate.value = true;
}

function onShowDeletePackage(rPackage: RPackageDto) {
  pkg.value = rPackage;
  showDelete.value = true;
}

function onShowViewPackage(rPackage: RPackageDto) {
  pkg.value = rPackage;
  showView.value = true;
}

function onDeletePackage() {
  if (!pkg.value) return;
  rStore.deleteRPackage(props.cluster.name, pkg.value.name).finally(() => {
    updateRPackages();
  });
}

function onOverPackage(row: RPackageDto) {
  packageToolsVisible.value[getPackageKey(row)] = true;
}

function onLeavePackage(row: RPackageDto) {
  packageToolsVisible.value[getPackageKey(row)] = false;
}
</script>
