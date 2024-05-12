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
        <q-input
          v-model="locale"
          :label="$t('locale')"
          dense
          class="q-mb-md"
          :debounce="500"
          @update:model-value="onUpdate"/>
      </div>
      <div class="col">
        <q-input
          v-model="idColumn"
          :label="$t('id_column')"
          :hint="$t('id_column_hint')"
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
  name: 'ImportHavenForm',
});
</script>
<script setup lang="ts">
import { DatasourceFactory } from 'src/components/models';
import { FileDto } from 'src/models/Opal';

interface ImportFsFormProps {
  modelValue: DatasourceFactory | undefined;
  file: FileDto;
}

const props = defineProps<ImportFsFormProps>();
const emit = defineEmits(['update:modelValue'])

const name = ref(initFileName());
const entityType = ref('Participant');
const idColumn = ref('');
const locale = ref('');

watch(() => props.file, (value) => {
  if (value) {
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
  updateModelValue();
}

function updateModelValue() {
  emit('update:modelValue', {
    'Magma.RHavenDatasourceFactoryDto.params': {
      entityType: entityType.value,
      file: props.file.path,
      idColumn: idColumn.value,
      symbol: name.value,
      locale: locale.value,
    },
  } as DatasourceFactory);
}
</script>
