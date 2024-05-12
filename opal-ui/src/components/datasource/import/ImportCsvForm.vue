<template>
  <div>
    <q-input
      v-model="name"
      autofocus
      :label="$t('table_name')"
      dense
      class="q-mb-md"
      :debounce="500"
      @update:model-value="onUpdate"/>
    <div class="row q-gutter-md">
      <div class="col">
        <q-input
          v-model="entityType"
          :label="$t('entity_type')"
          dense
          class="q-mb-md"
          :debounce="500"
          @update:model-value="onUpdate"/>
      </div>
      <div class="col">
        <q-select
          v-model="defaultValueType"
          :options="valueTypes"
          :label="$t('default_value_type')"
          dense/>
      </div>
    </div>
    <div class="row q-gutter-md">
      <div class="col">
        <q-input
          v-model="fieldSeparator"
          :label="$t('field_separator')"
          dense
          class="q-mb-md"
          :debounce="500"
          @update:model-value="onUpdate"/>
      </div>
      <div class="col">
        <q-input
          v-model="quotationMark"
          :label="$t('quotation_mark')"
          dense
          class="q-mb-md"
          :debounce="500"
          @update:model-value="onUpdate"/>
      </div>
    </div>
    <div class="row q-gutter-md">
      <div class="col">
        <q-input
          v-model="fromRow"
          type="number"
          min="1"
          :label="$t('from_row')"
          dense
          class="q-mb-md"
          :debounce="500"
          @update:model-value="onUpdate"/>
      </div>
      <div class="col">
        <q-input
          v-model="charSet"
          :label="$t('char_set')"
          dense
          class="q-mb-md"
          :debounce="500"
          @update:model-value="onUpdate"/>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'ImportCsvForm',
});
</script>
<script setup lang="ts">
import { DatasourceFactory } from 'src/components/models';
import { FileDto } from 'src/models/Opal';
import { valueTypes } from 'src/utils/magma';

interface ImportCsvFormProps {
  modelValue: DatasourceFactory | undefined;
  file: FileDto;
}

const props = defineProps<ImportCsvFormProps>();
const emit = defineEmits(['update:modelValue'])

const projectsStore = useProjectsStore();

const name = ref(initFileName());
const entityType = ref('Participant');
const defaultValueType = ref('text');
const fieldSeparator = ref(',');
const quotationMark = ref('"');
const fromRow = ref(1);
const charSet = ref('ISO-8859-1');

watch(() => props.file, (value) => {
  if (value) {
    name.value = initFileName();
    onUpdate();
  }
});

onMounted(() => {
  onUpdate();
});

function initFileName(): string {
  // base name on file name without extension
  return props.file ? props.file.name.substring(0, props.file.name.lastIndexOf('.')) : '';
}

function onUpdate() {
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
  updateModelValue();
}

function updateModelValue() {
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
          data: props.file.path,
          refTable: `${projectsStore.project.name}.${name.value}`,
        }
      ],
    },
  } as DatasourceFactory);
}
</script>
