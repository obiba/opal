<template>
  <q-dialog v-model="showDialog" persistent @hide="onHide" @before-show="onShow">
      <q-card style="width: 700px; max-width: 80vw;">
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
                  class="q-mb-md"/>
                <div class="text-help q-mb-md">
                  {{  $t('select_dictionary_file') }}
                </div>
                <file-select
                  v-model="dataFile"
                  :folder="filesStore.current"
                  selection="single"
                  :extensions="fileExtensions"
                  @select="onFileSelection"
                  class="q-mb-md"/>
              </div>
            </q-step>

            <q-step
              :name="2"
              :title="$t('configure_import_source')"
              icon="table_chart"
              :done="step > 2"
            >
              <div v-if="fileImporter.value === 'csv'">
                <import-csv-form v-model="factory" :file="dataFile" />
              </div>
              <div v-else>
                TODO: configure import source
              </div>
            </q-step>

            <q-step
              :name="3"
              :title="$t('preview_import_source')"
              icon="table_chart"
              :done="step > 3"
            >
              TODO: make transient datasource and preview values
              <pre>{{ transientDatasourceStore.datasource }}</pre>
            </q-step>

            <q-step
              :name="4"
              :title="$t('select_import_options')"
              :caption="$t('optional')"
              icon="assignment"
            >
              TODO: incremental etc.
            </q-step>

            <q-stepper-navigation>
              <q-btn
                v-if="step > 1"
                size="sm"
                flat
                icon="navigate_before"
                color="secondary"
                @click="$refs.stepper.previous()"
                :label="$t('back')" />
              <q-btn
                v-if="step < 4"
                size="sm"
                icon-right="navigate_next"
                @click="$refs.stepper.next()"
                color="primary"
                :label="$t('continue')"
                :disable="!canNext"
                class="on-right"/>

            </q-stepper-navigation>
          </q-stepper>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="bg-grey-3">
          <q-btn
            flat
            :label="$t('cancel')"
            color="secondary"
            v-close-popup
            @click="onCancel"
          />
          <q-btn
            :disable="step < 3"
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

function onFileSelection(file: FileDto) {
  console.log('File selected', file);
  console.log('File data', dataFile.value);
}

function onStepChange(value: number) {
  console.log('Step change', value);
  if (value === 2) {
    // clear transient datasource if any
    transientDatasourceStore.deleteDatasource();
  }
  if (value === 3) {
    transientDatasourceStore.createDatasource(factory.value);
  }
}
</script>
