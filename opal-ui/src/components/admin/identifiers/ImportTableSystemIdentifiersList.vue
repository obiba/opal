<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('id_mappings.import_identifiers_table') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-banner v-if="!hasTables" rounded class="bg-negative text-white">
            {{ t('id_mappings.entity_type_no_tables', { entityType: identifier.entityType }) }}
          </q-banner>
          <q-input
            v-model="tableName"
            dense
            :label="t('table')"
            :hint="t('table_identifiers_import_hint')"
            class="q-mb-md"
            debounce="300"
            lazy-rules
            :rules="[validateRequiredField]"
            @update:model-value="onSearchTable"
          >
            <q-menu v-model="showSuggestions" no-focus no-parent-event auto-close>
              <q-list style="min-width: 100px">
                <q-item
                  clickable
                  v-close-popup
                  v-for="(sugg, index) in filterOptions"
                  :key="index"
                  @click="onTableSuggestionSelected(sugg)"
                >
                  <q-item-section>{{ `${sugg.datasourceName}.${sugg.name}` }}</q-item-section>
                </q-item>
              </q-list>
            </q-menu>
          </q-input>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('add')" type="submit" color="primary" :disable="!selectedTable" @click="onImport" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { TableDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  identifier: TableDto;
}

const { t } = useI18n();
const datasourceStore = useDatasourceStore();
const identifiersStore = useIdentifiersStore();
const formRef = ref();
const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'update']);
const showDialog = ref(props.modelValue);
const showSuggestions = ref(false);
const tableName = ref<string | null>(null);
const selectedTable = ref<TableDto | null>(null);
const initialized = ref(false);
const filterOptions = ref([] as TableDto[]);
let identifiersOptions = [] as TableDto[];
const hasTables = ref(true);

const validateRequiredField = (val: TableDto | string) => {
  if (val) {
    const value = typeof val === 'string' ? val : val.name;
    if (value.trim().length > 0) {
      return true;
    }
  }

  return t('validation.table_name_required');
};

function initMappingOptions(tables: TableDto[]) {
  hasTables.value = tables.length > 0;
  if (tables.length > 0) {
    identifiersOptions = [...tables];
    filterOptions.value = [];
  }
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      tableName.value = null;
      datasourceStore
        .getAllTables(props.identifier.entityType)
        .then((response) => initMappingOptions(response))
        .catch((error) => console.error(error));

      showDialog.value = value;
    }
  }
);

function onSearchTable(val: string | number | null) {
  filterOptions.value = [];
  selectedTable.value = null;

  if (!val || (val + '').trim().length < 3) {
    showSuggestions.value = false;
    return;
  }

  const needle = (val + '').toLowerCase();
  filterOptions.value = [...identifiersOptions.filter((v) => v.name.toLowerCase().indexOf(needle) > -1)];

  showSuggestions.value = filterOptions.value.length > 0;
}

function onTableSuggestionSelected(table: TableDto) {
  tableName.value = `${table.datasourceName}.${table.name}`;
  selectedTable.value = table;
  showSuggestions.value = false;
}

function onHide() {
  selectedTable.value = null;
  identifiersOptions = [];
  filterOptions.value = [];
  showDialog.value = false;
  initialized.value = false;
  emit('update:modelValue', false);
}

async function onImport() {
  const valid = await formRef.value.validate();
  if (valid) {
    identifiersStore
      .importTableSystemIdentifiers(selectedTable.value?.datasourceName ?? '', selectedTable.value?.name ?? '')
      .then(() => {
        emit('update');
        onHide();
      })
      .catch(notifyError);
  }
}
</script>
