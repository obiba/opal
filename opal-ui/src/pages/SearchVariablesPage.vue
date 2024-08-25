<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('search')" to="/search" />
        <q-breadcrumbs-el :label="$t('variables')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h6">{{ $t('variables_search') }}</div>
      <div class="text-help q-mb-md">
        <q-markdown :src="$t('variables_search_info')" no-heading-anchor-links />
      </div>
      <q-card flat class="bg-grey-2 q-mb-md">
        <q-card-section>
          <div class="row q-gutter-md">
            <q-input
              v-model="searchStore.variablesQuery.query"
              :label="$t('query')"
              flat
              dense
              size="sm"
              style="min-width: 400px;"
              @update:model-value="onClear"
              @keyup.enter="onSubmit"
            />
            <q-btn
              :label="$t('search')"
              icon="search"
              color="primary"
              size="sm"
              @click="onSubmit"
              :disable="loading || !isValid"
              class="q-mt-lg"
              style="height: 2.5em;"
            />
            <q-btn
              outline
              color="secondary"
              icon="cleaning_services"
              :label="$t('clear')"
              size="sm"
              style="height: 2.5em;"
              @click="onClear"
              class="q-mt-lg" />
          </div>
        </q-card-section>
        <q-card-section class="q-pt-none">
          <div class="row q-gutter-md">
            <template v-for="field in fields" :key="field">
              <q-select
                :disable="!fieldsOptions[field] || fieldsOptions[field].length === 0"
                v-model="searchStore.variablesQuery.criteria[field]"
                :options="fieldsOptions[field]"
                :label="getFieldLabel(field)"
                flat
                dense
                multiple
                use-chips
                emit-value
                map-options
                size="sm"
                @update:model-value="onClearAndSubmit"
                style="min-width: 200px;" />
            </template>
            <q-btn-dropdown
              color="secondary"
              :label="$t('filters')"
              size="sm"
              class="q-mt-lg"
              style="height: 2.5em;"
            >
              <q-list>
                <template v-for="field in fieldsToAdd" :key="field">
                  <q-item clickable v-close-popup @click="onToggleField(field)">
                    <q-item-section>
                      <q-item-label>{{ getFieldLabel(field) }}</q-item-label>
                    </q-item-section>
                    <q-item-section side>
                      <q-icon :name="searchStore.variablesQuery.criteria[field] == undefined ? 'add_circle' : 'delete'" size="xs" color="grey-6" />
                    </q-item-section>
                  </q-item>
                </template>
              </q-list>
            </q-btn-dropdown>
          </div>
        </q-card-section>
      </q-card>

      <div v-if="loading">
        <q-spinner-dots size="lg" />
      </div>
      <div v-else-if="itemResults.length">
        <div class="text-bold q-mb-md">
          {{ $t('results') }} ({{ itemResults.length }})
        </div>
        <q-list separator>
          <q-item clickable v-close-popup v-for="item in itemResults" :key="item.identifier" @click="goToVariable(item)">
            <q-item-section avatar>
              <span>{{ searchStore.getField(item, 'name') }}</span>
              <div>
                <span class="text-hint text-primary">{{ searchStore.getField(item, 'project') }}.{{ searchStore.getField(item, 'table') }}</span>
              </div>
            </q-item-section>
            <q-item-section top>
              <div v-for="attr in searchStore.getLabels(item)" :key="attr.locale" class="text-hint">
                <q-badge
                  v-if="attr.locale"
                  color="grey-3"
                  :label="attr.locale"
                  class="q-mr-xs text-grey-6"
                />
                <span>{{ attr.value }}</span>
              </div>
            </q-item-section>
            <q-item-section side>
              <q-icon name="arrow_circle_right" color="grey-6" />
            </q-item-section>
          </q-item>
        </q-list>
        <div v-if="limit <= itemResults.length" class="q-mt-md">
          <q-btn
            no-caps
            icon="add_circle"
            :label="$t('more_results')"
            color="primary"
            size="sm"
            @click="addLimit"
          />
        </div>
      </div>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import { TableDto } from 'src/models/Magma';
import { QueryResultDto } from 'src/models/Search';
import { ItemFieldsResultDto } from 'src/stores/search';
import { VariableNatures, ValueTypes } from 'src/utils/magma';

const route = useRoute();
const router = useRouter();
const searchStore = useSearchStore();
const taxonomiesStore = useTaxonomiesStore();
const { t } = useI18n();

const loading = ref<boolean>(false);
const showResults = ref(false);
const results = ref<QueryResultDto>();
const limit = ref<number>(10);
const tables = ref<TableDto[]>([]);

const queryParam = computed(() => route.query.q as string || '');
const isValid = computed(() => !!searchStore.variablesQuery.query?.trim() || Object.values(searchStore.variablesQuery.criteria).some((criteria) => criteria.length > 0));
const itemResults = computed(() => results.value?.hits as ItemFieldsResultDto[] || []);

const fields = computed(() => Object.keys(searchStore.variablesQuery.criteria));
const projectsOptions = computed(() => Array.from(new Set(tables.value.map((table) => table.datasourceName))).sort());
const tablesOptions = computed(() => Array.from(new Set(tables.value.map((table) => table.name))).sort());
const fieldsOptions = computed<{ [key: string]: { label: string, value: string }[]}>(() => ({
  project: projectsOptions.value.filter((p) => p !== undefined).map((project) => ({ label: project, value: project })),
  table: tablesOptions.value.map((table) => ({ label: table, value: table })),
  nature: Object.keys(VariableNatures).map((key) => ({ label: t(`variable_nature.${key}`), value: key })),
  'value-type': ValueTypes.map((t) => ({ label: t, value: t })),
}));
const fieldsToAdd = computed(() => ['project', 'table', 'nature', 'value-type']);

onMounted(() => {
  if (queryParam.value) {
    searchStore.reset();
    searchStore.variablesQuery.query = queryParam.value;
  }
  loading.value = true;
  Promise.all([
    searchStore.getTables().then((res) => tables.value = res),
    taxonomiesStore.init(),
  ])
    .then(() => onSubmit())
    .catch(() => {
      showResults.value = false;
      results.value = undefined;
    })
    .finally(() => loading.value = false);
});

function onClear() {
  showResults.value = false;
  results.value = undefined;
  limit.value = 10;
}

function onSubmit() {
  if (isValid.value) {
    loading.value = true;
    searchStore.searchVariables(limit.value).then((res) => {
      showResults.value = res.totalHits > 0;
      results.value = res;
    })
    .finally(() => loading.value = false);
  } else {
    loading.value = false;
  }
}

function onClearAndSubmit() {
  onClear();
  onSubmit();
}

function goToVariable(item: ItemFieldsResultDto) {
  const fields = item['Search.ItemFieldsDto.item'].fields;
  let project;
  let table;
  let variable;
  for (const field of fields) {
    if (field.key === 'project') {
      project = field.value;
    } else if (field.key === 'table') {
      table = field.value;
    } else if (field.key === 'name') {
      variable = field.value;
    }
  }
  if (project || table || variable) {
    router.push(`/project/${project}/table/${table}/variable/${variable}`);
  }
}

function addLimit() {
  limit.value += 10;
  onSubmit();
}

function getFieldLabel(field: string) {
  if (field === 'project') {
    return t('projects');
  } else if (field === 'table') {
    return t('tables');
  }
  return t(field.replaceAll('-', '_'));
}

function onToggleField(field: string) {
  if (searchStore.variablesQuery.criteria[field] == undefined) {
    searchStore.variablesQuery.criteria[field] = [];
  } else {
    delete searchStore.variablesQuery.criteria[field];
  }
}
</script>
