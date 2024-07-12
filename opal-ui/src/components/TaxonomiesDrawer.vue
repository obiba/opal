<template>
  <div v-if="summaries.length > 0">
    <h6 class="q-mt-none q-mb-none q-pa-md">
      {{ $t('taxonomies') }}
    </h6>
    <q-list>
      <q-item
        v-for="summary in summaries"
        :active="name === summary.name"
        :key="summary.name"
        :to="`/admin/taxonomy/${summary.name}`"
      >
        <q-item-section avatar>
          <q-icon name="sell" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ summary.name }}</q-item-label>
        </q-item-section>
      </q-item>
      <!--
      <q-item-label header class="text-weight-bolder">{{
        $t('content')
      }}</q-item-label> -->
    </q-list>
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'TaxonomiesDrawer',
});
</script>

<script setup lang="ts">
import { onMounted, computed } from 'vue';
import { notifyError } from 'src/utils/notify';
import { TaxonomiesDto_TaxonomySummaryDto as TaxonomySummariesDto } from 'src/models/Opal';

const route = useRoute();
const taxonomiesStore = useTaxonomiesStore();
const summaries = computed<TaxonomySummariesDto[]>(() => taxonomiesStore.summaries);
const name = computed(() => route.params.name as string);

onMounted(() => {
  taxonomiesStore.initSummaries().catch(notifyError);
});
</script>
