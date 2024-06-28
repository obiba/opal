<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ $t(editMode ? 'edit' : 'register') }}</div>
      </q-card-section>
      <q-separator />
      <q-card-section>
        <q-input
          v-model="database.name"
          :label="$t('name')"
          :hint="$t('db.name_hint')"
          :disable="editMode"
          dense
          class="q-mb-md" />
        <q-select
          v-if="database.sqlSettings"
          v-model="database.usage"
          :options="usageOptions"
          :label="$t('usage')"
          :hint="$t('db.usage_hint')"
          :disable="hasDatasource"
          dense
          emit-value
          map-options
          class="q-mb-md" />
        <q-toggle
          v-if="database.usage === DatabaseDto_Usage.STORAGE"
          v-model="database.defaultStorage"
          :label="$t('default_storage')"
          dense
          class="q-mb-md" />
        <div v-if="database.sqlSettings">
          <q-select
            v-model="database.sqlSettings.driverClass"
            :options="driverOptions"
            :label="$t('db.driver')"
            :hint="$t('db.driver_hint')"
            dense
            emit-value
            map-options
            class="q-mb-md" />
          <q-input
            v-model="database.sqlSettings.url"
            label="URL"
            :hint="$t('example', { text: dbUrlPlaceholder })"
            dense
            class="q-mb-md" />
          <div class="row q-col-gutter-md q-mb-md">
            <div class="col">
              <q-input
                v-model="database.sqlSettings.username"
                :label="$t('username')"
                dense />
            </div>
            <div class="col">
              <q-input
                v-model="database.sqlSettings.password"
                :label="$t('password')"
                dense />
            </div>
          </div>
          <div v-if="database.usage !== DatabaseDto_Usage.STORAGE">
            <q-input
              v-model="jdbcDatasourceSettings.defaultEntityType"
              :label="$t('db.default_entity_type')"
              :hint="$t('db.default_entity_type_hint')"
              dense
              class="q-mb-md" />
            <q-input
              v-model="jdbcDatasourceSettings.defaultEntityIdColumnName"
              :label="$t('db.default_id_column')"
              :hint="$t('db.default_id_column_hint')"
              dense
              class="q-mb-md" />
            <q-input
              v-model="jdbcDatasourceSettings.defaultUpdatedTimestampColumnName"
              :label="$t('db.default_updated_column')"
              :hint="$t('db.default_updated_column_hint')"
              dense
              class="q-mb-md" />
            <div v-if="database.usage === DatabaseDto_Usage.EXPORT" class="q-mt-lg">
              <q-toggle
                v-model="jdbcDatasourceSettings.useMetadataTables"
                :label="$t('db.use_metadata_tables')"
                dense
                class="q-mb-sm" />
              <div class="text-hint">
                {{ $t('db.use_metadata_tables_hint') }}
              </div>
            </div>
          </div>

          <q-list>
            <q-expansion-item
              switch-toggle-side
              dense
              header-class="text-primary text-caption"
              :label="$t('advanced_options')"
            >
              <q-input
                v-model="jdbcDatasourceSettings.batchSize"
                :label="$t('db.batch_size')"
                :hint="$t('db.batch_size_hint')"
                dense
                class="q-mb-md" />
              <q-input
                v-model="database.sqlSettings.properties"
                :label="$t('options')"
                placeholder="key=value"
                dense
                type="textarea"
                class="q-mb-md" />
            </q-expansion-item>
          </q-list>
        </div>
        <div v-if="database.mongoDbSettings">
          <q-input
            v-model="database.mongoDbSettings.url"
            label="URL"
            :hint="$t('example', { text: dbUrlPlaceholder })"
            dense
            class="q-mb-md" />
          <div class="row q-col-gutter-md">
            <div class="col">
              <q-input
                v-model="database.mongoDbSettings.username"
                :label="$t('username')"
                dense
                class="q-mb-md" />
            </div>
            <div class="col">
              <q-input
                v-model="database.mongoDbSettings.password"
                :label="$t('password')"
                dense
                class="q-mb-md" />
            </div>
          </div>
          <q-list>
            <q-expansion-item
              switch-toggle-side
              dense
              header-class="text-primary text-caption"
              :label="$t('advanced_options')"
            >
              <q-input
                v-model="database.mongoDbSettings.batchSize"
                :label="$t('db.batch_size')"
                :hint="$t('db.batch_size_hint')"
                dense
                class="q-mb-md" />
              <q-input
                v-model="database.mongoDbSettings.properties"
                :label="$t('options')"
                placeholder="key=value"
                dense
                type="textarea"
                class="q-mb-md" />
            </q-expansion-item>
          </q-list>
        </div>
      </q-card-section>
      <q-separator />
      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
        <q-btn
          flat
          :label="$t('save')"
          color="primary"
          :disable="!database.name || !hasUrl"
          @click="onSubmit"
          v-close-popup
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'EditDatabaseDialog',
});
</script><script setup lang="ts">
import { DatabaseDto, DatabaseDto_Usage, SqlSettingsDto_SqlSchema } from 'src/models/Database';
import { JdbcDatasourceSettingsDto } from 'src/models/Magma';

