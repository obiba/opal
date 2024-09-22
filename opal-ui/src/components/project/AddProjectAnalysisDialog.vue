<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ dialogTitle }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <template v-if="editMode">
          <q-tabs
            v-model="tab"
            dense
            class="text-grey"
            active-color="primary"
            indicator-color="primary"
            align="justify"
          >
            <q-tab no-caps name="results" :label="$t('results')" />
            <q-tab no-caps name="parameters" :label="$t('parameters')" />
          </q-tabs>
          <q-tab-panels v-model="tab">
            <q-tab-panel name="results"> </q-tab-panel>
            <q-tab-panel name="parameters">
              <project-analysis-panel
                ref="analysisPanel"
                :project-name="projectName"
                :table-name="tableName"
                :analysis-name="analysisName"
                :analysis-names="analysisNames"
              />
            </q-tab-panel>
          </q-tab-panels>
        </template>
        <project-analysis-panel
          v-else
          ref="analysisPanel"
          :project-name="projectName"
          :table-name="tableName"
          :analysis-name="analysisName"
          :analysis-names="analysisNames"
        />
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="$t('close')" color="secondary" v-close-popup />
        <q-btn
          flat
          v-if="!editMode"
          :label="submitCaption"
          type="submit"
          color="primary"
          @click.prevent="onRunAnalysis"
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'AddProjectAnalysisDialog',
});
</script>

<script setup lang="ts">
import { OpalAnalysisDto } from 'src/models/Projects';
import ProjectAnalysisPanel from 'src/components/project/analyse/ProjectAnalysisPanel.vue';

interface DialogProps {
  modelValue: boolean;
  projectName: string;
  tableName: string;
  analysisName?: string;
  analysisNames: string[];
}

const { t } = useI18n();
const projectsStore = useProjectsStore();
const emit = defineEmits(['update:modelValue', 'update']);
const props = defineProps<DialogProps>();
const analysisPanel = ref();
const showDialog = ref(props.modelValue);
const dialogTitle = ref('');
const submitCaption = ref('');
const editMode = computed(() => !!props.analysisName);
const tab = ref('results');

// Handlers

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      submitCaption.value = 'Add';
      if (!!props.analysisName) {
        projectsStore
          .getAnalysis(props.projectName, props.tableName, props.analysisName)
          .then((response: OpalAnalysisDto) => {
            dialogTitle.value = `${response.name} - ${response.pluginName} / ${response.templateName}`;
          });
      } else {
        dialogTitle.value = t('analyse_validate.analysis_dialog.add_analysis');
        submitCaption.value = t('run');
      }
    }
    showDialog.value = value;
  }
);

function onHide() {
  showDialog.value = false;
  emit('update:modelValue', false);
}

async function onRunAnalysis() {
  const succeeded = await analysisPanel.value.runAnalysis();
  if (succeeded) {
    emit('update');
    onHide();
  }
}
</script>
