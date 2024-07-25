<template>
  <div>
    <q-table
      ref="tableRef"
      flat
      :rows="resourcesStore.resourceReferences"
      row-key="name"
      :columns="columns"
      :pagination="initialPagination"
      :loading="loading"
      @row-click="onRowClick"
      selection="multiple"
      v-model:selected="selected"
    >
      <template v-slot:top-left>
        <q-btn-dropdown color="primary" :label="$t('add')" icon="add" size="sm" :disable="!hasProviders">
          <q-list>
            <template v-for="provider in resourcesStore.resourceProviders?.providers" :key="provider.name">
              <q-item clickable v-close-popup @click.prevent="onShowAdd(provider)">
                <q-item-section>
                  <q-item-label>{{ provider.title }}</q-item-label>
                  <q-item-label caption style="max-width: 300px;">{{ provider.description }}</q-item-label>
                </q-item-section>
              </q-item>
            </template>
          </q-list>
        </q-btn-dropdown>
        <q-btn :disable="selected.length === 0" outline color="red" icon="delete" :title="$t('delete')" size="sm" @click="onShowDelete"  class="on-right"></q-btn>
      </template>
      <template v-slot:body-cell-name="props">
        <q-td :props="props">
          <span class="text-primary">{{ props.value }}</span>
        </q-td>
      </template>
      <template v-slot:body-cell-type="props">
        <q-td :props="props">
          <q-badge :color="getResourceFactory(props.row) ? 'positive' : 'negative'" class="on-left">{{ props.value }}</q-badge>
          <span class="text-caption">{{ getResourceFactory(props.row)?.title }}</span>
        </q-td>
      </template>
      <template v-slot:body-cell-url="props">
        <q-td :props="props">
          <a v-if="props.value?.startsWith('https')" :href="props.value" target="_blank">{{ props.value }}</a>
          <span v-else>{{ props.value }}</span>
        </q-td>
      </template>
    </q-table>

    <resource-reference-dialog v-if="selectedProvider" v-model="showAdd" :provider="selectedProvider" @saved="onSaved" />
    <confirm-dialog v-model="showDelete" :title="$t('delete')" :text="$t('delete_resources_confirm', { count: selected.length })" @confirm="onDeleteResources" />
  </div>
</template>


<script lang="ts">
export default defineComponent({
  name: 'ResourceReferences',
});
</script>
<script setup lang="ts">
import { ResourceReferenceDto } from 'src/models/Projects';
import { ResourceProviderDto } from 'src/models/Resources';
import ResourceReferenceDialog from 'src/components/resources/ResourceReferenceDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';

const { t } = useI18n();
const router = useRouter();
const route = useRoute();
const resourcesStore = useResourcesStore();

const tableRef = ref();
const loading = ref(false);
const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const selected = ref([] as ResourceReferenceDto[]);
const selectedProvider = ref<ResourceProviderDto>();
const showAdd = ref(false);
const showDelete = ref(false);

const pName = computed(() => route.params.id as string);
const columns = computed(() => [
  { name: 'name', label: t('name'), align: 'left', field: 'name' },
  { name: 'type', label: t('type'), align: 'left', field: 'provider' },
  { name: 'description', label: t('description'), align: 'left', field: 'description' },
  { name: 'url', label: 'URL', align: 'left', field: (row: ResourceReferenceDto) => row.resource?.url },
]);
const hasProviders = computed(() => resourcesStore.resourceProviders?.providers?.length ?? 0)

onMounted(onRefresh);

function getResourceFactory(reference: ResourceReferenceDto) {
  return resourcesStore.getResourceFactory(reference);
}

function onRowClick(evt: unknown, row: ResourceReferenceDto) {
  router.push(
    `/project/${resourcesStore.project}/resource/${row.name}`
  );
}

function onShowAdd(provider: ResourceProviderDto) {
  selectedProvider.value = provider;
  showAdd.value = true;
}

function onRefresh() {
  loading.value = true;
  resourcesStore.initResourceReferences(pName.value).finally(() => {
    loading.value = false;
  });
}

function onSaved() {
  loading.value = true;
  resourcesStore.loadResourceReferences(pName.value).finally(() => {
    loading.value = false;
  });
}

function onShowDelete() {
  showDelete.value = true;
}

function onDeleteResources() {
  loading.value = true;
  resourcesStore.deleteResources(pName.value, selected.value.map((r) => r.name)).then(() => {
    return resourcesStore.loadResourceReferences(pName.value);
  }).finally(() => {
    loading.value = false;
    selected.value = [];
  });
}
</script>
