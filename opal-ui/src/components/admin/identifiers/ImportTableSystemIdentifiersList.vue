<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ $t('id_mappings.import_identifiers_table') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-banner v-if="!hasTables" rounded class="bg-negative text-white">
             {{ $t('id_mappings.entity_type_no_tables', { entityType: identifier.entityType }) }}
          </q-banner>
          <q-select
            v-else
            v-model="selectedTable"
            :options="filterOptions"
            :label="$t('table')"
            :hint="$t('id_mappings.import_table_sys_ids_info')"
            dense
            map-options
            use-chips
            use-input
            input-debounce="0"
            @filter="onFilterFn"
            lazy-rules
            :rules="[validateRequiredField]"
          >
            <template v-slot:option="scope">
              <q-item v-show="!!!scope.opt.value" class="text-help" dense clickable disable :label="scope.opt.label">
                <q-item-section class="q-pa-none">
                  {{ scope.opt.label }}
                </q-item-section>
              </q-item>
              <q-item v-show="!!scope.opt.value" dense clickable v-close-popup @click="selectedTable = scope.opt.value">
                <q-item-section class="q-pl-md">
                  {{ scope.opt.label }}
                </q-item-section>
              </q-item>
            </template>
          </q-select>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="$t('add')" type="submit" color="primary" :disable="!hasTables" @click="onImport" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'ImportTableSystemIdentifiersList',
});
</script>
<script setup lang="ts">
import { TableDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  identifier: TableDto;
}

type GroupOption = { label: string; value: TableDto | undefined };

const { t } = useI18n();
const datasourceStore = useDatasourceStore();
const identifiersStore = useIdentifiersStore();
const formRef = ref();
const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'update']);
const showDialog = ref(props.modelValue);
const selectedTable = ref<TableDto | null>(null);
const initialized = ref(false);
const filterOptions = ref([] as GroupOption[]);
let identifiersOptions = [] as GroupOption[];
const hasTables = computed(() => !initialized.value || filterOptions.value.length > 0);

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
  initialized.value = true;
  if (tables.length > 0) {
    let lastGroup = '';
    tables.forEach((table) => {
      if (!!table.datasourceName && table.datasourceName !== lastGroup) {
        lastGroup = table.datasourceName;
        identifiersOptions.push({ label: lastGroup } as GroupOption);
      }
      identifiersOptions.push({ label: table.name, value: table } as GroupOption);
    });

    filterOptions.value = [...identifiersOptions];
  }
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      datasourceStore
        .getAllTables(props.identifier.entityType)
        .then((response) => initMappingOptions(response))
        .catch((error) => console.error(error));

      showDialog.value = value;
    }
  }
);

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function onFilterFn(val: string, update: any) {
  update(() => {
    if (val.trim().length === 0) {
      filterOptions.value = [...identifiersOptions];
    } else {
      const needle = val.toLowerCase();
      filterOptions.value = [
        ...identifiersOptions.filter((v: GroupOption) => 'label' in v && v.label.toLowerCase().indexOf(needle) > -1),
      ];
    }
  });
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
