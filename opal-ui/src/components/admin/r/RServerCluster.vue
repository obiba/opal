<template>
  <div>
    <q-tabs
      v-model="tab"
      dense
      class="text-grey"
      active-color="primary"
      indicator-color="primary"
      align="justify"
      narrow-indicator
    >
      <q-tab name="servers" :label="t('servers')" />
      <q-tab name="packages" :label="t('packages')" />
    </q-tabs>
    <q-separator />
    <q-tab-panels v-model="tab">
      <q-tab-panel name="servers">
        <div class="row">
          <q-btn
            v-if="cluster.servers.length"
            color="primary"
            :label="$t('download_logs')"
            icon="download"
            size="sm"
            @click="onClusterLogsDownload" />
        </div>
        <q-table
          flat
          :rows="cluster.servers"
          :columns="columns"
          row-key="name"
          :rows-per-page-options="[0]"
          hide-pagination
        >
        <template v-slot:body-cell-name="props">
          <q-td :props="props">
            <span class="text-primary">{{  props.row.app.name }}</span>
            <code class="on-right" :title="props.value">{{ props.row.app.id.split('-')[0] }}</code>
            <div class="float-right">
              <q-btn
                v-if="!props.row.running"
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="$t('start')"
                icon="start"
                class="q-ml-xs"
                @click="onRServerStart(props.row)"
              />
              <q-btn
                v-if="props.row.running"
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="$t('stop')"
                icon="highlight_off"
                class="q-ml-xs"
                @click="onRServerStop(props.row)"
              />
              <q-btn
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="$t('download_logs')"
                icon="download"
                class="q-ml-xs"
                @click="onRServerLogsDownload(props.row)"
              />
            </div>
          </q-td>
        </template>
        <template v-slot:body-cell-tags="props">
          <q-td :props="props">
            <q-badge
              color="primary"
              v-for="tag in props.value"
              :label="tag"
              :key="tag"
              class="on-left"
            />
          </q-td>
        </template>
        <template v-slot:body-cell-url="props">
          <q-td :props="props">
            <a :href="props.row.app.server" target="_blank">{{ props.row.app.server }}</a>
          </q-td>
        </template>
        <template v-slot:body-cell-running="props">
          <q-td :props="props">
            <q-icon
              name="circle"
              size="sm"
              :color="props.value ? 'green' : 'red'"
            />
          </q-td>
        </template>
      </q-table>
      </q-tab-panel>
      <q-tab-panel name="packages">
        <q-table
          flat
          :rows="filteredPackages"
          :columns="packagesColumns"
          :row-key="getPackageKey"
          wrap-cells
          :pagination="initialPagination"
          :filter="filter"
        >
          <template v-slot:top-left>
            <q-btn-dropdown color="primary" icon="add" :label="$t('install')" size="sm" class="on-left">
            <q-list>
              <q-item clickable v-close-popup @click="onShowInstallPackages">
                <q-item-section>
                  <q-item-label>{{ $t('install_r_package') }}</q-item-label>
                </q-item-section>
              </q-item>
              <q-item clickable v-close-popup @click="onShowUpdatePackages">
                <q-item-section>
                  <q-item-label>{{ $t('update_all_r_packages') }}</q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-btn-dropdown>
            <q-btn
              color="secondary"
              text-color="white"
              icon="refresh"
              :label="$t('refresh')"
              size="sm"
              @click="updateRPackages" />
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
                <span class="text-primary">{{  props.row.name }}</span>
                <div class="float-right" >
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
                    rounded
                    dense
                    flat
                    size="sm"
                    color="secondary"
                    :title="$t('delete')"
                    :icon="packageToolsVisible[getPackageKey(props.row)] ? 'delete' : 'none'"
                    class="q-ml-xs"
                    @click="onShowDeletePackage(props.row)"
                  />
                </div>
              </q-td>
              <q-td key="title" :props="props">
                {{  getDescriptionValue(props.row, 'Title') }}
              </q-td>
              <q-td key="version" :props="props">
                {{  getDescriptionValue(props.row, 'Version') }}
              </q-td>
              <q-td key="built" :props="props">
                {{  `R ${getDescriptionValue(props.row, 'Built')}` }}
              </q-td>
              <q-td key="libpath" :props="props">
                {{  getDescriptionValue(props.row, 'LibPath') }}
              </q-td>
              <q-td key="rserver" :props="props">
                <span>{{  props.row.rserver.split('~')[0] }}</span>
                <code class="on-right">{{  props.row.rserver.split('~')[1].split('-')[0] }}</code>
              </q-td>
            </q-tr>
          </template>
        </q-table>
      </q-tab-panel>
   </q-tab-panels>
   <confirm-dialog v-model="showDeletePackage" :title="$t('delete')" :text="$t('delete_r_package_confirm', { name: pkg?.name })" @confirm="onDeletePackage" />
   <install-r-dialog v-model="showInstallPackage" :cluster="cluster" />
   <update-r-dialog v-model="showUpdatePackages" :cluster="cluster" />
   <view-r-dialog v-model="showViewPackage" :pkg="pkg" />
  </div>
