<template>
  <q-table
    flat
    :rows="commits"
    :columns="columns"
    row-key="id"
    :pagination="initialPagination"
    :hide-pagination="commits.length <= initialPagination.rowsPerPage"
  >
    <template v-slot:body-cell-id="props">
      <q-td :props="props" class="items-start" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        {{ props.value }}
        <template v-if="props.rowIndex === 0"
          ><q-badge color="grey-6 q-ml-xs">{{ t('current') }}</q-badge></template
        >
        <div class="float-right">
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
            :title="t('taxonomy.compare_current')"
            :icon="toolsVisible[props.row.commitId] ? 'keyboard_tab' : 'none'"
            class="q-ml-xs"
            @click="onCompareWith(props.row)"
          />
          <q-btn
            v-if="props.rowIndex > 0"
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
  </q-table>

  <!-- Dialogs -->

  <confirm-dialog
    v-model="showRestore"
    :title="t('restore')"
    :text="t('taxonomy.restore_confirm', { date: getDateLabel(commitInfo.date) })"
    @confirm="doRestore"
  />

  <git-diff-viewer-dialog v-model="showDiff" :commit-info="commitInfo" @update:modelValue="onCloseDiffViewer" />
</template>

<script setup lang="ts">
import GitDiffViewerDialog from 'src/components/git/GitDiffViewerDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { getDateLabel } from 'src/utils/dates';
import type { VcsCommitInfoDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';
import { DefaultAlignment } from 'src/components/models';

interface Props {
  taxonomyName: string;
}
const emit = defineEmits(['restore']);
const props = defineProps<Props>();
const { t } = useI18n({ useScope: 'global' });
const taxonomiesStore = useTaxonomiesStore();
const showDiff = ref(false);
const showRestore = ref(false);
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
    commitInfo.value = await taxonomiesStore.gitCompare(props.taxonomyName, row.commitId);
    showDiff.value = true;
  } catch (error) {
    notifyError(error);
  }
}

async function onCompareWith(row: VcsCommitInfoDto) {
  try {
    commitInfo.value = await taxonomiesStore.gitCompareWith(props.taxonomyName, row.commitId, 'head');
    showDiff.value = true;
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
    await taxonomiesStore.gitRestore(props.taxonomyName, toRestore.commitId);
    emit('restore');
  } catch (error) {
    notifyError(error);
  }
}

function onCloseDiffViewer() {
  commitInfo.value = {} as VcsCommitInfoDto;
  showDiff.value = false;
}

async function getCommits() {
  taxonomiesStore
    .gitCommits(props.taxonomyName)
    .then((response) => {
      commits.value = response.commitInfos;
    })
    .catch((error) => notifyError(error));
}

watch(
  () => props.taxonomyName,
  async (newValue) => {
    if (newValue) {
      getCommits();
    }
  }
);

onMounted(() => {
  if (props.taxonomyName) {
    getCommits();
  }
});
</script>
