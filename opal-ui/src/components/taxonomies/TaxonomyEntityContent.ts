import type { VocabularyDto, TermDto, LocaleTextDto } from 'src/models/Opal';
import { locales } from 'boot/i18n';
import { flattenObjectToString } from 'src/utils/strings';
/**
 * Composable to handle common variables and functions for taxonomy contents
 *
 * @param getProps returns the proper component props field
 * @returns common variables and functions for taxonomy contents
 */
export default function useTaxonomyEntityContent<TYPE extends VocabularyDto | TermDto>(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  getProps: any,
  collectionName: string
) {
  const canSort = ref(true);
  const dirty = ref(false);
  const sortedName = ref<string[]>([]);
  const taxonomiesStore = useTaxonomiesStore();
  const rows = ref<TYPE[]>([]);
  const toolsVisible = ref<{ [key: string]: boolean }>({});
  const initialPagination = ref({
    descending: false,
    page: 1,
    rowsPerPage: 10,
    minRowsForPagination: 10,
  });
  const filter = ref('');
  const hasFilter = computed(() => (filter.value || '').length > 0);

  // Functions

  function customSort(rows: readonly TYPE[], sortBy: string, descending: boolean) {
    if (!canSort.value || !sortBy) return rows;

    const data = [...rows];
    dirty.value = true;

    data.sort((a: TYPE, b: TYPE): number => (descending ? b.name.localeCompare(a.name) : a.name.localeCompare(b.name)));

    sortedName.value = data.map((row) => row.name);

    return data;
  }

  function generateLocaleRows(val: LocaleTextDto[]) {
    if (val) {
      const validLocales = locales.filter((locale) => val.find((v) => v.locale === locale));

      const rows = validLocales
        .map(
          (locale) =>
            `
            <div class="row no-wrap q-py-xs">
              <div class="col-auto"><span class="q-badge bg-grey-6 flex inline items-center no-wrap" >${locale}</span></div>
              <div class="col q-ml-sm">${taxonomiesStore.getLabel(val, locale)}</div>
            </div>
            `
        )
        .join('');
      return rows;
    }

    return '';
  }

  function applySort() {
    const clone = JSON.parse(JSON.stringify(getProps()));
    clone[collectionName] = [...rows.value]; // to be sure all changes are applied

    const sortFunction = (a: TYPE, b: TYPE) => {
      const aIndex = sortedName.value.findIndex((name) => name === a.name);
      const bIndex = sortedName.value.findIndex((name) => name === b.name);
      return aIndex - bIndex;
    };

    clone[collectionName].sort(sortFunction);
    sortedName.value = [];

    return clone;
  }

  // Handlers

  function onSortUpdate() {
    canSort.value = true;
  }

  function onFilter() {
    if (filter.value.length === 0) {
      return rows.value;
    }
    const query = filter && filter.value.length > 0 ? filter.value.toLowerCase() : '';
    const result = rows.value.filter((row) => {
      const rowString = `${row.name.toLowerCase()} ${flattenObjectToString(row.title || {})} ${flattenObjectToString(
        row.description || {}
      )}`;
      return rowString.includes(query);
    });

    return result;
  }

  function onOverRow(row: TYPE) {
    toolsVisible.value[row.name] = true;
  }

  function onLeaveRow(row: TYPE) {
    toolsVisible.value[row.name] = false;
  }

  function onMoveUp(name: string) {
    dirty.value = true;
    const clone = sortedName.value.length > 0 ? applySort()[collectionName] : JSON.parse(JSON.stringify(rows.value));
    const index = clone.findIndex((row: TYPE) => row.name === name);

    if (index > 0) {
      const temp = clone[index - 1];
      clone[index - 1] = clone[index];
      clone[index] = temp;
      canSort.value = false;
      rows.value = clone;
    }
  }

  function onMoveDown(name: string) {
    dirty.value = true;
    const clone = sortedName.value.length > 0 ? applySort()[collectionName] : JSON.parse(JSON.stringify(rows.value));
    const index = clone.findIndex((row: TYPE) => row.name === name);

    if (index < rows.value.length - 1) {
      const temp = clone[index + 1];
      clone[index + 1] = clone[index];
      clone[index] = temp;
      canSort.value = false;
      rows.value = clone;
    }
  }

  return {
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
  };
}
