<template>
  <div>
    <file-select
      v-model="dataFile"
      :label="$t('data_file')"
      :folder="filesStore.current"
      selection="single"
      :extensions="fileExtensions"
      @select="onFileSelect"
      class="q-mb-md"/>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'ImportFsForm',
});
</script>
<script setup lang="ts">
import { DatasourceFactory } from 'src/components/models';
import { FileDto } from 'src/models/Opal';
import FileSelect from 'src/components/files/FileSelect.vue';

interface ImportFsFormProps {
  modelValue: DatasourceFactory | undefined;
}

defineProps<ImportFsFormProps>();
const emit = defineEmits(['update:modelValue'])

const filesStore = useFilesStore();

const fileExtensions = ['.zip'];

const dataFile = ref<FileDto>();

function onFileSelect() {
  if (!dataFile.value) {
    emit('update:modelValue', undefined);
    return;
  }
  emit('update:modelValue', {
    'Magma.FsDatasourceFactoryDto.params': {
      file: dataFile.value.path,
      onyxWrapper: true,
    },
  } as DatasourceFactory);
}
</script>
