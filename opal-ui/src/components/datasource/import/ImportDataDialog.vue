<template>
  <q-dialog v-model="showDialog" persistent @hide="onHide" @before-show="onShow">
      <q-card class="dialog-md">
        <q-card-section>
          <div class="text-h6">{{ $t('import_data') }}</div>
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
            <q-step
              :name="1"
              :title="$t('select_import_source')"
              icon="settings"
              :done="step > 1"
            >
              <div v-if="isFile">
                <q-select
                  v-model="fileImporter"
                  :options="fileImporters"
                  :label="$t('data_format')"
                  dense
                  @update:model-value="onFileImporterChange"
                  class="q-mb-md"/>
                <div class="text-help q-mb-md">
                  {{  $t('select_dictionary_file') }}
                </div>
                <file-select
                  v-model="dataFile"
                  :folder="filesStore.current"
                  selection="single"
                  :extensions="fileExtensions"
                  class="q-mb-md"/>
              </div>
            </q-step>

            <q-step
              :name="2"
              :title="$t('configure_import_source')"
              icon="table_chart"
              :done="step > 2"
            >
              <div v-if="dataFile">
                <div v-if="fileImporter.value === 'csv'">
                  <import-csv-form v-model="factory" :file="dataFile" />
                </div>
                <div v-else-if="fileImporter.value === 'opal'">
                  <import-fs-form v-model="factory" :file="dataFile" />
                </div>
                <div v-else>
                  <import-haven-form v-model="factory" :file="dataFile" />
                </div>
              </div>
            </q-step>

            <q-step
              :name="3"
              :title="$t('select_import_options')"
              :caption="$t('optional')"
              icon="assignment"
              :done="step > 3"
            >
              <div>
                <q-checkbox v-model="merge" :label="$t('merge_dictionaries')" />
                <div class="text-hint q-mb-md">
                  {{  $t('merge_dictionaries_hint') }}
                </div>
                <q-input
                  v-model="limit"
                  :label="$t('limit')"
                  dense
                  type="number"
                  min="0"
                  step="1000"
                  class="q-mb-md"
                  :hint="$t('import_limit_hint')"
                />
                <q-checkbox v-model="incremental" :label="$t('incremental_import')" />
                <div class="text-hint q-mb-md">
                  {{  $t('incremental_import_hint') }}
                </div>
              </div>
            </q-step>

            <q-step
              :name="4"
              :title="$t('preview_import_source')"
              icon="table_chart"
            >
              <div v-if="transientDatasourceStore.datasource.table">
                <q-select
                  v-show="transientDatasourceStore.datasource?.table?.length > 1"
                  v-model="selectedTable"
                  :options="transientDatasourceStore.datasource.table"
                  :label="$t('tables')"
                  dense
                  @update:model-value="onTableSelection"
                  class="q-mb-md"/>
                <table-preview
                  v-if="transientDatasourceStore.datasource"
                  :table="transientDatasourceStore.table"
                  :variables="transientDatasourceStore.variables"
                  :loading="variablesLoading" />
              </div>
              <div v-else>
                <q-spinner
                  color="grey-6"
                  size="3em"
                  :thickness="5"
                />
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
            @click="$refs.stepper.previous()"
            :label="$t('back')" />
          <q-btn
            v-if="step < 4"
            flat
            icon-right="navigate_next"
            @click="$refs.stepper.next()"
            color="primary"
            :label="$t('continue')"
            :disable="!canNext"
            class="on-right"/>
          <q-btn
            flat
            :label="$t('cancel')"
            color="secondary"
            v-close-popup
            @click="onCancel"
          />
          <q-btn
            :disable="step < 4"
            flat
            :label="$t('import')"
            color="primary"
            @click="onImportData"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
</template>


