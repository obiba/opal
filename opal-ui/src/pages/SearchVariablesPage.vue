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
              v-model="query"
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
              color="primary"
              size="sm"
              @click="onSubmit"
              :disable="loading || !isValid"
              class="q-mt-lg"
              style="height: 2.5em;"
            />
          </div>
        </q-card-section>
        <q-card-section class="q-pt-none">
          <div class="row q-gutter-md">
            <q-select
              :disable="projectsOptions.length === 0"
              v-model="projectsCriteria"
              :options="projectsOptions"
              :label="$t('projects')"
              flat
              dense
              multiple
              use-chips
              size="sm"
              @update:model-value="onClearAndSubmit"
              style="min-width: 200px;" />
            <q-select
              :disable="tablesOptions.length === 0"
              v-model="tablesCriteria"
              :options="tablesOptions"
              :label="$t('tables')"
              flat
              dense
              multiple
              use-chips
              size="sm"
              @update:model-value="onClearAndSubmit"
              style="min-width: 200px;" />
            <q-btn
              v-show="false"
              :label="$t('filter')"
              icon="add"
              color="secondary"
              size="sm"
              @click="onAddCriteria"
              :disable="loading || !isValid"
              class="q-mt-lg"
              style="height: 2.5em;"
            />
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
            <q-item-section>
              <span>{{ searchStore.getField(item, 'name') }}</span>
              <div>
                <span class="text-hint text-primary">{{ searchStore.getField(item, 'project') }}.{{ searchStore.getField(item, 'table') }}</span>
              </div>
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

const route = useRoute();
const router = useRouter();
const searchStore = useSearchStore();
const taxonomiesStore = useTaxonomiesStore();

const query = ref<string>('');
const loading = ref<boolean>(false);
const showResults = ref(false);
const results = ref<QueryResultDto>();
const limit = ref<number>(10);
const tables = ref<TableDto[]>([]);
const projectsCriteria = ref<string[]>([]);
const tablesCriteria = ref<string[]>([]);

const queryParam = computed(() => route.query.q as string);
const isValid = computed(() => !!query.value.trim());
const itemResults = computed(() => results.value?.hits as ItemFieldsResultDto[] || []);

const projectsOptions = computed(() => Array.from(new Set(tables.value.map((table) => table.datasourceName))).sort());
const tablesOptions = computed(() => Array.from(new Set(tables.value.map((table) => table.name))).sort());

onMounted(() => {
  if (queryParam.value) {
    query.value = queryParam.value;

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
    let fullQuery = query.value.trim();
    if (projectsCriteria.value.length > 0) {
      fullQuery += ' AND (' + projectsCriteria.value.map((p) => `project:"${p}"`).join(' OR ') + ')';
    }
    if (tablesCriteria.value.length > 0) {
      fullQuery += ' AND (' + tablesCriteria.value.map((p) => `table:"${p}"`).join(' OR ') + ')';
    }
    loading.value = true;
    searchStore.search(fullQuery, limit.value, ['label', 'label-en']).then((res) => {
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
</script>
