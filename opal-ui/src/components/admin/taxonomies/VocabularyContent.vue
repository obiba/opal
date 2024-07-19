<template>
  <div class="text-h5">
    <q-icon name="sell" size="sm" class="q-mb-xs"></q-icon><span class="on-right">{{ vocabulary.name }}</span>
    <q-btn outline color="secondary" icon="edit" size="sm" @click="onEditVocabulary" class="on-right"></q-btn>
    <q-btn outline color="red" icon="delete" size="sm" @click="onDelete" class="on-right"></q-btn>
  </div>
  <div class="q-gutter-md q-mt-md q-mb-md">
    <fields-list class="col-6" :items="properties" :dbobject="vocabulary" />
  </div>

  <div class="text-h6 q-mb-none q-mt-lg">{{ $t('terms') }}</div>
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
    <template v-slot:top-left>
      <div class="q-gutter-sm">
        <q-btn no-caps color="primary " icon="add" size="sm" :label="$t('add')" @click="onAddTerm" />
        <template v-if="dirty">
          <q-btn no-caps color="primary" icon="check" size="sm" :label="$t('apply')" @click="onApply" />
          <q-btn no-caps color="secondary" icon="close" size="sm" :label="$t('reset')" @click="onResetSort" />
        </template>
      </div>
    </template>
    <template v-slot:top-right>
      <q-input dense clearable debounce="400" color="primary" v-model="filter">
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
            rounded
            dense
            flat
            size="sm"
            color="secondary"
            :title="$t('edit')"
            :icon="toolsVisible[props.row.name] ? 'edit' : 'none'"
            class="q-ml-xs"
            @click="onAddTerm(props.row)"
          />
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
    <template v-slot:body-cell-title="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <template v-for="locale in locales" :key="locale">
          <div v-if="props.value" class="row no-wrap q-py-xs">
            <div class="col-auto">
              <q-badge v-if="locale" color="grey-6" :label="locale" class="on-left q-mr-sm" />
            </div>
            <div class="col q-ml-none">{{ taxonomiesStore.getLabel(props.value, locale) }}</div>
          </div>
        </template>
      </q-td>
    </template>
    <template v-slot:body-cell-description="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <template v-for="locale in locales" :key="locale">
          <div v-if="props.value" class="row no-wrap q-py-xs">
            <div class="col-auto">
              <q-badge v-if="locale" color="grey-6" :label="locale" class="on-left q-mr-sm" />
            </div>
            <div class="col q-ml-none">{{ taxonomiesStore.getLabel(props.value, locale) }}</div>
          </div>
        </template>
      </q-td>
    </template>
    <template v-slot:body-cell-keywords="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <template v-for="locale in locales" :key="locale">
          <div v-if="props.value" class="row no-wrap q-py-xs">
            <div class="col-auto">
              <code class="text-secondary q-my-xs">{{ locale }}</code>
            </div>
            <div class="col q-ml-sm">{{ taxonomiesStore.getLabel(props.value, locale) }}</div>
          </div>
        </template>
      </q-td>
    </template>
  </q-table>

  <!-- Dialogs -->

  <confirm-dialog
    v-model="showDelete"
    :title="$t('delete')"
    :text="$t('delete_vocabulary_confirm', { taxonomy: taxonomy, vocabulary: vocabulary.name })"
    @confirm="doDelete"
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

<script lang="ts">
export default defineComponent({
  name: 'VocabularyContent',
});
</script>

<script setup lang="ts">
import { VocabularyDto, TermDto } from 'src/models/Opal';
import useTaxonomyEntityContent from 'src/components/admin/taxonomies/TaxonomyEntityContent';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import AddVocabularyDialog from 'src//components/admin/taxonomies/AddVocabularyDialog.vue';
import AddTermDialog from 'src//components/admin/taxonomies/AddTermDialog.vue';
import FieldsList, { FieldItem } from 'src/components/FieldsList.vue';
import { locales } from 'boot/i18n';
import { notifyError } from 'src/utils/notify';

interface Props {
  taxonomy: string;
  vocabulary: VocabularyDto;
}

const emit = defineEmits(['update', 'refresh']);
const props = defineProps<Props>();
const { t } = useI18n({ useScope: 'global' });
const router = useRouter();
const tableKey = ref(0);
const showDelete = ref(false);
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
  applySort,
  onOverRow,
  onLeaveRow,
  onMoveUp,
  onMoveDown,
  generateLocaleRows,
  customSort,
  onFilter,
} = useTaxonomyEntityContent<TermDto>(() => props.vocabulary, 'terms');

const properties: FieldItem<VocabularyDto>[] = [
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
    align: 'left',
    field: 'name',
    format: (val: string) => val,
    sortable: true,
    headerStyle: 'width: 20%; white-space: normal;',
    style: 'width: 20%; white-space: normal;',
  },
  {
    name: 'title',
    label: t('title'),
    align: 'left',
    field: 'title',
    headerStyle: 'width: 20%; white-space: normal;',
    style: 'width: 20%; white-space: normal;',
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
    name: 'keywords',
    label: t('keywords'),
    align: 'left',
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
    router.replace(`/admin/taxonomies/${props.taxonomy}`);
  } catch (error) {
    notifyError(error);
  }
}

// Handlers

function onEditVocabulary() {
  showEditVocabulary.value = true;
}

function onClose() {
  showEditVocabulary.value = false;
}

function onVocabularyUpdated(updated: VocabularyDto, oldName?: string) {
  emit('refresh', updated.name !== oldName ? updated.name : null);
}

function onAddTerm(term: TermDto) {
  showAddTerm.value = true;
  newTerm.value = term;
}

function onTermUpdated() {
  showAddTerm.value = false;
  newTerm.value = null;
  emit('refresh');
}

async function onDelete() {
  showDelete.value = true;
}

watch(
  () => props.vocabulary,
  (newValue) => {
    if (!!newValue.name) {
      console.log('WATCH Vocabulary name changed');
      tableKey.value += 1;
      sortedName.value = [];
      canSort.value = true;
      dirty.value = false;
      rows.value = props.vocabulary.terms || [];
    }
  }
);
</script>
