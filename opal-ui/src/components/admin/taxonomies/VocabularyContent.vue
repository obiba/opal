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
    :rows="rows"
    :columns="columns"
    row-key="name"
    :pagination="initialPagination"
    :hide-pagination="rows.length <= initialPagination.rowsPerPage"
  >
    <template v-slot:body-cell-license="props">
      <q-td :props="props">
        {{ props.col.format(props.value) }}
      </q-td>
    </template>
    <template v-slot:body-cell-title="props">
      <q-td :props="props">
        <template v-for="locale in locales" :key="locale">
          <div class="q-py-xs">
            <code class="text-secondary q-my-xs">{{ locale }}</code> {{ taxonomiesStore.getLabel(props.value, locale) }}
          </div>
        </template>
      </q-td>
    </template>
    <template v-slot:body-cell-description="props">
      <q-td :props="props">
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
import { VocabularyDto, LocaleTextDto } from 'src/models/Opal';
import FieldsList, { FieldItem } from 'src/components/FieldsList.vue';
import { locales } from 'boot/i18n';

// import ConfirmDialog from 'src/components/ConfirmDialog.vue';

interface Props {
  vocabulary: VocabularyDto;
}

const taxonomiesStore = useTaxonomiesStore();
const props = defineProps<Props>();
const { t } = useI18n({ useScope: 'global' });
const initialPagination = ref({
  sortBy: 'name',
  descending: false,
  page: 1,
  rowsPerPage: 10,
  minRowsForPagination: 10,
});
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
    html: (val) => val.repeatable ? t('yes') : t('no'),
  },
];

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

const rows = computed(() => props.vocabulary.terms || []);
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

</script>
