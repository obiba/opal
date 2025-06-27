<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ dialogTitle }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="formRef" persistent>
          <div class="row q-col-gutter-md q-mb-md">
            <div class="col">
              <q-input
                v-model="spec.id"
                dense
                label="ID *"
                :hint="t('kubernetes.id_hint')"
                :disable="editMode"
                lazy-rules
                :rules="[(val) => (val && val.trim().length > 0) || t('required')]"
              >
              </q-input>
            </div>
            <div class="col">
              <q-input
                v-model="spec.namespace"
                dense
                :label="t('kubernetes.namespace')"
                :hint="t('kubernetes.namespace_hint')"
              >
              </q-input>
            </div>
          </div>
          <q-toggle v-model="spec.enabled" dense :label="t('enabled')" class="q-mt-sm q-mb-sm" />
          <q-input
            v-model="spec.description"
            dense
            autogrow
            type="textarea"
            :label="t('description')"
            :hint="t('kubernetes.description_hint')"
            class="q-mb-md"
          >
          </q-input>


          <q-tabs
            v-model="tab"
            dense
            class="text-grey"
            active-color="primary"
            indicator-color="primary"
            align="justify"
          >
            <q-tab name="container" :label="t('kubernetes.container')" />
            <q-tab name="resources" :label="t('kubernetes.resources')" />
            <!-- q-tab name="environment" :label="t('kubernetes.environment')" /-->
            <q-tab name="labels" :label="t('kubernetes.labels')" />
            <q-tab name="node" :label="t('kubernetes.node')" />
          </q-tabs>
          <q-separator />
          <q-tab-panels v-model="tab">
            <q-tab-panel name="container">
              <q-input
                v-model="container.name"
                dense
                :label="`${t('kubernetes.name_prefix')} *`"
                :hint="t('kubernetes.name_prefix_hint')"
                class="q-mb-md"
                lazy-rules
                :rules="[
                  (val) => (val && val.trim().length > 0) || t('required'),
                  (val) => isValidPodName(val) || t('kubernetes.invalid_pod_name'),
                ]"
              ></q-input>
              <q-input
                v-model="container.image"
                dense
                :label="`${t('kubernetes.image')} *`"
                :hint="t('kubernetes.image_hint')"
                class="q-mb-md"
                lazy-rules
                :rules="[
                  (val) => (val && val.trim().length > 0) || t('required'),
                  (val) => isValidDockerImageName(val) || t('kubernetes.invalid_docker_image_name'),
                ]"
              >
              </q-input>
              <q-select
                v-model="container.imagePullPolicy"
                :options="pullPolicyOptions"
                :label="t('kubernetes.image_pull_policy')"
                :hint="t('kubernetes.image_pull_policy_hint')"
                class="q-mb-md"
              >
              </q-select>
              <q-input
                v-model="container.imagePullSecret"
                dense
                :label="t('kubernetes.image_pull_secret')"
                :hint="t('kubernetes.image_pull_secret_hint')"
                class="q-mb-md"
              >
              </q-input>
            </q-tab-panel>

            <q-tab-panel name="resources">
              <div class="row q-col-gutter-md">
                <div class="col">
                  <div class="text-bold">{{ t('kubernetes.resources_requests') }}</div>
                  <div class="text-hint">{{ t('kubernetes.resources_requests_hint') }}</div>
                  <q-input
                    v-model="requests_cpu"
                    dense
                    :label="`${t('cpu')}`"
                    :hint="t('kubernetes.cpu_hint')"
                    class="q-mb-md"
                  >
                  </q-input>
                  <q-input
                    v-model="requests_memory"
                    dense
                    :label="`${t('memory')}`"
                    :hint="t('kubernetes.memory_hint')"
                    class="q-mb-md"
                  >
                  </q-input>
                </div>
                <div class="col">
                  <div class="text-bold">{{ t('kubernetes.resources_limits') }}</div>
                  <div class="text-hint">{{ t('kubernetes.resources_limits_hint') }}</div>
                  <q-input
                    v-model="limits_cpu"
                    dense
                    :label="`${t('cpu')}`"
                    :hint="t('kubernetes.cpu_hint')"
                    class="q-mb-md"
                  >
                  </q-input>
                  <q-input
                    v-model="limits_memory"
                    dense
                    :label="`${t('memory')}`"
                    :hint="t('kubernetes.memory_hint')"
                    class="q-mb-md"
                  >
                  </q-input>
                </div>
              </div>
            </q-tab-panel>
            <q-tab-panel name="environment">
              <div class="text-hint">{{ t('kubernetes.environment_hint') }}</div>
              <q-list v-if="env.length" separator bordered class="q-mt-md">
                <q-item v-for="(pair, index) in env" :key="index">
                  <q-item-section>
                    <q-input
                      v-model="pair.value"
                      dense
                      :label="pair.key"
                      hide-bottom-space
                      lazy-rules
                      :rules="[(val) => (val && val.trim().length > 0) || t('required')]"
                    >
                    </q-input>
                  </q-item-section>
                  <q-item-section side>
                    <q-btn flat dense round icon="delete" @click="onRemoveEnv(index)" size="sm" />
                  </q-item-section>
                </q-item>
              </q-list>
              <div class="row q-mt-md">
                <q-input v-model="envKey" dense :label="t('key')"> </q-input>
                <q-input v-model="envValue" dense :label="t('value')" class="on-right"> </q-input>
                <q-btn
                  color="primary"
                  icon="add"
                  :disable="!canAddEnv"
                  @click="onAddEnv"
                  size="sm"
                  class="on-right q-mt-md"
                />
              </div>
            </q-tab-panel>
            <q-tab-panel name="node">
              <div class="text-hint q-mb-md">{{ t('kubernetes.node_hint') }}</div>
              <q-input
                v-model="spec.nodeName"
                dense
                :label="t('kubernetes.node_name')"
                :hint="t('kubernetes.node_name_hint')"
                class="q-mb-md"
              >
              </q-input>
              <div class="text-bold">{{ t('kubernetes.node_selector') }}</div>
              <div class="text-hint">{{ t('kubernetes.node_selector_hint') }}</div>
              <q-list v-if="nodeSelector.length" separator bordered class="q-mt-md">
                <q-item v-for="(pair, index) in nodeSelector" :key="index">
                  <q-item-section>
                    <q-input
                      v-model="pair.value"
                      dense
                      :label="pair.key"
                      hide-bottom-space
                      lazy-rules
                      :rules="[(val) => (val && val.trim().length > 0) || t('required')]"
                    >
                    </q-input>
                  </q-item-section>
                  <q-item-section side>
                    <q-btn flat dense round icon="delete" @click="onRemoveNodeSelector(index)" size="sm" />
                  </q-item-section>
                </q-item>
              </q-list>
              <div class="row q-mt-md">
                <q-input v-model="nodeSelectorKey" dense :label="t('key')"> </q-input>
                <q-input v-model="nodeSelectorValue" dense :label="t('value')" class="on-right"> </q-input>
                <q-btn
                  color="primary"
                  icon="add"
                  :disable="!canAddNodeSelector"
                  @click="onAddNodeSelector"
                  size="sm"
                  class="on-right q-mt-md"
                />
              </div>
            </q-tab-panel>
            
          <q-tab-panel name="labels">
            <div class="text-hint">{{ t('kubernetes.labels_hint') }}</div>
              <q-list v-if="labels.length" separator bordered class="q-mt-md">
                <q-item v-for="(pair, index) in labels" :key="index">
                  <q-item-section>
                    <q-input
                      v-model="pair.value"
                      dense
                      :label="pair.key"
                      hide-bottom-space
                      lazy-rules
                      :rules="[(val) => (val && val.trim().length > 0) || t('required')]"
                    >
                    </q-input>
                  </q-item-section>
                  <q-item-section side>
                    <q-btn flat dense round icon="delete" @click="onRemoveLabel(index)" size="sm" />
                  </q-item-section>
                </q-item>
              </q-list>
              <div class="row q-mt-md">
                <q-input v-model="labelKey" dense :label="t('key')"> </q-input>
                <q-input v-model="labelValue" dense :label="t('value')" class="on-right"> </q-input>
                <q-btn
                  color="primary"
                  icon="add"
                  :disable="!canAddLabel"
                  @click="onAddLabel"
                  size="sm"
                  class="on-right q-mt-md"
                />
              </div>
          </q-tab-panel>
          </q-tab-panels>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="submitCaption" type="submit" color="primary" @click="onSubmit" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { notifyError } from 'src/utils/notify';
