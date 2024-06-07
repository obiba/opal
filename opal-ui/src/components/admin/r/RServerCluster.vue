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
          row-key="name"
          wrap-cells
          :pagination="initialPagination"
          :filter="filter"
        >
          <template v-slot:top-left>
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
          <template v-slot:body-cell-name="props">
            <q-td :props="props">
              <span class="text-primary">{{  props.value }}</span>
            </q-td>
          </template>
          <template v-slot:body-cell-rserver="props">
            <q-td :props="props">
              <span>{{  props.value.split('~')[0] }}</span>
              <code class="on-right">{{  props.value.split('~')[1].split('-')[0] }}</code>
            </q-td>
          </template>
        </q-table>
      </q-tab-panel>
   </q-tab-panels>
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
    field: (row: RPackageDto) => getDescriptionValue(row, 'Title'),
    sortable: true,
  },
  {
    name: 'version',
    required: true,
    label: t('version'),
    align: 'left',
    field: (row: RPackageDto) => getDescriptionValue(row, 'Version'),
    classes: 'text-caption',
    sortable: true,
  },
  {
    name: 'built',
    required: true,
    label: t('built'),
    align: 'left',
    field: (row: RPackageDto) => `R ${getDescriptionValue(row, 'Built')}`,
    classes: 'text-caption',
    sortable: true,
  },
  {
    name: 'libpath',
    required: true,
    label: t('libpath'),
    align: 'left',
    field: (row: RPackageDto) => getDescriptionValue(row, 'LibPath'),
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


</script>
