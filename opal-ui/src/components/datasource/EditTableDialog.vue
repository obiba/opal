<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t(isView ? 'edit_view' : 'edit_table') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-input v-model="name" dense type="text" :label="t('name')" style="min-width: 300px" class="q-mb-md">
        </q-input>
        <div v-if="isView">
          <q-select
            v-model="fromSelection"
            dense
            use-input
            :options="fromTables"
            input-debounce="0"
            @filter="filterFn"
            :label="t('from_tables')"
            :hint="t('from_tables_select_hint')"
            style="min-width: 300px"
            class="q-mb-md"
            @update:model-value="onAdd"
          />
          <q-list separator bordered>
            <q-item v-for="tbl in from" :key="tbl">
              <q-item-section>
                <q-item-label class="text-caption text-bold">{{ tbl }}</q-item-label>
                <q-checkbox v-model="innerFrom[tbl]" dense size="sm" :label="t('inner_join')" class="q-mt-sm text-caption" />
              </q-item-section>
              <q-item-section side>
                <table>
                  <tbody>
                    <tr>
                      <td>
                        <q-btn flat dense round icon="arrow_upward" @click="onUp(tbl)" size="sm" />
                      </td>
                      <td>
                        <q-btn flat dense round icon="arrow_downward" @click="onDown(tbl)" size="sm" />
                      </td>
                      <td>
                        <q-btn flat dense round icon="delete" @click="onRemove(tbl)" size="sm" />
                      </td>
                    </tr>
                  </tbody>
                </table>
              </q-item-section>
            </q-item>
          </q-list>
          <div class="text-hint q-mt-sm">
            {{ t('from_tables_hint') }}
          </div>
        </div>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn
          flat
          :label="t('save')"
          color="primary"
          :disable="!isTableNameValid"
          @click="onSaveTable"
          v-close-popup
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { TableDto, ViewDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  table: TableDto;
  view?: ViewDto;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'update:table', 'update:view']);

const { t } = useI18n();
const datasourceStore = useDatasourceStore();

const showDialog = ref(props.modelValue);
const name = ref(props.table.name);
const from = ref<string[]>(props.view?.from || []);
const innerFrom = ref<{[key: string]: boolean}>({});
const fromSelection = ref<string>();
const fromAllTables = ref<string[]>([]);
const allTables = ref<string[]>([]);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      name.value = props.table.name;
      innerFrom.value = {};
      from.value = props.view?.from || [];
      props.view?.from?.forEach((tbl) => (innerFrom.value[tbl] = false));
      props.view?.innerFrom?.forEach((tbl) => (innerFrom.value[tbl] = true));
      if (props.view) {
        datasourceStore
          .getAllTables(props.table.entityType)
          .then(
            (tables) =>
              (allTables.value = tables
                .map((table) => `${table.datasourceName}.${table.name}`)
                .filter((tbl) => tbl !== `${props.table.datasourceName}.${props.table.name}`))
          )
          .then(() => (fromAllTables.value = allTables.value));
      }
    }
    showDialog.value = value;
  }
);

const fromTables = computed(() => fromAllTables.value.filter((tbl) => !from.value.includes(tbl)));

function onHide() {
  emit('update:modelValue', false);
}

const isTableNameValid = computed(
  () => datasourceStore.isNewTableNameValid(name.value) || props.table.name === name.value
);

const isView = computed(() => props.view && props.view.name);

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function filterFn(val: string, update: any) {
  if (val === '') {
    update(() => {
      fromAllTables.value = allTables.value;
    });
    return;
  }

  update(() => {
    const needle = val.toLowerCase();
    fromAllTables.value = allTables.value.filter((v) => v.toLowerCase().indexOf(needle) > -1);
  });
}

function onSaveTable() {
  if (props.view && isView.value) {
    const newInner = Object.entries(innerFrom.value).filter(([, value]) => value).map(([key]) => key);
    const updatedView = { ...props.view, name: name.value, from: from.value as string[], innerFrom: newInner };
    datasourceStore
      .updateView(
        datasourceStore.datasource.name,
        props.view.name || name.value,
        updatedView,
        `Editing ${props.view.name}`
      )
      .then(() => emit('update:view', updatedView))
      .catch((err) => {
        notifyError(err);
      });
  } else {
    const updatedTable = { ...props.table, name: name.value };
    datasourceStore
      .updateTable(props.table, updatedTable)
      .then(() => emit('update:table', updatedTable))
      .catch((err) => {
        notifyError(err);
      });
  }
}

function onAdd() {
  if (fromSelection.value && !from.value.includes(fromSelection.value)) {
    from.value.push(fromSelection.value);
    innerFrom.value[fromSelection.value] = false;
  }
  fromSelection.value = undefined;
}

function onRemove(tbl: string) {
  from.value = from.value.filter((t) => t !== tbl);
  innerFrom.value = Object.fromEntries(Object.entries(innerFrom.value).filter(([key]) => key !== tbl));
}

function onUp(tbl: string) {
  const index = from.value.indexOf(tbl);
  if (index > 0) {
    from.value.splice(index, 1);
    from.value.splice(index - 1, 0, tbl);
  }
}

function onDown(tbl: string) {
  const index = from.value.indexOf(tbl);
  if (index < from.value.length - 1) {
    from.value.splice(index, 1);
    from.value.splice(index + 1, 0, tbl);
  }
}
</script>
