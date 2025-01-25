<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent :full-height="fullscreen" :full-width="fullscreen">
    <q-card :class="{ 'dialog-card': fullscreen, 'dialog-md': !fullscreen }">
      <q-card-section>
        <div class="row items-center">
          <div class="col">
            <div class="text-h6">{{ t('git.diff_viewer.title') }}</div>
          </div>
          <div class="col-auto">
            <q-btn
              color="grey-7"
              round
              flat
              :icon="fullscreen ? 'fullscreen_exit' : 'fullscreen'"
              @click.prevent="onScreenSizeToggle"
            >
            </q-btn>
          </div>
        </div>
      </q-card-section>
      <q-separator />

      <q-card-section class="q-pt-none">
        <git-diff-header :commit-info="commitInfo" />
      </q-card-section>

      <q-separator />

      <q-card-section :style="`max-height: ${fullscreen ? '75vh' : '50vh'}`" class="scroll">
        <git-diff-viewer :commit-info="commitInfo" :show-header="false" />
      </q-card-section>

      <q-card-actions align="right" class="bg-grey-3" :class="{ 'action-section': fullscreen }">
        <q-btn flat :label="t('close')" color="secondary" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import GitDiffHeader from 'src/components/git/GitDiffHeader.vue';
import GitDiffViewer from 'src/components/git/GitDiffViewer.vue';
import type { VcsCommitInfoDto } from 'src/models/Opal';

interface DialogProps {
  modelValue: boolean;
  commitInfo: VcsCommitInfoDto;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();

const showDialog = ref(props.modelValue);
const fullscreen = ref(false);

function onHide() {
  emit('update:modelValue', false);
}

function onScreenSizeToggle() {
  fullscreen.value = !fullscreen.value;
}

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
  }
);
</script>

<style scoped>
.dialog-card {
  display: flex;
  flex-direction: column;
  height: 100vh; /* Full viewport height */
}

.action-section {
  margin-top: auto; /* Pushes the actions to the bottom */
}
</style>
