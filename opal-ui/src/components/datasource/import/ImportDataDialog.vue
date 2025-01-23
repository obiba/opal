<template>
  <q-dialog v-model="showDialog" persistent @hide="onHide" @before-show="onShow">
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ t('import_data') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-stepper
          v-model="step"
          ref="stepper"
          flat
          vertical
          animated
          class="q-pt-none q-pb-none"
          @update:model-value="onStepChange"
        >
          <q-step :name="1" :title="t('select_import_source')" icon="settings" :done="step > 1">
            <div v-if="isFile">
              <q-select
                v-model="fileImporter"
                :options="fileImporters"
                :label="t('data_format')"
                dense
                @update:model-value="onImporterSelection"
                class="q-mb-md"
              />
              <div class="text-hint">
                {{ fileImporterHint }}
              </div>
            </div>
            <div v-else-if="isServer">
              <q-select
                v-model="serverImporter"
                :options="serverImporters"
                :label="t('data_server')"
                dense
                @update:model-value="onImporterSelection"
                class="q-mb-md"
              />
              <div class="text-hint">
                {{ serverImporterHint }}
              </div>
            </div>
            <div v-else-if="isDatabase">
              <q-select
                v-model="databaseImporter"
                :options="databaseImporters"
                :label="t('database')"
                dense
                @update:model-value="onImporterSelection"
                emit-value
                map-options
              />
            </div>
            <div v-else>
              <div class="text-negative">Unidentified data source.</div>
            </div>
          </q-step>

          <q-step :name="2" :title="t('configure_import_source')" icon="table_chart" :done="step > 2">
            <div v-if="isFile">
              <div v-if="fileImporter.value === 'csv'">
                <import-csv-form v-model="factory" />
              </div>
              <div v-else-if="fileImporter.value === 'opal'">
                <import-fs-form v-model="factory" />
              </div>
              <div v-else-if="fileImporter.value.startsWith('haven_')">
                <import-haven-form v-model="factory" :type="fileImporter.value" />
              </div>
              <div v-else>
                <import-plugin-form v-model="factory" :type="fileImporter.value" />
              </div>
            </div>
            <div v-else-if="isServer">
              <div v-if="serverImporter.value === 'opal'">
                <import-opal-form v-model="factory" />
              </div>
              <div v-else>
                <import-plugin-form v-model="factory" :type="serverImporter.value" />
              </div>
            </div>
            <div v-else-if="isDatabase">
              <import-database-form v-model="factory" :database="databaseImporter" />
            </div>
          </q-step>

          <q-step
            :name="3"
            :title="t('select_import_options')"
            :caption="t('optional')"
            icon="assignment"
            :done="step > 3"
          >
            <div>
              <q-checkbox v-model="merge" :label="t('merge_dictionaries')" />
              <div class="text-hint q-mb-md">
                {{ t('merge_dictionaries_hint') }}
              </div>
              <q-input
                v-model="limit"
                :label="t('limit')"
                dense
                type="number"
                min="0"
                step="1000"
                class="q-mb-md"
                :hint="t('import_limit_hint')"
              />
              <q-checkbox v-model="incremental" :label="t('incremental_import')" />
              <div class="text-hint q-mb-md">
                {{ t('incremental_import_hint') }}
              </div>
              <identifiers-mapping-select v-model="idConfig" :for-import="true" />
            </div>
          </q-step>

          <q-step :name="4" :title="t('preview_import_source')" icon="table_chart">
            <div v-if="transientDatasourceStore.datasource.table">
              <q-select
                v-show="transientDatasourceStore.datasource?.table?.length > 1"
                v-model="selectedTable"
                :options="transientDatasourceStore.datasource.table"
                :label="t('tables')"
                dense
                @update:model-value="onTableSelection"
                class="q-mb-md"
              />
              <table-preview
                v-if="transientDatasourceStore.datasource"
                :table="transientDatasourceStore.table"
                :variables="transientDatasourceStore.variables"
                :loading="variablesLoading"
              />
            </div>
            <div v-else>
              <q-spinner-dots size="lg" />
            </div>
          </q-step>
        </q-stepper>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn
          v-if="step > 1"
          flat
          icon="navigate_before"
          color="primary"
          @click="stepper.previous()"
          :label="t('back')"
        />
        <q-btn
          v-if="step < 4"
          flat
          icon-right="navigate_next"
          @click="stepper.next()"
          color="primary"
          :label="t('continue')"
          :disable="!canNext"
          class="on-right"
        >
        </q-btn>
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup @click="onCancel" />
        <q-btn :disable="step < 4" flat :label="t('import')" color="primary" @click="onImportData" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { DatasourceFactory } from 'src/components/models';
