<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ dialogTitle }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section style="max-height: 75vh" class="scroll">
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-input
            v-model="newTerm.name"
            dense
            type="text"
            :label="t('name') + '*'"
            :hint="t('taxonomy.vocabulary.name_hint')"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredField('required_field')]"
          >
          </q-input>

          <localized-field-large
            v-model="newTerm.title"
            :title="t('title')"
            :hint="t('title_hint')"
          ></localized-field-large>

          <localized-field-large
            v-model="newTerm.description"
            :title="t('description')"
            :hint="t('taxonomy.term.description_hint')"
          />

          <localized-field-large
            v-model="newTerm.keywords"
            :title="t('keywords')"
            :hint="t('taxonomy.term.keywords_hint')"
          />
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="submitCaption" type="submit" color="primary" @click="onAddTerm" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { TermDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';
import LocalizedFieldLarge from 'src/components/LocalizedFieldLarge.vue';

interface DialogProps {
  modelValue: boolean;
  taxonomy: string;
  vocabulary: string;
  term: TermDto | null;
}

const { t } = useI18n();
const taxonomiesStore = useTaxonomiesStore();
const oldName = computed(() => props.term?.name);
const formRef = ref();
const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'updated']);
const showDialog = ref(props.modelValue);
const emptyTerm = {
  name: '',
  title: [],
  description: [],
  terms: [],
  keywords: [],
  attributes: [],
} as TermDto;

const newTerm = ref<TermDto>({ ...emptyTerm });
const editMode = computed(() => props.term?.name !== undefined && props.term?.name !== '');
const submitCaption = computed(() => (editMode.value ? t('update') : t('add')));
const dialogTitle = computed(() => (editMode.value ? t('taxonomy.term.edit') : t('taxonomy.term.add')));

// Validation rules
const validateRequiredField = (msgKey: string) => (val: string) => (val && val.trim().length > 0) || t(msgKey);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      if (props.term) {
        newTerm.value = JSON.parse(JSON.stringify(props.term));
      } else {
        newTerm.value = { ...emptyTerm };
      }

      showDialog.value = value;
    }
  }
);

// Handlers

function onHide() {
  newTerm.value = { ...emptyTerm };
  emit('update:modelValue', false);
}

async function onAddTerm() {
  const valid = await formRef.value.validate();
  if (valid) {
    (editMode.value
      ? taxonomiesStore.updateTerm(props.taxonomy, props.vocabulary, newTerm.value, oldName.value)
      : taxonomiesStore.addTerm(props.taxonomy, props.vocabulary, newTerm.value)
    )
      .then(() => {
        emit('updated', newTerm.value, oldName.value);
        showDialog.value = false;
      })
      .catch(notifyError);
  }
}
</script>
