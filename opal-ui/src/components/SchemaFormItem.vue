<template>
  <div>
    <div v-if="isFileItem()">
      <file-select
        v-model="dataFile"
        :label="field.title"
        :hint="field.description"
        :folder="filesStore.current"
        selection="single"
        :disable="disable"
        :type="field.format"
        :extensions="field.fileFormats"
        @select="onFileSelect()"
        class="q-mb-md"
      />
    </div>
    <div v-else-if="isPasswordItem()">
      <q-input
        v-model="data"
        :label="field.title"
        :hint="field.description"
        type="password"
        autocomplete="new-password"
        dense
        :disable="disable"
        class="q-mb-md"
        :debounce="500"
        @update:model-value="onUpdate"
      />
    </div>
    <div v-else-if="field.type === 'string'">
      <q-select
        v-if="field.enum"
        v-model="data"
        :label="field.title"
        :hint="field.description"
        dense
        :disable="disable"
        class="q-mb-md"
        emit-value
        map-options
        :options="field.enum.map((opt) => ({ label: opt.title, value: opt.key }))"
        @update:model-value="onUpdate"
      />
      <q-input
        v-else
        v-model="data"
        :label="field.title"
        :hint="field.description"
        autocomplete="off"
        dense
        :disable="disable"
        class="q-mb-md"
        :debounce="500"
        @update:model-value="onUpdate"
      />
    </div>
    <div v-else-if="field.type === 'integer'">
      <q-input
        v-model.number="data"
        :label="field.title"
        :hint="field.description"
        type="number"
        dense
        :disable="disable"
        class="q-mb-md"
        :debounce="500"
        @update:model-value="onUpdate"
      />
    </div>
    <div v-else-if="field.type === 'boolean'">
      <q-toggle
        v-model="data"
        :label="field.title"
        :hint="field.description"
        dense
        :disable="disable"
        :class="field.description ? 'q-mb-xs' : 'q-mb-md'"
        @update:model-value="onUpdate"
      />
      <div v-if="field.description" class="text-hint q-mb-md">
        {{ field.description }}
      </div>
    </div>
    <div v-else-if="field.type === 'array'">
      <div class="text-help">{{ field.title }}</div>
      <div class="text-hint q-mb-sm">{{ field.description }}</div>
      <q-list separator bordered>
        <q-item v-for="(datum, index) in dataArray" :key="index">
          <q-item-section>
            <div v-for="item in field.items" :key="item.key">
              <schema-form-item
                v-model="dataArray[index][item.key]"
                :field="item"
                :disable="disable"
                @update:model-value="onArrayUpdate(index)"
              />
            </div>
          </q-item-section>
          <q-item-section v-if="!disable" avatar>
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="$t('delete')"
              icon="delete"
              @click="dataArray.splice(index, 1)"
            />
          </q-item-section>
        </q-item>
      </q-list>
      <q-btn
        v-if="!disable"
        color="primary"
        icon="add"
        :label="$t('add')"
        @click="dataArray.push({})"
        size="sm"
        class="q-mt-sm"
      />
    </div>
    <div v-else>
      {{ logUnsupported() }}
    </div>
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'SchemaFormItem',
});
</script>
<script setup lang="ts">
import FileSelect from 'src/components/files/FileSelect.vue';
import { FileObject, FormObject, SchemaFormField } from 'src/components/models';

interface Props {
  modelValue: string | number | boolean | FileObject | FormObject | Array<FormObject> | undefined;
  field: SchemaFormField;
  disable?: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits(['update:modelValue']);

const filesStore = useFilesStore();

const data = ref(props.modelValue);
const dataFile = ref<FileObject>();
const dataArray = ref<Array<FormObject>>([]);

//onMounted(init);

watch([() => props.modelValue, () => props.field], init, { immediate: true });

function init() {
  data.value = props.modelValue;
  if (isArray()) {
    dataArray.value = data.value ? (data.value as Array<FormObject>) : [];
  } else if (isFileItem()) {
    if (props.modelValue && typeof props.modelValue === 'string') {
      filesStore.getFile(props.modelValue).then((file) => {
        dataFile.value = file;
      });
    }
  }
}
function isFileItem() {
  return props.field.type === 'string' && (props.field.format === 'file' || props.field.format === 'folder');
}

function isPasswordItem() {
  return props.field.type === 'string' && props.field.format === 'password';
}

function isArray() {
  return props.field.type === 'array';
}

function onFileSelect() {
  data.value = dataFile.value?.path;
  onUpdate();
}

function onUpdate() {
  if (isArray()) {
    data.value = dataArray.value;
  }
  emit('update:modelValue', data.value);
}

function onArrayUpdate(index: number) {
  onUpdate();
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  return (value: any) => {
    dataArray.value[index] = value;
  };
}

function logUnsupported() {
  console.error('Unsupported form item', props.field);
}
</script>
