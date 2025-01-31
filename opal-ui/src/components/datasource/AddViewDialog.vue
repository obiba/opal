<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card>
      <q-card-section>
        <div class="text-h6">{{ t('add_view') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-input v-model="name" dense type="text" :label="t('name')" style="min-width: 300px" class="q-mb-md">
        </q-input>
        <q-select
          v-model="from"
          dense
          multiple
          use-chips
          use-input
          :options="fromTables"
          input-debounce="0"
          @filter="filterFn"
          :label="t('from_tables')"
          :hint="t('from_tables_select_hint')"
          style="min-width: 300px"
          class="q-mb-md"
        />
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('add')" color="primary" :disable="!isTableNameValid" @click="onAddTable" v-close-popup />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);

const { t } = useI18n();
const datasourceStore = useDatasourceStore();

const showDialog = ref(props.modelValue);
const name = ref<string>('');
const from = ref<string[]>([]);
const fromTables = ref<string[]>([]);
const allTables = ref<string[]>([]);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      name.value = '';
      datasourceStore
        .getAllTables(undefined)
        .then((tables) => (allTables.value = tables.map((table) => `${table.datasourceName}.${table.name}`)))
        .then(() => (fromTables.value = allTables.value));
    }
    showDialog.value = value;
  }
);

function onHide() {
  emit('update:modelValue', false);
}

const isTableNameValid = computed(() => datasourceStore.isNewTableNameValid(name.value));

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

function onAddTable() {
  if (!isTableNameValid.value) {
    return;
  }
  datasourceStore
    .addVariablesView(datasourceStore.datasource.name, name.value, from.value, [])
    .then(() => datasourceStore.initDatasourceTables(datasourceStore.datasource.name))
    .catch((err) => {
      notifyError(err);
    });
}
</script>
