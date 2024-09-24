<template>
  <div v-if="schema" :class="{ 'o-border-negative rounded-borders': !isFormValid }">
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
  <span v-if="!isFormValid" class="text-negative text-caption">{{ $t('validation.missing_required_fields') }}</span>
</template>

<script lang="ts">
export default defineComponent({
  name: 'SchemaForm',
});
</script>
<script setup lang="ts">
import { FormObject, SchemaFormObject } from 'src/components/models';
import SchemaFormItem from 'src/components/SchemaFormItem.vue';
import { isEmpty } from 'src/utils/validations';

interface Props {
  modelValue: FormObject | undefined;
  schema: SchemaFormObject;
  disable?: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits(['update:modelValue']);
const data = ref(props.modelValue || {});
const isFormValid = ref(true);

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


// TODO needs a better validation handling, this is just a basic one
function validate() {
  isFormValid.value = !!data.value;
  if (props.schema.required) {
    const missings = props.schema.required.filter((key) => isEmpty(data.value[key]));
    isFormValid.value = missings.length === 0;
  }

  return isFormValid.value;
}

function onUpdate(key: string) {
  validate();
  if (!isFormValid.value) emit('update:modelValue', undefined);
  else emit('update:modelValue', data.value);
}

defineExpose({
  validate,
});
</script>

<style lang="scss" scoped>
.o-border-negative {
  color: $negative !important;
  outline: 1px solid $negative !important;
  min-height: 90%;
  padding: 0.3rem !important;
}
</style>
