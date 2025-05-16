<template>
  <div>
    <div class="text-h6">{{ t('kubernetes.pod_specs.title') }}</div>
    <div class="text-help">{{ t('kubernetes.pod_specs.info') }}</div>

    <div class="text-bold q-mt-md">Rock</div>
    <html-anchor-hint class="text-help" trKey="kubernetes.rock_info" text="OBiBa/Rock"
      url="https://rockdoc.obiba.org/" />
    <q-table flat :rows="rockPodSpecs" :columns="columns" row-key="id" :pagination="initialPagination">
      <template v-slot:top-left>
        <q-btn size="sm" icon="add" color="primary" :title="t('add')" @click="onAdd"></q-btn>
      </template>
      <template v-slot:body-cell-id="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <code :title="props.row.id">{{ props.value }}</code>
          <div class="float-right">
            <q-btn rounded dense flat size="sm" color="secondary" :title="t('edit')"
              :icon="toolsVisible[props.row.id] ? 'edit' : 'none'" class="q-ml-xs" @click="onEdit(props.row)" />
            <q-btn rounded dense flat size="sm" color="secondary" :title="t('delete')"
              :icon="toolsVisible[props.row.id] ? 'delete' : 'none'" class="q-ml-xs" @click="onRemove(props.row)" />
          </div>
        </q-td>
      </template>
      <template v-slot:body-cell-enabled="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-icon v-if="props.value" name="check" size="sm" />
        </q-td>
      </template>
      <template v-slot:body-cell-image="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-chip size="12px" color="primary" class="q-ml-none text-white">{{ props.value }}</q-chip>
        </q-td>
      </template>
      <template v-slot:body-cell-image_pull_policy="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <span class="text-caption text-grey-8">{{ props.value }}</span>
        </q-td>
      </template>
      <template v-slot:body-cell-resources_requests="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-chip size="sm" class="q-ml-none" v-for="(tag, index) in props.value" :key="index">
            {{ `${t(index)}: ${tag}` }}
          </q-chip>
        </q-td>
      </template>
      <template v-slot:body-cell-resources_limits="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-chip size="sm" class="q-ml-none" v-for="(tag, index) in props.value" :key="index">
            {{ `${t(index)}: ${tag}` }}
          </q-chip>
        </q-td>
      </template>
    </q-table>
    <pod-spec-dialog v-model="showDialog" :podSpec="selected" @update="init" />
  </div>
</template>
<script setup lang="ts">
import PodSpecDialog from 'src/components/admin/kubernetes/PodSpecDialog.vue';
import HtmlAnchorHint from 'src/components/HtmlAnchorHint.vue';
import { DefaultAlignment } from 'src/components/models';
import type { PodSpecDto } from 'src/models/K8s';

const { t } = useI18n();
const podsStore = usePodsStore();

const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});
const toolsVisible = ref<{ [key: string]: boolean }>({});
const showDialog = ref(false);
const selected = ref<PodSpecDto>();

const rockPodSpecs = computed(() => podsStore.podSpecs.filter((podSpec) => podSpec.type === 'rock'));

const columns = computed(() => [
  {
    name: 'id',
    required: true,
    label: 'ID',
    align: DefaultAlignment,
    field: 'id',
    sortable: true,
    style: 'width: 150px',
  },
  {
    name: 'namespace',
    label: t('kubernetes.namespace'),
    align: DefaultAlignment,
    field: 'namespace',
  },
  {
    name: 'enabled',
    label: t('enabled'),
    align: DefaultAlignment,
    field: 'enabled',
  },
  {
    name: 'name',
    label: t('kubernetes.name_prefix'),
    align: DefaultAlignment,
    field: (row: PodSpecDto) => row.container?.name,
  },
  {
    name: 'image',
    label: t('kubernetes.image'),
    align: DefaultAlignment,
    field: (row: PodSpecDto) => row.container?.image,
  },
  {
    name: 'image_pull_policy',
    label: t('kubernetes.image_pull_policy'),
    align: DefaultAlignment,
    field: (row: PodSpecDto) => row.container?.imagePullPolicy,
  },
  {
    name: 'resources_requests',
    label: t('kubernetes.resources_requests'),
    align: DefaultAlignment,
    field: (row: PodSpecDto) => row.container?.resources?.requests,
  },
  {
    name: 'resources_limits',
    label: t('kubernetes.resources_limits'),
    align: DefaultAlignment,
    field: (row: PodSpecDto) => row.container?.resources?.limits,
  },
]);

onMounted(() => init());

function init() {
  podsStore.initPodSpecs();
}

// Handlers

function onOverRow(row: PodSpecDto) {
  toolsVisible.value[row.id] = true;
}

function onLeaveRow(row: PodSpecDto) {
  toolsVisible.value[row.id] = false;
}

function onAdd() {
  selected.value = undefined;
  showDialog.value = true;
}

function onEdit(row: PodSpecDto) {
  selected.value = row;
  showDialog.value = true;
}

function onRemove(row: PodSpecDto) {
  console.log('Remove', row);
}

</script>
