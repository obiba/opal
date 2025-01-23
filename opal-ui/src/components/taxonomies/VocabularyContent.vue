<template>
  <div class="text-h5">
    <router-link :to="`/taxonomy/${taxonomyName}`" :title="taxonomyName" class="q-mr-xs">
      <q-icon name="arrow_back_ios_new" />
    </router-link>
    <span>{{ vocabulary.name }}</span>
    <q-btn v-if="taxonomiesStore.canEdit" outline color="secondary" icon="edit" :title="t('edit')" size="sm" @click="onEditVocabulary" class="on-right"></q-btn>
    <q-btn v-if="taxonomiesStore.canEdit" outline color="red" icon="delete" :title="t('delete')" size="sm" @click="onDelete" class="on-right"></q-btn>
    <q-btn v-if="taxonomiesStore.canEdit" outline color="secondary" icon="search" :title="t('search')" size="sm" @click="onSearchVocabulary" class="on-right"></q-btn>
  </div>
  <div class="q-gutter-md q-mt-md q-mb-md">
    <fields-list class="col-6" :items="properties" :dbobject="vocabulary" />
  </div>

  <div class="text-h6 q-mb-none q-mt-lg">{{ t('terms') }}</div>
  <q-table
    flat
    :key="tableKey"
    :rows="rows"
    :columns="columns"
    :sort-method="customSort"
    :filter="filter"
    :filter-method="onFilter"
    binary-state-sort
    row-key="name"
    :pagination="initialPagination"
    :hide-pagination="rows.length <= initialPagination.rowsPerPage"
  >
    <template v-slot:header-cell-name="props">
      <q-th :props="props" @click="onSortUpdate">
        {{ props.col.label }}
      </q-th>
    </template>
    <template v-slot:top-left>
      <div v-if="taxonomiesStore.canEdit" class="q-gutter-sm">
        <q-btn no-caps color="primary " icon="add" size="sm" :label="t('add')" @click="onAddTerm()" />
        <template v-if="dirty">
          <q-btn no-caps color="primary" icon="check" size="sm" :label="t('apply')" @click="onApply" />
          <q-btn no-caps color="secondary" icon="close" size="sm" :label="t('reset')" @click="onResetSort" />
        </template>
      </div>
    </template>
    <template v-slot:top-right>
      <q-input
        dense
        clearable
        debounce="400"
        color="primary"
        v-model="filter"
        :placeholder="t('taxonomy.vocabulary.filter_terms')"
      >
        <template v-slot:append>
          <q-icon name="search" />
        </template>
      </q-input>
    </template>
    <template v-slot:body-cell-name="props">
      <q-td :props="props" class="items-start" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        {{ props.value }}
        <div class="float-right">
          <q-btn
            v-if="taxonomiesStore.canEdit"
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="t('search')"
            :icon="toolsVisible[props.row.name] ? 'search' : 'none'"
            class="q-ml-xs"
            @click="onSearchTerm(props.row)"
          />
          <q-btn
            v-if="taxonomiesStore.canEdit"
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="t('edit')"
            :icon="toolsVisible[props.row.name] ? 'edit' : 'none'"
            class="q-ml-xs"
            @click="onAddTerm(props.row)"
          />
          <q-btn
            v-show="taxonomiesStore.canEdit && !hasFilter && props.rowIndex > 0"
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="t('move_up')"
            :icon="toolsVisible[props.row.name] ? 'arrow_upward' : 'none'"
            class="q-ml-xs"
            @click="onMoveUp(props.value)"
          />
          <q-btn
            v-show="taxonomiesStore.canEdit && !hasFilter && props.rowIndex < rows.length - 1"
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="t('move_down')"
            :icon="toolsVisible[props.value] ? 'arrow_downward' : 'none'"
            class="q-ml-xs"
            @click="onMoveDown(props.value)"
          />
          <q-btn
            v-if="taxonomiesStore.canEdit"
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="t('delete')"
            :icon="toolsVisible[props.value] ? 'delete' : 'none'"
            class="q-ml-xs"
            @click="onDeleteTerm(props.row)"
          />
        </div>
      </q-td>
    </template>
    <template v-slot:body-cell-title="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <template v-for="prop in props.value" :key="prop.locale">
          <div v-if="prop.text" class="row no-wrap q-py-xs">
            <div class="col-auto">
              <q-badge color="grey-6" :label="prop.locale" class="on-left q-mr-sm" />
            </div>
            <div class="col q-ml-none">{{ prop.text }}</div>
          </div>
        </template>
      </q-td>
    </template>
    <template v-slot:body-cell-description="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <template v-for="prop in props.value" :key="prop.locale">
          <div v-if="prop.text" class="row no-wrap q-py-xs">
            <div class="col-auto">
              <q-badge color="grey-6" :label="prop.locale" class="on-left q-mr-sm" />
            </div>
            <div class="col q-ml-none">{{ prop.text }}</div>
          </div>
        </template>
      </q-td>
    </template>
    <template v-slot:body-cell-keywords="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <template v-for="prop in props.value" :key="prop.locale">
          <div v-if="prop.text" class="row no-wrap q-py-xs">
            <div class="col-auto">
              <q-badge color="grey-6" :label="prop.locale" class="on-left q-mr-sm" />
            </div>
            <div class="col q-ml-none">{{ prop.text }}</div>
          </div>
        </template>
      </q-td>
    </template>
  </q-table>

  <!-- Dialogs -->

  <confirm-dialog
    v-model="showDelete"
    :title="t('delete')"
    :text="t('delete_vocabulary_confirm', { taxonomy: taxonomy, vocabulary: vocabulary.name })"
    @confirm="doDelete"
  />

  <confirm-dialog
    v-model="showDeleteTerm"
    :title="t('delete')"
    :text="t('delete_term_confirm', { vocabulary: vocabulary.name, term: newTerm?.name })"
    @confirm="doDeleteTerm"
  />

  <add-vocabulary-dialog
    v-model="showEditVocabulary"
    :taxonomy="taxonomy"
    :vocabulary="vocabulary"
    @update:modelValue="onClose"
    @updated="onVocabularyUpdated"
  />

  <add-term-dialog
    v-model="showAddTerm"
    :taxonomy="taxonomy"
    :vocabulary="vocabulary.name || ''"
    :term="newTerm"
    @update:modelValue="onTermUpdated"
  />
