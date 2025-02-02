<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ t(editMode ? 'edit' : 'add') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section v-if="provider">
        <div class="q-mb-md">
          <div class="text-bold q-mb-sm">{{ provider.title }}</div>
          <q-markdown :src="provider.description" no-heading-anchor-links />
          <q-badge :label="provider.name" class="on-left" /><a
            v-if="provider.web"
            :href="provider.web"
            target="_blank"
            class="q-mt-md"
            >{{ t('Website') }} <q-icon name="open_in_new"
          /></a>
        </div>

        <q-input
          v-model="name"
          :label="t('name')"
          :hint="t('resource_ref.name_hint')"
          :disable="editMode"
          dense
          class="q-mb-md"
        />
        <q-input
          v-model="description"
          :label="t('description')"
          :hint="t('resource_ref.description_hint')"
          dense
          type="textarea"
          class="q-mb-md"
        />

        <q-select
          v-model="category"
          :options="categories"
          :label="t('resource_ref.category')"
          dense
          @update:model-value="onCategoryUpdated"
          class="q-mb-sm"
        >
          <template v-slot:option="scope">
            <q-item v-bind="scope.itemProps">
              <q-item-section>
                <q-item-label>{{ scope.opt.title }}</q-item-label>
                <q-item-label v-if="scope.opt.description" caption style="max-width: 400px">
                  {{ scope.opt.description }}
                </q-item-label>
              </q-item-section>
            </q-item>
          </template>
        </q-select>
        <div class="text-hint q-mb-md">
          <q-markdown v-if="category?.description" :src="category.description" />
          <span v-else>{{ t('resource_ref.category_hint') }}</span>
        </div>

        <q-select
          v-model="factory"
          :options="factories"
          :label="t('resource_ref.factory')"
          :disable="!category"
          dense
          class="q-mb-sm"
        >
          <template v-slot:option="scope">
            <q-item v-bind="scope.itemProps">
              <q-item-section>
                <q-item-label>{{ scope.opt.title }}</q-item-label>
                <q-item-label v-if="scope.opt.description" caption style="max-width: 400px">
                  {{ scope.opt.description }}
                </q-item-label>
              </q-item-section>
            </q-item>
          </template>
        </q-select>
        <div class="text-hint q-mb-md">
          <q-markdown v-if="factory?.description" :src="factory.description" />
          <span v-else>{{ t('resource_ref.factory_hint') }}</span>
        </div>

        <div v-if="factory">
          <div class="row q-col-gutter-md">
            <div class="col-6">
              <div class="text-bold q-mb-sm">{{ t('parameters') }}</div>
              <schema-form ref="sfParameters" v-model="refParameters" :schema="parametersSchemaForm" />
            </div>
            <div class="col-6">
              <div class="text-bold q-mb-sm">{{ t('credentials') }}</div>
              <schema-form ref="sfCredentials"  v-model="refCredentials" :schema="credentialsSchemaForm" />
            </div>
          </div>
        </div>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('save')" color="primary" :disable="!factory" @click="onSave" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { ResourceProviderDto } from 'src/models/Resources';
import type { ResourceReferenceDto } from 'src/models/Projects';
import SchemaForm from 'src/components/SchemaForm.vue';

interface DialogProps {
  modelValue: boolean;
  provider?: ResourceProviderDto | undefined;
  resource?: ResourceReferenceDto;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'saved']);

const resourcesStore = useResourcesStore();
const projectStore = useProjectsStore();
const { t } = useI18n();

const editMode = ref<boolean>(false);
const showDialog = ref(props.modelValue);
const name = ref('');
const description = ref('');
const category = ref();
const factory = ref();
const refParameters = ref();
const refCredentials = ref();
const sfParameters = ref();
const sfCredentials = ref();

const categories = computed(() =>
  props.provider?.categories
    .map((p) => {
      return {
        label: p.title,
        value: p.name,
        ...p,
      };
    })
    .sort(compareTitles)
);
const factories = computed(() =>
  category.value
    ? props.provider?.resourceFactories
        .filter((f) => f.tags.includes(category.value.name))
        .map((f) => {
          return {
            label: f.title,
            value: f.name,
            ...f,
          };
        })
        .sort(compareTitles)
    : []
);
const parametersSchemaForm = computed(() => (factory.value ? JSON.parse(factory.value.parametersSchemaForm) : {}));
const credentialsSchemaForm = computed(() => (factory.value ? JSON.parse(factory.value.credentialsSchemaForm) : {}));

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      if (props.resource) {
        editMode.value = props.resource.name !== undefined && props.resource.name !== '';
        name.value = props.resource.name;
        description.value = props.resource.description || '';
        const resourceFactory = props.provider?.resourceFactories.find((cat) => cat.name === props.resource?.factory);
        factory.value = resourceFactory
          ? {
              label: resourceFactory.title,
              value: resourceFactory.name,
              ...resourceFactory,
            }
          : undefined;
        category.value = categories.value?.find((cat) => cat.value === factory.value?.tags[0]);
        try {
          refParameters.value = props.resource.parameters ? JSON.parse(props.resource.parameters) : {};
        } catch (e) {
          console.error(e);
          refParameters.value = {};
        }
        try {
          refCredentials.value = props.resource.credentials ? JSON.parse(props.resource.credentials) : {};
        } catch (e) {
          console.error(e);
          refCredentials.value = {};
        }
      } else {
        editMode.value = false;
        name.value = '';
        description.value = '';
        category.value = undefined;
        factory.value = undefined;
        refParameters.value = {};
        refCredentials.value = {};
      }
    }
    showDialog.value = value;
  }
);

function onHide() {
  emit('update:modelValue', false);
}

function onCategoryUpdated() {
  factory.value = undefined;
}

function compareTitles(a: { title: string }, b: { title: string }) {
  if (a.title < b.title) return -1;
  if (a.title > b.title) return 1;
  return 0;
}

function onSave() {
  if (!props.provider || !sfParameters.value.validate() || !sfCredentials.value.validate()) {
    return;
  }
  const resourceRef: ResourceReferenceDto = {
    project: projectStore.project.name,
    name: name.value,
    description: description.value,
    provider: props.provider.name,
    factory: factory.value.name,
    parameters: JSON.stringify(refParameters.value),
    credentials: JSON.stringify(refCredentials.value),
  };
  if (editMode.value) {
    resourcesStore
      .saveResource(resourceRef)
      .then(() => emit('saved', resourceRef))
      .finally(() => {
        emit('update:modelValue', false);
      });
  } else {
    resourcesStore
      .addResource(resourceRef)
      .then(() => emit('saved', resourceRef))
      .finally(() => {
        emit('update:modelValue', false);
      });
  }
}
</script>
