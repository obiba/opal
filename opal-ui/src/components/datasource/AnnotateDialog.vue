<template>
  <q-dialog v-model="showDialog" @hide="onHide">
      <q-card class="dialog-sm">
        <q-card-section>
          <div class="text-h6">{{ $t('annotate') }}</div>
        </q-card-section>

        <q-separator />

        <q-card-section>
          <div class="q-mb-md box-info">
            <q-icon name="info" size="1.2rem"/>
            <span class="on-right">
              {{ $t('annotate_info', { count: props.variables.length }) }}
            </span>
          </div>

          <q-select
            v-model="taxonomyName"
            :options="taxonomiesOptions"
            :label="$t('taxonomy')"
            dense
            emit-value
            map-options
            class="q-mb-md"
            @update:model-value="onTaxonomyChange"
          />
          <q-select
            v-model="vocabularyName"
            :options="vocabulariesOptions"
            :label="$t('vocabulary')"
            dense
            emit-value
            map-options
            class="q-mb-md"
            @update:model-value="onVocabularyChange"
          />
          <q-select
            v-if="termsOptions.length > 0"
            v-model="termName"
            :options="termsOptions"
            :label="$t('term')"
            dense
            emit-value
            map-options
            class="q-mb-md"
          />
          <q-input
            v-else
            v-model="text"
            :label="$t('text')"
            type="textarea"
            auto-grow
            dense
            class="q-mb-md"/>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="bg-grey-3">
          <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
          <q-btn
            flat
            :label="$t('apply')"
            color="primary"
            @click="onApply"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'AnnotateDialog',
});
</script>
<script setup lang="ts">
import { TableDto, VariableDto } from 'src/models/Magma';

interface DialogProps {
  modelValue: boolean;
  table: TableDto;
  variables: VariableDto[];
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue'])

const taxonomiesStore = useTaxonomiesStore();
const datasourceStore = useDatasourceStore();
const { locale } = useI18n({ useScope: 'global' });

const taxonomyName = ref<string>('');
const vocabularyName = ref<string>('');
const termName = ref<string>('');
const text = ref<string>('');

const taxonomiesOptions = computed(() => {
  return taxonomiesStore.taxonomies ? taxonomiesStore.taxonomies.map((item) => {
    return {
      label: item.title ? taxonomiesStore.getLabel(item.title, locale.value) : item.name,
      value: item.name,
    };
  }) : [];
});

const taxonomy = computed(() => {
  return taxonomiesStore.taxonomies.find((taxo) => taxo.name === taxonomyName.value);
});

const vocabulariesOptions = computed(() => {
  if (!taxonomy.value) {
    return [];
  }
  return taxonomy.value.vocabularies ? taxonomy.value.vocabularies.map((item) => {
    return {
      label: item.title ? taxonomiesStore.getLabel(item.title, locale.value) : item.name,
      value: item.name,
    };
  }) : [];
});

const vocabulary = computed(() => {
  return taxonomiesStore.taxonomies.find((taxo) => taxo.name === taxonomyName.value)?.vocabularies.find((voc) => voc.name === vocabularyName.value);
});

const termsOptions = computed(() => {
  if (!vocabulary.value) {
    return [];
  }
  return vocabulary.value.terms ? vocabulary.value.terms.map((item) => {
    return {
      label: item.title ? taxonomiesStore.getLabel(item.title, locale.value) : item.name,
      value: item.name,
    };
  }) : [];
});

const showDialog = ref(props.modelValue);

watch(() => props.modelValue, (value) => {
  if (value) {
    taxonomiesStore.init().then(() => {
      taxonomyName.value = taxonomiesStore.taxonomies ? taxonomiesStore.taxonomies[0].name : '';
      onTaxonomyChange();
    });
  }
  showDialog.value = value;
});

function onTaxonomyChange() {
  if (taxonomy.value) {
    vocabularyName.value = taxonomy.value.vocabularies ? taxonomy.value.vocabularies[0].name : '';
  } else {
    vocabularyName.value = '';
  }
  onVocabularyChange();
}

function onVocabularyChange() {
  if (vocabulary.value) {
    termName.value = vocabulary.value.terms ? vocabulary.value.terms[0].name : '';
  } else {
    termName.value = '';
  }
}

function onHide() {
  emit('update:modelValue', false);
}

function onApply() {
  datasourceStore.annotate(props.variables, taxonomyName.value, vocabularyName.value, termsOptions.value.length ? termName.value : text.value);
}

</script>
