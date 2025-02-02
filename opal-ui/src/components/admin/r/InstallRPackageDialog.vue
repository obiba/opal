<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('install') }}</div>
      </q-card-section>
      <q-separator />
      <q-card-section class="bg-warning">
        <q-icon name="warning" />
        {{ t('r.packages_warn') }}
      </q-card-section>
      <q-card-section>
        <q-select v-model="manager" :options="managersOptions" dense :label="t('package_manager')" class="q-mb-md" />
        <q-input v-model="name" dense :label="t(manager?.value === 'gh' ? 'gh_repo' : 'name')" />
        <div v-if="manager?.value === 'gh'" class="text-hint">{{ t('gh_repo_hint') }}</div>
        <div v-if="manager?.value === 'gh'" class="q-mt-xs row q-col-gutter-md">
          <div class="col">
            <q-input v-model="organization" dense :label="t('gh_org')" />
            <div class="text-hint">{{ t('gh_org_hint') }}</div>
          </div>
          <div class="col">
            <q-input v-model="reference" dense :label="t('gh_ref')" />
            <div class="text-hint">{{ t('gh_ref_hint') }}</div>
          </div>
        </div>
      </q-card-section>
      <q-separator />
      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('install_action')" color="primary" @click="onUpdate" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { RServerClusterDto } from 'src/models/OpalR';
import { notifyError, notifySuccess } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  cluster: RServerClusterDto;
  managers?: string[];
}

const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();
const rStore = useRStore();

watch(
  () => props.modelValue,
  (value) => {
    manager.value = managersOptions.value[0];
    name.value = '';
    organization.value = '';
    reference.value = '';
    showDialog.value = value;
  }
);

const managersOptions = computed(() => {
  return [
    { label: 'CRAN', value: 'cran' },
    { label: 'Github', value: 'gh' },
    { label: 'Bioconductor', value: 'bioc' },
  ].filter((m) => props.managers === undefined || props.managers.includes(m.value));
});
const manager = ref(managersOptions.value[0]);
const name = ref('');
const organization = ref('');
const reference = ref('');

function onHide() {
  emit('update:modelValue', false);
}

function onUpdate() {
  const isgh = manager.value?.value === 'gh';
  const pname = isgh ? `${organization.value}/${name.value}` : name.value;
  const pref = isgh && reference.value ? reference.value : undefined;
  rStore
    .installRPackage(props.cluster.name, manager.value?.value || 'cran', pname, pref)
    .then((response) => {
      notifySuccess(t('install_r_package_task_created', { id: response.data.id }));
    })
    .catch((err) => {
      console.error(err);
      notifyError(err);
    });
}
</script>
