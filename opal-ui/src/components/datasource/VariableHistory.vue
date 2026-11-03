<template>
  <git-history
    :key="script"
    :name="variable.name"
    :load-commits="() => datasourceStore.getVariableCommits(variable.name)"
    :diff="(id: string) => datasourceStore.getVariableCommit(variable.name, id)"
    :diff-with-head="(id: string) => datasourceStore.getVariableCommitFromHead(variable.name, id)"
    :restore="onRestore"
    :can-restore="!!datasourceStore.perms.variable?.canUpdate()"
    restore-confirm-key="git.restore_script_confirm"
  />
</template>

<script setup lang="ts">
import GitHistory from 'src/components/git/GitHistory.vue';
import type { VariableDto } from 'src/models/Magma';
import type { VcsCommitInfoDto } from 'src/models/Opal';

interface Props {
  variable: VariableDto;
}

const props = defineProps<Props>();
const datasourceStore = useDatasourceStore();

// remount, so reload the commits, when the script is saved from the editor
const script = computed(() => props.variable.attributes?.find((a) => a.name === 'script')?.value || '');

// Only the script attribute is versioned: restore it, keep the other variable properties.
async function onRestore(commit: VcsCommitInfoDto) {
  const { blob } = await datasourceStore.getVariableBlob(props.variable.name, commit.commitId);
  const attributes = (props.variable.attributes || []).filter((a) => a.name !== 'script');
  if (blob !== undefined && blob !== 'null') attributes.push({ name: 'script', value: blob });
  const restored: VariableDto = { ...props.variable, attributes };
  return datasourceStore.saveDerivedVariable(restored, `Restore script from commit ${commit.commitId.slice(0, 7)}`);
}
</script>
