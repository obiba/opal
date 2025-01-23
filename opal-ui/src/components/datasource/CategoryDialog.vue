<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ category.name === '' ? t('add_category') : t('edit_category') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-input
          v-model="newCategory.name"
          dense
          type="text"
          :label="t('name')"
          :hint="t('unique_name_hint')"
          style="min-width: 300px"
          class="q-mb-md"
        >
        </q-input>
        <div class="text-grey-6 q-mb-sm">
          {{ t('labels') }}
        </div>
        <q-card flat class="q-mb-md bg-grey-2">
          <q-card-section>
            <div v-for="label in labels" :key="label.locale" class="row q-col-gutter-md q-mb-md">
              <q-input
                v-model="label.locale"
                dense
                type="text"
                :label="t('locale')"
                :debounce="500"
                style="width: 80px"
              />
              <q-input v-model="label.value" dense type="text" :label="t('value')" style="min-width: 290px" />
              <span class="q-mt-md">
                <q-btn
                  flat
                  size="sm"
                  color="negative"
                  icon="delete"
                  @click="labels = labels.filter((l) => l.locale !== label.locale)"
                  class="on-right"
                />
              </span>
            </div>
            <q-btn
              size="sm"
              color="primary"
              icon="add"
              :label="labels.length ? '' : t('add')"
              @click="labels = labels.concat({ locale: '', value: '' })"
            />
          </q-card-section>
        </q-card>
        <q-checkbox v-model="newCategory.isMissing" :label="t('is_missing')" dense />
        <div class="text-hint q-mt-xs">
          {{ t('is_missing_hint') }}
        </div>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('save')" color="primary" :disable="!isValid" @click="onSave" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { CategoryDto, VariableDto } from 'src/models/Magma';

interface DialogProps {
  modelValue: boolean;
  variable: VariableDto;
  category: CategoryDto;
}

interface Label {
  locale: string;
  value: string;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'saved']);

const { t } = useI18n();
const datasourceStore = useDatasourceStore();

const showDialog = ref(props.modelValue);
const newCategory = ref<CategoryDto>({ name: '', attributes: [], isMissing: false } as CategoryDto);
const labels = ref<Label[]>([]);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      if (props.category) {
        newCategory.value = { ...props.category };
        newCategory.value.attributes = [];
        labels.value = props.category.attributes
          ? props.category.attributes
              .filter((a) => a.name === 'label')
              .map((a) => ({ locale: a.locale || '', value: a.value }))
          : [];
      } else {
        newCategory.value = { name: '', attributes: [], isMissing: false } as CategoryDto;
        labels.value = [];
      }
    }
    showDialog.value = value;
  }
);

const isValid = computed(
  () =>
    newCategory.value.name !== '' &&
    (props.variable.categories === undefined ||
      props.variable.categories
        .filter((c) => c.name !== props.category.name)
        .find((c) => c.name === newCategory.value.name) === undefined)
);

function onHide() {
  emit('update:modelValue', false);
}

function onSave() {
  const newVariable = { ...props.variable } as VariableDto;
  newVariable.categories = props.variable.categories ? [...props.variable.categories] : [];
  const idx = newVariable.categories.findIndex((c) => props.category.name === c.name);
  newCategory.value.attributes = labels.value
    .filter((l) => l.value !== '') // remove empty labels
    .filter((l, index, array) => array.findIndex((ll) => ll.locale === l.locale) === index) // remove duplicates
    .map((l) => ({ name: 'label', locale: l.locale === '' ? undefined : l.locale, value: l.value }));
  if (idx !== -1) {
    newVariable.categories[idx] = newCategory.value;
  } else {
    newVariable.categories.push(newCategory.value);
  }
  datasourceStore.updateVariable(newVariable).then(() => {
    emit('saved', newVariable);
  });
}
</script>
