<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t(editMode ? 'edit' : 'register') }}</div>
      </q-card-section>
      <q-separator />
      <q-card-section>
        <q-input
          v-if="!database.usedForIdentifiers"
          v-model="database.name"
          :label="t('name')"
          :hint="t('db.name_hint')"
          :disable="editMode"
          dense
          class="q-mb-md"
        />
        <q-select
          v-if="!database.usedForIdentifiers && database.sqlSettings"
          v-model="database.usage"
          :options="usageOptions"
          :label="t('usage')"
          :hint="t('db.usage_hint')"
          :disable="hasDatasource"
          dense
          emit-value
          map-options
          class="q-mb-md"
        />
        <q-toggle
          v-if="!database.usedForIdentifiers && database.usage === DatabaseDto_Usage.STORAGE"
          v-model="database.defaultStorage"
          :label="t('default_storage')"
          dense
          class="q-mb-md"
        />
        <div v-if="database.sqlSettings">
          <q-select
            v-model="database.sqlSettings.driverClass"
            :options="driverOptions"
            :label="t('db.driver')"
            :hint="t('db.driver_hint')"
            dense
            emit-value
            map-options
            class="q-mb-md"
            @update:model-value="onDriverChange"
          />
          <q-input v-model="database.sqlSettings.url" label="URL" dense class="q-mb-md" />
          <q-form ref="formRef">
            <div class="row q-col-gutter-md q-mb-md">
              <div class="col">
                <q-input v-model="database.sqlSettings.username" :label="t('username')" dense />
              </div>
              <div class="col">
                <q-input
                  v-model="database.sqlSettings.password"
                  autocomplete="off"
                  type="password"
                  :label="t('password')"
                  lazy-rules
                  :rules="[validateRequiredPassword]"
                  dense
                />
              </div>
            </div>
          </q-form>
          <div v-if="database.usage !== DatabaseDto_Usage.STORAGE">
            <q-input
              v-model="jdbcDatasourceSettings.defaultEntityType"
              :label="t('db.default_entity_type')"
              :hint="t('db.default_entity_type_hint')"
              dense
              class="q-mb-md"
            />
            <q-input
              v-model="jdbcDatasourceSettings.defaultEntityIdColumnName"
              :label="t('db.default_id_column')"
              :hint="t('db.default_id_column_hint')"
              dense
              class="q-mb-md"
            />
            <q-input
              v-model="jdbcDatasourceSettings.defaultUpdatedTimestampColumnName"
              :label="t('db.default_updated_column')"
              :hint="t('db.default_updated_column_hint')"
              dense
              class="q-mb-md"
            />
            <div v-if="database.usage === DatabaseDto_Usage.EXPORT" class="q-mt-lg">
              <q-toggle
                v-model="jdbcDatasourceSettings.useMetadataTables"
                :label="t('db.use_metadata_tables')"
                dense
                class="q-mb-sm"
              />
              <div class="text-hint">
                {{ t('db.use_metadata_tables_hint') }}
              </div>
            </div>
          </div>

          <q-list>
            <q-expansion-item
              switch-toggle-side
              dense
              header-class="text-primary text-caption"
              :label="t('advanced_options')"
            >
              <q-input
                v-model="jdbcDatasourceSettings.batchSize"
                :label="t('db.batch_size')"
                :hint="t('db.batch_size_hint')"
                dense
                class="q-mb-md"
              />
              <q-input
                v-model="database.sqlSettings.properties"
                :label="t('options')"
                placeholder="key=value"
                dense
                type="textarea"
                class="q-mb-md"
              />
            </q-expansion-item>
          </q-list>
        </div>
        <div v-if="database.mongoDbSettings">
          <q-input v-model="database.mongoDbSettings.url" label="URL" dense class="q-mb-md" />
          <q-form ref="formRef">
            <div class="row q-col-gutter-md q-mb-md">
              <div class="col">
                <q-input v-model="database.mongoDbSettings.username" :label="t('username')" dense />
              </div>
              <div class="col">
                <q-input v-model="database.mongoDbSettings.password" :label="t('password')" dense />
              </div>
            </div>
          </q-form>
          <q-list>
            <q-expansion-item
              switch-toggle-side
              dense
              header-class="text-primary text-caption"
              :label="t('advanced_options')"
            >
              <q-input
                v-model="database.mongoDbSettings.batchSize"
                :label="t('db.batch_size')"
                :hint="t('db.batch_size_hint')"
                dense
                class="q-mb-md"
              />
              <q-input
                v-model="database.mongoDbSettings.properties"
                :label="t('options')"
                placeholder="key=value"
                dense
                type="textarea"
                class="q-mb-md"
              />
            </q-expansion-item>
          </q-list>
        </div>
      </q-card-section>
      <q-separator />
      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('save')" color="primary" :disable="!database.name || !hasUrl" @click="onSubmit" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { type DatabaseDto, DatabaseDto_Usage, SqlSettingsDto_SqlSchema } from 'src/models/Database';
import type { JdbcDatasourceSettingsDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';
import type { JdbcDriverDto } from 'src/models/Database';

interface DialogProps {
  modelValue: boolean;
  database: DatabaseDto;
}

const props = withDefaults(defineProps<DialogProps>(), {
  database: () => ({} as DatabaseDto),
});

const emit = defineEmits(['update:modelValue', 'save']);
const formRef = ref();
const systemStore = useSystemStore();
const { t } = useI18n();

const showDialog = ref(props.modelValue);
const database = ref<DatabaseDto>(props.database || ({} as DatabaseDto));
const jdbcDatasourceSettings = ref<JdbcDatasourceSettingsDto>({} as JdbcDatasourceSettingsDto);
const editMode = ref<boolean>(false);
const hasDatasource = ref<boolean>(false);
let jdbcDrivers = [] as JdbcDriverDto[];
const driverOptions = ref([] as { label: string; value: string }[]);

const hasUrl = computed(() => database.value.sqlSettings?.url || database.value.mongoDbSettings?.url);
const usageOptions = [
  { label: t('storage'), value: DatabaseDto_Usage.STORAGE },
  { label: t('import'), value: DatabaseDto_Usage.IMPORT },
  { label: t('export'), value: DatabaseDto_Usage.EXPORT },
];

const validateRequiredPassword = (value: string) => {
  if (database.value.sqlSettings) {
    return (Boolean(database.value.sqlSettings.username) && Boolean(value)) || t('validation.password_required');
  }

  return true;
};

function initializeJdbcDrivers() {
  if (!jdbcDrivers.length) {
    systemStore.getJdbcDrivers().then((drivers) => {
      jdbcDrivers = drivers;
      (jdbcDrivers || []).forEach((driver: JdbcDriverDto) => {
        driverOptions.value.push({ label: driver.driverName, value: driver.driverClass });
      });
    });
  }
}

function onDriverChange(driverClass: string) {
  if (database.value.sqlSettings) {
    const jdbcDriver: JdbcDriverDto | undefined = jdbcDrivers.find((d) => d.driverClass === driverClass);
    if (jdbcDriver) database.value.sqlSettings.url = jdbcDriver.jdbcUrlExample;
  }
}

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
    if (value) {
      database.value = { ...props.database };
      hasDatasource.value = database.value.hasDatasource || false;
      delete database.value.hasDatasource;
      editMode.value = (props.database.name||'') !== '';
      if (database.value.usedForIdentifiers) {
        database.value.name = '_identifiers';
      }
      if (props.database.sqlSettings) {
        database.value.sqlSettings = { ...props.database.sqlSettings };
        if (!database.value.sqlSettings.driverClass) {
          database.value.sqlSettings.driverClass = driverOptions.value[0]?.value || '';
        }
        jdbcDatasourceSettings.value = database.value.sqlSettings.jdbcDatasourceSettings
          ? { ...database.value.sqlSettings.jdbcDatasourceSettings }
          : {
              defaultEntityType: 'Participant',
              defaultEntityIdColumnName: 'opal_id',
              defaultCreatedTimestampColumnName: 'opal_created',
              defaultUpdatedTimestampColumnName: 'opal_updated',
              useMetadataTables: true,
              multipleDatasources: true,
              multilines: false,
              mappedTables: [],
              tableSettings: [],
              tableSettingsFactories: [],
              batchSize: 100,
            };
      } else if (props.database.mongoDbSettings) {
        database.value.mongoDbSettings = { ...props.database.mongoDbSettings };
        database.value.usage = DatabaseDto_Usage.STORAGE;
        if (!database.value.mongoDbSettings.url) {
          database.value.mongoDbSettings.url = 'mongodb://localhost:27017/opal';
        }
      }
    }
  }
);

function onHide() {
  showDialog.value = false;
  emit('update:modelValue', false);
}

async function onSubmit() {
  let valid = true;
  if (database.value.sqlSettings) {
    database.value.sqlSettings.jdbcDatasourceSettings = jdbcDatasourceSettings.value;
    database.value.sqlSettings.sqlSchema = SqlSettingsDto_SqlSchema.JDBC;
    valid = await formRef.value.validate();
  }
  if (valid) {
    systemStore
      .saveDatabase(database.value, editMode.value)
      .then(() => {
        emit('save', true);
        onHide();
      })
      .catch((error) => {
        notifyError(t('db.save_error', { error: error.response.data.message }));
      });
  }
}

onMounted(() => {
  initializeJdbcDrivers();
});
</script>
