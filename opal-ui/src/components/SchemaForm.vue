<template>
  <div v-if="schema">
    <div v-for="item in schema.items" :key="item.key">
      <div v-if="item.type === 'string' && item.format === 'file'">
        <file-select
          v-model="dataFiles[item.key]"
          :label="item.title"
          :hint="item.description"
          :folder="filesStore.current"
          selection="single"
          :extensions="item.fileFormats"
          @select="onFileSelect(item.key)"
          class="q-mb-md"/>
      </div>
      <div v-else-if="item.type === 'string'">
        <q-input
          v-model="data[item.key]"
          :label="item.title"
          :hint="item.description"
          dense
          class="q-mb-md"
          :debounce="500"
          @update:model-value="onUpdate(item.key)"/>
      </div>
      <div v-else-if="item.type === 'integer'">
        <q-input
          v-model.number="data[item.key]"
          :label="item.title"
          :hint="item.description"
          type="number"
          dense
          class="q-mb-md"
          :debounce="500"
          @update:model-value="onUpdate(item.key)"/>
      </div>
      <div v-else>
        {{ item }}
      </div>
    </div>
  </div>
</template>


<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'SchemaForm',
});
</script>
<script setup lang="ts">
import { FileObject, FormObject, SchemaFormObject } from 'src/components/models';
import FileSelect from 'src/components/files/FileSelect.vue';

interface SchemaFormFormProps {
  modelValue: FormObject | undefined;
  schema: SchemaFormObject;
}

const props = defineProps<SchemaFormFormProps>();
const emit = defineEmits(['update:modelValue']);

const filesStore = useFilesStore();

const dataFiles = ref<{ [key: string]: FileObject }>({});
const data = ref(props.modelValue || {});

watch(() => props.schema, () => {
  if (props.schema) {
    initDefaults();
  }
});

function initDefaults() {
  props.schema.items.forEach((item) => {
    if (item.default !== undefined && data.value[item.key] === undefined) {
      data.value[item.key] = item.default;
    }
  });
}

function onFileSelect(key: string) {
  data.value[key] = dataFiles.value[key].path;
  onUpdate(key);
}

function isValid() {
  if (!data.value) return false;
  if (props.schema.required) {
    const missings = props.schema.required.filter((key) => data.value[key] === undefined);
    return missings.length === 0;
  }
  return true;
}

function onUpdate(key: string) {
  initDefaults();
  if (!isValid()) emit('update:modelValue', undefined);
  else emit('update:modelValue', data.value);
}
</script>