interface DialogProps {
  modelValue: boolean
  database: DatabaseDto
}

const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const emit = defineEmits(['update:modelValue'])

const { t } = useI18n();

const database = ref<DatabaseDto>(props.database);
const jdbcDatasourceSettings = ref<JdbcDatasourceSettingsDto>({} as JdbcDatasourceSettingsDto);
const editMode = ref<boolean>(false);
const hasDatasource = ref<boolean>(false);

const hasUrl = computed(() => database.value.sqlSettings?.url || database.value.mongoDbSettings?.url);
const dbUrlPlaceholder = computed(() => {
  if (database.value.sqlSettings) {
    if (database.value.sqlSettings.driverClass === 'org.postgresql.Driver') {
      return 'jdbc:postgresql://localhost:5432/opal';
    } else if (database.value.sqlSettings.driverClass === 'org.mariadb.jdbc.Driver') {
      return 'jdbc:mariadb://localhost:4306/opal';
    } else if (database.value.sqlSettings.driverClass === 'com.mysql.jdbc.Driver') {
      return 'jdbc:mysql://localhost:3306/opal';
    }
    return 'jdbc:';
  } else if (database.value.mongoDbSettings) {
    return 'mongodb://localhost:27017/opal';
  }
});

const usageOptions = [
  { label: t('storage'), value: DatabaseDto_Usage.STORAGE },
  { label: t('import'), value: DatabaseDto_Usage.IMPORT },
  { label: t('export'), value: DatabaseDto_Usage.EXPORT },
];

const driverOptions = [
  { label: 'MariaDB', value: 'org.mariadb.jdbc.Driver' },
  { label: 'MySQL', value: 'com.mysql.jdbc.Driver' },
  { label: 'PostgreSQL', value: 'org.postgresql.Driver' },
];

watch(() => props.modelValue, (value) => {
  showDialog.value = value;
  if (value) {
    database.value = { ...props.database };
    hasDatasource.value = !!database.value.hasDatasource;
    delete database.value.hasDatasource;
    editMode.value = !!props.database.name;
    if (props.database.sqlSettings) {
      database.value.sqlSettings = { ...props.database.sqlSettings };
      if (!database.value.sqlSettings.driverClass) {
        database.value.sqlSettings.driverClass = driverOptions[0].value;
      }
      jdbcDatasourceSettings.value = database.value.sqlSettings.jdbcDatasourceSettings ?
        { ...database.value.sqlSettings.jdbcDatasourceSettings } :
        {
          defaultEntityType: 'Participant',
          defaultEntityIdColumnName: 'opal_id',
          defaultCreatedTimestampColumnName: 'opal_created',
          defaultUpdatedTimestampColumnName: 'opal_updated',
          useMetadataTables: true,
          multipleDatasources: true,
          multilines: false,
          mappedTables: [],
          tableSettings: [],
          tableSettingsFactories:[],
          batchSize: 100
        };
    } else if (props.database.mongoDbSettings) {
      database.value.mongoDbSettings = { ...props.database.mongoDbSettings };
      database.value.usage = DatabaseDto_Usage.STORAGE;
      if (!database.value.mongoDbSettings.url) {
        database.value.mongoDbSettings.url = 'mongodb://localhost:27017/opal';
      }
    }
  }
});

function onHide() {
  emit('update:modelValue', false);
}

function onSubmit() {
  if (database.value.sqlSettings) {
    database.value.sqlSettings.jdbcDatasourceSettings = jdbcDatasourceSettings.value;
    database.value.sqlSettings.sqlSchema = SqlSettingsDto_SqlSchema.JDBC;
  }
  console.log(database.value);
}
</script>
