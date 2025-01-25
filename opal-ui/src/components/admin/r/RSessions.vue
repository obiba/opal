<template>
  <div>
    <q-table
      flat
      :rows="rStore.sessions"
      :columns="columns"
      row-key="id"
      :pagination="initialPagination"
      wrap-cells
      selection="multiple"
      v-model:selected="selected"
    >
      <template v-slot:top-left>
        <q-btn outline color="secondary" icon="refresh" :title="t('refresh')" size="sm" @click="updateRSessions" />
        <q-btn
          outline
          color="red"
          icon="highlight_off"
          size="sm"
          class="on-right"
          :disable="selected.length === 0"
          @click="onShowTerminateSessions"
        />
      </template>
      <template v-slot:body-cell-id="props">
        <q-td :props="props">
          <code :title="props.value">{{ props.value.split('-')[0] }}</code>
        </q-td>
      </template>
      <template v-slot:body-cell-server="props">
        <q-td :props="props">
          <span>{{ props.value.split('~')[0] }}</span>
          <code class="on-right">{{ props.value.split('~')[1].split('-')[0] }}</code>
        </q-td>
      </template>
      <template v-slot:body-cell-user="props">
        <q-td :props="props">
          <q-chip>{{ props.value }}</q-chip>
        </q-td>
      </template>
      <template v-slot:body-cell-status="props">
        <q-td :props="props">
          <q-icon
            name="circle"
            size="sm"
            :title="t(props.value.toLowerCase())"
            :color="getSessionColor(props.value)"
          />
        </q-td>
      </template>
    </q-table>
    <confirm-dialog
      v-model="showTerminate"
      :title="t('terminate')"
      :text="t('terminate_r_sessions_confirm', { count: selected.length })"
      @confirm="onTerminateSessions"
    />
  </div>
</template>

<script setup lang="ts">
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import type { RSessionDto } from 'src/models/OpalR';
import { getDateLabel } from 'src/utils/dates';
import { DefaultAlignment } from 'src/components/models';

const rStore = useRStore();
const { t } = useI18n();

const showTerminate = ref(false);
const selected = ref<RSessionDto[]>([]);
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});

const columns = computed(() => [
  { name: 'id', label: 'ID', align: DefaultAlignment, field: 'id', sortable: true },
  { name: 'profile', label: t('profile'), align: DefaultAlignment, field: 'profile', sortable: true, classes: 'text-caption' },
  { name: 'cluster', label: t('r.cluster'), align: DefaultAlignment, field: 'cluster', sortable: true, classes: 'text-caption' },
  { name: 'server', label: t('server'), align: DefaultAlignment, field: 'server', sortable: true },
  { name: 'context', label: t('context'), align: DefaultAlignment, field: 'context', sortable: true, classes: 'text-caption' },
  { name: 'user', label: t('user'), align: DefaultAlignment, field: 'user', sortable: true },
  {
    name: 'creationDate',
    label: t('started'),
    align: DefaultAlignment,
    field: 'creationDate',
    sortable: true,
    format: getDateLabel,
  },
  {
    name: 'lastAccessDate',
    label: t('last_access'),
    align: DefaultAlignment,
    field: 'lastAccessDate',
    sortable: true,
    format: getDateLabel,
  },
  { name: 'status', label: 'Status', align: DefaultAlignment, field: 'status', sortable: true },
]);

function getSessionColor(status: string) {
  return status === 'BUSY' ? 'warning' : status === 'WAITING' ? 'positive' : 'grey-6';
}

function updateRSessions() {
  rStore.initSessions();
}

function onShowTerminateSessions() {
  showTerminate.value = true;
}

function onTerminateSessions() {
  rStore.terminateSessions(selected.value).finally(() => {
    selected.value = [];
  });
}
</script>
