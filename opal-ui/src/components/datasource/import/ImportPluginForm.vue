<template>
  <div>
    <schema-form v-if="schema" v-model="parameters" :schema="schema" @update:model-value="onUpdate" />
  </div>
</template>

<script setup lang="ts">
import type { DatasourceFactory, FormObject, SchemaFormObject } from 'src/components/models';
import SchemaForm from 'src/components/SchemaForm.vue';

interface ImportPluginFormProps {
  modelValue: DatasourceFactory | undefined;
  type: string;
}

const props = defineProps<ImportPluginFormProps>();
const emit = defineEmits(['update:modelValue']);

const pluginsStore = usePluginsStore();

const schema = ref<SchemaFormObject>();
const parameters = ref<FormObject>();

onMounted(() => {
  pluginsStore.getDatasourcePluginForm(props.type, 'import').then((form) => {
    schema.value = form;
  });
  if (props.modelValue && props.modelValue['Magma.PluginDatasourceFactoryDto.params']?.parameters) {
    parameters.value = JSON.parse(props.modelValue['Magma.PluginDatasourceFactoryDto.params'].parameters);
  }
});

function onUpdate() {
  if (!parameters.value) {
    emit('update:modelValue', undefined);
    return;
  }
  emit('update:modelValue', {
    'Magma.PluginDatasourceFactoryDto.params': {
      name: props.type,
      parameters: JSON.stringify(parameters.value),
    },
  } as DatasourceFactory);
}
</script>
