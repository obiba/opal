<template>
  <div>
    <div v-if="datasourceStore.perms.variable?.canUpdate" class="row q-gutter-sm q-mb-md">
      <q-btn
        size="sm"
        color="primary"
        icon="edit"
        :label="$t('edit')"
        @click="onShowEdit" />
    </div>
    <v-ace-editor
      v-model:value="script"
      @init="onEditorInit"
      lang="javascript"
      theme="monokai"
      style="height: 300px" />
    <edit-script-dialog v-model="showEdit" :variable="variable" @save="onSave"/>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'VariableScript',
});
</script>
<script setup lang="ts">
import { Variable } from 'src/components/models';
import { VAceEditor } from 'vue3-ace-editor';
import EditScriptDialog from 'src/components/datasource/EditScriptDialog.vue';

interface VariableScriptProps {
  variable: Variable;
}

const props = defineProps<VariableScriptProps>();

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

function onSave(newVariable: Variable, comment: string) {
  console.log(newVariable);
  console.log(comment);
  datasourceStore.saveDerivedVariable(newVariable, comment);
}

</script>
