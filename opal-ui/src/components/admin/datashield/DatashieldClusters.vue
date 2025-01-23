<template>
  <div v-if="rStore.clusters.length">
    <div class="row q-gutter-md">
      <div class="col" style="max-width: 200px;">
        <div v-for="cluster in rStore.clusters" :key="cluster.name">
          <q-btn
            flat
            no-caps
            icon="cloud"
            color="primary"
            size="12px"
            :label="cluster.name"
            align="left"
            class="full-width"
            :class="`${ tab === cluster.name ? 'bg-grey-2' : '' }`"
            @click="tab = cluster.name"
          ></q-btn>
        </div>
      </div>
      <div class="col">
        <q-tab-panels v-model="tab">
          <q-tab-panel v-for="cluster in rStore.clusters" :key="cluster.name" :name="cluster.name"
            style="padding-top: 0">
            <div class="text-h6 q-mb-sm">
              {{ cluster.name }}
            </div>
            <q-separator />
            <datashield-packages :cluster="cluster" />
          </q-tab-panel>
        </q-tab-panels>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { RServerClusterDto } from 'src/models/OpalR';
import DatashieldPackages from 'src/components/admin/datashield/DatashieldPackages.vue';

const rStore = useRStore();

const tab = ref<string>(rStore.clusters.length && rStore.clusters[0] ? rStore.clusters[0].name : '');

watch(
  () => rStore.clusters,
  () => {
    if (rStore.clusters.length) {
      if (tab.value === '' || !rStore.clusters.find((cluster: RServerClusterDto) => cluster.name === tab.value))
        tab.value = rStore.clusters[0]?.name || '';
    }
  }
);
</script>
