<template>
  <div>
    <div v-if="hasDatabase">
      <div class="row">
        <q-btn
          v-if="authStore.isAdministrator"
          :label="t('edit')"
          icon="edit"
          color="primary"
          size="sm"
          class="on-left"
          @click="onShowEdit"
        />
        <q-btn
          v-if="authStore.isAdministrator"
          outline
          :label="t('test')"
          icon="settings_ethernet"
          color="secondary"
          size="sm"
          class="on-left"
          @click="onTest"
        />
        <q-btn
          v-if="authStore.isAdministrator"
          outline
          color="red"
          icon="delete"
          size="sm"
          :disable="database?.hasDatasource"
          @click="onShowDelete"
        />
      </div>
      <div class="row q-mt-md">
        <div class="col-6">
          <q-list separator dense>
            <q-item>
              <q-item-section style="max-width: 50px">
                <q-item-label overline class="text-grey-6"> URL </q-item-label>
              </q-item-section>
              <q-item-section>
                <q-item-label class="text-help">
                  <span v-if="database?.sqlSettings">{{ database.sqlSettings.url }}</span>
                  <span v-if="database?.mongoDbSettings">{{ database.mongoDbSettings.url }}</span>
                </q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </div>
      </div>
    </div>
    <div v-else>
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
    </div>

    <confirm-dialog
      v-model="showDelete"
      :title="t('unregister')"
      :text="t('db.unregister_confirm', { name: database?.name })"
      @confirm="onDelete"
    >
      <template v-if="isH2">
        <div>
          <q-checkbox v-model="deleteFiles" dense class="q-mt-md" :label="t('db.delete_files')" />
          <div class="text-help q-mt-sm">{{ t('db.delete_files_hint') }}</div>
        </div>
      </template>
    </confirm-dialog>
    <edit-database-dialog v-model="showEdit" :database="selected" @save="onSave" />
  </div>
</template>

<script setup lang="ts">
import { type DatabaseDto, DatabaseDto_Usage } from 'src/models/Database';
import EditDatabaseDialog from 'src/components/admin/databases/EditDatabaseDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { notifyError, notifySuccess } from 'src/utils/notify';

const systemStore = useSystemStore();
const authStore = useAuthStore();
const { t } = useI18n();

const database = ref<DatabaseDto>();
const showEdit = ref(false);
const showDelete = ref(false);
const deleteFiles = ref(false);
const selected = ref();

const hasDatabase = computed(() => database.value?.name);
/**
 * H2 is embedded: its database is a file Opal created in its own folder, and that file outlives the registration -
 * keeping the credentials it was created with, so a database registered again at the same URL with a different
 * password cannot open it. Removing it is offered here, and never implied.
 */
const isH2 = computed(() => database.value?.sqlSettings?.driverClass === 'org.h2.Driver');

onMounted(() => {
  refresh();
});

function refresh() {
  systemStore
    .getIdentifiersDatabase()
    .then((data) => {
      database.value = data;
    })
    .catch(() => {
      database.value = {} as DatabaseDto;
    });
}

function onShowDelete() {
  deleteFiles.value = false;
  showDelete.value = true;
}

function onDelete() {
  if (!database.value?.name) {
    return;
  }
  systemStore
    .deleteDatabase(database.value.name, deleteFiles.value)
    .then(() => {
      refresh();
    })
    .catch((error) => {
      notifyError(error);
      refresh();
    });
}

function onTest() {
  if (!database.value?.name) {
    return;
  }
  systemStore
    .testDatabase(database.value.name)
    .then(() => {
      notifySuccess(t('db.test_success'));
    })
    .catch((error) => {
      notifyError(t('db.test_error', { error: error.response.data.message }));
    });
}

function onShowEdit() {
  selected.value = { ...database.value };
  showEdit.value = true;
}

function onShowAddSQLDB() {
  selected.value = {
    name: '',
    usage: DatabaseDto_Usage.STORAGE,
    sqlSettings: {
      driverClass: 'org.postgresql.Driver',
      url: 'jdbc:postgresql://localhost:5432/opal_ids',
    },
    defaultStorage: false,
    usedForIdentifiers: true,
  } as DatabaseDto;
  showEdit.value = true;
}

function onShowAddMondoDB() {
  selected.value = {
    name: '',
    usage: DatabaseDto_Usage.STORAGE,
    mongoDbSettings: {
      url: 'mongodb://localhost:27017/opal_ids',
    },
    defaultStorage: false,
    usedForIdentifiers: true,
  } as DatabaseDto;
  showEdit.value = true;
}

function onSave() {
  refresh();
}
</script>
