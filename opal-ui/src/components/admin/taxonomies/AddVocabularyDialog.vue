<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ dialogTitle }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-input
            v-model="newVocabulary.name"
            dense
            type="text"
            :label="$t('name') + '*'"
            :hint="$t('taxonomy.vocabulary.name_hint')"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredField('required_field')]"
          >
          </q-input>

          <localized-field
            v-model="newVocabulary.title"
            :title="$t('title')"
            :hint="$t('taxonomy.vocabulary.title_hint')"
          ></localized-field>

          <localized-field
            v-model="newVocabulary.description"
            :title="$t('description')"
            :hint="$t('taxonomy.vocabulary.title_hint')"
          />

          <q-checkbox v-model="newVocabulary.repeatable" :label="$t('taxonomy.vocabulary.repeatable')" />
          <div class="text-hint q-mb-md">
            {{ $t('taxonomy.vocabulary.repeatable_hint') }}
          </div>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="submitCaption" type="submit" color="primary" @click="onAddTaxonomy" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'AddVocabularyDialog',
});
</script>
<script setup lang="ts">
import { VocabularyDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';
import LocalizedField from 'src/components/LocalizedField.vue';

interface DialogProps {
  modelValue: boolean;
  taxonomy: string;
  vocabulary: VocabularyDto | null;
}

const { t } = useI18n();
const taxonomiesStore = useTaxonomiesStore();
const formRef = ref();
const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'updated']);
const showDialog = ref(props.modelValue);
const emptyVocabulary = {
  name: '',
  title: [],
  description: [],
  repeatable: false,
  terms: [],
  keywords: [],
  attributes: [],
} as VocabularyDto;

const newVocabulary = ref<VocabularyDto>({ ...emptyVocabulary });
const editMode = computed(() => !!props.vocabulary);
const submitCaption = computed(() => (editMode.value ? t('update') : t('add')));
const dialogTitle = computed(() => (editMode.value ? t('taxonomy.vocabulary.edit') : t('taxonomy.vocabulary.add')));

// Validation rules
const validateRequiredField = (msgKey: string) => (val: string) => (val && val.trim().length > 0) || t(msgKey);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      if (props.vocabulary) {
        newVocabulary.value = JSON.parse(JSON.stringify(props.vocabulary));
      } else {
        newVocabulary.value = { ...emptyVocabulary };
      }

      showDialog.value = value;
    }
  }
);

// Handlers

function onHide() {
  newVocabulary.value = { ...emptyVocabulary };
  emit('update:modelValue', false);
}

async function onAddTaxonomy() {
  const valid = await formRef.value.validate();
  if (valid) {
    (editMode.value
      ? taxonomiesStore.updateVocabulary(props.taxonomy, newVocabulary.value)
      : taxonomiesStore.addVocabulary(props.taxonomy, newVocabulary.value)
    )
      .then(() => {
        emit('updated', newVocabulary.value);
        showDialog.value = false;
      })
      .catch(notifyError);
  }
}
</script>
