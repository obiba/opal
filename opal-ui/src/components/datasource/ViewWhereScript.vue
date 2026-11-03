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
    <!-- v-show, not v-if: the history must stay mounted to load the commits -->
    <div v-if="datasourceStore.perms.table?.canUpdate()" v-show="hasHistory">
      <div class="text-h6 q-mt-lg">{{ t('history') }}</div>
      <!-- the where script is versioned like a variable script, as <view>/__where.js -->
      <git-history
        :key="view.where || ''"
        :name="WHERE"
        :load-commits="loadCommits"
        :diff="(id: string) => datasourceStore.getVariableCommit(WHERE, id)"
        :diff-with-head="(id: string) => datasourceStore.getVariableCommitFromHead(WHERE, id)"
        :restore="onRestore"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { VAceEditor } from 'vue3-ace-editor';
import ScriptDialog from 'src/components/datasource/ScriptDialog.vue';
import GitHistory from 'src/components/git/GitHistory.vue';
import type { ViewDto } from 'src/models/Magma';
import type { VcsCommitInfoDto } from 'src/models/Opal';

// name of the where script file in the view git folder, without the .js extension
const WHERE = '__where';

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

// a view that never had a where script has no history: hide it
const hasHistory = ref(false);

async function loadCommits() {
  const commits = await datasourceStore.getVariableCommits(WHERE);
  hasHistory.value = (commits.commitInfos?.length ?? 0) > 0;
  return commits;
}

async function onRestore(commit: VcsCommitInfoDto) {
  const { blob } = await datasourceStore.getVariableBlob(WHERE, commit.commitId);
  return onSave(blob || '', `Restore view where script from commit ${commit.commitId.slice(0, 7)}`);
}

async function onDelete() {
  onSave('', 'Delete view where script');
}
</script>
