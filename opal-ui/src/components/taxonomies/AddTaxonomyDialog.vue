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
            v-model="newTaxonomy.name"
            dense
            type="text"
            :label="t('name') + '*'"
            :hint="t('taxonomy.name_hint')"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredField('required_field')]"
          >
          </q-input>

          <q-input
            v-model="newTaxonomy.author"
            dense
            type="text"
            :label="t('author')"
            :hint="t('taxonomy.author_hint')"
            class="q-mb-md"
            lazy-rules
          >
          </q-input>

          <q-input
            v-model="newTaxonomy.license"
            dense
            type="text"
            :label="t('license')"
            class="q-mb-md"
            lazy-rules
            :rules="[() => true]"
          >
            <template v-slot:hint>
              <html-anchor-hint
                :tr-key="'taxonomy.license_hint'"
                :text="t('taxonomy.creative_commons_licenses')"
                :url="getCreativeCommonsLicenseUrl()"
              />
            </template>
          </q-input>

          <localized-field-large
            v-model="newTaxonomy.title"
            :title="t('title')"
            :hint="t('title_hint')"
          ></localized-field-large>

          <localized-field-large
            v-model="newTaxonomy.description"
            :title="t('description')"
            :hint="t('taxonomy.description_hint')"
          />
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="submitCaption" type="submit" color="primary" @click="onAddTaxonomy" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { TaxonomyDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';
import LocalizedFieldLarge from 'src/components/LocalizedFieldLarge.vue';
import HtmlAnchorHint from 'src/components/HtmlAnchorHint.vue';
import { getCreativeCommonsLicenseUrl } from 'src/utils/taxonomies';

interface DialogProps {
  modelValue: boolean;
  taxonomy: TaxonomyDto | null;
}

const { t } = useI18n();
const taxonomiesStore = useTaxonomiesStore();

const formRef = ref();
const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'updated']);

const showDialog = ref(props.modelValue);
const emptyTaxonomy = {
  name: '',
  title: [],
  description: [],
  vocabularies: [],
  keywords: [],
  attributes: [],
} as TaxonomyDto;

const newTaxonomy = ref<TaxonomyDto>({ ...emptyTaxonomy });
const oldName = computed(() => props.taxonomy?.name);
const editMode = computed(() => props.taxonomy?.name !== undefined);
const submitCaption = computed(() => (editMode.value ? t('update') : t('add')));
const dialogTitle = computed(() => (editMode.value ? t('taxonomy.edit') : t('taxonomy.add')));

// Validation rules
const validateRequiredField = (msgKey: string) => (val: string) => (val && val.trim().length > 0) || t(msgKey);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      if (props.taxonomy) {
        newTaxonomy.value = JSON.parse(JSON.stringify(props.taxonomy));
      } else {
        newTaxonomy.value = { ...emptyTaxonomy };
      }

      showDialog.value = value;
    }
  }
);

// Handlers

function onHide() {
  newTaxonomy.value = { ...emptyTaxonomy };
  emit('update:modelValue', false);
}

async function onAddTaxonomy() {
  const valid = await formRef.value.validate();
  if (valid) {
    (editMode.value
      ? taxonomiesStore.updateTaxonomy(newTaxonomy.value, oldName.value)
      : taxonomiesStore.addTaxonomy(newTaxonomy.value)
    )
      .then(() => {
        emit('updated', newTaxonomy.value, oldName.value);
        showDialog.value = false;
        if (oldName.value) onHide(); // due to reroute in the page
      })
      .catch(notifyError);
  }
}
</script>
