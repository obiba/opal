<template>
  <div class="text-h5">
    <q-icon name="sell" size="sm" class="q-mb-xs"></q-icon><span class="on-right">{{ taxonomy.name }}</span>
    <q-btn outline color="primary" icon="download" size="sm" @click="onDownload" class="on-right"></q-btn>
    <q-btn outline color="red" icon="delete" size="sm" @click="onDelete" class="on-right"></q-btn>
  </div>

  <div class="q-gutter-md q-mt-md q-mb-md">
    <fields-list class="col-6" :items="properties" :dbobject="taxonomy" />
  </div>

  <div class="text-h6 q-mb-md q-mt-lg">{{ $t('vocabularies') }}</div>
  <q-table
    flat
    :key="tableKey"
    :rows="rows"
    :columns="columns"
    :sort-method="customSort"
    binary-state-sort
    row-key="name"
    :pagination="initialPagination"
    :hide-pagination="rows.length <= initialPagination.rowsPerPage"
  >
    <template v-slot:top-left v-if="dirty">
      <div class="q-gutter-sm">
        <q-btn no-caps color="primary" icon="check" size="sm" :label="$t('apply')" @click="onApply" />
        <q-btn no-caps color="secondary" icon="close" size="sm" :label="$t('reset')" @click="onResetSort" />
      </div>
    </template>
    <template v-slot:body-cell-name="props">
      <q-td :props="props" class="items-start" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <router-link :to="`/admin/taxonomies/${taxonomy.name}/${props.value}`">
          {{ props.value }}
        </router-link>
        <div class="float-right">
          <q-btn
            v-show="props.rowIndex > 0"
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="$t('move_up')"
            :icon="toolsVisible[props.row.name] ? 'arrow_upward' : 'none'"
            class="q-ml-xs"
            @click="onMoveUp(props.value)"
          />
          <q-btn
            v-show="props.rowIndex < rows.length - 1"
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="$t('move_down')"
            :icon="toolsVisible[props.value] ? 'arrow_downward' : 'none'"
            class="q-ml-xs"
            @click="onMoveDown(props.value)"
          />
        </div>
      </q-td>
    </template>
    <template v-slot:body-cell-license="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        {{ props.col.format(props.value) }}
      </q-td>
    </template>
    <template v-slot:body-cell-title="props">
      <q-td :props="props">
        <template v-for="locale in locales" :key="locale">
          <div class="q-py-xs">
            <code class="text-secondary q-my-xs">{{ locale }}</code>
            {{ taxonomiesStore.getLabel(props.value, locale) }}
          </div>
        </template>
      </q-td>
    </template>
    <template v-slot:body-cell-description="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <template v-for="locale in locales" :key="locale">
          <div class="row no-wrap q-py-xs">
            <div class="col-auto">
              <code class="text-secondary q-my-xs">{{ locale }}</code>
            </div>
            <div class="col q-ml-sm">{{ taxonomiesStore.getLabel(props.value, locale) }}</div>
          </div>
        </template>
      </q-td>
    </template>
    <template v-slot:body-cell-terms="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        {{ props.value }}
      </q-td>
    </template>
  </q-table>

  <!-- Dialogs -->
  <confirm-dialog
    v-model="showDelete"
    :title="$t('delete')"
    :text="$t('delete_taxonomy_confirm', { taxonomy: taxonomy.name })"
    @confirm="doDelete"
  />
</template>

<script lang="ts">
export default defineComponent({
  name: 'TaxonomyContent',
});
</script>

<script setup lang="ts">
import { TaxonomyDto, VocabularyDto, LocaleTextDto } from 'src/models/Opal';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import FieldsList, { FieldItem } from 'src/components/FieldsList.vue';
import { locales } from 'boot/i18n';

interface Props {
  taxonomy: TaxonomyDto;
}

const emit = defineEmits(['update', 'refresh']);
const taxonomiesStore = useTaxonomiesStore();
const props = defineProps<Props>();
const router = useRouter();
const { t } = useI18n({ useScope: 'global' });
const toolsVisible = ref<{ [key: string]: boolean }>({});
const rows = ref<VocabularyDto[]>([]);
const showDelete = ref(false);
const tableKey = ref(0);
const dirty = ref(false);
let canSort = true;
const sortedName = ref<string[]>([]);
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});
const properties: FieldItem<TaxonomyDto>[] = [
  {
    field: 'name',
  },
  {
    field: 'author',
    label: 'author',
  },
  {
    field: 'license',
    label: 'license',
    html: (val) => getCreativeCommonsLicense(val),
  },
  {
    field: 'title',
    label: 'title',
    html: (val) => generateLocaleRows(val.title),
  },
  {
    field: 'description',
    label: 'description',
    html: (val) => generateLocaleRows(val.description),
  },
];

