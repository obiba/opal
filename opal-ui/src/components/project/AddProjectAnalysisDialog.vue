<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ dialogTitle }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-input
            v-model="analysis.name"
            dense
            type="text"
            :label="$t('name') + ' *'"
            class="q-mb-md"
            lazy-rules
            :disable="editMode"
            :rules="[validateRequiredField]"
          >
          </q-input>
          <q-select
            v-model="selectedTemplate"
            :options="templateOptions"
            :label="$t('vcf_store.export_participants_filter_label')"
            :hint="$t('vcf_store.export_mapping_table_hint')"
            dense
            map-options
            input-debounce="0"
          >
            <template v-slot:option="scope">
              <q-item v-show="!!!scope.opt.value" class="text-help" dense clickable disable :label="scope.opt.label">
                <q-item-section class="q-pa-none">
                  {{ scope.opt.label }}
                </q-item-section>
              </q-item>
              <q-item
                v-show="!!scope.opt.value"
                dense
                clickable
                v-close-popup
                @click="selectedTemplate = scope.opt.value"
              >
                <q-item-section class="q-pl-md">
                  {{ scope.opt.label }}
                </q-item-section>
              </q-item>
            </template>
          </q-select>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="$t('close')" color="secondary" v-close-popup />
        <q-btn flat v-if="!editMode" :label="submitCaption" type="submit" color="primary" @click="onRunAnalysis" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'AddProjectAnalysisDialog',
});
</script>

<script setup lang="ts">
import { OpalAnalysisDto } from 'src/models/Projects';
// import { AnalyseCommandOptionsDto, AnalyseCommandOptionsDto_AnalyseDto } from 'src/models/Commands';

interface DialogProps {
  modelValue: boolean;
  projectName: string;
  tableName: string;
  analysisName?: string;
}

type TemplateOption = { label: string; value?: { pluginName: string; templateName: string } };

const { t } = useI18n();
const projectsStore = useProjectsStore();
const pluginsStore = usePluginsStore();

const emit = defineEmits(['update:modelValue', 'update']);
const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const formRef = ref();
const dialogTitle = ref('FILL ME');
const submitCaption = ref('FILL ME');
const selectedTemplate = ref<TemplateOption>();
const templateOptions = ref([] as TemplateOption[]);
const analysis = ref({} as OpalAnalysisDto);

const editMode = computed(() => !!props.analysisName);

// Validators
const validateRequiredField = (val: string) => (val && val.trim().length > 0) || t('validation.name_required');

function createSelectOptions() {
  (pluginsStore.analysisPlugins.packages || []).forEach((plugin) => {
    templateOptions.value.push({ label: t(`plugins.${plugin.name}`) });

    (plugin['Plugins.AnalysisPluginPackageDto.analysis'].analysisTemplates || []).forEach((template) => {
      templateOptions.value.push({
        label: t(`plugins.${plugin.name}.${template.name}.title`),
        value: { pluginName: plugin.name, templateName: template.name },
      });
    });
  });
}

// Handlers

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      submitCaption.value = 'Add';
      if (!!props.analysisName) {
        projectsStore.getAnalysis(props.projectName, props.tableName, props.analysisName).then((response) => {
          dialogTitle.value = `${response.name} - ${response.pluginName} / ${response.templateName}`;
          analysis.value = response;
          selectedTemplate.value = {
            label: t(`plugins.${analysis.value.pluginName}.${analysis.value.templateName}.title`),
            value: { pluginName: analysis.value.pluginName, templateName: analysis.value.templateName },
          };
        });
      } else {
        dialogTitle.value = t('analyse_validate.analysis_dialog.add_analysis');
        submitCaption.value = t('run');
        analysis.value = {
          name: '',
          datasource: props.projectName,
          table: props.tableName,
          pluginName: '',
          templateName: '',
          parameters: '',
          variables: [],
          analysisResults: [],
        } as OpalAnalysisDto;
      }
    }

    showDialog.value = value;
  }
);

function onHide() {
  analysis.value = {} as OpalAnalysisDto;
  showDialog.value = false;
  emit('update:modelValue', false);
}

async function onRunAnalysis() {
  // run
  emit('update');
  onHide();
}

onMounted(() => {
  createSelectOptions();
});
</script>
