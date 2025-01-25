<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('taxonomy.import_gh.title') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section style="max-height: 75vh" class="scroll">
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-input
            v-model="formData.user"
            dense
            :label="t('gh_org') + '*'"
            :hint="t('taxonomy.import_gh.org_hint')"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredField('validation.github.org_required')]"
          >
          </q-input>
          <q-input
            v-model="formData.repo"
            dense
            :label="t('gh_repo') + '*'"
            :hint="t('taxonomy.import_gh.repo_hint')"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredField('validation.github.repo_required')]"
          >
          </q-input>
          <q-input
            v-model="formData.file"
            dense
            :label="t('taxonomy.import_gh.file')"
            :hint="t('taxonomy.import_gh.file_hint')"
            class="q-mb-md"
            lazy-rules
            :rules="[() => true]"
          >
          </q-input>

          <q-checkbox class="q-ml-sm" v-model="formData.override" :label="t('taxonomy.import_gh.override')" />
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup @click="onHide" />
        <q-btn flat :label="t('import')" type="submit" color="primary" @click="onImportTaxonomy" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { notifyError } from 'src/utils/notify';
import type { TaxonomiesImportOptions } from 'src/stores/taxonomies';

interface DialogProps {
  modelValue: boolean;
}

const { t } = useI18n();
const taxonomiesStore = useTaxonomiesStore();
const formRef = ref();
const props = defineProps<DialogProps>();
const emptyFormData = {
  user: '',
  repo: '',
  override: false,
};
const formData = ref<TaxonomiesImportOptions>({ ...emptyFormData });
const emit = defineEmits(['update:modelValue', 'updated']);
const showDialog = ref(props.modelValue);

// Validations
const validateRequiredField = (id: string) => (val: string) => (val && val.trim().length > 0) || t(id);

// Handlers

function onHide() {
  showDialog.value = false;
  formData.value = { ...emptyFormData };
  emit('update:modelValue', false);
}

async function onImportTaxonomy() {
  const valid = await formRef.value.validate();
  if (valid) {
    try {
      await taxonomiesStore.importGithubTaxonomies(formData.value);
      onHide();
      emit('updated');
    } catch (error) {
      notifyError(error);
    }
  }
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      showDialog.value = value;
    }
  }
);
</script>
