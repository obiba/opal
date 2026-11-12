<template>
  <q-dialog v-model="showDialog" :maximized="maximizedToggle" @hide="onHide">
    <q-card :class="maximizedToggle ? '' : 'dialog-lg'">
      <q-card-section>
        <div class="row">
          <div class="text-h6">{{ title || t('edit_script') }}</div>
          <q-space />
          <q-btn dense flat icon="close_fullscreen" @click="maximizedToggle = false" v-if="maximizedToggle" />
          <q-btn dense flat icon="open_in_full" @click="maximizedToggle = true" v-if="!maximizedToggle" />
        </div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <v-ace-editor
          v-model:value="scriptEdit"
          @init="onEditorInit"
          lang="javascript"
          theme="monokai"
          :style="maximizedToggle ? 'height: 77vh' : 'height: 50vh'"
        />
      </q-card-section>

      <q-separator />

      <q-card-actions v-if="readonly" align="right" class="bg-grey-3">
        <q-btn flat :label="t('close')" color="secondary" v-close-popup />
      </q-card-actions>
      <q-card-actions v-else align="right" class="bg-grey-3">
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

interface DialogProps {
  modelValue: boolean;
  script?: string | undefined;
  readonly?: boolean;
  title?: string;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'save']);

const { t } = useI18n();

const scriptEdit = ref();
const comment = ref();
const showDialog = ref(props.modelValue);
const maximizedToggle = ref(false);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      scriptEdit.value = props.script || '';
    }
    showDialog.value = value;
  },
);

function onHide() {
  emit('update:modelValue', false);
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function onEditorInit(editor: any) {
  editor.setOptions({
    readOnly: !!props.readonly,
    enableBasicAutocompletion: true,
    enableSnippets: true,
    enableLiveAutocompletion: true,
  });
}

function onSave() {
  emit('save', scriptEdit.value, comment.value?.trim());
}
</script>
