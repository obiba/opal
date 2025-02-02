<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('id_mappings.import_identifiers_mapping') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-select
            v-model="mappingName"
            use-input
            use-chips
            input-debounce="0"
            :label="t('name') + ' *'"
            :hint="t('id_mappings.mapping_name_hint')"
            :options="filterOptions"
            @new-value="addName"
            @filter="filterFn"
            lazy-rules
            :rules="[validateRequiredField('name')]"
          ></q-select>

          <q-card flat class="q-mt-lg">
            <q-card-section class="q-px-none">
              <div class="row items-center justify-center">
                <div class="col">
                  <q-input
                    v-model="systemIdentifiers"
                    dense
                    rows="10"
                    type="textarea"
                    :label="t('id_mappings.system_identifiers') + ' *'"
                    class="q-mb-md"
                    lazy-rules
                    :rules="[validateRequiredField('identifiers')]"
                  >
                  </q-input>
                </div>
                <div class="col-2 text-center">
                  <q-icon name="sync_alt" size="sm" />
                </div>
                <div class="col">
                  <q-input
                    v-model="mappedIdentifiers"
                    dense
                    rows="10"
                    type="textarea"
                    :label="t('id_mappings.mapped_identifiers') + ' *'"
                    class="q-mb-md"
                    lazy-rules
                    :rules="[validateRequiredField('identifiers')]"
                  >
                  </q-input>
                </div>
              </div>
            </q-card-section>
          </q-card>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('add')" type="submit" color="primary" @click="onImport" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { TableDto, VariableDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  identifier: TableDto;
}

const { t } = useI18n();
const identifiersStore = useIdentifiersStore();
const formRef = ref();
const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'update']);
const showDialog = ref(props.modelValue);
const mappingName = ref<string | null>(null);
const systemIdentifiers = ref('');
const mappedIdentifiers = ref('');
let nameFilterOptions = Array<string>();
const filterOptions = ref(Array<string>());

// Validation rules
const validateRequiredField = (id: string) => (val: string) =>
  (val && val.trim().length > 0) || t(`validation.${id}_required`);

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function addName(val: string, done: any) {
  if (val.trim().length > 0) {
    if (nameFilterOptions.includes(val) === false) {
      nameFilterOptions.push(val);
      filterOptions.value = [...nameFilterOptions];
    }

    mappingName.value = val;
    done(null);
  }
}

function createCsvContent() {
  const lines = [];
  const systemIds = systemIdentifiers.value.split('\n');
  const mappedIds = mappedIdentifiers.value.split('\n');
  for (let i = 0; i < systemIds.length; i++) {
    lines.push(`${systemIds[i]},${mappedIds[i]}`);
  }
  return lines.join('\n');
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function filterFn(val: string, update: any) {
  update(() => {
    if (val.trim().length === 0) {
      filterOptions.value = [...nameFilterOptions];
    } else {
      const needle = val.toLowerCase();
      filterOptions.value = filterOptions.value.filter((v) => v.toLowerCase().indexOf(needle) > -1);
    }
  });
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      identifiersStore.initMappings(props.identifier.name).then(() => {
        nameFilterOptions = identifiersStore.mappings.map((m) => m.name);
        filterOptions.value = [...nameFilterOptions];
      });
      showDialog.value = value;
    }
  }
);

function onHide() {
  mappingName.value = null;
  nameFilterOptions = [];
  filterOptions.value = [];
  systemIdentifiers.value = '';
  mappedIdentifiers.value = '';
  showDialog.value = false;
  emit('update:modelValue', false);
}

async function onImport() {
  const valid = await formRef.value.validate();
  if (valid) {
    const newMapping = {
      name: mappingName.value,
      isNewVariable: true,
      entityType: props.identifier.entityType,
      isRepeatable: false,
      valueType: 'text',
    } as VariableDto;
    identifiersStore
      .addMapping(props.identifier.name, newMapping)
      .then(() => {
        identifiersStore
          .importMappingSystemIdentifiers(props.identifier.entityType, mappingName.value ?? '', createCsvContent())
          .then(() => {
            emit('update');
            onHide();
          })
          .catch(notifyError);
      })
      .catch(notifyError);
  }
}
</script>
