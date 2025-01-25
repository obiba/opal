<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ copyTablesTitle }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <div class="q-mb-md box-info">
          <q-icon name="info" size="1.2rem" />
          <span class="on-right">
            {{ copyTablesText }}
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
          v-if="tables.length === 1"
          v-model="newTableName"
          dense
          type="text"
          :label="t('new_name')"
          style="min-width: 300px"
          class="q-mb-md"
        >
        </q-input>
        <q-list class="q-mt-md">
          <q-expansion-item
            switch-toggle-side
            dense
            header-class="text-primary text-caption"
            :label="t('advanced_options')"
          >
            <div class="q-mt-md">
              <q-checkbox v-model="incremental" :label="t('copy_incremental')" />
              <div class="text-help q-pl-sm q-pr-sm">{{ t('copy_incremental_hint') }}</div>
            </div>
            <div class="q-mt-md">
              <q-checkbox v-model="nulls" :label="t('copy_nulls')" />
              <div class="text-help q-pl-sm q-pr-sm">{{ t('copy_nulls_hint') }}</div>
            </div>
          </q-expansion-item>
        </q-list>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('copy')" color="primary" @click="onCopyTables" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { TableDto } from 'src/models/Magma';
import type { CopyCommandOptionsDto } from 'src/models/Commands';
import { notifyError, notifySuccess } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  tables: TableDto[];
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const projectsStore = useProjectsStore();
const datasourceStore = useDatasourceStore();
const { t } = useI18n();

const projectNames = computed(() => projectsStore.projects.map((p) => p.name));
const copyTablesTitle = computed(() => t('copy_tables_data', { count: props.tables.length }));
const copyTablesText = computed(() => t('copy_tables_data_text', { count: props.tables.length }));

const showDialog = ref(props.modelValue);
const projectDestination = ref('');
const newTableName = ref('');
const incremental = ref(false);
const nulls = ref(false);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      projectDestination.value =
        (props.tables.length === 1 ? props.tables[0]?.datasourceName : projectsStore.projects[0]?.name) || '';
      newTableName.value = '';
      if (props.tables.length === 1) {
        const tableNames = datasourceStore.tables.map((t) => t.name);
        let idx = 1;
        while (newTableName.value === '') {
          const name = `${props.tables[0]?.name}_${idx}`;
          if (!tableNames.includes(name)) {
            newTableName.value = name;
          }
          idx += 1;
        }
      }
      incremental.value = false;
      nulls.value = false;
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

function onCopyTables() {
  const options: CopyCommandOptionsDto = {
    tables: props.tables.map((t) => `${t.datasourceName}.${t.name}`),
    destination: projectDestination.value,
    destinationTableName: newTableName.value ? newTableName.value : undefined,
    nonIncremental: !incremental.value,
    copyNullValues: nulls.value,
    noVariables: false,
  };
  projectsStore
    .copyCommand(projectsStore.project.name, options)
    .then((response) => {
      notifySuccess(t('copy_tables_data_task_created', { id: response.data.id }));
    })
    .catch((err) => {
      console.error(err);
      notifyError(err);
    });
}
</script>
