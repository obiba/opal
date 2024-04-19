<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="dsName" :to="`/project/${dsName}`" />
        <q-breadcrumbs-el
          :label="$t('tables')"
          :to="`/project/${dsName}/tables`"
        />
        <q-breadcrumbs-el :label="tName" />
      </q-breadcrumbs>
      <q-icon
        name="circle"
        :color="tableStatusColor(datasourceStore.table.status)"
        size="sm"
        class="on-right"
      />
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5">
        <q-icon name="table_chart" size="sm" class="q-mb-xs"></q-icon
        ><span class="on-right">{{ datasourceStore.table.name }}</span>
      </div>
      <div class="row q-mt-md q-mb-md">
        <div class="col-12 col-md-6">
          <fields-list
            :items="items1"
            :dbobject="datasourceStore.table"
          />
        </div>
        <div class="col-12 col-md-6">
          <fields-list
            :items="items2"
            :dbobject="datasourceStore.table"
          />
        </div>
      </div>
      <q-tabs
        v-model="tab"
        dense
        class="text-grey"
        active-color="primary"
        indicator-color="primary"
        align="justify"
        narrow-indicator
      >
        <q-tab name="dictionary" :label="$t('dictionary')" />
        <q-tab name="summary" :label="$t('summary')" />
        <q-tab name="values" :label="$t('values')" />
        <q-tab name="permissions" :label="$t('permissions')" />
      </q-tabs>

      <q-separator />

      <q-tab-panels v-model="tab">
        <q-tab-panel name="dictionary">
          <table-variables />
        </q-tab-panel>

        <q-tab-panel name="summary">
          <div class="text-h6">{{ $t('summary') }}</div>
        </q-tab-panel>

        <q-tab-panel name="values">
          <div class="text-h6">{{ $t('values') }}</div>
        </q-tab-panel>

        <q-tab-panel name="permissions">
          <div class="text-h6">{{ $t('permissions') }}</div>
        </q-tab-panel>
      </q-tab-panels>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import TableVariables from 'src/components/TableVariables.vue';
import FieldsList, { FieldItem } from 'src/components/FieldsList.vue';
import { Table } from 'src/components/models';
import { tableStatusColor } from 'src/utils/colors';

const route = useRoute();
const datasourceStore = useDatasourceStore();

const tab = ref('dictionary');

const items1: FieldItem<Table>[] = [
  {
    field: 'name',
  },
  {
    field: 'name',
    label: 'full_name',
    html: (val) =>
      val ? `<code>${val.datasourceName}.${val.name}</code>` : '',
  },
  {
    field: 'entityType',
    label: 'entity_type',
  },
];

const items2: FieldItem<Table>[] = [
  {
    field: 'timestamps',
    label: 'created',
    format: (val) => (val ? val.timestamps?.created : ''),
  },
  {
    field: 'timestamps',
    label: 'last_update',
    format: (val) => (val ? val.timestamps?.lastUpdate : ''),
  },
];

onMounted(() => {
  init();
});

const dsName = computed(() => route.params.id as string);
const tName = computed(() => route.params.tid as string);

function init() {
  datasourceStore.initDatasourceTable(dsName.value, tName.value);
}
</script>
