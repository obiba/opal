<template>
  <div v-if="rStore.clusters.length">
    <q-toolbar>
      <q-select v-model="tab" :options="clusterNames" :label="$t('r.cluster')" dense outlined size="sm" class="text-grey" />
      <div class="text-help on-right">{{ $t('r.clusters_count', { count: clusterNames.length }) }}</div>
      <q-space />
      <q-btn
        outline
        color="secondary"
        icon="cleaning_services"
        :label="$t('cache')"
        :title="$t('r.clear_cache')"
        size="sm"
        class="on-right"
        @click="onClearCache()"
      />
    </q-toolbar>
    <q-tab-panels v-model="tab">
      <q-tab-panel v-for="cluster in rStore.clusters" :key="cluster.name" :name="cluster.name">
        <r-cluster :cluster="cluster" />
      </q-tab-panel>
    </q-tab-panels>
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'RClusters',
});
</script>
<script setup lang="ts">
import { RServerClusterDto } from 'src/models/OpalR';
import RCluster from 'src/components/admin/r/RCluster.vue';

const rStore = useRStore();

const tab = ref<string>(rStore.clusters.length ? rStore.clusters[0].name : '');

watch(
  () => rStore.clusters,
  () => {
    if (rStore.clusters.length) {
      if (tab.value === '' || !rStore.clusters.find((cluster: RServerClusterDto) => cluster.name === tab.value))
        tab.value = rStore.clusters[0].name;
    }
  }
);

const clusterNames = computed(() => rStore.clusters.map((cluster: RServerClusterDto) => cluster.name));

function onClearCache() {
  rStore.clearRCache();
}
</script>