const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'name',
    format: (val: string) => val,
    sortable: true,
    style: 'width: 20%',
  },
  {
    name: 'title',
    label: t('title'),
    align: 'left',
    field: 'title',
    headerStyle: 'width: 30%; white-space: normal;',
    style: 'width: 30%; white-space: normal;',
  },
  {
    name: 'description',
    label: t('description'),
    align: 'left',
    field: 'description',
    headerStyle: 'width: 50%; white-space: normal;',
    style: 'width: 50%; white-space: normal;',
  },
  {
    name: 'terms',
    label: t('terms'),
    align: 'left',
    field: (row: VocabularyDto) => (row.terms || []).length,
  },
]);

// Functions

function customSort(rows: VocabularyDto[], sortBy: string, descending: string) {
  if (!canSort || !sortBy) return rows;

  const data = rows;
  dirty.value = true;

  data.sort((a: VocabularyDto, b: VocabularyDto): number =>
    descending ? b.name.localeCompare(a.name) : a.name.localeCompare(b.name)
  );

  sortedName.value = data.map((row) => row.name);

  return data;
}

function generateLocaleRows(val: LocaleTextDto[]) {
  if (val) {
    const rows = locales
      .map(
        (locale) =>
          `
          <div class="row no-wrap q-py-xs">
            <div class="col-auto"><code class="text-secondary q-my-xs">${locale}</code></div>
            <div class="col q-ml-sm">${taxonomiesStore.getLabel(val, locale)}</div>
          </div>
          `
      )
      .join('');
    return rows;
  }

  return '';
}

function getCreativeCommonsLicense(taxonomy: TaxonomyDto) {
  const theLicense = taxonomy.license || '';
  const licenseParts = theLicense.split(/\s+/);
  if (licenseParts.length === 3) {
    return `
        <a href="https://creativecommons.org/licenses/${licenseParts[1]}/${licenseParts[2]}" target="_blank">${theLicense}</a>`;
  } else {
    return theLicense;
  }
}

async function doDelete() {
  showDelete.value = false;
  await taxonomiesStore.deleteTaxonomy(props.taxonomy);
  router.replace('/admin/taxonomies');
}


function applySort() {
  const clone = JSON.parse(JSON.stringify(props.taxonomy));
  clone.vocabularies = [...rows.value]; // to be sure all changes are applied

  const sortFunction = (a: VocabularyDto, b: VocabularyDto) => {
    const aIndex = sortedName.value.findIndex((name) => name === a.name);
    const bIndex = sortedName.value.findIndex((name) => name === b.name);
    return aIndex - bIndex;
  };

  clone.vocabularies.sort(sortFunction);
  sortedName.value = [];

  return clone;
}

// Handlers

function onMoveUp(name: string) {
  dirty.value = true;
  const clone = sortedName.value.length > 0 ? applySort().vocabularies : JSON.parse(JSON.stringify(rows.value));
  const index = clone.findIndex((row: VocabularyDto) => row.name === name);

  if (index > 0) {
    const temp = clone[index - 1];
    clone[index - 1] = clone[index];
    clone[index] = temp;
    canSort = false;
    rows.value = clone;
  }

  nextTick(() => {
    // Wait so the default sort is not applied right after the move
    canSort = true;
  });
}

function onMoveDown(name: string) {
  dirty.value = true;
  const clone = sortedName.value.length > 0 ? applySort().vocabularies : JSON.parse(JSON.stringify(rows.value));
  const index = clone.findIndex((row: VocabularyDto) => row.name === name);

  if (index < rows.value.length - 1) {
    const temp = clone[index + 1];
    clone[index + 1] = clone[index];
    clone[index] = temp;
    canSort = false;
    rows.value = clone;
  }

  nextTick(() => {
    // Wait so the default sort is not applied right after the move
    canSort = true;
  });
}

function onOverRow(row: VocabularyDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: VocabularyDto) {
  toolsVisible.value[row.name] = false;
}

function onApply() {
  emit('update', applySort());
}

function onResetSort() {
  emit('refresh');
}

function onDownload() {
  taxonomiesStore.downloadTaxonomy(props.taxonomy);
}

function onDelete() {
  showDelete.value = true;
}

watch(
  () => props.taxonomy,
  (newValue) => {
    if (!!newValue.name) {
      tableKey.value += 1;
      sortedName.value = [];
      canSort = true;
      dirty.value = false;
      rows.value = props.taxonomy.vocabularies || [];
    }
  }
);
</script>
