<template>
  <div class="q-gutter-md q-mt-md q-mb-md">
    <fields-list class="col-6" :items="properties" :dbobject="commitInfo" />
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'GitDiffHeader',
});
</script>

<script setup lang="ts">
import { VcsCommitInfoDto } from 'src/models/Opal';
import FieldsList, { FieldItem } from 'src/components/FieldsList.vue';
import { getDateLabel } from 'src/utils/dates';

interface Props {
  commitInfo: VcsCommitInfoDto;
}

defineProps<Props>();
const properties: FieldItem<VcsCommitInfoDto>[] = [
  {
    field: 'commitId',
    label: 'id',
  },
  {
    field: 'author',
    label: 'author',
  },
  {
    field: 'date',
    label: 'date',
    format: (val) => (val ? getDateLabel(val.date) : ''),
  },
  {
    field: 'comment',
    label: 'comment',
  },
];
</script>
