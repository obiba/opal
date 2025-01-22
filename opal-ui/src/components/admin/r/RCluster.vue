<template>
  <div>
    <q-tabs v-model="tab" dense class="text-grey" active-color="primary" indicator-color="primary" align="justify">
      <q-tab name="servers" :label="t('servers')" />
      <q-tab name="packages" :label="t('packages')" />
    </q-tabs>
    <q-separator />
    <q-tab-panels v-model="tab">
      <q-tab-panel name="servers">
        <q-table
          flat
          :rows="cluster.servers"
          :columns="columns"
          row-key="name"
          :rows-per-page-options="[0]"
          hide-pagination
        >
          <template v-slot:top-left>
            <q-btn
              v-if="cluster.servers.length"
              color="primary"
              :label="t('download_logs')"
              icon="download"
              size="sm"
              @click="onClusterLogsDownload"
            />
          </template>
          <template v-slot:body-cell-name="props">
            <q-td :props="props">
              <span class="text-primary">{{ props.row.app.name }}</span>
              <code class="on-right" :title="props.value">{{ props.row.app.id.split('-')[0] }}</code>
              <div class="float-right">
                <q-btn
                  v-if="!props.row.running"
                  rounded
                  dense
                  flat
                  size="sm"
                  color="secondary"
                  :title="t('start')"
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
                  :title="t('stop')"
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
                  :title="t('download_logs')"
                  icon="download"
                  class="q-ml-xs"
                  @click="onRServerLogsDownload(props.row)"
                />
              </div>
            </q-td>
          </template>
          <template v-slot:body-cell-tags="props">
            <q-td :props="props">
              <q-badge color="primary" v-for="tag in props.value" :label="tag" :key="tag" class="on-left" />
            </q-td>
          </template>
          <template v-slot:body-cell-url="props">
            <q-td :props="props">
              <a :href="props.row.app.server" target="_blank">{{ props.row.app.server }}</a>
            </q-td>
          </template>
          <template v-slot:body-cell-running="props">
            <q-td :props="props">
              <q-icon name="circle" size="sm" :color="props.value ? 'green' : 'red'" />
            </q-td>
          </template>
        </q-table>
      </q-tab-panel>
      <q-tab-panel name="packages">
        <r-packages :cluster="cluster" />
      </q-tab-panel>
    </q-tab-panels>
  </div>
</template>

<script setup lang="ts">
import { baseUrl } from 'src/boot/api';
import type { RServerClusterDto, RServerDto } from 'src/models/OpalR';
import { getSizeLabel } from 'src/utils/files';
import RPackages from 'src/components/admin/r/RPackages.vue';
import { DefaultAlignment } from 'src/components/models';

interface Props {
  cluster: RServerClusterDto;
}

const props = defineProps<Props>();

const { t } = useI18n();
const rStore = useRStore();

const tab = ref<string>('servers');

const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: DefaultAlignment,
    field: 'name',
  },
  {
    name: 'version',
    required: true,
    label: t('r.version'),
    align: DefaultAlignment,
    field: 'version',
    classes: 'text-caption',
  },
  {
    name: 'sessionCount',
    required: true,
    label: t('r.sessions'),
    align: DefaultAlignment,
    field: (row: RServerDto) => t('r.sessions_counts', { count: row.sessionCount, active: row.busySessionCount }),
  },
  {
    name: 'system',
    required: true,
    label: t('system'),
    align: DefaultAlignment,
    field: (row: RServerDto) => t('r.system', { cores: row.cores, memory: getSizeLabel(row.freeMemory * 1000) }),
  },
  {
    name: 'url',
    required: true,
    label: 'URL',
    align: DefaultAlignment,
    field: 'app',
  },
  {
    name: 'running',
    required: true,
    label: t('status'),
    align: DefaultAlignment,
    field: 'running',
  },
]);

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
