<template>
  <div>
    <div v-if="datasourceStore.perms.table?.canUpdate()" class="row q-gutter-sm q-mb-md">
      <q-btn outline size="sm" color="secondary" icon="edit" :title="t('edit')" @click="onShowEdit" />
      <q-btn
        outline
        size="sm"
        color="red"
        icon="delete"
        :title="t('delete')"
        :disable="view.where === undefined"
        @click="onDelete"
      />
    </div>
    <v-ace-editor
      v-if="view.where"
      v-model:value="script"
      @init="onEditorInit"
      lang="javascript"
      theme="monokai"
      style="height: 80px"
    />
    <div v-else class="text-help">
      {{ t('no_entity_filter') }}
    </div>
    <script-dialog v-model="showEdit" :script="view.where" @save="onSave" />
  </div>
</template>

<script setup lang="ts">
import { VAceEditor } from 'vue3-ace-editor';
import ScriptDialog from 'src/components/datasource/ScriptDialog.vue';
import type { ViewDto } from 'src/models/Magma';

interface VariableScriptProps {
  view: ViewDto;
}

const props = defineProps<VariableScriptProps>();

const { t } = useI18n();
const datasourceStore = useDatasourceStore();

const showEdit = ref(false);

const script = computed(() => {
  return props.view.where || '// no filter';
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

async function onSave(script: string, comment: string) {
  const newScript = script.trim() === '' ? undefined : script.trim();
  const newView = { ...props.view, where: newScript };
  if (!newView.name) return;
  const updateComment = comment || 'Update view where script';
  await datasourceStore.updateView(datasourceStore.datasource.name, newView.name, newView, updateComment);
  await datasourceStore.loadTable(newView.name);
}

async function onDelete() {
  onSave('', 'Delete view where script');
}
</script>
