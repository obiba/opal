<template>
  <div class="text-h6 q-mb-md">{{ $t('properties') }}</div>

  <fields-list v-if="!!vocabulary" :items="vocabularyProperties" :dbobject="vocabulary" />
  <fields-list v-else :items="taxonomyProperties" :dbobject="taxonomy" />

  <!--
  -->

  <div class="text-h6 q-mb-md q-mt-lg">{{ tableTitle }}</div>
  <q-table
    flat
    :rows="rows"
    :columns="columns"
    row-key="name"
    :pagination="initialPagination"
    :hide-pagination="rows.length <= initialPagination.rowsPerPage"
  >
    <template v-slot:body-cell-title="props">
      <q-td :props="props">
        <template v-for="locale in locales" :key="locale">
          <div class="q-py-xs">
            <code class="text-secondary q-my-xs">{{ locale }}</code> {{ taxonomiesStore.getLabel(props.value, locale) }}
          </div>
        </template>
      </q-td>
    </template>
  </q-table>
</template>

<script lang="ts">
export default defineComponent({
  name: 'TaxonomyContent',
});
</script>

<script setup lang="ts">
import { onMounted } from 'vue';
import { TaxonomyDto, VocabularyDto, LocaleTextDto } from 'src/models/Opal';
import FieldsList, { FieldItem } from 'src/components/FieldsList.vue';
import { locales } from 'boot/i18n';

// import ConfirmDialog from 'src/components/ConfirmDialog.vue';

enum ContentMode {
  TAXONOMY,
  VOCABULARY,
}

interface Props {
  taxonomy: TaxonomyDto;
  vocabulary: VocabularyDto | null;
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
const taxonomyProperties: FieldItem<TaxonomyDto>[] = [
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

const vocabularyProperties: FieldItem<TaxonomyDto>[] = [
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
];

function generateLocaleRows(val: LocaleTextDto[]) {
  if (val) {
    const rows = locales
      .map(
        (locale) =>
          `<div class="q-py-xs"><code class="text-secondary q-my-xs">${locale}</code> ${taxonomiesStore.getLabel(
            val,
            locale
          )}</div>`
      )
      .join('');
    return rows;
  }

  return '';
}

const rows = computed(() => (!!props.vocabulary ? props.vocabulary.terms || [] : props.taxonomy.vocabularies || []));
const tableTitle = computed(() => (!!props.vocabulary ? t('Terms') : t('Vocabularies')));
const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'name',
    format: (val: string) => val,
    sortable: true,
    style: 'width: 25%',
  },
  {
    name: 'title',
    label: t('title'),
    align: 'left',
    field: 'title',
  },
  // {
  //   name: 'description',
  //   label: t('description'),
  //   align: 'left',
  //   field: 'authenticationType',
  //   format: (val: LocaleTextDto[]) => generateLocaleRows(val),
  //   html: true,
  // },
]);

onMounted(() => {
  console.log('TaxonomyContent mounted', props.taxonomy);
});
</script>
