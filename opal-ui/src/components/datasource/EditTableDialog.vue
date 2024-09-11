<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ $t(isView ? 'edit_view' : 'edit_table') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-input v-model="name" dense type="text" :label="$t('name')" style="min-width: 300px" class="q-mb-md">
        </q-input>
        <div v-if="isView">
          <q-select
            v-model="from"
            dense
            multiple
            use-chips
            use-input
            :options="fromTables"
            input-debounce="0"
            @filter="filterFn"
            :label="$t('from_tables')"
            style="min-width: 300px"
            class="q-mb-md"
          />
        </div>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
        <q-btn
          flat
          :label="$t('save')"
          color="primary"
          :disable="!isTableNameValid"
          @click="onSaveTable"
          v-close-popup
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'EditTableDialog',
});
</script>
<script setup lang="ts">
import { TableDto, ViewDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  table: TableDto;
  view?: ViewDto;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'update:table', 'update:view']);

const datasourceStore = useDatasourceStore();

const showDialog = ref(props.modelValue);
const name = ref(props.table.name);
const from = ref(props.view?.from);
const fromTables = ref<string[]>([]);
const allTables = ref<string[]>([]);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      name.value = props.table.name;
      from.value = props.view?.from;
      if (props.view) {
        datasourceStore
          .getAllTables(props.table.entityType)
          .then(
            (tables) =>
              (allTables.value = tables
                .map((table) => `${table.datasourceName}.${table.name}`)
                .filter((tbl) => tbl !== `${props.table.datasourceName}.${props.table.name}`))
          )
          .then(() => (fromTables.value = allTables.value));
      }
    }
    showDialog.value = value;
  }
);

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
      fromTables.value = allTables.value;
    });
    return;
  }

  update(() => {
    const needle = val.toLowerCase();
    fromTables.value = allTables.value.filter((v) => v.toLowerCase().indexOf(needle) > -1);
  });
}

function onSaveTable() {
  if (props.view && isView.value) {
    const updatedView = { ...props.view, name: name.value, from: from.value as string[] };
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
</script>
