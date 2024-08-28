<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('administration')" to="/admin" />
        <q-breadcrumbs-el :label="$t('id_mappings.title')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-help q-mb-md">
        {{ $t('id_mappings.info') }}
      </div>
      <div class="row q-gutter-md">
        <div class="col">
          <div class="text-h5 q-mb-md">{{ $t('id_mappings.ids_list_title') }}</div>
          <q-list dense padding>
            <q-item
              clickable
              active-class="bg-grey-2"
              v-for="(identifier, index) in identifiers"
              :key="index"
              :active="identifier.name === selectedIdentifier?.name"
              @click="onSelectIdentifier(identifier)"
            >
              <q-item-section class="q-pa-none q-mr-sm text-caption"> {{ identifier.entityType }} </q-item-section>
            </q-item>
          </q-list>
        </div>
        <div class="col-10">
          <div class="text-h5 q-mb-md">
            {{ selectedIdentifier?.entityType }}
          </div>

          <fields-list :items="properties" :dbobject="selectedIdentifier" />

          <q-tabs
            v-model="tab"
            dense
            class="text-grey q-mt-lg"
            active-color="primary"
            indicator-color="primary"
            align="justify"
          >
            <q-tab name="mappings" :label="$t('mappings')" />
            <q-tab name="identifiers" :label="$t('identifiers')" />
          </q-tabs>

          <q-separator />

          <q-tab-panels v-model="tab">
            <q-tab-panel name="mappings">
              <q-table
                flat
                :rows="mappings"
                :columns="columns"
                row-key="name"
                :pagination="initialPagination"
                :hide-pagination="mappings.length <= initialPagination.rowsPerPage"
                :loading="loading"
              />
            </q-tab-panel>
            <q-tab-panel name="identifiers"> </q-tab-panel>
          </q-tab-panels>
        </div>
      </div>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import { set } from 'date-fns';
import { TableDto, VariableDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';
import FieldsList, { FieldItem } from 'src/components/FieldsList.vue';
import { getDateLabel } from 'src/utils/dates';

const { t } = useI18n();
const initialPagination = ref({
  sortBy: 'resource',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});
const loading = ref(false);
const tab = ref('mappings');
const identifiersStore = useIdentifiersStore();
const selectedIdentifier = ref<TableDto>();
const identifiers = computed({
  get: () => identifiersStore.identifiers || [],
  set: (value: TableDto[]) => {
    identifiersStore.identifiers = value;
    onSelectIdentifier(value[0]);
  },
});
const mappings = computed(() => identifiersStore.mappings || []);
const columns = computed(() => [
  { name: 'name', label: t('name'), align: 'left', field: 'name' },
  { name: 'description', label: t('description'), align: 'left' },
]);

async function onSelectIdentifier(identifier: TableDto) {
  selectedIdentifier.value = identifier;
  loading.value = true;
  identifiersStore.initMappings(identifier.name).then(() => {
    loading.value = false;
  });
}

const properties: FieldItem<TableDto>[] = [
  {
    field: 'timestamps',
    label: 'last_update',
    format: (val) => (val ? getDateLabel(val.timestamps?.lastUpdate) : ''),
  },
  {
    field: 'variableCount',
    label: t('id_mappings.mappings_count'),
  },
  {
    field: 'valueSetCount',
    label: t('id_mappings.system_ids_count'),
  },
];

onMounted(() => {
  identifiersStore
    .initIdentifiers()
    .then(
      () => (identifiers.value = identifiersStore.identifiers.sort((a, b) => a.entityType.localeCompare(b.entityType)))
    )
    .catch(notifyError);
});
</script>
