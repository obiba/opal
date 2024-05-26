<template>
  <q-dialog v-model="showDialog" persistent @hide="onHide" @before-show="onShow">
      <q-card class="dialog-sm">
        <q-card-section>
          <div class="text-h6">{{ $t('export_data') }}</div>
        </q-card-section>

        <q-separator />

        <q-card-section>
          <div class="q-mb-md box-info">
            <q-icon name="info" size="1.2rem"/>
            <span class="on-right">
              {{ exportTablesText }}
            </span>
          </div>

          <div v-if="isFile">
            <q-select
              v-model="fileExporter"
              :options="fileExporters"
              :label="$t('data_format')"
              dense
              class="q-mb-md"/>
            <div class="text-hint q-mb-md">
              {{ fileExporterHint }}
            </div>

            <div class="q-mb-md">
              <export-csv-form
                v-if="fileExporter.value === 'csv'"
                v-model="out"
                :tables="props.tables"/>
              <export-fs-form
                v-else-if="fileExporter.value === 'opal'"
                v-model="out"
                :tables="props.tables"/>
              <export-haven-form
                v-else-if="fileExporter.value.startsWith('haven_')"
                v-model="out"
                :tables="props.tables"
                :type="fileExporter.value" />
              <export-plugin-form
                v-else
                v-model="out"
                :tables="props.tables"
                :type="fileExporter.value" />
            </div>

            <q-list class="q-mt-lg">
              <q-expansion-item
                switch-toggle-side
                dense
                header-class="text-primary text-caption"
                :label="$t('advanced_options')"
              >
                <q-input
                  v-model="entityIdNames"
                  :label="$t('id_column_name')"
                  :hint="$t('id_column_name_hint')"
                  dense
                  class="q-mb-md"
                  :debounce="500"/>
              </q-expansion-item>
            </q-list>
          </div>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="bg-grey-3">
          <q-btn
            flat
            :label="$t('cancel')"
            color="secondary"
            v-close-popup
          />
          <q-btn
            flat
            :label="$t('export')"
            color="primary"
            @click="onExportData"
            :disable="out === undefined"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
</template>


<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'ExportDataDialog',
});
</script>
<script setup lang="ts">
import { ExportCommandOptionsDto } from 'src/models/Commands';
import { TableDto } from 'src/models/Magma';
import ExportCsvForm from 'src/components/datasource/export/ExportCsvForm.vue';
import ExportFsForm from 'src/components/datasource/export/ExportFsForm.vue';
import ExportHavenForm from 'src/components/datasource/export/ExportHavenForm.vue';
import ExportPluginForm from 'src/components/datasource/export/ExportPluginForm.vue';
import { notifyError, notifySuccess } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  tables: TableDto[];
  type: 'file' | 'server';
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue'])

const pluginsStore = usePluginsStore();
const projectsStore = useProjectsStore();
const { t } = useI18n();

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
const out = ref<string>(); // output parameters
const entityIdNames = ref('');
const fileExporters = ref([...builtinFileExporters]);
const fileExporter = ref();

const exportTablesText = computed(() => t('export_tables_text', { count: props.tables.length }));

const fileExporterHint = computed(() => {
  return fileExporter.value ? (fileExporter.value.hint ? fileExporter.value.hint : t(`exporter.file.${fileExporter.value.value}`)) : '';
});

const isFile = computed(() => props.type === 'file');

watch(() => props.modelValue, (value) => {
  showDialog.value = value;
});

function onShow() {
  fileExporter.value = [...builtinFileExporters];
  pluginsStore.initDatasourcePlugins('export').then(() => {
    pluginsStore.datasourceExportPlugins
    .forEach((plugin) => {
      if (plugin['Plugins.DatasourcePluginPackageDto.datasource']?.group === 'FILE') {
        if (!fileExporters.value.find((exporter) => exporter.value === plugin.name))
          fileExporters.value.push({ label: plugin.title, value: plugin.name, hint: plugin.description});
      }
    });
  });
  fileExporter.value = fileExporters.value[0];
}

function onHide() {
  emit('update:modelValue', false);
}

function onExportData() {
  if (!out.value) {
    notifyError(t('destination_folder_required'));
    return;
  }
  const options = {
    format: fileExporter.value.value,
    tables: props.tables.map((t) => `${t.datasourceName}.${t.name}`),
    out: out.value,
    entityIdNames: entityIdNames.value ? entityIdNames.value : undefined,
    copyNullValues: true,
    noVariables: false,
    nonIncremental: true,
  } as ExportCommandOptionsDto;
  projectsStore.exportCommand(projectsStore.project.name, options).then((response) => {
    notifySuccess(t('export_tables_task_created', { id: response.data.id }));
  }).catch((err) => {
    console.error(err);
    notifyError(err);
  });
}

</script>
