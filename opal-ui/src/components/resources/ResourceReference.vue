<template>
  <div>
    <div class="row q-col-gutter-md q-mb-md">
      <div class="col-12 col-md-6">
        <div class="text-h6 q-mb-md">{{ t('properties') }}</div>
        <fields-list :items="itemsReference" :dbobject="reference" />
        <fields-list :items="itemsFactory" :dbobject="factory" />
        <div v-if="factory">
          <q-markdown :src="factory.description" no-heading-anchor-links class="q-pa-md" />
        </div>
      </div>
      <div class="col-12 col-md-6">
        <div class="text-h6 q-mb-md">{{ t('resource_ref.provider') }}</div>
        <div class="q-mb-md">
          <q-badge :label="reference?.provider" :color="provider ? 'positive' : 'negative'" />
        </div>
        <div v-if="provider">
          <div class="text-bold q-mb-sm">{{ provider.title }}</div>
          <q-markdown :src="provider.description" no-heading-anchor-links />
          <a v-if="provider.web" :href="provider.web" target="_blank" class="q-mt-md"
            >{{ t('Website') }} <q-icon name="open_in_new"
          /></a>
        </div>
        <div v-else>
          <div class="q-mb-md box-warning">
            <q-icon name="error" size="1.2rem" />
            <span class="on-right">
              {{ t('resource_ref.provider_not_found', { provider: reference?.provider }) }}
            </span>
          </div>
        </div>
      </div>
    </div>
    <div v-if="reference && factory" class="row q-col-gutter-md">
      <div class="col-12 col-md-6">
        <div class="text-h6 q-mb-md">{{ t('parameters') }}</div>
        <schema-form
          v-model="refParameters"
          :schema="parametersSchemaForm"
          @update:model-value="onParametersUpdate"
          disable
        />
      </div>
      <div class="col-12 col-md-6">
        <div class="text-h6 q-mb-md">{{ t('credentials') }}</div>
        <schema-form
          v-model="refCredentials"
          :schema="credentialsSchemaForm"
          @update:model-value="onParametersUpdate"
          disable
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import FieldsList, { type FieldItem } from 'src/components/FieldsList.vue';
import SchemaForm from 'src/components/SchemaForm.vue';

const route = useRoute();
const resourcesStore = useResourcesStore();
const { t } = useI18n();

const loading = ref(false);

const pName = computed(() => route.params.id as string);
const rName = computed(() => route.params.rid as string);

const reference = computed(() => resourcesStore.getResourceReference(rName.value));
const factory = computed(() => (reference.value ? resourcesStore.getResourceFactory(reference.value) : null));
const provider = computed(() => (reference.value ? resourcesStore.getResourceProvider(reference.value) : null));
const parametersSchemaForm = computed(() => (factory.value ? JSON.parse(factory.value.parametersSchemaForm) : {}));
const credentialsSchemaForm = computed(() => (factory.value ? JSON.parse(factory.value.credentialsSchemaForm) : {}));
const refParameters = computed(() => (reference.value?.parameters ? JSON.parse(reference.value.parameters) : {}));
const refCredentials = computed(() => (reference.value?.credentials ? JSON.parse(reference.value.credentials) : {}));

watch([pName, rName], () => {
  init();
});

onMounted(init);

function init() {
  loading.value = true;
  resourcesStore.initResourceReferences(pName.value).finally(() => {
    loading.value = false;
  });
}

const itemsReference: FieldItem[] = [
  { field: 'name' },
  { field: 'description' },
  {
    field: 'resource',
    label: 'URL',
    html: (val) =>
      val.resource && val.resource?.url.startsWith('http')
        ? `<a href="${val.resource?.url}" target="_blank">${val.resource?.url}</a>`
        : val.resource?.url,
  },
  {
    field: 'resource',
    label: 'format',
    html: (val) => (val.resource?.format ? `<code>${val.resource?.format}</code>` : undefined),
  },
];

const itemsFactory: FieldItem[] = [{ field: 'title', label: 'type' }];

function onParametersUpdate() {
  console.log('onParametersUpdate');
}
</script>
