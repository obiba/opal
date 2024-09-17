<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ dialogTitle }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="formRef" class="q-gutter-md" persistent>

        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="submitCaption" type="submit" color="primary" @click="onAddProject" />
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

interface DialogProps {
  modelValue: boolean;
  projectName: string;
  tableName: string;
}

// const { t } = useI18n();
const projectsStore = useProjectsStore();

const emit = defineEmits(['update:modelValue', 'update']);
const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const formRef = ref();

// Validators
// const validateRequiredField = (val: string) => (val && val.trim().length > 0) || t('validation.name_required');

// Handlers

function onHide() {
  showDialog.value = false;
  emit('update:modelValue', false);
}

onMounted(() => {
    console.log('mounted');
});

</script>