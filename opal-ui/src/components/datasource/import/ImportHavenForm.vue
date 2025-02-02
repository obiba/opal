<template>
  <div>
    <file-select
      v-model="dataFile"
      :label="t('data_file')"
      :folder="filesStore.current"
      selection="single"
      :extensions="fileExtensions"
      @select="onFileSelect"
      class="q-mb-md"
    />
    <q-input
      v-model="name"
      :label="t('table_name')"
      :disable="dataFile === undefined"
      dense
      class="q-mb-md"
      :debounce="500"
      @update:model-value="onUpdate"
    />
    <div class="row q-gutter-md">
      <div class="col">
        <q-input
          v-model="entityType"
          :label="t('entity_type')"
          dense
          class="q-mb-md"
          :debounce="500"
          @update:model-value="onUpdate"
        />
        <q-input
          v-model="locale"
          :label="t('locale')"
          dense
          class="q-mb-md"
          :debounce="500"
          @update:model-value="onUpdate"
        />
      </div>
      <div class="col">
        <q-input
          v-model="idColumn"
          :label="t('id_column')"
          :hint="t('id_column_hint')"
          dense
          class="q-mb-md"
          :debounce="500"
          @update:model-value="onUpdate"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { DatasourceFactory } from 'src/components/models';
import type { FileDto } from 'src/models/Opal';
import FileSelect from 'src/components/files/FileSelect.vue';

interface ImportHavenFormProps {
  modelValue: DatasourceFactory | undefined;
  type: string;
}

const props = defineProps<ImportHavenFormProps>();
const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();
const filesStore = useFilesStore();

const fileExtensions = computed(() => fileImporterExtensions[props.type]);

const fileImporterExtensions: {
  [key: string]: string[];
} = {
  haven_rds: ['.rds', '.rdata'],
  haven_sas: ['.sas7bdat', '.sas7bcat'],
  haven_sast: ['.xpt'],
  haven_spss: ['.sav'],
  haven_stata: ['.dta'],
};

const dataFile = ref<FileDto>();
const name = ref(initFileName());
const entityType = ref('Participant');
const idColumn = ref('');
const locale = ref('');

onMounted(() => {
  if (!props.modelValue || !props.modelValue['Magma.RHavenDatasourceFactoryDto.params']) return;
  const params = props.modelValue['Magma.RHavenDatasourceFactoryDto.params'];
  if (params.file) {
    filesStore.getFile(props.modelValue['Magma.RHavenDatasourceFactoryDto.params'].file).then((file) => {
      dataFile.value = file;
    });
  }
  if (params.symbol) {
    name.value = props.modelValue['Magma.RHavenDatasourceFactoryDto.params'].symbol;
  }
  if (params.entityType) {
    entityType.value = props.modelValue['Magma.RHavenDatasourceFactoryDto.params'].entityType || 'Participant';
  }
  if (params.idColumn) {
    idColumn.value = props.modelValue['Magma.RHavenDatasourceFactoryDto.params'].idColumn || '';
  }
  if (params.locale) {
    locale.value = props.modelValue['Magma.RHavenDatasourceFactoryDto.params'].locale || '';
  }
});

function onFileSelect() {
  name.value = initFileName();
  onUpdate();
}

function initFileName(): string {
  // base name on file name without extension
  return dataFile.value ? dataFile.value.name.substring(0, dataFile.value.name.lastIndexOf('.')) : '';
}

function onUpdate() {
  if (!dataFile.value) {
    emit('update:modelValue', undefined);
    return;
  }
  if (name.value === '') {
    name.value = initFileName();
  }
  if (entityType.value === '') {
    entityType.value = 'Participant';
  }
  emit('update:modelValue', {
    'Magma.RHavenDatasourceFactoryDto.params': {
      entityType: entityType.value,
      file: dataFile.value.path,
      idColumn: idColumn.value,
      symbol: name.value,
      locale: locale.value,
    },
  } as DatasourceFactory);
}
</script>
