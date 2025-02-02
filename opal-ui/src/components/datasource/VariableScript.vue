<template>
  <div>
    <div v-if="datasourceStore.perms.variable?.canUpdate()" class="row q-gutter-sm q-mb-md">
      <q-btn size="sm" color="primary" icon="edit" :title="t('edit')" @click="onShowEdit" />
    </div>
    <v-ace-editor v-model:value="script" @init="onEditorInit" lang="javascript" theme="monokai" style="height: 300px" />
    <variable-script-dialog v-model="showEdit" :variable="variable" @save="onSave" />
  </div>
</template>

<script setup lang="ts">
import type { VariableDto } from 'src/models/Magma';
import { VAceEditor } from 'vue3-ace-editor';
import VariableScriptDialog from 'src/components/datasource/VariableScriptDialog.vue';

interface VariableScriptProps {
  variable: VariableDto;
}

const props = defineProps<VariableScriptProps>();

const { t } = useI18n();
const datasourceStore = useDatasourceStore();

const showEdit = ref(false);

const script = computed(() => {
  return props.variable.attributes?.find((a) => a.name === 'script')?.value || '';
});

function onShowEdit() {
  showEdit.value = true;
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function onEditorInit(editor: any) {
  editor.setOptions({
    readOnly: true,
  });
}

function onSave(newVariable: VariableDto, comment: string) {
  datasourceStore.saveDerivedVariable(newVariable, comment);
}
</script>
