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
            >{{ t('website') }} <q-icon name="open_in_new"
          /></a>
          <div>
            <div class="text-bold q-mt-md">{{ t('resource_ref.r_example_code') }}</div>
            <div class="text-hint q-my-sm">{{ t('resource_ref.r_example_code_hint') }}</div>
            <div>
              <q-btn
                flat
                dense
                size="sm"
                icon="content_copy"
                color="white"
                :title="t('clipboard.copy')"
                @click="onCopyToClipboard"
                aria-label="Copy to clipboard"
                class="copy-button"
              />
              <code class="r-code">
{{ rCode }}</code>
            </div>
          </div>
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
    <div v-if="reference && factory">
      <code>
      </code>
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
import { copyToClipboard } from 'quasar';
import FieldsList, { type FieldItem } from 'src/components/FieldsList.vue';
import SchemaForm from 'src/components/SchemaForm.vue';
import { notifyInfo } from 'src/utils/notify';

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

const rCode = computed(() => {
  if (!reference.value || !factory.value) {
    return '';
  }
  let code = `library(${reference.value.provider})

# Create a resource object
res <- newResource(
  name = "${reference.value.name}",
  url = "${reference.value.resource?.url}"`;

  if (reference.value.resource?.format) 
    code = code + `,
  format = "${reference.value.resource?.format}"`;

  code = code + `,
  identity = NULL, # Add identity as needed
  secret = NULL    # Add secret as needed
)

# Create a connection client to this resource
client <- newResourceClient(res)

# Coerce to a data frame (if it applies)
as.resource.data.frame(client)

# Or as a raw object (if it applies)
as.resource.object(client)`;
  return code;
});

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
  console.debug('onParametersUpdate');
}

function onCopyToClipboard() {
  copyToClipboard(rCode.value).then(() => {
    notifyInfo(t('resource_ref.r_example_code_copied'));
  });
}
</script>

<style scoped>
.copy-button {
  float: right;
  margin-top: 20px;
  margin-right: 20px;
}
.r-code {
  background-color: #333;
  color: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  display: block;
  white-space: pre-wrap;
}
</style>
