<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('add_profile') }}</div>
      </q-card-section>
      <q-separator />
      <q-card-section>
        <q-input v-model="name" :label="t('name')" :hint="t('datashield.profile_name_hint')" dense class="q-mb-md" />
        <q-select v-model="cluster" :options="clusterNames" :label="t('r.cluster')" dense class="text-grey" />
        <div class="text-hint q-mt-xs">
          {{ t('datashield.profile_cluster_hint') }}
        </div>
      </q-card-section>
      <q-separator />
      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('add')" color="primary" :disable="!name" @click="onSubmit" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { RServerClusterDto } from 'src/models/OpalR';

interface DialogProps {
  modelValue: boolean;
}

const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();
const datashieldStore = useDatashieldStore();
const rStore = useRStore();

const name = ref<string>('');
const cluster = ref<string>(rStore.clusters.length && rStore.clusters[0] ? rStore.clusters[0].name : '');

const clusterNames = computed(() => rStore.clusters.map((cluster: RServerClusterDto) => cluster.name));

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
    if (value) {
      name.value = '';
      cluster.value = rStore.clusters.length && rStore.clusters[0] ? rStore.clusters[0].name : '';
    }
  }
);

function onHide() {
  emit('update:modelValue', false);
}

function onSubmit() {
  datashieldStore.addProfile(name.value, cluster.value);
}
</script>
