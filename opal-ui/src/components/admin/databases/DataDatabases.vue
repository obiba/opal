<template>
  <div>
    <q-table
      :rows="databases"
      flat
      row-key="name"
      :columns="columns"
      :pagination="initialPagination"
    >
      <template v-slot:top-left>
        <q-btn-dropdown color="primary" :label="$t('register')" icon="add" size="sm">
          <q-list>
            <q-item clickable v-close-popup @click.prevent="onShowAddSQLDB">
              <q-item-section>
                <q-item-label>{{ $t('db.register_sqldb') }}</q-item-label>
              </q-item-section>
            </q-item>

            <q-item clickable v-close-popup @click.prevent="onShowAddMondoDB">
              <q-item-section>
                <q-item-label>{{ $t('db.register_mongodb') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>
      </template>
      <template v-slot:body="props">
      <q-tr :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-td key="name" :props="props">
            <span class="text-primary">{{  props.row.name }}</span>
            <q-icon
              v-if="props.row.defaultStorage"
              name="check"
              size="sm"
              class="on-right"
              :title="$t('default_storage')" />
            <div class="float-right" >
              <q-btn
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :icon="toolsVisible[props.row.name] ? 'settings_ethernet' : 'none'"
                :title="$t('test')"
                class="q-ml-xs"
                @click="onTest(props.row)"
              />
              <q-btn
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :icon="toolsVisible[props.row.name] ? 'edit' : 'none'"
                :title="$t('edit')"
                class="q-ml-xs"
                @click="onShowEdit(props.row)"
              />
              <q-btn
                v-if="!props.row.hasDatasource"
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="$t('delete')"
                :icon="toolsVisible[props.row.name] ? 'delete' : 'none'"
                class="q-ml-xs"
                @click="onShowDelete(props.row)"
              />
            </div>
          </q-td>
          <q-td key="hasDatasource" :props="props">
            <q-icon
              :name="props.row.hasDatasource ? 'check' : 'close'"
              :class="props.row.hasDatasource ? 'text-positive' : ''" />
          </q-td>
          <q-td key="url" :props="props" class="text-help">
            <span v-if="props.row.sqlSettings">{{ props.row.sqlSettings.url }}</span>
            <span v-if="props.row.mongoDbSettings">{{ props.row.mongoDbSettings.url }}</span>
          </q-td>
          <q-td key="usage" :props="props" class="text-caption">
            {{ $t(props.row.usage.toLowerCase()) }}
          </q-td>
        </q-tr>
      </template>
    </q-table>
    <edit-database-dialog v-model="showEdit" :database="selected" />
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'DataDatabases',
});
</script>
<script setup lang="ts">
import { DatabaseDto, DatabaseDto_Usage } from 'src/models/Database';
import EditDatabaseDialog from 'src/components/admin/databases/EditDatabaseDialog.vue';
import { notifyError, notifySuccess } from 'src/utils/notify';

const systemStore = useSystemStore();
const { t } = useI18n();

const databases = ref<DatabaseDto[]>([]);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const showEdit = ref(false);
const showDelete = ref(false);
const selected = ref();

const columns = [
  { name: 'name', label: t('name'), align: 'left', field: 'name' },
  { name: 'hasDatasource', label: t('db.in_use'), align: 'left' },
  { name: 'url', label: 'URL', align: 'left' },
  { name: 'usage', label: t('usage'), align: 'left', field: 'usage' },
];

onMounted(() => {
  refresh();
});

function refresh() {
  systemStore.getDatabasesWithSettings().then((data) => {
    databases.value = data;
  });
}

function onOverRow(row: DatabaseDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: DatabaseDto) {
  toolsVisible.value[row.name] = false;
}

function onTest(row: DatabaseDto) {
  systemStore.testDataDatabase(row.name)
    .then(() => {
      notifySuccess(t('db.test_success'));
    })
    .catch((error) => {
      notifyError(t('db.test_error', { error: error.response.data.message }));
    });
}

function onShowEdit(row: DatabaseDto) {
  selected.value = row;
  showEdit.value = true;
}

function onShowDelete(row: DatabaseDto) {
  selected.value = row;
  showDelete.value = true;
}

function onShowAddSQLDB() {
  selected.value = {
    name: '',
    usage: DatabaseDto_Usage.STORAGE,
    sqlSettings: {
      driverClass: 'org.postgresql.Driver',
      url: 'jdbc:postgresql://localhost:5432/opal',
    },
    defaultStorage: false,
  } as DatabaseDto;
  showEdit.value = true;
}

function onShowAddMondoDB() {
  selected.value = {
    name: '',
    usage: DatabaseDto_Usage.STORAGE,
    mongoDbSettings: {
      url: 'mongodb://localhost:27017/opal',
    },
    defaultStorage: false,
  } as DatabaseDto;
  showEdit.value = true;
}
</script>
