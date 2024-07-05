<template>
  <q-dialog v-model="showDialog" @hide="onHide">
      <q-card class="dialog-sm">
        <q-card-section>
          <div class="text-h6">{{ $t('add_to_view') }}</div>
        </q-card-section>

        <q-separator />

        <q-card-section>
          <div class="q-mb-md box-info">
            <q-icon name="info" size="1.2rem"/>
            <span class="on-right">
              {{ $t('add_to_view_info', { count: props.variables.length }) }}
            </span>
          </div>

          <q-select
            v-model="projectDestination"
            :options="projectNames"
            :label="$t('project_destination')"
            dense
            style="min-width: 300px"
            class="q-mb-md"/>
          <q-input
            v-model="newTableName"
            dense
            type="text"
            :label="$t('view_name')"
            :hint="$t('view_destination_hint')"
            style="min-width: 300px"
            class="q-mb-md"
          >
          </q-input>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="bg-grey-3">
          <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
          <q-btn
            flat
            :label="$t('save')"
            color="primary"
            @click="onSaveView"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'AddToViewDialog',
});
</script>
<script setup lang="ts">
import { AttributeDto, TableDto, VariableDto, ViewDto } from 'src/models/Magma';
import { EmitHint } from 'typescript';

interface DialogProps {
  modelValue: boolean;
  table: TableDto;
  variables: VariableDto[];
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue'])

const router = useRouter();
const projectsStore = useProjectsStore();
const datasourceStore = useDatasourceStore();

const projectNames = computed(() => projectsStore.projects.map((p) => p.name));

const showDialog = ref(props.modelValue);
const projectDestination = ref('');
const newTableName = ref('');

watch(() => props.modelValue, (value) => {
  if (value) {
    projectDestination.value = props.table.datasourceName as string;
    newTableName.value = '';
    const tableNames = datasourceStore.tables.map((t) => t.name);
    let idx = 1;
    while (newTableName.value === '') {
      const name = `${props.table.name}_${idx}`;
      if (!tableNames.includes(name)) {
        newTableName.value = name;
      }
      idx += 1;
    }
  }
  showDialog.value = value;
});

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

  const from = `${props.table.datasourceName}.${props.table.name}`;
  const newViewPage = `/project/${projectDestination.value}/table/${newTableName.value}`;

  datasourceStore.getView(projectDestination.value, newTableName.value)
    .then((view) => {
      if (!view.from.includes(from)) {
        view.from.push(from);
      }
      view['Magma.VariableListViewDto.view'].variables = mergeVariables(view['Magma.VariableListViewDto.view'].variables, makeDerivedVariables());
      datasourceStore.updateView(projectDestination.value, newTableName.value, view, `Added variables from ${from} to view`)
        .then(() => router.push(newViewPage));
    })
    .catch((err) => {
      datasourceStore.addVariablesView(projectDestination.value, newTableName.value, [from], makeDerivedVariables())
        .then(() => router.push(newViewPage));
    });
}

function makeDerivedVariables() {
  return props.variables.map((variable) => {
    const scriptAttr: AttributeDto = {
      name: 'script',
      value: `$('${variable.name}')`,
    };
    const newVariable = { ...variable };
    delete newVariable.link;
    delete newVariable.parentLink;
    if (variable.categories) {
      newVariable.categories = [...variable.categories];
    }
    if (variable.attributes) {
      newVariable.attributes = [...variable.attributes].filter((attr) => attr.name !== 'script');
      newVariable.attributes.push(scriptAttr);
    } else {
      newVariable.attributes = [ scriptAttr ];
    }
    return newVariable;
  });
}

function mergeVariables(originalVariables: VariableDto[], variables: VariableDto[]) {
  const newVariables = [...originalVariables];
  variables.forEach((variable) => {
    const original = originalVariables.find((v) => v.name === variable.name);
    if (original) {
      const index = newVariables.indexOf(original);
      newVariables[index] = variable;
    } else {
      newVariables.push(variable);
    }
  });
  return newVariables;
}
</script>
