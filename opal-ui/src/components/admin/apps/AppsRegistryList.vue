<template>
  <div class="text-h5">{{ $t('apps.registry_list') }}</div>
  <q-table
    flat
    :rows="apps"
    :columns="columns"
    row-key="name"
    :pagination="initialPagination"
    :hide-pagination="apps.length <= initialPagination.rowsPerPage"
    :loading="loading"
  >
    <template v-slot:top-left>
      <q-btn
        size="sm"
        icon="cached"
        color="primary"
        :label="$t('refresh')"
        @click="onRefresh"
      ></q-btn>
    </template>
    <template v-slot:body-cell-name="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <span class="text-primary">{{ props.value }}</span>
        <div class="float-right">
          <q-btn
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="$t('edit')"
            :icon="toolsVisible[props.row.name] ? 'cancel' : 'none'"
            class="q-ml-xs"
            @click="onUnregister(props.row)"
          />
        </div>
      </q-td>
    </template>
    <template v-slot:body-cell-groups="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <q-chip class="q-ml-none" v-for="group in props.col.format(props.row.groups)" :key="group.name">
          {{ group }}
        </q-chip>
      </q-td>
    </template>
    <template v-slot:body-cell-authentication="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <span class="text-caption">{{ props.value }}</span>
      </q-td>
    </template>
    <template v-slot:body-cell-enabled="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <q-icon :name="props.value ? 'check' : 'close'" size="sm" />
      </q-td>
    </template>
  </q-table>
</template>

<script lang="ts">
export default defineComponent({
  name: 'AppsRegistryList',
});
</script>

<script setup lang="ts">
import { AppDto } from 'src/models/Apps';

const { t } = useI18n();
const loading = ref(false);
const apps = computed(() => []);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});

const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'name',
    sortable: true,
    style: 'width: 25%',
  },
  {
    name: 'type',
    label: t('type'),
    align: 'left',
    field: 'type',
  },
  {
    name: 'cluster',
    label: t('cluster'),
    align: 'left',
    field: 'cluster',
  },
  {
    name: 'host',
    label: t('host'),
    align: 'left',
    field: 'server',
  },
  {
    name: 'tags',
    label: t('tags'),
    align: 'left  ',
    field: 'tags',
  },
]);

// Handlers

function onOverRow(row: AppDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: AppDto) {
  toolsVisible.value[row.name] = false;
}

async function onUnregister(row: AppDto) {
  //
}

async function onRefresh() {
  //
}
</script>
