<template>
  <div :class="statusClass">
    <div>
      <span>{{ t(`table_index.status_title.${datasourceStore.tableIndex.status}`) }}</span>
      <q-btn
        v-if="statusActionIcon"
        :color="statusActionColor"
        :icon="statusActionIcon"
        :title="t(statusActionLabel)"
        rounded
        dense
        outline
        size="sm"
        class="on-right"
        @click="onStatusAction"
      />
      <q-btn
        v-if="datasourceStore.tableIndex.status === TableIndexationStatus.UPTODATE"
        :color="statusActionColor"
        icon="cleaning_services"
        :title="t('clear')"
        dense
        flat
        size="sm"
        class="on-right"
        @click="onClear"
      />
      <q-btn
        v-if="statusActionIcon"
        :color="statusActionColor"
        icon="event"
        :title="t('schedule')"
        flat
        dense
        size="sm"
        class="on-right"
        @click="onSchedule"
      />
    </div>
    <q-linear-progress
      v-if="datasourceStore.tableIndex.progress"
      color="white"
      :value="datasourceStore.tableIndex.progress"
      class="q-mt-xs"
    />

    <table-indexer-dialog v-model="showDialog" />
  </div>
</template>

<script setup lang="ts">
import { TableIndexationStatus } from 'src/models/Opal';
import TableIndexerDialog from 'src/components/datasource/TableIndexerDialog.vue';

const { t } = useI18n();
const datasourceStore = useDatasourceStore();

const showDialog = ref(false);

const statusClass = computed(() => {
  switch (datasourceStore.tableIndex.status) {
    case TableIndexationStatus.UPTODATE:
      return 'box-positive';
    case TableIndexationStatus.IN_PROGRESS:
      return 'box-info';
    case TableIndexationStatus.OUTDATED:
      return 'box-warning';
    default:
      return 'box-negative';
  }
});

const statusActionLabel = computed(() => {
  switch (datasourceStore.tableIndex.status) {
    case TableIndexationStatus.UPTODATE:
      return 'refresh';
    case TableIndexationStatus.IN_PROGRESS:
      return 'stop';
    case TableIndexationStatus.OUTDATED:
      return 'start';
    default:
      return '';
  }
});

const statusActionIcon = computed(() => {
  switch (datasourceStore.tableIndex.status) {
    case TableIndexationStatus.UPTODATE:
      return 'refresh';
    case TableIndexationStatus.IN_PROGRESS:
      return 'stop';
    case TableIndexationStatus.OUTDATED:
      return 'play_arrow';
    default:
      return '';
  }
});

const statusActionColor = computed(() => {
  switch (datasourceStore.tableIndex.status) {
    case TableIndexationStatus.OUTDATED:
      return 'black';
    default:
      return 'white';
  }
});

onMounted(() => datasourceStore.loadTableIndex());

function onStatusAction() {
  if (datasourceStore.tableIndex.status === TableIndexationStatus.IN_PROGRESS) {
    datasourceStore.deleteTableIndex();
  } else {
    datasourceStore.updateTableIndex().then(() => {
      setInterval(() => {
        if (
          datasourceStore.tableIndex.progress ||
          datasourceStore.tableIndex.status === TableIndexationStatus.IN_PROGRESS
        ) {
          datasourceStore.loadTableIndex();
        }
      }, 1000); // 1 second
    });
  }
}

function onClear() {
  datasourceStore.deleteTableIndex();
}

function onSchedule() {
  showDialog.value = true;
}
</script>