</template>

<script setup lang="ts">
import type { VocabularyDto, TermDto } from 'src/models/Opal';
import useTaxonomyEntityContent from 'src/components/taxonomies/TaxonomyEntityContent';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import AddVocabularyDialog from 'src/components/taxonomies/AddVocabularyDialog.vue';
import AddTermDialog from 'src/components/taxonomies/AddTermDialog.vue';
import FieldsList, { type FieldItem } from 'src/components/FieldsList.vue';
import { notifyError } from 'src/utils/notify';
import { DefaultAlignment } from 'src/components/models';

interface Props {
  taxonomy: string;
  vocabulary: VocabularyDto;
}

const emit = defineEmits(['update', 'refresh']);
const props = defineProps<Props>();
const { t } = useI18n({ useScope: 'global' });
const searchStore = useSearchStore();
const router = useRouter();
const route = useRoute();

const taxonomyName = computed(() => route.params.name as string);
const tableKey = ref(0);
const showDelete = ref(false);
const showDeleteTerm = ref(false);
const showEditVocabulary = ref(false);
const showAddTerm = ref(false);
const newTerm = ref<TermDto | null>(null);
const {
  initialPagination,
  toolsVisible,
  canSort,
  sortedName,
  dirty,
  taxonomiesStore,
  rows,
  filter,
  hasFilter,
  applySort,
  onOverRow,
  onLeaveRow,
  onMoveUp,
  onMoveDown,
  generateLocaleRows,
  customSort,
  onSortUpdate,
  onFilter,
} = useTaxonomyEntityContent<TermDto>(() => props.vocabulary, 'terms');

const properties: FieldItem[] = [
  {
    field: 'name',
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
  {
    field: 'repeatable',
    label: 'repeatable',
    html: (val) => (val.repeatable ? t('yes') : t('no')),
  },
];

const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: DefaultAlignment,
    field: 'name',
    format: (val: string) => val,
    sortable: true,
    headerStyle: 'width: 25%; white-space: normal;',
    style: 'width: 25%; white-space: normal;',
  },
  {
    name: 'title',
    label: t('title'),
    align: DefaultAlignment,
    field: 'title',
    headerStyle: 'width: 20%; white-space: normal;',
    style: 'width: 20%; white-space: normal;',
  },
  {
    name: 'description',
    label: t('description'),
    align: DefaultAlignment,
    field: 'description',
    headerStyle: 'width: 50%; white-space: normal;',
    style: 'width: 50%; white-space: normal;',
  },
  {
    name: 'keywords',
    label: t('keywords'),
    align: DefaultAlignment,
    field: 'keywords',
  },
]);

// Functions

function onApply() {
  emit('update', applySort());
}

function onResetSort() {
  emit('refresh');
}

async function doDelete() {
  showDelete.value = false;
  try {
    await taxonomiesStore.deleteVocabulary(props.taxonomy, props.vocabulary);
    router.replace(`/taxonomy/${props.taxonomy}`);
  } catch (error) {
    notifyError(error);
  }
}

async function doDeleteTerm() {
  showDelete.value = false;
  if (newTerm.value) {
    const toDelete: TermDto = newTerm.value;
    newTerm.value = null;

    try {
      await taxonomiesStore.deleteTerm(props.taxonomy, props.vocabulary.name, toDelete);
      emit('refresh');
    } catch (error) {
      notifyError(error);
    }
  }
}

// Handlers

function onEditVocabulary() {
  showEditVocabulary.value = true;
}

function onSearchVocabulary() {
  searchStore.reset();
  searchStore.variablesQuery.criteria[`${props.taxonomy}-${props.vocabulary.name}`] = props.vocabulary.terms?.map((term) => term.name) || [];
  router.push('/search/variables');
}

function onSearchTerm(term: TermDto) {
  searchStore.reset();
  searchStore.variablesQuery.criteria[`${props.taxonomy}-${props.vocabulary.name}`] = [ term.name ];
  router.push('/search/variables');
}

function onClose() {
  showEditVocabulary.value = false;
}

function onVocabularyUpdated(updated: VocabularyDto, oldName?: string) {
  emit('refresh', updated.name !== oldName ? updated.name : null);
}

function onAddTerm(term: TermDto | undefined = undefined) {
  showAddTerm.value = true;
  newTerm.value = term || {
    name: '',
    title: [],
    description: [],
    keywords: [],
    terms: [],
    attributes: [],
  };
}

function onTermUpdated() {
  showAddTerm.value = false;
  newTerm.value = null;
  emit('refresh');
}

function onDeleteTerm(term: TermDto) {
  showDeleteTerm.value = true;
  newTerm.value = term;
}

async function onDelete() {
  showDelete.value = true;
}

watch(
  () => props.vocabulary,
  (newValue) => {
    if (newValue.name) {
      tableKey.value += 1;
      sortedName.value = [];
      canSort.value = true;
      dirty.value = false;
      rows.value = props.vocabulary.terms || [];
    }
  }
);
</script>