import type { PodSpecDto, ContainerDto, ContainerDto_EnvEntry, PodSpecDto_LabelsEntry, PodSpecDto_NodeSelectorEntry } from 'src/models/K8s';

interface DialogProps {
  modelValue: boolean;
  podSpec: PodSpecDto | undefined;
}

const emptyContainer = {
  image: '',
  imagePullPolicy: 'IfNotPresent',
} as ContainerDto;
const emptyPodSpec = {
  id: '',
  type: 'rock',
  namespace: '',
  description: '',
  enabled: false,
  container: emptyContainer,
} as PodSpecDto;

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'update']);
const podsStore = usePodsStore();
const { t } = useI18n();
const formRef = ref();
const showDialog = ref(props.modelValue);
const spec = ref<PodSpecDto>(emptyPodSpec);
const container = ref<ContainerDto>(emptyContainer);
const pullPolicyOptions = ['IfNotPresent', 'Always', 'Never'];
const requests_cpu = ref();
const requests_memory = ref();
const limits_cpu = ref();
const limits_memory = ref();
const labels = ref<PodSpecDto_LabelsEntry[]>([]);
const labelKey = ref('');
const labelValue = ref('');
const nodeSelector = ref<PodSpecDto_NodeSelectorEntry[]>([]);
const nodeSelectorKey = ref('');
const nodeSelectorValue = ref('');
const env = ref<ContainerDto_EnvEntry[]>([]);
const envKey = ref('');
const envValue = ref('');
const tab = ref('container');

