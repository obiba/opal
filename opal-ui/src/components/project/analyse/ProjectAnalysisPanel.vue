<template>
  <q-form ref="formRef" class="q-gutter-lg" persistent>
    <q-input
      v-model="analysisOptions.name"
      dense
      type="text"
      class="q-pb-none"
      :label="t('name') + ' *'"
      lazy-rules
      :disable="editMode"
      :rules="[validateRequiredField, validateUniqueField]"
    >
    </q-input>
    <q-select
      v-model="selectedTemplate"
      :options="templateOptions"
      :label="t('type')"
      :hint="t('analyse_validate.analysis_dialog.type_hint')"
      :disable="editMode"
      dense
      map-options
      emit-value
    >
      <template v-slot:option="scope">
        <q-item v-show="!scope.opt.value" class="text-help" dense clickable disable :label="scope.opt.label">
          <q-item-section class="q-pa-none">
            {{ scope.opt.label }}
          </q-item-section>
        </q-item>
        <q-item v-show="scope.opt.value" dense clickable v-close-popup @click="onTemplateSelected(scope.opt.value)">
          <q-item-section class="q-pl-md">
            {{ scope.opt.label }}
          </q-item-section>
        </q-item>
      </template>
    </q-select>

    <q-select
      ref="variableSelect"
      v-model="selectedVariables"
      :options="variableOptions"
      :label="t('variables')"
      :hint="t('analyse_validate.analysis_dialog.variables_hint')"
      :loading="loadingVariables"
      :disable="editMode"
      class="q-py-md"
      dense
      multiple
      emit-value
      map-options
      use-input
      use-chips
      hide-selection
      input-debounce="0"
      @filter="onFilterFn"
    >
      <template v-slot:option="scope">
        <q-item dense clickable :label="scope.opt.group" v-close-popup @click="onAddVariable(scope.opt.value)">
          <q-item-section class="q-pa-none">
            {{ scope.opt.label.name }}
            <span class="text-help">{{ scope.opt.label.vlabel }}</span>
          </q-item-section>
        </q-item>
      </template>
      <template v-slot:selected-item="scope">
        <q-chip removable @remove="onRemoveVariable(scope.opt.value)">
          {{ scope.opt.value }}
        </q-chip>
      </template>
    </q-select>

    <schema-form
      class="q-pt-md"
      ref="sfForm"
      v-if="sfModel && sfSchema"
      v-model="sfModel"
      :schema="sfSchema"
      :disable="editMode"
    />
  </q-form>
</template>

<script setup lang="ts">
import type { OpalAnalysisDto } from 'src/models/Projects';
import SchemaForm from 'src/components/SchemaForm.vue';
import type { FormObject, SchemaFormObject } from 'src/components/models';
import type { AnalysisPluginTemplateDto } from 'src/models/Plugins';
import type { AnalyseCommandOptionsDto, AnalyseCommandOptionsDto_AnalyseDto } from 'src/models/Commands';
import { notifyError, notifySuccess } from 'src/utils/notify';
import type { QueryResultDto } from 'src/models/Search';
import type { EntryDto } from 'src/models/Opal';

interface Props {
  projectName: string;
  tableName: string;
  analysisNames: string[];
  analysis?: OpalAnalysisDto | undefined;
  clone?: OpalAnalysisDto | undefined;
}

type PluginTemplate = { pluginName: string; templateName: string };
type TemplateOption = { label: string; value?: PluginTemplate };
type VariableOption = { label: { [key: string]: string }; value: string };

const { t } = useI18n();
const projectsStore = useProjectsStore();
const pluginsStore = usePluginsStore();
const searchStore = useSearchStore();

const variableSelect = ref();
const emit = defineEmits(['update:modelValue', 'update']);
const props = defineProps<Props>();
const formRef = ref();
const sfForm = ref();
const selectedVariables = ref<string[]>([]);
const loadingVariables = ref(false);
const variableOptions = ref([] as VariableOption[]);
const selectedTemplate = ref<PluginTemplate>({} as PluginTemplate);
const templateOptions = ref([] as TemplateOption[]);
const analysisOptions = ref({} as AnalyseCommandOptionsDto_AnalyseDto);
const pluginSchemaFormData = {} as { [key: string]: { schema: SchemaFormObject; model: FormObject } };
const sfModel = ref<FormObject>({});
const sfSchema = ref<SchemaFormObject>();
const editMode = computed(() => props.analysis && props.analysis.name !== undefined && props.analysis.name !== '' && props.analysis.name !== null || false);

// Validators
const validateRequiredField = (val: string) => (val && val.trim().length > 0) || t('validation.name_required');
const validateUniqueField = (val: string) =>
  !val || !props.analysisNames.includes(val) || t('validation.analysis.name_exists');

