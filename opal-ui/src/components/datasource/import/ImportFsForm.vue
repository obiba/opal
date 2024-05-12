<template>
  <div class="text-help">
    {{ $t('no_options') }}
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

interface ImportFsFormProps {
  modelValue: DatasourceFactory | undefined;
  file: FileDto;
}

const props = defineProps<ImportFsFormProps>();
const emit = defineEmits(['update:modelValue'])

watch(() => props.file, (value) => {
  if (value) {
    onUpdate();
  }
});

onMounted(() => {
  onUpdate();
});

function onUpdate() {
  updateModelValue();
}

function updateModelValue() {
  emit('update:modelValue', {
    'Magma.FsDatasourceFactoryDto.params': {
      file: props.file.path,
      onyxWrapper: true,
    },
  } as DatasourceFactory);
}
</script>