const editMode = computed(() => (props.podSpec?.id && props.podSpec.id.length > 0) || false);
const submitCaption = computed(() => (editMode.value ? t('update') : t('add')));
const dialogTitle = computed(() => (editMode.value ? t('kubernetes.edit_pod_spec') : t('kubernetes.add_pod_spec')));
const canAddEnv = computed(() => {
  return envKey.value.trim() !== '' && envValue.value.trim() !== '';
});
const canAddLabel = computed(() => {
  return labelKey.value.trim() !== '' && labelValue.value.trim() !== '';
});
const canAddNodeSelector = computed(() => {
  return nodeSelectorKey.value.trim() !== '' && nodeSelectorValue.value.trim() !== '';
});

// Handlers

function onHide() {
  showDialog.value = false;
  emit('update:modelValue', false);
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      if (props.podSpec) {
        spec.value = JSON.parse(JSON.stringify(props.podSpec));
        container.value = JSON.parse(JSON.stringify(props.podSpec.container || emptyContainer));
        container.value.imagePullPolicy = container.value.imagePullPolicy || 'IfNotPresent';
        requests_cpu.value = spec.value.container?.resources?.requests?.cpu || '1000m';
        requests_memory.value = spec.value.container?.resources?.requests?.memory || '500Mi';
        limits_cpu.value = spec.value.container?.resources?.limits?.cpu || '1000m';
        limits_memory.value = spec.value.container?.resources?.limits?.memory || '1Gi';
      } else {
        spec.value = JSON.parse(JSON.stringify(emptyPodSpec));
        container.value = JSON.parse(JSON.stringify(emptyContainer));
        requests_cpu.value = '1000m';
        requests_memory.value = '500Mi';
        limits_cpu.value = '1000m';
        limits_memory.value = '1Gi';
      }
      spec.value.enabled = spec.value.enabled || false;
      labels.value = spec.value.labels || [];
      labelKey.value = '';
      labelValue.value = '';
      nodeSelector.value = spec.value.nodeSelector || [];
      nodeSelectorKey.value = '';
      nodeSelectorValue.value = '';
      env.value = container.value.env || [];
      envKey.value = '';
      envValue.value = '';
      tab.value = 'container';
      showDialog.value = value;
    }
  },
);

