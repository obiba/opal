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
      <bookmark-icon :resource="`/datasource/${dsName}/table/${tName}`" />
      <q-space />
      <q-btn
        outline
        icon="navigate_before"
        size="sm"
        :label="previousTable?.name"
        :to="`/project/${dsName}/table/${previousTable?.name}`"
        v-if="previousTable"
        class="on-right"
      />
      <q-btn
        outline
        icon-right="navigate_next"
        size="sm"
        :label="nextTable?.name"
        :to="`/project/${dsName}/table/${nextTable?.name}`"
        v-if="nextTable"
        class="on-right"
      />
    </q-toolbar>
    <q-page class="q-pa-md" v-show="datasourceStore.table.name">
      <div class="text-h5">
        <q-icon name="table_chart" size="sm" class="q-mb-xs"></q-icon
        ><span class="on-right">{{ datasourceStore.table.name }}</span>
        <q-btn-dropdown outline color="primary" icon="download" size="sm" :title="$t('download')" class="on-right">
          <q-list>
            <q-item clickable v-close-popup @click="onDownloadDictionary">
              <q-item-section>
                <q-item-label>{{ $t('download_dictionary') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item v-if="isView" clickable v-close-popup @click="onDownloadView">
              <q-item-section>
                <q-item-label>{{ $t('download_view') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>
        <q-btn v-if="!isView && projectsStore.perms.copy?.canCreate()" color="secondary" icon="content_copy" :title="$t('copy')" size="sm" @click="onShowCopyData" class="on-right"></q-btn>
        <q-btn-dropdown v-if="isView" color="secondary" icon="content_copy" size="sm" :title="$t('copy')" class="on-right">
          <q-list>
            <q-item clickable v-close-popup @click="onShowCopyData">
              <q-item-section>
                <q-item-label>{{ $t('copy_data') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item v-if="isView" clickable v-close-popup @click="onShowCopyView">
              <q-item-section>
                <q-item-label>{{ $t('copy_view') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>

        <q-btn v-if="datasourceStore.perms.table?.canUpdate()" outline color="secondary" icon="edit" size="sm" @click="onShowEdit" class="on-right"></q-btn>
        <q-btn v-if="datasourceStore.perms.table?.canDelete()" outline color="red" icon="delete" size="sm" @click="onShowDelete" class="on-right"></q-btn>
        <q-btn v-if="isView && datasourceStore.perms.table?.canUpdate()"
          no-caps
          dense
          flat
          :label="$t('reconnect_view')"
          icon="settings_ethernet"
          size="sm"
          @click="onReconnect"
          class="on-right" />
      </div>
      <div class="row q-col-gutter-md q-mt-md q-mb-md">
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

      <div v-if="loading">
        <q-spinner-dots size="lg" class="q-mt-md" />
      </div>
      <div v-else>
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
          <q-tab name="values" :label="$t('values')" v-if="datasourceStore.perms.tableValueSets?.canRead()"/>
          <q-tab name="permissions" :label="$t('permissions')" v-if="datasourceStore.perms.tablePermissions?.canRead()"/>
        </q-tabs>

        <q-separator />

        <q-tab-panels v-model="tab">
          <q-tab-panel name="dictionary">
            <table-variables />
          </q-tab-panel>

          <q-tab-panel name="values" v-if="datasourceStore.perms.tableValueSets?.canRead()">
            <table-values />
          </q-tab-panel>

          <q-tab-panel name="permissions" v-if="datasourceStore.perms.tablePermissions?.canRead()">
            <div class="text-h6">{{ $t('permissions') }}</div>
            <access-control-list
              :resource="`/project/${dsName}/permissions/table/${tName}`"
              :options="['TABLE_READ', 'TABLE_VALUES', 'TABLE_EDIT', 'TABLE_VALUES_EDIT', 'TABLE_ALL']"
            />
          </q-tab-panel>
        </q-tab-panels>
      </div>

      <copy-tables-dialog v-model="showCopyData" :tables="[datasourceStore.table]"/>
      <copy-view-dialog v-model="showCopyView" :table="datasourceStore.table" :view="datasourceStore.view"/>
      <edit-table-dialog v-model="showEdit" :table="datasourceStore.table" :view="datasourceStore.view"
        @update:table="onTableUpdate" @update:view="onViewUpdate"/>
      <confirm-dialog v-model="showDelete" :title="$t('delete')" :text="$t('delete_tables_confirm', { count: 1 })" @confirm="onDeleteTable" />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import BookmarkIcon from 'src/components/BookmarkIcon.vue';
import TableVariables from 'src/components/datasource/TableVariables.vue';
import TableValues from 'src/components/datasource/TableValues.vue';
import AccessControlList from 'src/components/permissions/AccessControlList.vue';
import FieldsList, { FieldItem } from 'src/components/FieldsList.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import CopyTablesDialog from 'src/components/datasource/CopyTablesDialog.vue';
import CopyViewDialog from 'src/components/datasource/CopyViewDialog.vue';
import EditTableDialog from 'src/components/datasource/EditTableDialog.vue';
import { TableDto, ViewDto } from 'src/models/Magma';
import { tableStatusColor } from 'src/utils/colors';
import { getDateLabel } from 'src/utils/dates';

const route = useRoute();
const router = useRouter();
const projectsStore = useProjectsStore();
const datasourceStore = useDatasourceStore();

const tab = ref('dictionary');
const showDelete = ref(false);
const showCopyData = ref(false);
const showCopyView = ref(false);
const showEdit = ref(false);
const loading = ref(false);

const isView = computed(() => datasourceStore.table.viewType !== undefined);
const isTablesView = computed(() => datasourceStore.table.viewType === 'View');
const isResourceView = computed(() => datasourceStore.table.viewType === 'ResourceView');

const previousTable = computed(() => {
  const idx = datasourceStore.tables.findIndex((t) => t.name === tName.value);
  return idx > 0 ? datasourceStore.tables[idx - 1] : null;
});

const nextTable = computed(() => {
  const idx = datasourceStore.tables.findIndex((t) => t.name === tName.value);
  return idx === datasourceStore.tables.length - 1 ? null : datasourceStore.tables[idx + 1];
});

const items1: FieldItem<TableDto>[] = computed(() => {
  return [
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
    {
      field: 'from',
      label: isTablesView.value ? 'table_references' : 'resource_ref.label',
      links: (val) => (val ? datasourceStore.view.from?.map((f) => {
        return {
          label: f,
          to: `/project/${f.split('.')[0]}/${isTablesView.value ? 'table' : 'resource'}/${f.split('.')[1]}`
        };
      }) : []),
      visible: () => isView.value,
    },
    {
      field: 'idColumn',
      label: 'resource_ref.id_column',
      format: () => datasourceStore.view['Magma.ResourceViewDto.view']?.idColumn || '',
      visible: () => isResourceView.value,
    }
  ];
});

const items2: FieldItem<TableDto>[] = [
  {
    field: 'timestamps',
    label: 'created',
    format: (val) => (val ? getDateLabel(val.timestamps?.created) : ''),
  },
  {
    field: 'timestamps',
    label: 'last_update',
    format: (val) => (val ? getDateLabel(val.timestamps?.lastUpdate) : ''),
  },
];

const dsName = computed(() => route.params.id as string);
const tName = computed(() => route.params.tid as string);

watch([dsName, tName], () => {
  init();
});

function init() {
  datasourceStore.initDatasourceTable(dsName.value, tName.value);
}

function onDownloadDictionary() {
  datasourceStore.downloadTableDictionary();
}

function onDownloadView() {
  datasourceStore.downloadView();
}

function onShowCopyData() {
  showCopyData.value = true;
}

function onShowCopyView() {
  showCopyView.value = true;
}

function onShowEdit() {
  datasourceStore.initDatasourceTables(dsName.value).then(() => showEdit.value = true);
}

function onShowDelete() {
  showDelete.value = true;
}

function onDeleteTable() {
  datasourceStore.deleteTable(tName.value).then(() => router.push(`/project/${dsName.value}/tables`));
}

function onTableUpdate(updated: TableDto) {
  router.push(`/project/${dsName.value}/table/${updated.name}`);
}

function onViewUpdate(updated: ViewDto) {
  if (updated.name === tName.value) {
    datasourceStore.view = updated;
  } else {
    router.push(`/project/${dsName.value}/table/${updated.name}`);
  }
}

function onReconnect() {
  loading.value = true;
  datasourceStore.reconnectView(dsName.value, tName.value)
    .finally(() => {
      datasourceStore.loadTable(tName.value).finally(() => loading.value = false);
    });
}
</script>
