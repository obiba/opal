<template>
  <div>
    <q-table :rows="databases" flat row-key="name" :columns="columns" :pagination="initialPagination">
      <template v-slot:top-left>
        <q-btn-dropdown v-if="authStore.isAdministrator" color="primary" :label="t('register')" icon="add" size="sm">
          <q-list>
            <q-item clickable v-close-popup @click.prevent="onShowAddSQLDB">
              <q-item-section>
                <q-item-label>{{ t('db.register_sqldb') }}</q-item-label>
              </q-item-section>
            </q-item>

            <q-item clickable v-close-popup @click.prevent="onShowAddMondoDB">
              <q-item-section>
                <q-item-label>{{ t('db.register_mongodb') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>
      </template>
      <template v-slot:body="props">
        <q-tr :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-td key="name" :props="props">
            <span class="text-primary">{{ props.row.name }}</span>
            <q-icon
              v-if="props.row.defaultStorage"
              name="check"
              size="sm"
              class="on-right"
              :title="t('default_storage')"
            />
            <div class="float-right">
              <q-btn
                v-if="authStore.isAdministrator"
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :icon="toolsVisible[props.row.name] ? 'settings_ethernet' : 'none'"
                :title="t('test')"
                class="q-ml-xs"
                @click="onTest(props.row)"
              />
              <q-btn
                v-if="authStore.isAdministrator"
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :icon="toolsVisible[props.row.name] ? (isManaged(props.row) ? 'visibility' : 'edit') : 'none'"
                :title="t(isManaged(props.row) ? 'view' : 'edit')"
                class="q-ml-xs"
                @click="onShowEdit(props.row)"
              />
              <q-btn
                v-if="authStore.isAdministrator && !props.row.hasDatasource && !isManaged(props.row)"
                rounded
                dense
                flat
                size="sm"
                color="secondary"
                :title="t('delete')"
                :icon="toolsVisible[props.row.name] ? 'delete' : 'none'"
                class="q-ml-xs"
                @click="onShowDelete(props.row)"
              />
            </div>
          </q-td>
          <q-td key="ownerProject" :props="props" class="text-caption">
            {{ props.row.ownerProject }}
          </q-td>
          <q-td key="hasDatasource" :props="props">
            <q-icon
              :name="props.row.hasDatasource ? 'check' : 'close'"
              :class="props.row.hasDatasource ? 'text-positive' : ''"
            />
          </q-td>
          <q-td key="url" :props="props" class="text-help">
            <span v-if="props.row.sqlSettings">{{ props.row.sqlSettings.url }}</span>
            <span v-if="props.row.mongoDbSettings">{{ props.row.mongoDbSettings.url }}</span>
          </q-td>
          <q-td key="usage" :props="props" class="text-caption">
            {{ t(props.row.usage.toLowerCase()) }}
          </q-td>
        </q-tr>
      </template>
    </q-table>
    <confirm-dialog
      v-model="showDelete"
      :title="t(isOwned(selected) ? 'delete' : 'unregister')"
      :text="t(isOwned(selected) ? 'db.delete_internal_confirm' : 'db.unregister_confirm', { name: selected?.name })"
      @confirm="onDelete"
    >
      <template v-if="canDeleteFiles">
        <div>
          <q-checkbox v-model="deleteFiles" dense class="q-mt-md" :label="t('db.delete_files')" />
          <div class="text-help q-mt-sm">{{ t('db.delete_files_hint') }}</div>
        </div>
      </template>
    </confirm-dialog>
    <edit-database-dialog v-model="showEdit" :database="selected" :read-only="isManaged(selected)" @save="onSave" />
  </div>
</template>

<script setup lang="ts">
import { type DatabaseDto, DatabaseDto_Usage } from 'src/models/Database';
import EditDatabaseDialog from 'src/components/admin/databases/EditDatabaseDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { notifyError, notifySuccess } from 'src/utils/notify';
import { DefaultAlignment } from 'src/components/models';

const systemStore = useSystemStore();
const authStore = useAuthStore();
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
const deleteFiles = ref(false);
// a database a project owned takes its files with it in any case, so there is nothing to choose there
const canDeleteFiles = computed(() => isH2(selected.value) && !isOwned(selected.value));
const selected = ref();

const columns = computed(() => [
  { name: 'name', label: t('name'), align: DefaultAlignment, field: 'name' },
  {
    name: 'ownerProject',
    label: t('db.owner_project'),
    align: DefaultAlignment,
    field: 'ownerProject',
    sortable: true,
  },
  { name: 'hasDatasource', label: t('db.in_use'), align: DefaultAlignment, field: 'hasDatasource' },
  { name: 'url', label: 'URL', align: DefaultAlignment, field: 'url' },
  { name: 'usage', label: t('usage'), align: DefaultAlignment, field: 'usage' },
]);

onMounted(() => {
  refresh();
});

function refresh() {
  systemStore.getDatabasesWithSettings().then((data) => {
    databases.value = data;
  });
}

/**
 * H2 is embedded: its database is a file Opal created in its own folder, and that file outlives the registration -
 * keeping the credentials it was created with, so a database registered again at the same URL with a different
 * password cannot open it. Removing it is offered here, and never implied.
 */
function isH2(row: DatabaseDto | undefined) {
  return row?.sqlSettings?.driverClass === 'org.h2.Driver';
}

/** Whether this database belongs to a project at all, whether or not that project still exists. */
function isOwned(row: DatabaseDto | undefined) {
  return Boolean(row?.ownerProject);
}

/**
 * Whether Opal, and not the operator, is what edits and deletes this database. That is decided by the owner project
 * still existing rather than by the database being in use: a project that failed to load has no datasource but is
 * still there, and offering a delete button that comes back with a conflict is worse than not offering one. The
 * server has the last word - the page can be stale, and a project can be created between the listing and the click.
 */
function isManaged(row: DatabaseDto | undefined) {
  return Boolean(row?.ownerProjectExists);
}

function onOverRow(row: DatabaseDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: DatabaseDto) {
  toolsVisible.value[row.name] = false;
}

function onTest(row: DatabaseDto) {
  systemStore
    .testDatabase(row.name)
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
  deleteFiles.value = false;
  showDelete.value = true;
}

function onDelete() {
  systemStore
    .deleteDatabase(selected.value.name, deleteFiles.value)
    .then(() => {
      refresh();
    })
    .catch((error) => {
      notifyError(error);
      refresh();
    });
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
    usedForIdentifiers: false,
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
    usedForIdentifiers: false,
  } as DatabaseDto;
  showEdit.value = true;
}

function onSave() {
  refresh();
}
</script>