function isValidPodName(name: string): boolean {
  const maxLength = 253 - 10; // need to leave space for the pod name suffix
  const regex = /^[a-z0-9]([-a-z0-9]*[a-z0-9])?$/;

  return name.length > 0 && name.length <= maxLength && regex.test(name);
}

function isValidDockerImageName(name: string): boolean {
  // Regex pattern for a full Docker image name (tag OR digest allowed)
  const dockerImageRegex = new RegExp(
    // optional registry: host[:port]/
    '^(?:(?:[a-zA-Z0-9.-]+(?::[0-9]+)?)/)?' +
      // repository path (may include namespace)
      '(?:[a-z0-9]+(?:[._-][a-z0-9]+)*(?:/[a-z0-9]+(?:[._-][a-z0-9]+)*)*)' +
      // optional tag
      '(?::[\\w][\\w.-]{0,127})?' +
      // optional digest
      '(?:@sha256:[a-fA-F0-9]{64})?$',
  );
  return dockerImageRegex.test(name);
}

function onRemoveLabel(index: number) {
  labels.value.splice(index, 1);
}

function onAddLabel() {
  if (canAddLabel.value) {
    // find if the key already exists
    const existingLabel = labels.value.find((e) => e.key === labelKey.value);
    if (existingLabel) {
      existingLabel.value = labelValue.value;
    } else {
      labels.value.push({ key: labelKey.value, value: labelValue.value });
    }
    labelKey.value = '';
    labelValue.value = '';
  }
}

function onRemoveNodeSelector(index: number) {
  nodeSelector.value.splice(index, 1);
}

function onAddNodeSelector() {
  if (canAddNodeSelector.value) {
    // find if the key already exists
    const existingSelector = nodeSelector.value.find((e) => e.key === nodeSelectorKey.value);
    if (existingSelector) {
      existingSelector.value = nodeSelectorValue.value;
    } else {
      nodeSelector.value.push({ key: nodeSelectorKey.value, value: nodeSelectorValue.value });
    }
    nodeSelectorKey.value = '';
    nodeSelectorValue.value = '';
  }
}

function onRemoveEnv(index: number) {
  env.value.splice(index, 1);
}

function onAddEnv() {
  if (canAddEnv.value) {
    // find if the key already exists
    const existingEnv = env.value.find((e) => e.key === envKey.value);
    if (existingEnv) {
      existingEnv.value = envValue.value;
    } else {
      env.value.push({ key: envKey.value, value: envValue.value });
    }
    envKey.value = '';
    envValue.value = '';
  }
}

async function onSubmit() {
  const valid = await formRef.value.validate();
  if (valid) {
    try {
      spec.value.container = container.value;
      spec.value.container.resources = {
        requests: {
          cpu: requests_cpu.value,
          memory: requests_memory.value,
        },
        limits: {
          cpu: limits_cpu.value,
          memory: limits_memory.value,
        },
      };
      spec.value.container.env = env.value;
      spec.value.labels = labels.value;
      spec.value.nodeSelector = nodeSelector.value;
      // ensure it is a rock-based image
      spec.value.type = 'rock';
      spec.value.container.port = 8085;
      await podsStore.savePodSpec(spec.value);
      emit('update');
      onHide();
    } catch (err) {
      notifyError(err);
    }
  }
}
</script>
