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
      <template v-slot:body-cell-name="props">
        <q-td :props="props">
          <span class="text-primary">{{ props.value }}</span>
        </q-td>
      </template>
      <template v-slot:body-cell-type="props">
        <q-td :props="props">
          <q-badge color="grey-6" class="on-left">{{ props.value }}</q-badge>
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
    <pre>{{ resourcesStore.resourceReferences }}</pre>
  </div>
</template>


<script lang="ts">
export default defineComponent({
  name: 'ResourceReferences',
});
</script>
<script setup lang="ts">
import { ResourceReferenceDto } from 'src/models/Projects';

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

const pName = computed(() => route.params.id as string);
const columns = computed(() => [
  { name: 'name', label: t('name'), align: 'left', field: 'name' },
  { name: 'type', label: t('type'), align: 'left', field: 'provider' },
  { name: 'description', label: t('description'), align: 'left', field: 'description' },
  { name: 'url', label: 'URL', align: 'left', field: (row: ResourceReferenceDto) => row.resource?.url },
]);

onMounted(() => {
  loading.value = true;
  resourcesStore.initResourceReferences(pName.value).finally(() => {
    loading.value = false;
  });
});

function getResourceFactory(reference: ResourceReferenceDto) {
  return resourcesStore.getResourceFactory(reference);
}

function onRowClick(evt: unknown, row: ResourceReferenceDto) {
  router.push(
    `/project/${resourcesStore.project}/resource/${row.name}`
  );
}

</script>