<script lang="ts">
import { defineComponent } from 'vue';
import { ImportCommandOptionsDto } from 'src/models/Commands';
export default defineComponent({
  name: 'ImportDataDialog',
});
</script>
<script setup lang="ts">
import { FileDto } from 'src/models/Opal';
import { DatasourceFactory } from 'src/components/models';
import FileSelect from 'src/components/files/FileSelect.vue';
import ImportCsvForm from 'src/components/datasource/import/ImportCsvForm.vue';
import ImportFsForm from 'src/components/datasource/import/ImportFsForm.vue';
import ImportHavenForm from 'src/components/datasource/import/ImportHavenForm.vue';
import TablePreview from 'src/components/datasource/preview/TablePreview.vue';
import { notifyError, notifySuccess } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  type: 'file' | 'server';
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue'])

const pluginsStore = usePluginsStore();
const filesStore = useFilesStore();
const transientDatasourceStore = useTransientDatasourceStore();
const projectsStore = useProjectsStore();
const { t } = useI18n();

// TODO add plugins
const fileImporters = [
  { label: 'CSV', value: 'csv' },
  { label: 'Opal archive', value: 'opal' },
  { label: 'RDS (R)', value: 'rds' },
  { label: 'SAS (R)', value: 'haven-sas' },
  { label: 'SAS Transport (R)', value: 'haven-sast' },
  { label: 'SPSS (R)', value: 'haven-spss' },
  { label: 'Stata (R)', value: 'haven-stata' },
];

const fileImporterExtensions: {
  [key: string]: string[];
} = {
  csv: ['.csv', '.tsv'],
  opal: ['.zip'],
  rds: ['.rds', '.rdata'],
  'haven-sas': ['.sas7bdat', '.sas7bcat'],
  'haven-sast': ['.xpt'],
  'haven-spss': ['.sav'],
  'haven-stata': ['.dta'],
};

const showDialog = ref(props.modelValue);
const step = ref(1);
const dataFile = ref<FileDto>();
const factory = ref<DatasourceFactory>();
const merge = ref(false);
const incremental = ref(false);
const limit = ref();
const selectedTable = ref<string>();
const variablesLoading = ref(false);

const fileImporter = ref(fileImporters[0]);

const fileExtensions = computed(() => {
  const extensions = fileImporterExtensions[fileImporter.value.value];
  return extensions ? fileImporterExtensions[fileImporter.value.value] : [];
});

watch(() => props.modelValue, (value) => {
  step.value = 1;
  showDialog.value = value;
});

const canNext = computed(() => {
  if (step.value === 1) {
    return !!dataFile.value;
  }
  if (step.value === 2) {
    return factory.value !== undefined;
  }
  return step.value < 4;
});

const isFile = computed(() => props.type === 'file');

function onShow() {
  pluginsStore.initDatasourcePlugins('import');
  dataFile.value = undefined;
}

function onHide() {
  emit('update:modelValue', false);
}

function onCancel() {
  transientDatasourceStore.deleteDatasource();
}

function onFileImporterChange() {
  dataFile.value = undefined;
}

function onImportData() {
  const dsName = transientDatasourceStore.datasource.name;
  const options = {
    destination: projectsStore.project.name,
    tables: transientDatasourceStore.datasource.table.map((tbl) => `${dsName}.${tbl}`),
  } as ImportCommandOptionsDto;
  projectsStore.importCommand(projectsStore.project.name, options).then((response) => {
    notifySuccess(t('import_data_task_created', { id: response.data.id }));
  }).catch((err) => {
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
      }
    }
    transientDatasourceStore.createDatasource(factory.value, merge.value).then(() => {
      selectedTable.value = transientDatasourceStore.datasource.table[0];
      onTableSelection();
    }).catch((err) => {
      console.error(err);
      notifyError(err);
    })
  }
}

function onTableSelection() {
  if (!selectedTable.value) {
    return;
  }
  variablesLoading.value = true;
  transientDatasourceStore.loadTable(selectedTable.value).then(() => {
    transientDatasourceStore.loadVariables().catch((err) => {
      notifyError(err);
    }).finally(() => {
      variablesLoading.value = false;
    });
  }).catch((err) => {
    variablesLoading.value = false;
    notifyError(err);
  });
}
</script>
