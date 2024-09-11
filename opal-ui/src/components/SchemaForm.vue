<template>
  <div v-if="schema">
    <form autocomplete="off">
      <div v-if="schema.title" class="text-help">{{ schema.title }}</div>
      <div v-if="schema.description" class="text-hint q-mb-sm">{{ schema.description }}</div>
      <div v-for="item in schema.items" :key="item.key">
        <schema-form-item
          v-model="data[item.key]"
          :field="item"
          :disable="disable"
          @update:model-value="onUpdate(item.key)"
        />
      </div>
    </form>
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'SchemaForm',
});
</script>
<script setup lang="ts">
import { FormObject, SchemaFormObject } from 'src/components/models';
import SchemaFormItem from 'src/components/SchemaFormItem.vue';

interface Props {
  modelValue: FormObject | undefined;
  schema: SchemaFormObject;
  disable?: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits(['update:modelValue']);

const data = ref(props.modelValue || {});

watch([() => props.modelValue, () => props.schema], () => {
  data.value = props.modelValue || {};
  if (props.schema) {
    initDefaults();
  }
});

function initDefaults() {
  if (!props.schema?.items) return;
  props.schema.items.forEach((item) => {
    if (item.default !== undefined && data.value[item.key] === undefined) {
      data.value[item.key] = item.default;
    } else if (item.type === 'array') {
      data.value[item.key] = [];
    }
  });
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
