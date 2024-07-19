<template>
  <div>
    <div class="row q-col-gutter-md q-mb-md">
      <div class="col-12 col-md-6">
        <div class="text-h6 q-mb-md">{{ $t('properties') }}</div>
        <fields-list
          :items="itemsReference"
          :dbobject="reference"
        />
        <fields-list
          :items="itemsFactory"
          :dbobject="factory"
        />
        <div v-if="factory">
          <q-markdown :src="factory.description" no-heading-anchor-links class="q-pa-md"/>
          <q-separator />
        </div>
      </div>
      <div class="col-12 col-md-6">
        <div class="text-h6 q-mb-md">{{ $t('provider') }}</div>
        <div class="q-mb-md">
          <q-badge :label="reference?.provider" :color="provider ? 'positive' : 'negative'" />
        </div>
        <div v-if="provider">
          <div class="text-bold q-mb-sm">{{ provider.title }}</div>
          <q-markdown :src="provider.description" no-heading-anchor-links/>
          <a v-if="provider.web" :href="provider.web" target="_blank" class="q-mt-md">{{ $t('Website') }} <q-icon name="open_in_new" /></a>
        </div>
        <div v-else>
          <div class="q-mb-md box-warning">
            <q-icon name="error" size="1.2rem"/>
            <span class="on-right">
              {{ $t('provider_not_found', { provider: reference?.provider }) }}
            </span>
          </div>
        </div>
      </div>
    </div>
    <!-- <pre>{{ reference }}</pre> -->
  </div>
</template>


<script lang="ts">
export default defineComponent({
  name: 'ResourceReference',
});
</script>
<script setup lang="ts">
import { ResourceReferenceDto } from 'src/models/Projects';
import FieldsList, { FieldItem } from 'src/components/FieldsList.vue';
import { ResourceFactoryDto } from 'src/models/Resources';

const route = useRoute();
const resourcesStore = useResourcesStore();

const loading = ref(false);
const reference = ref<ResourceReferenceDto>();
const factory = computed(() => reference.value ? resourcesStore.getResourceFactory(reference.value) : null);
const provider = computed(() => reference.value ? resourcesStore.getResourceProvider(reference.value) : null);

const pName = computed(() => route.params.id as string);
const rName = computed(() => route.params.rid as string);

watch([pName, rName], () => {
  init();
});

onMounted(init);

function init() {
  loading.value = true;
  resourcesStore.initResourceReferences(pName.value).finally(() => {
    reference.value = resourcesStore.getResourceReference(rName.value);
    loading.value = false;
  });
}

const itemsReference: FieldItem<ResourceReferenceDto>[] = [
  { field: 'name', },
  { field: 'description', },
  { field: 'resource', label: 'URL', html: (val) => val.resource ? `<a href="${val.resource?.url}" target="_blank">${val.resource?.url}</a>` : '' },
  { field: 'resource', label: 'format', html: (val) => val.resource?.format ? `<code>${val.resource?.format}</code>` : undefined },
];

const itemsFactory: FieldItem<ResourceFactoryDto>[] = [
  { field: 'title', label: 'type' },
];
</script>
