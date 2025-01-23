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
      dense
      class="q-mb-md"
      :debounce="500"
      :disable="dataFile === undefined"
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
      </div>
      <div class="col">
        <q-select
          v-model="defaultValueType"
          :options="ValueTypes"
          :label="t('default_value_type')"
          @update:model-value="onUpdate"
          dense
        />
      </div>
    </div>
    <div class="row q-gutter-md">
      <div class="col">
        <q-input
          v-model="fieldSeparator"
          :label="t('field_separator')"
          dense
          class="q-mb-md"
          :debounce="500"
          @update:model-value="onUpdate"
        />
      </div>
      <div class="col">
        <q-input
          v-model="quotationMark"
          :label="t('quotation_mark')"
          dense
          class="q-mb-md"
          :debounce="500"
          @update:model-value="onUpdate"
        />
      </div>
    </div>
    <div class="row q-gutter-md">
      <div class="col">
        <q-input
          v-model="fromRow"
          type="number"
          min="1"
          :label="t('from_row')"
          dense
          class="q-mb-md"
          :debounce="500"
          @update:model-value="onUpdate"
        />
      </div>
      <div class="col">
        <q-input
          v-model="charSet"
          :label="t('char_set')"
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
import { ValueTypes } from 'src/utils/magma';
import FileSelect from 'src/components/files/FileSelect.vue';

interface Props {
  modelValue: DatasourceFactory | undefined;
}

const props = defineProps<Props>();
const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();
const projectsStore = useProjectsStore();
const filesStore = useFilesStore();

const fileExtensions = ['.csv', '.tsv'];

const dataFile = ref<FileDto>();
const name = ref(initFileName());
const entityType = ref('Participant');
const defaultValueType = ref('text');
const fieldSeparator = ref(',');
const quotationMark = ref('"');
const fromRow = ref(1);
const charSet = ref('ISO-8859-1');

onMounted(() => {
  if (props.modelValue) {
    const params = props.modelValue['Magma.CsvDatasourceFactoryDto.params'];
    if (params) {
      name.value = params.tables[0]?.name || '';
      entityType.value = params.tables[0]?.entityType || 'Participant';
      defaultValueType.value = params.defaultValueType || 'text';
      fieldSeparator.value = params.separator || ',';
      quotationMark.value = params.quote || '"';
      fromRow.value = params.firstRow || 1;
      charSet.value = params.characterSet || 'ISO-8859-1';
      if (params.tables[0]?.data)
        filesStore.getFile(params.tables[0].data).then((file) => {
          dataFile.value = file;
        });
    }
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
  if (defaultValueType.value === '') {
    defaultValueType.value = 'text';
  }
  if (fieldSeparator.value === '') {
    fieldSeparator.value = ',';
  }
  if (quotationMark.value === '') {
    quotationMark.value = '"';
  }
  if (fromRow.value === 0) {
    fromRow.value = 1;
  }
  if (charSet.value === '') {
    charSet.value = 'ISO-8859-1';
  }
  emit('update:modelValue', {
    'Magma.CsvDatasourceFactoryDto.params': {
      defaultValueType: defaultValueType.value,
      separator: fieldSeparator.value,
      quote: quotationMark.value,
      firstRow: fromRow.value,
      characterSet: charSet.value,
      tables: [
        {
          name: name.value,
          entityType: entityType.value,
          data: dataFile.value.path,
          refTable: `${projectsStore.project.name}.${name.value}`,
        },
      ],
    },
  } as DatasourceFactory);
}
</script>
