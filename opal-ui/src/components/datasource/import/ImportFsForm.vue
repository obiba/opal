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
  </div>
</template>

<script setup lang="ts">
import type { DatasourceFactory } from 'src/components/models';
import type { FileDto } from 'src/models/Opal';
import FileSelect from 'src/components/files/FileSelect.vue';

interface ImportFsFormProps {
  modelValue: DatasourceFactory | undefined;
}

const props = defineProps<ImportFsFormProps>();
const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();
const filesStore = useFilesStore();

const fileExtensions = ['.zip'];

const dataFile = ref<FileDto>();

onMounted(() => {
  if (props.modelValue && props.modelValue['Magma.FsDatasourceFactoryDto.params']?.file) {
    filesStore.getFile(props.modelValue['Magma.FsDatasourceFactoryDto.params'].file).then((file) => {
      dataFile.value = file;
    });
  }
});

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
