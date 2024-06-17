<template>
  <div>
    <schema-form v-model="parameters" :schema="schema" @update:model-value="onUpdate" />
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'ExportPluginForm',
});
</script>
<script setup lang="ts">
import { TableDto } from 'src/models/Magma';
import { FormObject, SchemaFormObject } from 'src/components/models';
import SchemaForm from 'src/components/SchemaForm.vue';

interface ExportPluginFormProps {
  modelValue: string | undefined;
  tables: TableDto[];
  type: string;
}

const props = defineProps<ExportPluginFormProps>();
const emit = defineEmits(['update:modelValue'])

const pluginsStore = usePluginsStore();

const schema = ref<SchemaFormObject>();
const parameters = ref<FormObject>();

onMounted(() => {
  onInit();
});

watch(() => props.type, () => {
  onInit();
});

function onInit() {
  pluginsStore.getDatasourcePluginForm(props.type, 'export').then((form) => {
    schema.value = form;
  });
  if (props.modelValue) {
    parameters.value = JSON.parse(props.modelValue);
  }
}

function onUpdate() {
  if (!parameters.value) {
    emit('update:modelValue', undefined);
    return;
  }
  const out = JSON.stringify(parameters.value);
  emit('update:modelValue', out);
}
</script>
