<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ dialogTitle }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-input
            v-model="analysisOptions.name"
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
            :label="$t('type')"
            :hint="$t('analyse_validate.analysis_dialog.type_hint')"
            :disable="editMode"
            dense
            map-options
            emit-value
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
                @click="onTemplateSelected(scope.opt.value)"
              >
                <q-item-section class="q-pl-md">
                  {{ scope.opt.label }}
                </q-item-section>
              </q-item>
            </template>
          </q-select>

          <schema-form
            v-if="!!sfModel && !!sfSchema"
            v-model="sfModel"
            :schema="sfSchema"
            :disable="editMode"
          />
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
import SchemaForm from 'src/components/SchemaForm.vue';
import { FormObject, SchemaFormObject } from 'src/components/models';
import { AnalysisPluginTemplateDto } from 'src/models/Plugins';
import { AnalyseCommandOptionsDto, AnalyseCommandOptionsDto_AnalyseDto } from 'src/models/Commands';
import { notifyError, notifySuccess } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  projectName: string;
  tableName: string;
  analysisName?: string;
}

type PluginTemplate = { pluginName: string; templateName: string };
type TemplateOption = { label: string; value?: PluginTemplate };

const { t } = useI18n();
const projectsStore = useProjectsStore();
const pluginsStore = usePluginsStore();

const emit = defineEmits(['update:modelValue', 'update']);
const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const formRef = ref();
const dialogTitle = ref('FILL ME');
const submitCaption = ref('FILL ME');
const selectedTemplate = ref<PluginTemplate>({} as PluginTemplate);
const templateOptions = ref([] as TemplateOption[]);
const analysisOptions = ref({} as AnalyseCommandOptionsDto_AnalyseDto);
const pluginSchemaFormData = {} as { [key: string]: { schema: SchemaFormObject; model: FormObject } };
const sfModel = ref<FormObject>({});
const sfSchema = ref<SchemaFormObject>();
const editMode = computed(() => !!props.analysisName);

// Validators
const validateRequiredField = (val: string) => (val && val.trim().length > 0) || t('validation.name_required');

function initPluginData() {
  (pluginsStore.analysisPlugins.packages || []).forEach((plugin) => {
    templateOptions.value.push({ label: t(`plugins.${plugin.name}.title`) });

    (plugin['Plugins.AnalysisPluginPackageDto.analysis'].analysisTemplates || []).forEach(
      (template: AnalysisPluginTemplateDto) => {
        const schema = JSON.parse(template.schemaForm);
        // Exclude title and description from schema so they are not rendered as fields
        delete schema.title;
        delete schema.description;

        pluginSchemaFormData[`${plugin.name}.${template.name}`] = { schema: schema, model: {} };
        templateOptions.value.push({
          label: t(`plugins.${plugin.name}.${template.name}.title`),
          value: { pluginName: plugin.name, templateName: template.name },
        });
      }
    );
  });
}

function updateSchemaForm() {
  if (!selectedTemplate.value) return;

  const schemaKey = `${selectedTemplate.value.pluginName}.${selectedTemplate.value.templateName}`;
  sfModel.value = pluginSchemaFormData[schemaKey].model;
  sfSchema.value = pluginSchemaFormData[schemaKey].schema;
}

// Handlers

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      submitCaption.value = 'Add';
      if (!!props.analysisName) {
        projectsStore.getAnalysis(props.projectName, props.tableName, props.analysisName).then((response: OpalAnalysisDto) => {
          pluginSchemaFormData[`${response.pluginName}.${response.templateName}`].model = JSON.parse(
            response.parameters
          );
          dialogTitle.value = `${response.name} - ${response.pluginName} / ${response.templateName}`;
          analysisOptions.value.name = response.name;
          analysisOptions.value.table = response.table;
          analysisOptions.value.plugin = response.pluginName;
          analysisOptions.value.template = response.templateName;
          analysisOptions.value.params = response.parameters;
          analysisOptions.value.variables = (response.variables || []).join(',');

          selectedTemplate.value = {
            pluginName: analysisOptions.value.plugin,
            templateName: analysisOptions.value.template,
          };
          updateSchemaForm();
        });
      } else {
        dialogTitle.value = t('analyse_validate.analysis_dialog.add_analysis');
        submitCaption.value = t('run');
        const found = templateOptions.value.find((opt) => !!opt.value) || null;
        if (!!found && found.value) {
          selectedTemplate.value = found.value;
          updateSchemaForm();
          analysisOptions.value = {
            name: '',
            table: props.tableName,
            plugin: '',
            template: '',
            params: '',
            variables: ''
          } as AnalyseCommandOptionsDto_AnalyseDto;
        } else {
          throw new Error('No templates found');
        }
      }

    }
    showDialog.value = value;
  }
);

function onTemplateSelected(value: PluginTemplate) {
  selectedTemplate.value = value;
  updateSchemaForm();
}

function onHide() {
  pluginSchemaFormData[`${selectedTemplate.value?.pluginName}.${selectedTemplate.value?.templateName}`].model = {};
  selectedTemplate.value = {} as PluginTemplate;
  sfModel.value = {};
  sfSchema.value = undefined;
  analysisOptions.value = {} as AnalyseCommandOptionsDto_AnalyseDto;
  showDialog.value = false;
  emit('update:modelValue', false);
}

async function onRunAnalysis() {
  const valid: boolean = await formRef.value.validate();
  if (valid) {
    try {
      analysisOptions.value.plugin = selectedTemplate.value.pluginName;
      analysisOptions.value.template = selectedTemplate.value.templateName;
      analysisOptions.value.params = JSON.stringify(sfModel.value);
      const commandOptions: AnalyseCommandOptionsDto = {
        project: props.projectName,
        analyses: [analysisOptions.value],
      };

      projectsStore.runAnalysis(props.projectName, commandOptions).then((id) => {
        notifySuccess(t('analyse_validate.analyse_command_created', { id }));
      });

      emit('update');
      onHide();
    } catch (error) {
      notifyError(error);
    }
  }
}

onMounted(() => {
  initPluginData();
});
</script>
