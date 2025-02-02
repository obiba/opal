<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ t('add_tables') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <file-select
          v-model="variablesFile"
          :folder="filesStore.current"
          selection="single"
          :extensions="['.xlsx', '.xls', '.xml']"
          class="q-mb-md"
        />
        <div class="text-help q-mb-md">
          {{ t('select_dictionary_file') }}
        </div>
        <div class="text-help q-mb-md">
          <span class="on-left">{{ t('select_dictionary_file_template') }}</span>
          <a :href="`${baseUrl}/templates/OpalVariableTemplate.xlsx`" class="text-primary">OpalVariableTemplate.xlsx</a>
        </div>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('add')" color="primary" @click="onAddTables" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { FileDto } from 'src/models/Opal';
import FileSelect from 'src/components/files/FileSelect.vue';
import { baseUrl } from 'src/boot/api';
import { notifyError } from 'src/utils/notify';

const { t } = useI18n();

interface DialogProps {
  modelValue: boolean;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const filesStore = useFilesStore();
const authStore = useAuthStore();
const datasourceStore = useDatasourceStore();
const transientDatasourceStore = useTransientDatasourceStore();

const username = computed(() => (authStore.profile.principal ? authStore.profile.principal : ''));

const showDialog = ref(props.modelValue);
const variablesFile = ref<FileDto>();

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      variablesFile.value = undefined;
    }
    transientDatasourceStore.reset();
    showDialog.value = value;
  }
);

onMounted(() => {
  filesStore.initFiles(`/home/${username.value}`);
});

function onHide() {
  transientDatasourceStore.deleteDatasource();
  emit('update:modelValue', false);
}

async function onAddTables() {
  if (!variablesFile.value) return;
  try {
    await transientDatasourceStore.createFileDatasource(variablesFile.value);
  } catch (err) {
    notifyError(err);
    return;
  }

  for (const tName of transientDatasourceStore.datasource.table) {
    try {
      await onAddTable(tName);
    } catch (err) {
      notifyError(err);
    }
  }

  await datasourceStore.initDatasourceTables(datasourceStore.datasource.name);
}

async function onAddTable(tName: string) {
  await transientDatasourceStore.loadTable(tName);
  await transientDatasourceStore.loadVariables();
  if (transientDatasourceStore.variables.length === 0) return;

  const variables = [...transientDatasourceStore.variables];

  if (!datasourceStore.tables.map((tbl) => tbl.name).includes(tName)) {
    const entityType = variables[0]?.entityType || 'Participant';
    await datasourceStore.addTable(tName, entityType);
  }
  await datasourceStore.addOrUpdateVariables(tName, variables);
}
</script>
