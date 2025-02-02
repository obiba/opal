<template>
  <q-dialog v-model="showDialog" persistent @hide="onHide" @before-show="onShow">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('export_data') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <div class="q-mb-md box-info">
          <q-icon name="info" size="1.2rem" />
          <span class="on-right">
            {{ exportTablesText }}
          </span>
        </div>
        <div v-if="isFile">
          <q-select v-model="fileExporter" :options="fileExporters" :label="t('data_format')" dense class="q-mb-md" />
          <div class="text-hint q-mb-md">
            {{ fileExporterHint }}
          </div>

          <div class="q-mb-md">
            <export-csv-form
              v-if="exportType === FileExportType.CSV"
              v-model="out"
              :folder="folder"
              :tables="props.tables"
            />
            <export-fs-form
              v-else-if="exportType === FileExportType.OPAL"
              v-model="out"
              :folder="folder"
              :tables="props.tables"
            />
            <export-haven-form
              v-else-if="exportType === FileExportType.HAVEN"
              v-model="out"
              :folder="folder"
              :tables="props.tables"
              :type="fileExporter.value"
            />
            <export-plugin-form v-else v-model="outPlugin" :tables="props.tables" :type="fileExporter.value" />
          </div>

          <q-list class="q-mt-lg">
            <q-expansion-item
              switch-toggle-side
              dense
              header-class="text-primary text-caption"
              :label="t('advanced_options')"
            >
              <q-input
                v-model="entityIdNames"
                :label="t('id_column_name')"
                :hint="t('id_column_name_hint')"
                dense
                class="q-mb-md"
                :debounce="500"
              />
            </q-expansion-item>
          </q-list>
        </div>
        <div v-else-if="isDatabase">
          <q-select
            v-model="databaseExporter"
            :options="databaseExporters"
            :label="t('database')"
            dense
            emit-value
            map-options
          />
        </div>
        <identifiers-mapping-select v-model="idConfig" :for-import="false" />
      </q-card-section>

      <q-separator />
      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn
          flat
          :label="t('export')"
          color="primary"
          @click="onExportData"
          :disable="isFile ? out === undefined : databaseExporter === undefined"
          v-close-popup
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { ExportCommandOptionsDto } from 'src/models/Commands';
import type { TableDto } from 'src/models/Magma';
import ExportCsvForm from 'src/components/datasource/export/ExportCsvForm.vue';
import ExportFsForm from 'src/components/datasource/export/ExportFsForm.vue';
import ExportHavenForm from 'src/components/datasource/export/ExportHavenForm.vue';
import ExportPluginForm from 'src/components/datasource/export/ExportPluginForm.vue';
import IdentifiersMappingSelect from 'src/components/datasource/IdentifiersMappingSelect.vue';
import { notifyError, notifySuccess } from 'src/utils/notify';
import { type DatabaseDto, DatabaseDto_Usage } from 'src/models/Database';
import type { IdentifiersMappingConfigDto } from 'src/models/Identifiers';

interface DialogProps {
  modelValue: boolean;
  tables: TableDto[];
  type: 'file' | 'server' | 'database';
}

