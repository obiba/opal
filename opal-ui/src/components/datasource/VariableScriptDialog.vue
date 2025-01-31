<template>
  <q-dialog v-model="showDialog" :maximized="maximizedToggle" @hide="onHide">
    <q-card :class="maximizedToggle ? '' : 'dialog-lg'">
      <q-card-section>
        <div class="row">
          <div class="text-h6">{{ t('edit_script') }}</div>
          <q-space />
          <q-btn dense flat icon="close_fullscreen" @click="maximizedToggle = false" v-if="maximizedToggle" />
          <q-btn dense flat icon="open_in_full" @click="maximizedToggle = true" v-if="!maximizedToggle" />
        </div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <div class="row q-gutter-md q-mb-md">
          <q-select v-model="valueType" :options="ValueTypes" :label="t('value_type')" dense style="width: 200px" />
          <q-checkbox v-model="isRepeatable" :label="t('repeatable')" dense />
          <q-input
            v-if="isRepeatable"
            v-model="occurrenceGroup"
            :label="t('occurrence_group')"
            dense
            style="width: 300px"
          />
        </div>
        <v-ace-editor
          v-model:value="scriptEdit"
          @init="onEditorInit"
          lang="javascript"
          theme="monokai"
          :style="maximizedToggle ? 'height: 77vh' : 'height: 50vh'"
        />
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-input v-model="comment" :label="t('comment')" dense />
        <q-space />
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <!-- <q-btn
            flat
            :label="t('test')"
            color="positive"
            @click="onTest"
          /> -->
        <q-btn flat :label="t('save')" color="primary" @click="onSave" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { VAceEditor } from 'vue3-ace-editor';
import { ValueTypes } from 'src/utils/magma';
import type { VariableDto, AttributeDto } from 'src/models/Magma';

interface DialogProps {
  modelValue: boolean;
  variable: VariableDto;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'save']);

const { t } = useI18n();

const scriptEdit = ref();
const valueType = ref();
const isRepeatable = ref(false);
const occurrenceGroup = ref();
const comment = ref();
const showDialog = ref(props.modelValue);
const maximizedToggle = ref(false);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      scriptEdit.value = getScript();
      valueType.value = props.variable.valueType;
      isRepeatable.value = props.variable.isRepeatable;
      occurrenceGroup.value = props.variable.occurrenceGroup;
      comment.value = 'Udpate ' + props.variable.name;
    }
    showDialog.value = value;
  }
);

function onHide() {
  emit('update:modelValue', false);
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function onEditorInit(editor: any) {
  editor.setOptions({
    enableBasicAutocompletion: true,
    enableSnippets: true,
    enableLiveAutocompletion: true,
  });
}

function onSave() {
  emit('save', makeNewVariable(), comment.value.trim() ? comment.value.trim() : 'Udpate ' + props.variable.name);
}

function makeNewVariable() {
  const attributes = props.variable.attributes ? props.variable.attributes.filter((a) => a.name !== 'script') : [];
  return {
    ...props.variable,
    attributes: [...attributes, { name: 'script', value: scriptEdit.value }],
    valueType: valueType.value,
    isRepeatable: isRepeatable.value,
    occurrenceGroup: isRepeatable.value ? occurrenceGroup.value : '',
  };
}

function getScript() {
  return props.variable.attributes?.find((a: AttributeDto) => a.name === 'script')?.value || '';
}
</script>
