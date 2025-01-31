<template>
  <git-diff-header v-if="showHeader" :commit-info="commitInfo" />

  <div v-html="diffHtml"></div>
</template>

<script setup lang="ts">
import { html as Diff2Html } from 'diff2html';
import 'diff2html/bundles/css/diff2html.min.css';
import GitDiffHeader from 'src/components/git/GitDiffHeader.vue';
import type { VcsCommitInfoDto } from 'src/models/Opal';

interface Props {
  commitInfo: VcsCommitInfoDto;
  showHeader: boolean;
}

const props = defineProps<Props>();
const diffHtml = ref('');
const diffEntry = computed(() =>
  props.commitInfo && props.commitInfo.diffEntries ? props.commitInfo.diffEntries[0] : ''
);

function refresh() {
  if (props.commitInfo) {
    diffHtml.value = Diff2Html(diffEntry.value || '', {
      drawFileList: false,
      matching: 'lines',
      outputFormat: 'side-by-side',
    });
  }
}

watch(
  () => props.commitInfo,
  (newValue) => {
    if (newValue) {
      refresh();
    }
  }
);

onMounted(() => refresh());
</script>