</template>


<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'RServerCluster',
});
</script>
<script setup lang="ts">
import { baseUrl } from 'src/boot/api';
import { RServerClusterDto, RServerDto, RPackageDto } from 'src/models/OpalR';
import { getSizeLabel } from 'src/utils/files';
import UpdateRDialog from 'src/components/admin/r/UpdateRDialog.vue';
import InstallRDialog from 'src/components/admin/r/InstallRDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import ViewRDialog from 'src/components/admin/r/ViewRDialog.vue';

interface Props {
  cluster: RServerClusterDto
}

const props = defineProps<Props>();

const { t } = useI18n();
const rStore = useRStore();

const tab = ref<string>('servers');
const packages = ref<RPackageDto[]>([]);
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const filter = ref('');
const showInstallPackage = ref(false);
const showUpdatePackages = ref(false);
const showDeletePackage = ref(false);
const showViewPackage = ref(false);
const pkg = ref<RPackageDto>();
const packageToolsVisible = ref<{ [key: string]: boolean }>({});

onMounted(() => {
  updateRPackages();
});

watch(() => props.cluster, () => {
  updateRPackages();
});

const filteredPackages = computed(() => {
  if (filter.value.length === 0) {
    return packages.value;
  }
  const query = !!filter.value && filter.value.length > 0 ? filter.value.toLowerCase() : '';
  return packages.value.filter((pkg) => {
    const description = ['Title', 'Version', 'Built', 'LibPath'].map((key) => getDescriptionValue(pkg, key)).join(' ');
    return `${pkg.name} ${pkg.rserver} ${description}`.toLowerCase().includes(query);
  });
});

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

const columns = [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'name',
  },
  {
    name: 'version',
    required: true,
    label: t('r_version'),
    align: 'left',
    field: 'version',
    classes: 'text-caption',
  },
  {
    name: 'sessionCount',
    required: true,
    label: t('r_sessions'),
    align: 'left',
    field: (row: RServerDto) => t('r_sessions_counts', { count: row.sessionCount, active: row.busySessionCount }),
  },
  {
    name: 'system',
    required: true,
    label: t('system'),
    align: 'left',
    field: (row: RServerDto) => t('r_system', { cores: row.cores, memory: getSizeLabel(row.freeMemory * 1000) }),
  },
  {
    name: 'url',
    required: true,
    label: 'URL',
    align: 'left',
    field: 'app',
  },
  {
    name: 'running',
    required: true,
    label: t('status'),
    align: 'left',
    field: 'running',
  },
];

const packagesColumns = [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'name',
    sortable: true,
  },
  {
    name: 'title',
    required: true,
    label: t('title'),
    align: 'left',
    sortable: true,
  },
  {
    name: 'version',
    required: true,
    label: t('version'),
    align: 'left',
    classes: 'text-caption',
    sortable: true,
  },
  {
    name: 'built',
    required: true,
    label: t('built'),
    align: 'left',
    classes: 'text-caption',
    sortable: true,
  },
  {
    name: 'libpath',
    required: true,
    label: t('libpath'),
    align: 'left',
    classes: 'text-caption',
    sortable: true,
  },
  {
    name: 'rserver',
    required: true,
    label: t('server'),
    align: 'left',
    field: 'rserver',
    sortable: true,
  },
];

function getPackageKey(row: RPackageDto) {
  return `${row.name}-${getDescriptionValue(row, 'LibPath')}`;
}

function getDescriptionValue(pkg: RPackageDto, key: string) {
  return pkg.description.find((entry) => entry.key === key)?.value;
}

function onClusterLogsDownload() {
  window.open(`${baseUrl}/service/r/cluster/${props.cluster.name}/_log`);
}

function onRServerLogsDownload(server: RServerDto) {
  window.open(`${baseUrl}/service/r/cluster/${props.cluster.name}/server/${server.name}/_log`);
}

function onRServerStart(server: RServerDto) {
  rStore.startRServer(props.cluster.name, server.name);
}

function onRServerStop(server: RServerDto) {
  rStore.stopRServer(props.cluster.name, server.name);
}

function onShowInstallPackages() {
  showInstallPackage.value = true;
}

function onShowUpdatePackages() {
  showUpdatePackages.value = true;
}

function onShowDeletePackage(rPackage: RPackageDto) {
  pkg.value = rPackage;
  showDeletePackage.value = true;
}

function onShowViewPackage(rPackage: RPackageDto) {
  pkg.value = rPackage;
  showViewPackage.value = true;
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
