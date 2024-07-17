<template>
  <div class="text-h5">
    <q-icon name="sell" size="sm" class="q-mb-xs"></q-icon><span class="on-right">{{ vocabulary.name }}</span>
  </div>
  <div class="q-gutter-md q-mt-md q-mb-md">
    <fields-list class="col-6" :items="properties" :dbobject="vocabulary" />
  </div>

  <div class="text-h6 q-mb-md q-mt-lg">{{ $t('terms') }}</div>
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
        {{ props.value }}
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
    <template v-slot:body-cell-title="props">
      <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
        <template v-for="locale in locales" :key="locale">
          <div class="q-py-xs">
            <code class="text-secondary q-my-xs">{{ locale }}</code> {{ taxonomiesStore.getLabel(props.value, locale) }}
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
  </q-table>
</template>

<script lang="ts">
export default defineComponent({
  name: 'VocabularyContent',
});
</script>

<script setup lang="ts">
import { VocabularyDto, TermDto, LocaleTextDto } from 'src/models/Opal';
import useEntityContent from 'src/components/admin/taxonomies/EntityContent';
import FieldsList, { FieldItem } from 'src/components/FieldsList.vue';
import { locales } from 'boot/i18n';

// import ConfirmDialog from 'src/components/ConfirmDialog.vue';

interface Props {
  vocabulary: VocabularyDto;
}

const emit = defineEmits(['update', 'refresh']);
const props = defineProps<Props>();
const { t } = useI18n({ useScope: 'global' });
const tableKey = ref(0);
const {
  initialPagination,
  toolsVisible,
  canSort,
  sortedName,
  dirty,
  taxonomiesStore,
  rows,
  applySort,
  onOverRow,
  onLeaveRow,
  onMoveUp,
  onMoveDown,
  generateLocaleRows,
  customSort,
} = useEntityContent<TermDto>(() => props.vocabulary, 'terms');

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
    style: 'width: 15%',
  },
  {
    name: 'title',
    label: t('title'),
    align: 'left',
    field: 'title',
  },
  {
    name: 'description',
    label: t('description'),
    align: 'left',
    field: 'description',
    headerStyle: 'width: 60%; white-space: normal;',
    style: 'width: 60%; white-space: normal;',
  },
]);

// Functions

function onApply() {
  emit('update', applySort());
}

function onResetSort() {
  emit('refresh');
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
