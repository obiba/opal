import { VocabularyDto, TermDto, LocaleTextDto } from 'src/models/Opal';
import { locales } from 'boot/i18n';
/**
 * Composable to handle common variables and functions for taxonomy contents
 *
 * @param getProps returns the proper component props field
 * @returns common variables and functions for taxonomy contents
 */
export default function useEntityContent<TYPE extends VocabularyDto | TermDto>(getProps: any, collectionName: string) {
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

  // Functions

  function customSort(rows: TYPE[], sortBy: string, descending: string) {
    if (!canSort || !sortBy) return rows;

    const data = rows;
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

    nextTick(() => {
      // Wait so the default sort is not applied right after the move
      canSort.value = true;
    });
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

    nextTick(() => {
      // Wait so the default sort is not applied right after the move
      canSort.value = true;
    });
  }

  return {
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
  };
}