enum FileExportType {
  NA = 'na',
  CSV = 'csv',
  OPAL = 'opal',
  HAVEN = 'haven',
  PLUGIN = 'plugin',
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const authStore = useAuthStore();
const systemStore = useSystemStore();
const pluginsStore = usePluginsStore();
const projectsStore = useProjectsStore();
const filesStore = useFilesStore();
const { t } = useI18n();
const idConfig = ref<IdentifiersMappingConfigDto | undefined>();

interface ExporterOption {
  label: string;
  value: string;
  hint?: string;
}

const builtinFileExporters: ExporterOption[] = [
  { label: 'CSV', value: 'csv' },
  { label: 'Opal archive', value: 'opal' },
  { label: 'RDS (R)', value: 'haven_rds' },
  { label: 'SAS (R)', value: 'haven_sas' },
  { label: 'SAS Transport (R)', value: 'haven_sast' },
  { label: 'SPSS (R)', value: 'haven_spss' },
  { label: 'Stata (R)', value: 'haven_stata' },
];

const showDialog = ref(props.modelValue);
const folder = ref<string>();
const out = ref<string>(); // output parameters
const outPlugin = ref<string>(); // output parameters for plugin
const entityIdNames = ref('');
const fileExporters = ref([...builtinFileExporters]);
const fileExporter = ref();
const databaseExporter = ref();
const databases = ref<DatabaseDto[]>([]);

const databaseExporters = computed(() => {
  return databases.value.map((db) => ({ label: db.name, value: db }));
});

const exportTablesText = computed(() => t('export_tables_text', { count: props.tables.length }));

const fileExporterHint = computed(() => {
  return fileExporter.value
    ? fileExporter.value.hint
      ? fileExporter.value.hint
      : t(`exporter.file.${fileExporter.value.value}`)
    : '';
});

const isFile = computed(() => props.type === 'file');
const isDatabase = computed(() => props.type === 'database');
const exportType = computed(() => {
  if (isFile) {
    if (fileExporter.value) {
      if (fileExporter.value.value === 'csv') return FileExportType.CSV;
      if (fileExporter.value.value === 'opal') return FileExportType.OPAL;
      if (fileExporter.value.value.startsWith('haven_')) return FileExportType.HAVEN;
      return FileExportType.PLUGIN;
    }
  }
  return FileExportType.NA;
});

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
    if (value) {
      out.value = undefined;
      folder.value = undefined;
      if (projectsStore.project.exportFolder) {
        folder.value = projectsStore.project.exportFolder;
      } else {
        folder.value = `/home/${authStore.profile.principal}/export`;
      }
      if (isFile.value) {
        // assume the plugin's data schema uses the 'file' key
        outPlugin.value = JSON.stringify({ file: folder.value });
      }
      filesStore.refreshFiles(folder.value);
    }
  },
);

function onShow() {
  fileExporter.value = [...builtinFileExporters];
  pluginsStore.initDatasourcePlugins('export').then(() => {
    pluginsStore.datasourceExportPlugins.forEach((plugin) => {
      if (plugin['Plugins.DatasourcePluginPackageDto.datasource']?.group === 'FILE') {
        if (!fileExporters.value.find((exporter) => exporter.value === plugin.name))
          fileExporters.value.push({ label: plugin.title, value: plugin.name, hint: plugin.description });
      }
    });
  });
  if (isDatabase.value) {
    databaseExporter.value = null;
    systemStore
      .getDatabases(DatabaseDto_Usage.EXPORT)
      .then((response) => {
        databases.value = response?.filter((db) => db.usedForIdentifiers !== true) || [];
        if (databaseExporters.value.length > 0) databaseExporter.value = databaseExporters.value[0]?.value;
      })
      .catch((err) => {
        notifyError(err);
      });
  }
  fileExporter.value = fileExporters.value[0];
}

function onHide() {
  emit('update:modelValue', false);
}

function onExportData() {
  let options: ExportCommandOptionsDto | undefined;
  if (isFile.value) {
    options = exportFile();
  } else if (isDatabase.value) {
    options = exportDatabase();
  }
  if (!options) {
    return;
  }

  if (idConfig.value && idConfig.value.name) {
    options.idConfig = idConfig.value;
  }

  projectsStore
    .exportCommand(projectsStore.project.name, options)
    .then((response) => {
      notifySuccess(t('export_tables_task_created', { id: response.data.id }));
    })
    .catch((err) => {
      console.error(err);
      notifyError(err);
    });
}

function exportFile() {
  if (!out.value) {
    notifyError(t('destination_folder_required'));
    return;
  }

  const options = {
    format: fileExporter.value.value,
    tables: props.tables.map((t) => `${t.datasourceName}.${t.name}`),
    out: isFile && exportType.value === FileExportType.PLUGIN ? outPlugin.value : out.value,
    entityIdNames: entityIdNames.value ? entityIdNames.value : undefined,
    copyNullValues: true,
    noVariables: false,
    nonIncremental: true,
  } as ExportCommandOptionsDto;
  return options;
}

function exportDatabase() {
  if (!databaseExporter.value) {
    notifyError(t('database_required'));
    return;
  }
  const options = {
    format: 'JDBC',
    out: databaseExporter.value.name,
    tables: props.tables.map((t) => `${t.datasourceName}.${t.name}`),
    copyNullValues: true,
    noVariables: false,
    nonIncremental: true,
  } as ExportCommandOptionsDto;
  return options;
}
</script>