import type { ImportCommandOptionsDto } from 'src/models/Commands';
import ImportCsvForm from 'src/components/datasource/import/ImportCsvForm.vue';
import ImportFsForm from 'src/components/datasource/import/ImportFsForm.vue';
import ImportHavenForm from 'src/components/datasource/import/ImportHavenForm.vue';
import ImportOpalForm from 'src/components/datasource/import/ImportOpalForm.vue';
import ImportPluginForm from 'src/components/datasource/import/ImportPluginForm.vue';
import ImportDatabaseForm from 'src/components/datasource/import/ImportDatabaseForm.vue';
import TablePreview from 'src/components/datasource/preview/TablePreview.vue';
import { notifyError, notifySuccess } from 'src/utils/notify';
import { type DatabaseDto, DatabaseDto_Usage } from 'src/models/Database';
import IdentifiersMappingSelect from 'src/components/datasource/IdentifiersMappingSelect.vue';
import type { IdentifiersMappingConfigDto } from 'src/models/Identifiers';

interface DialogProps {
  modelValue: boolean;
  type: 'file' | 'server' | 'database';
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const systemStore = useSystemStore();
const pluginsStore = usePluginsStore();
const transientDatasourceStore = useTransientDatasourceStore();
const projectsStore = useProjectsStore();
const { t } = useI18n();

const stepper = ref();
const showDialog = ref(props.modelValue);
const step = ref(1);
const factory = ref<DatasourceFactory>();
const merge = ref(false);
const incremental = ref(false);
const limit = ref();
const selectedTable = ref<string>();
const variablesLoading = ref(false);

interface ImporterOption {
  label: string;
  value: string;
  hint?: string;
}

const builtinFileImporters: ImporterOption[] = [
  { label: 'CSV', value: 'csv' },
  { label: 'Opal archive', value: 'opal' },
  { label: 'RDS (R)', value: 'haven_rds' },
  { label: 'SAS (R)', value: 'haven_sas' },
  { label: 'SAS Transport (R)', value: 'haven_sast' },
  { label: 'SPSS (R)', value: 'haven_spss' },
  { label: 'Stata (R)', value: 'haven_stata' },
];
const fileImporters = ref([...builtinFileImporters]);
const fileImporter = ref();

const builtinServerImporters: ImporterOption[] = [{ label: 'Opal', value: 'opal' }];
const serverImporters = ref([...builtinServerImporters]);
const serverImporter = ref();
const databaseImporter = ref();
const databases = ref<DatabaseDto[]>([]);
const idConfig = ref<IdentifiersMappingConfigDto | undefined>();

const databaseImporters = computed(() => {
  return databases.value.map((db) => ({ label: db.name, value: db }));
});

const fileImporterHint = computed(() => {
  return fileImporter.value
    ? fileImporter.value.hint
      ? fileImporter.value.hint
      : t(`importer.file.${fileImporter.value.value}`)
    : '';
});

const serverImporterHint = computed(() => {
  return serverImporter.value
    ? serverImporter.value.hint
      ? serverImporter.value.hint
      : t(`importer.server.${serverImporter.value.value}`)
    : '';
});

watch(
  () => props.modelValue,
  (value) => {
    step.value = 1;
    // reset, not delete, otherwise a transient datasource in a previous import task would be deleted
    transientDatasourceStore.reset();
    showDialog.value = value;
  }
);

const canNext = computed(() => {
  if (isDatabase.value && databaseImporters.value.length === 0) {
    return false;
  }
  if (step.value === 2) {
    return factory.value !== undefined;
  }
  return step.value < 4;
});

const isFile = computed(() => props.type === 'file');
const isServer = computed(() => props.type === 'server');
const isDatabase = computed(() => props.type === 'database');

function onShow() {
  fileImporter.value = [...builtinFileImporters];
  serverImporter.value = [...builtinServerImporters];
  pluginsStore.initDatasourcePlugins('import').then(() => {
    pluginsStore.datasourceImportPlugins.forEach((plugin) => {
      if (plugin['Plugins.DatasourcePluginPackageDto.datasource']?.group === 'FILE') {
        if (!fileImporters.value.find((importer) => importer.value === plugin.name))
          fileImporters.value.push({ label: plugin.title, value: plugin.name, hint: plugin.description });
      } else if (plugin['Plugins.DatasourcePluginPackageDto.datasource']?.group === 'SERVER') {
        if (!serverImporters.value.find((importer) => importer.value === plugin.name))
          serverImporters.value.push({ label: plugin.title, value: plugin.name, hint: plugin.description });
      }
    });
  });
  if (isDatabase.value) {
    databaseImporter.value = null;
    systemStore
      .getDatabases(DatabaseDto_Usage.IMPORT)
      .then((response) => {
        databases.value = response?.filter((db) => db.usedForIdentifiers !== true) || [];
        if (databaseImporters.value.length > 0) databaseImporter.value = databaseImporters.value[0]?.value;
      })
      .catch((err) => {
        notifyError(err);
      });
  }

  fileImporter.value = fileImporters.value[0];
  serverImporter.value = serverImporters.value[0];
  factory.value = undefined;
}

function onHide() {
  emit('update:modelValue', false);
}

function onCancel() {
  transientDatasourceStore.deleteDatasource();
}

function onImportData() {
  const dsName = transientDatasourceStore.datasource.name;
  const options = {
    destination: projectsStore.project.name,
    tables: transientDatasourceStore.datasource.table.map((tbl) => `${dsName}.${tbl}`),
  } as ImportCommandOptionsDto;

  if (idConfig.value && idConfig.value.name) {
    options.idConfig = idConfig.value;
  }

  projectsStore
    .importCommand(projectsStore.project.name, options)
    .then((response) => {
      notifySuccess(t('import_data_task_created', { id: response.data.id }));
    })
    .catch((err) => {
      console.error(err);
      notifyError(err);
    });
}

function onStepChange(value: string | number) {
  if (value === 3) {
    // clear transient datasource if any
    transientDatasourceStore.deleteDatasource();
  }
  if (value === 4 && factory.value) {
    if (limit.value) {
      factory.value.batchConfig = { limit: parseInt(limit.value) };
    }
    if (incremental.value) {
      factory.value.incrementalConfig = {
        incremental: true,
        incrementalDestinationName: projectsStore.project.name,
      };
    }
    transientDatasourceStore
      .createDatasource(factory.value, merge.value)
      .then(() => {
        selectedTable.value = transientDatasourceStore.datasource.table[0];
        onTableSelection();
      })
      .catch((err) => {
        console.error(err);
        notifyError(err);
      });
  }
}

function onTableSelection() {
  if (!selectedTable.value) {
    return;
  }
  variablesLoading.value = true;
  transientDatasourceStore
    .loadTable(selectedTable.value)
    .then(() => {
      transientDatasourceStore
        .loadVariables()
        .catch((err) => {
          notifyError(err);
        })
        .finally(() => {
          variablesLoading.value = false;
        });
    })
    .catch((err) => {
      variablesLoading.value = false;
      notifyError(err);
    });
}

function onImporterSelection() {
  factory.value = undefined;
}
</script>