function initPluginData() {
  (pluginsStore.analysisPlugins.packages || []).forEach((plugin) => {
    templateOptions.value.push({ label: t(`plugins.${plugin.name}.title`) });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    ((plugin as any)['Plugins.AnalysisPluginPackageDto.analysis'].analysisTemplates || []).forEach(
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
  sfModel.value = pluginSchemaFormData[schemaKey]?.model || {};
  sfSchema.value = pluginSchemaFormData[schemaKey]?.schema;
}

function initFromDto(dto: OpalAnalysisDto) {
  const sf = pluginSchemaFormData[`${dto.pluginName}.${dto.templateName}`];
  if (sf) sf.model = JSON.parse(dto.parameters);

  analysisOptions.value.name = dto.name;
  analysisOptions.value.table = dto.table;
  analysisOptions.value.plugin = dto.pluginName;
  analysisOptions.value.template = dto.templateName;
  analysisOptions.value.params = dto.parameters;
  if (dto.variables) {
    analysisOptions.value.variables = dto.variables.join(',');
    selectedVariables.value = dto.variables;
  } else {
    selectedVariables.value = [];
  }

  variableOptions.value = selectedVariables.value.map((variable) => {
    return { label: { name: variable, vlabel: variable }, value: variable };
  });
  selectedTemplate.value = {
    pluginName: analysisOptions.value.plugin,
    templateName: analysisOptions.value.template,
  };
}

// Handlers

function onTemplateSelected(value: PluginTemplate) {
  selectedTemplate.value = value;
  updateSchemaForm();
}

function onAddVariable(value: string) {
  if (!selectedVariables.value) selectedVariables.value = [];
  if (!selectedVariables.value.includes(value)) selectedVariables.value.push(value);
  else onRemoveVariable(value);

  // To remove the text after selecting a variable
  setTimeout(() => variableSelect.value?.updateInputValue(''), 50);
}

function onRemoveVariable(value: string) {
  if (!selectedVariables.value) return;
  selectedVariables.value = selectedVariables.value.filter((item) => item !== value);
}

function getSearchHitFieldMap(fields: EntryDto[], keys: string[]): { [key: string]: string } {
  const result: EntryDto[] = fields.filter((field) => keys.includes(field.key)) || ([] as EntryDto[]);
  return result.reduce((acc, f: EntryDto) => {
    acc[f.key] = f.value ?? '';
    return acc;
  }, {} as { [key: string]: string });
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
async function onFilterFn(query: string, update: any) {
  try {
    loadingVariables.value = true;

    const fullQuery = `query=${query} AND (project:"${props.projectName}") AND (table:"${props.tableName}")`;
    const result: QueryResultDto = await searchStore.search(fullQuery, 5, ['label', 'label-en'], undefined);
    if (result.totalHits > 0) {
      variableOptions.value = [];
      result.hits.map((hit) => {
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const fieldMap = getSearchHitFieldMap((hit as any)['Search.ItemFieldsDto.item'].fields, ['name', 'label-en']);
        variableOptions.value.push({
          label: { name: fieldMap['name'] || '', vlabel: fieldMap['label-en'] || '' },
          value: fieldMap['name'] || '',
        });
      });
    }
  } catch (e) {
    console.error(e);
    // ignore
  } finally {
    update();
    loadingVariables.value = false;
  }
}

function onHide() {
  const key = `${selectedTemplate.value?.pluginName}.${selectedTemplate.value?.templateName}`;
  if (key in pluginSchemaFormData && pluginSchemaFormData[key]) {
    pluginSchemaFormData[key].model = {};
  }

  selectedTemplate.value = {} as PluginTemplate;
  sfModel.value = {};
  sfSchema.value = undefined;
  analysisOptions.value = {} as AnalyseCommandOptionsDto_AnalyseDto;
  selectedVariables.value = [];
  emit('update:modelValue', false);
}

async function runAnalysis() {
  const valid: boolean = await formRef.value.validate();

  if (valid && sfForm.value.validate()) {
    try {
      analysisOptions.value.plugin = selectedTemplate.value.pluginName;
      analysisOptions.value.template = selectedTemplate.value.templateName;
      analysisOptions.value.params = JSON.stringify(sfModel.value);

      if (selectedVariables.value) {
        analysisOptions.value.variables = selectedVariables.value.join(',');
      }

      const commandOptions: AnalyseCommandOptionsDto = {
        project: props.projectName,
        analyses: [analysisOptions.value],
      };

      const id = await projectsStore.runAnalysis(props.projectName, commandOptions);
      notifySuccess(t('analyse_validate.analyse_command_created', { id }));
      return true;
    } catch (error) {
      notifyError(error);
    }
  }

  return false;
}

onUnmounted(() => {
  onHide();
});

onMounted(() => {
  watch(
    () => [props.projectName, props.tableName, props.analysisNames, props.analysis, props.clone],
    () => {
      initPluginData();

      if (props.analysis && props.analysis.name) {
        initFromDto(props.analysis);
        updateSchemaForm();
      } else {
        const found = templateOptions.value.find((opt) => opt.value) || null;
        if (found && found.value) {
          selectedTemplate.value = found.value;
          analysisOptions.value = {
            name: '',
            table: props.tableName,
            plugin: '',
            template: '',
            params: '',
            variables: '',
          } as AnalyseCommandOptionsDto_AnalyseDto;

          if (props.clone) {
            initFromDto(props.clone);
          }

          updateSchemaForm();
        } else {
          throw new Error('No templates found');
        }
      }
    },
    { immediate: true }
  );
});

defineExpose({
  runAnalysis,
});
</script>
