<template>
  <div v-if="taxonomyName">
    <div class="text-h5">
      <span>{{ taxonomy.name }}</span>
      <q-btn v-if="taxonomiesStore.canEdit" outline color="primary" icon="download" size="sm" @click="onDownload" class="on-right"></q-btn>
      <q-btn v-if="taxonomiesStore.canEdit" outline color="secondary" icon="edit" size="sm" @click="onEditTaxonomy" class="on-right"></q-btn>
      <q-btn v-if="taxonomiesStore.canEdit" outline color="red" icon="delete" size="sm" @click="onDelete" class="on-right"></q-btn>
    </div>

    <div class="q-gutter-md q-mt-md q-mb-md">
      <fields-list class="col-6" :items="properties" :dbobject="taxonomy" />
    </div>

    <div class="text-h6 q-mb-none q-mt-lg">{{ t('vocabularies') }}</div>
    <q-table
      flat
      :key="tableKey"
      :rows="rows"
      :columns="columns"
      :sort-method="customSort"
      @update:sort="onSortUpdate"
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
          <q-btn no-caps color="primary " icon="add" size="sm" :label="t('add')" @click="onAddVocabulary" />
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
          :placeholder="t('taxonomy.filter_vocabulary')"
        >
          <template v-slot:append>
            <q-icon name="search" />
          </template>
        </q-input>
      </template>
      <template v-slot:body-cell-name="props">
        <q-td :props="props" class="items-start" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <router-link :to="`/taxonomy/${taxonomy.name}/vocabulary/${props.value}`">
            {{ props.value }}
          </router-link>
          <div class="float-right" v-if="taxonomiesStore.canEdit && !hasFilter">
            <q-btn
              v-show="props.rowIndex > 0"
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
              v-show="props.rowIndex < rows.length - 1"
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
      <template v-slot:body-cell-terms="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          {{ props.value }}
        </q-td>
      </template>
    </q-table>

    <div v-if="taxonomiesStore.canEdit" class="text-h6 q-mb-none q-mt-lg">{{ t('taxonomy.change_history') }}</div>
    <taxonomy-git-history v-if="taxonomiesStore.canEdit" :taxonomy-name="taxonomyName" @restore="onGitRestored" />

    <!-- Dialogs -->
    <confirm-dialog
      v-model="showDelete"
      :title="t('delete')"
      :text="t('delete_taxonomy_confirm', { taxonomy: taxonomy.name })"
      @confirm="doDelete"
    />

    <add-taxonomy-dialog
      v-model="showEditTaxonomy"
      :taxonomy="taxonomy"
      @update:modelValue="onClose"
      @updated="onTaxonomyUpdated"
    />

    <add-vocabulary-dialog
      v-model="showAddVocabulary"
      :taxonomy="taxonomyName"
      :vocabulary="null"
      @update:modelValue="onVocabularyAdded"
    />
  </div>
</template>

<script setup lang="ts">
import type { TaxonomyDto, VocabularyDto } from 'src/models/Opal';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import AddTaxonomyDialog from 'src/components/taxonomies/AddTaxonomyDialog.vue';
import AddVocabularyDialog from 'src/components/taxonomies/AddVocabularyDialog.vue';
import FieldsList, { type FieldItem } from 'src/components/FieldsList.vue';
import useTaxonomyEntityContent from 'src/components/taxonomies/TaxonomyEntityContent';
import TaxonomyGitHistory from 'src/components/taxonomies/TaxonomyGitHistory.vue';
import { getCreativeCommonsLicenseAnchor } from 'src/utils/taxonomies';
import { notifyError } from 'src/utils/notify';
import { DefaultAlignment } from 'src/components/models';

interface Props {
  taxonomy: TaxonomyDto;
}

const emit = defineEmits(['update', 'refresh']);
const props = defineProps<Props>();
const router = useRouter();
const { t, locale } = useI18n({ useScope: 'global' });
const showDelete = ref(false);
const showEditTaxonomy = ref(false);
const showAddVocabulary = ref(false);
const tableKey = ref(0);

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
} = useTaxonomyEntityContent<VocabularyDto>(() => props.taxonomy, 'vocabularies');

const properties: FieldItem[] = [
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
    html: (val) => getCreativeCommonsLicenseAnchor(val, locale.value),
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

const taxonomyName = computed(() => props.taxonomy.name || '');
const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: DefaultAlignment,
    field: 'name',
    format: (val: string) => val,
    sortable: true,
    style: 'width: 20%',
  },
  {
    name: 'title',
    label: t('title'),
    align: DefaultAlignment,
    field: 'title',
    headerStyle: 'width: 30%; white-space: normal;',
    style: 'width: 30%; white-space: normal;',
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
    name: 'terms',
    label: t('terms'),
    align: DefaultAlignment,
    field: (row: VocabularyDto) => (row.terms || []).length,
  },
]);

// Handlers

async function doDelete() {
  showDelete.value = false;
  try {
    await taxonomiesStore.deleteTaxonomy(props.taxonomy);
    await taxonomiesStore.refreshSummaries();
    router.replace('/taxonomies');
  } catch (error) {
    notifyError(error);
  }
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

function onEditTaxonomy() {
  showEditTaxonomy.value = true;
}

function onClose() {
  showEditTaxonomy.value = false;
}

function onGitRestored() {
  emit('refresh');
}

function onTaxonomyUpdated(updated: TaxonomyDto, oldName?: string) {
  emit('refresh', updated.name !== oldName ? updated.name : null);
}

function onAddVocabulary() {
  showAddVocabulary.value = true;
}

function onVocabularyAdded() {
  showAddVocabulary.value = false;
  emit('refresh');
}

watch(
  () => props.taxonomy,
  (newValue) => {
    if (newValue.name) {
      tableKey.value += 1;
      sortedName.value = [];
      canSort.value = true;
      dirty.value = false;
      rows.value = props.taxonomy.vocabularies || [];
    }
  }
);
</script>
