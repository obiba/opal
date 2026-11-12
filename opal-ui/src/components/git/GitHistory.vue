<template>
  <q-table
    flat
    :rows="commits"
    :columns="columns"
    row-key="commitId"
    :pagination="initialPagination"
    :hide-pagination="commits.length <= initialPagination.rowsPerPage"
  >
    <template v-slot:body-cell-id="props">
      <q-td :props="props" class="items-start" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        {{ props.value }}
        <template v-if="props.rowIndex === 0"
          ><q-badge color="grey-6 q-ml-xs">{{ t('current') }}</q-badge></template
        >
        <q-badge v-for="tag in props.row.tags" :key="tag" color="primary" class="q-ml-xs">
          <q-icon name="sell" size="xs" class="q-mr-xs" />{{ tag }}
        </q-badge>
        <div class="float-right">
          <q-btn
            v-if="blob"
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="t('view')"
            :icon="toolsVisible[props.row.commitId] ? 'visibility' : 'none'"
            class="q-ml-xs"
            @click="onView(props.row)"
          />
          <q-btn
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="t('compare')"
            :icon="toolsVisible[props.row.commitId] ? 'compare_arrows' : 'none'"
            class="q-ml-xs"
            @click="onCompare(props.row)"
          />
          <q-btn
            v-if="props.rowIndex > 0"
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="t('git.compare_current')"
            :icon="toolsVisible[props.row.commitId] ? 'keyboard_tab' : 'none'"
            class="q-ml-xs"
            @click="onCompareWith(props.row)"
          />
          <q-btn
            v-if="canRestore && props.rowIndex > 0"
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="t('restore')"
            :icon="toolsVisible[props.row.commitId] ? 'replay' : 'none'"
            class="q-ml-xs"
            @click="onRestore(props.row)"
          />
        </div>
      </q-td>
    </template>
    <template v-slot:body-cell-date="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        {{ props.value }}
      </q-td>
    </template>
    <template v-slot:body-cell-author="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        {{ props.value }}
      </q-td>
    </template>
    <template v-slot:body-cell-comment="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        {{ props.value }}
      </q-td>
    </template>
  </q-table>

  <!-- Dialogs -->

  <confirm-dialog
    v-model="showRestore"
    :title="t('restore')"
    :text="t(restoreConfirmKey, { date: getDateLabel(commitInfo.date) })"
    @confirm="doRestore"
  />

  <script-dialog
    v-model="showView"
    readonly
    :script="viewScript"
    :title="`${t('script')} ${commitInfo.commitId?.slice(0, 7) ?? ''}`"
  />

  <git-diff-viewer-dialog v-model="showDiff" :commit-info="commitInfo" @update:modelValue="onCloseDiffViewer" />
</template>

<script setup lang="ts">
import GitDiffViewerDialog from 'src/components/git/GitDiffViewerDialog.vue';
import ScriptDialog from 'src/components/datasource/ScriptDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { getDateLabel } from 'src/utils/dates';
import type { VcsCommitInfoDto, VcsCommitInfosDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';
import { DefaultAlignment } from 'src/components/models';

interface Props {
  // changes whenever the history must be reloaded (e.g. the versioned item name)
  name: string;
  loadCommits: () => Promise<VcsCommitInfosDto>;
  diff: (commitId: string) => Promise<VcsCommitInfoDto>;
  diffWithHead: (commitId: string) => Promise<VcsCommitInfoDto>;
  restore: (commit: VcsCommitInfoDto) => Promise<unknown>;
  // the script at a commit, enables the view action
  blob?: (commitId: string) => Promise<VcsCommitInfoDto>;
  canRestore?: boolean;
  restoreConfirmKey?: string;
}
const emit = defineEmits(['restore']);
const props = withDefaults(defineProps<Props>(), {
  canRestore: true,
  restoreConfirmKey: 'git.restore_confirm',
});
const { t } = useI18n({ useScope: 'global' });
const showDiff = ref(false);
const showRestore = ref(false);
const showView = ref(false);
const viewScript = ref('');
const commits = ref<VcsCommitInfoDto[]>([]);
const commitInfo = ref<VcsCommitInfoDto>({} as VcsCommitInfoDto);
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});
const toolsVisible = ref<{ [key: string]: boolean }>({});
const columns = computed(() => [
  {
    name: 'id',
    required: true,
    label: t('id'),
    align: DefaultAlignment,
    field: 'commitId',
    format: (val: string) => val.slice(0, 7),
    headerStyle: 'width: 20%; white-space: normal;',
    style: 'width: 20%; white-space: normal;',
  },
  {
    name: 'date',
    label: t('date'),
    align: DefaultAlignment,
    field: 'date',
    format: (val: string) => getDateLabel(val),
  },
  {
    name: 'author',
    label: t('author'),
    align: DefaultAlignment,
    field: 'author',
  },
  {
    name: 'comment',
    label: t('comment'),
    align: DefaultAlignment,
    field: 'comment',
  },
]);

// Handlers

function onOverRow(row: VcsCommitInfoDto) {
  toolsVisible.value[row.commitId] = true;
}

function onLeaveRow(row: VcsCommitInfoDto) {
  toolsVisible.value[row.commitId] = false;
}

async function onCompare(row: VcsCommitInfoDto) {
  try {
    commitInfo.value = await props.diff(row.commitId);
    showDiff.value = true;
  } catch (error) {
    notifyError(error);
  }
}

async function onCompareWith(row: VcsCommitInfoDto) {
  try {
    commitInfo.value = await props.diffWithHead(row.commitId);
    showDiff.value = true;
  } catch (error) {
    notifyError(error);
  }
}

async function onView(row: VcsCommitInfoDto) {
  if (!props.blob) return;
  try {
    const { blob } = await props.blob(row.commitId);
    commitInfo.value = row;
    viewScript.value = blob && blob !== 'null' ? blob : '';
    showView.value = true;
  } catch (error) {
    notifyError(error);
  }
}

function onRestore(row: VcsCommitInfoDto) {
  showRestore.value = true;
  commitInfo.value = row;
}

async function doRestore() {
  showRestore.value = false;
  const toRestore: VcsCommitInfoDto = commitInfo.value;
  commitInfo.value = {} as VcsCommitInfoDto;

  try {
    await props.restore(toRestore);
    emit('restore');
    getCommits();
  } catch (error) {
    notifyError(error);
  }
}

function onCloseDiffViewer() {
  commitInfo.value = {} as VcsCommitInfoDto;
  showDiff.value = false;
}

function getCommits() {
  props
    .loadCommits()
    .then((response) => {
      commits.value = response.commitInfos;
    })
    .catch((error) => notifyError(error));
}

watch(
  () => props.name,
  (newValue) => {
    if (newValue) {
      getCommits();
    }
  },
);

onMounted(() => {
  if (props.name) {
    getCommits();
  }
});
</script>
