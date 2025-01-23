<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ dialogTitle }}</div>
      </q-card-section>

      <q-separator />

      <q-card flat style="max-height: 75vh" class="scroll">
        <q-card-section>
          <template v-if="editMode">
            <template v-if="analysis?.analysisResults">
              <q-tabs
                v-model="tab"
                dense
                class="text-grey"
                active-color="primary"
                indicator-color="primary"
                align="justify"
              >
                <q-tab no-caps name="results" :label="t('results')" />
                <q-tab no-caps name="parameters" :label="t('parameters')" />
              </q-tabs>
              <q-tab-panels v-model="tab">
                <q-tab-panel name="results">
                  <project-results-panel
                    :project-name="projectName"
                    :table-name="tableName"
                    :analysis-name="analysis.name"
                    :results="analysis.analysisResults"
                  />
                </q-tab-panel>

                <q-tab-panel name="parameters">
                  <project-analysis-panel
                    ref="analysisPanel"
                    :project-name="projectName"
                    :table-name="tableName"
                    :analysis-names="analysisNames"
                    :analysis="analysis"
                  />
                </q-tab-panel>
              </q-tab-panels>
            </template>
            <project-analysis-panel
              v-else
              ref="analysisPanel"
              :project-name="projectName"
              :table-name="tableName"
              :analysis-names="analysisNames"
              :analysis="analysis"
            />
          </template>
          <project-analysis-panel
            v-else
            ref="analysisPanel"
            :project-name="projectName"
            :table-name="tableName"
            :analysis-names="analysisNames"
            :analysis="analysis"
            :clone="clone"
          />
        </q-card-section>
      </q-card>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('close')" color="secondary" v-close-popup />
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

<script setup lang="ts">
import type { OpalAnalysisDto } from 'src/models/Projects';
import ProjectAnalysisPanel from 'src/components/project/analyse/ProjectAnalysisPanel.vue';
import ProjectResultsPanel from 'src/components/project/analyse/ProjectResultsPanel.vue';

interface DialogProps {
  modelValue: boolean;
  projectName: string;
  tableName: string;
  analysisNames: string[];
  analysisName?: string | undefined;
  clone?: OpalAnalysisDto | undefined;
}

const { t } = useI18n();
const projectsStore = useProjectsStore();
const emit = defineEmits(['update:modelValue', 'update']);
const props = defineProps<DialogProps>();
const analysisPanel = ref();
const showDialog = ref(props.modelValue);
const analysis = ref<OpalAnalysisDto>();
const dialogTitle = ref('');
const submitCaption = ref('');
const editMode = computed(() => props.analysisName);
const tab = ref('results');

// Handlers

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      submitCaption.value = 'Add';
      if (props.analysisName) {
        projectsStore
          .getAnalysis(props.projectName, props.tableName, props.analysisName)
          .then((response: OpalAnalysisDto) => {
            analysis.value = response;
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
  analysis.value = undefined;
  tab.value = 'results';
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
