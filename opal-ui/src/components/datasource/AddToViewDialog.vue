<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('add_to_view') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <div class="q-mb-md box-info">
          <q-icon name="info" size="1.2rem" />
          <span class="on-right">
            {{ t('add_to_view_info', { count: props.variables.length }) }}
          </span>
        </div>

        <q-select
          v-model="projectDestination"
          :options="projectNames"
          :label="t('project_destination')"
          dense
          style="min-width: 300px"
          class="q-mb-md"
        />
        <q-input
          v-model="newTableName"
          dense
          type="text"
          :label="t('view_name')"
          :hint="t('view_destination_hint')"
          style="min-width: 300px"
          class="q-mb-md"
        >
        </q-input>

        <div class="q-mt-lg">
          {{ t('derived_variables') }}
        </div>
        <div class="text-hint">
          {{ t('derived_variables_hint') }}
        </div>
        <q-table
          :rows="derivedVariables"
          :columns="columns"
          row-key="index"
          :hide-pagination="derivedVariables.length <= 5"
          flat
          class="q-mb-md"
        >
          <template v-slot:body-cell="props">
            <q-td :props="props">
              <q-input v-if="props.col.name === 'name'" v-model="props.row[props.col.name]" dense borderless />
              <q-select v-else v-model="props.row[props.col.name]" :options="ValueTypes" dense borderless></q-select>
            </q-td>
          </template>
        </q-table>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn
          flat
          :label="t('save')"
          color="primary"
          @click="onSaveView"
          :disable="!projectDestination || !newTableName || !validDerivedVariables.length"
          v-close-popup
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { AttributeDto, TableDto, VariableDto } from 'src/models/Magma';
import { ValueTypes } from 'src/utils/magma';
import { DefaultAlignment } from 'src/components/models';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  table?: TableDto;
  variables: VariableDto[];
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const router = useRouter();
const projectsStore = useProjectsStore();
const datasourceStore = useDatasourceStore();
const { t } = useI18n();

const projectNames = computed(() => projectsStore.projects.map((p) => p.name));

const showDialog = ref(props.modelValue);
const projectDestination = ref('');
const newTableName = ref('');
const derivedVariables = ref<VariableDto[]>([]);

const validDerivedVariables = computed(() => derivedVariables.value.filter((v) => v.name));

const columns = computed(() => [
  { name: 'name', align: DefaultAlignment, label: t('name'), field: 'name' },
  { name: 'valueType', align: DefaultAlignment, label: t('value_type'), field: 'valueType' },
]);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      projectDestination.value = props.table?.datasourceName as string;
      newTableName.value = '';
      const tableNames = datasourceStore.tables.map((t) => t.name);
      if (props.table) {
        let idx = 1;
        while (newTableName.value === '') {
          const name = `${props.table.name}_${idx}`;
          if (!tableNames.includes(name)) {
            newTableName.value = name;
          }
          idx += 1;
        }
      }
      derivedVariables.value = makeDerivedVariables();
    }
    showDialog.value = value;
  }
);

onMounted(() => {
  projectsStore.initProjects();
});

function onHide() {
  emit('update:modelValue', false);
}

function onSaveView() {
  if (!projectDestination.value || !newTableName.value) {
    return;
  }

  const from = getFromTables();
  const newViewPage = `/project/${projectDestination.value}/table/${newTableName.value}`;

  datasourceStore
    .getView(projectDestination.value, newTableName.value)
    .then((view) => {
      const merged = [...view.from, ...from];
      view.from = merged.filter((f, idx) => merged.indexOf(f) === idx);
      view['Magma.VariableListViewDto.view'].variables = mergeVariables(
        view['Magma.VariableListViewDto.view'].variables,
        validDerivedVariables.value
      );
      datasourceStore
        .updateView(projectDestination.value, newTableName.value, view, `Added variables from ${from} to view`)
        .then(() => router.push(newViewPage))
        .catch(notifyError);
    })
    .catch(() => {
      datasourceStore
        .addVariablesView(projectDestination.value, newTableName.value, from, validDerivedVariables.value)
        .then(() => router.push(newViewPage))
        .catch(notifyError);
    });
}

function getFromTables() {
  const tables = props.variables.map((v) => v.parentLink?.link.replace('/datasource/', '').replace('/table/', '.'));
  return tables.filter((table, idx) => table && tables.indexOf(table) === idx).filter((table) => table !== undefined);
}

function makeDerivedVariables() {
  let idx = 0;
  return props.variables.map((variable) => {
    const scriptAttr: AttributeDto = {
      name: 'script',
      value: `$('${variable.name}')`,
    };
    const newVariable = { ...variable };
    delete newVariable.link;
    delete newVariable.parentLink;
    newVariable.index = idx++;
    if (variable.categories) {
      newVariable.categories = [...variable.categories];
    }
    if (variable.attributes) {
      newVariable.attributes = [...variable.attributes].filter((attr) => attr.name !== 'script');
      newVariable.attributes.push(scriptAttr);
    } else {
      newVariable.attributes = [scriptAttr];
    }
    return newVariable;
  });
}

function mergeVariables(originalVariables: VariableDto[], variables: VariableDto[]) {
  const newVariables = originalVariables ? [...originalVariables] : [];
  variables.forEach((variable) => {
    const original = newVariables.find((v) => v.name === variable.name);
    if (original) {
      const index = newVariables.indexOf(original);
      variable.index = original.index;
      newVariables[index] = variable;
    } else {
      variable.index = newVariables.length;
      newVariables.push(variable);
    }
  });
  return newVariables;
}
</script>
