<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ $t('id_mappings.generate_identifiers') }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section class="q-gutter-y-md">
        <q-banner v-if="!hasSystemIdentifiers" rounded class="bg-warning">
          {{ $t('id_mappings.generate_identifiers_warning') }}
        </q-banner>
        <div v-else class="text-help">
          {{ $t('id_mappings.generate_identifiers_info', { count: mappingIdentifiersCount }) }}
        </div>

        <q-form ref="formRef" class="q-gutter-md" persistent>
          <div>{{ $t('id_mappings.sample_identifier', { sampleIdentifier }) }}</div>
          <q-input
            v-model.number="options.size"
            dense
            type="text"
            :label="$t('id_mappings.identifier_size')"
            :hint="
              $t('id_mappings.identifier_size_hint', { min: MIN_IDENTIFIER_SIZE, max: MAX_IDENTIFIER_SIZE }) + ' *'
            "
            :disable="!hasSystemIdentifiers"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRange]"
          >
          </q-input>

          <q-checkbox
            :disable="!hasSystemIdentifiers"
            v-model="useChecksum"
            :label="$t('id_mappings.with_checksum')"
            :hint="$t('id_mappings_hint.with_checksum')"
            dense
          />

          <q-input
            v-model.number="options.prefix"
            dense
            type="text"
            :label="$t('id_mappings.identifier_prefix')"
            :disable="!hasSystemIdentifiers"
            class="q-mb-md"
            lazy-rules
          >
          </q-input>

          <q-checkbox
            :disable="!hasSystemIdentifiers"
            v-model="useChecksum"
            :label="$t('id_mappings.leading_zero')"
            dense
          />

        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="$t('add')" type="submit" color="primary" @click="onAddMapping" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'GenerateMappingIdentifiersDialog',
});
</script>
<script setup lang="ts">
import { TableDto, VariableDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';
import { generateIdentifier } from 'src/utils/identifiers';

interface DialogProps {
  modelValue: boolean;
  identifier: TableDto;
  mapping: VariableDto;
}

interface GenerateIdentifiersOptions {
  prefix: string | '';
  size: number;
  zeros: boolean | false;
  checksum: boolean | false;
}

const MIN_IDENTIFIER_SIZE = 5;
const MAX_IDENTIFIER_SIZE = 20;
const { t } = useI18n();
const identifiersStore = useIdentifiersStore();
const formRef = ref();
const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'update']);
const showDialog = ref(props.modelValue);
const newIdentifier = ref<TableDto>({} as TableDto);
const mappingIdentifiersCount = ref(0);
const sampleIdentifier = ref('');
const options = ref({} as GenerateIdentifiersOptions);
const hasSystemIdentifiers = computed(() => mappingIdentifiersCount.value > 0);
const useLeadingZeros = computed({
  get: () => options.value.zeros,
  set: (val: boolean) => {
    if (val && useChecksum.value) useChecksum.value = false;
    options.value.zeros = val;
  },
});
const useChecksum = computed({
  get: () => options.value.checksum,
  set: (val: boolean) => {
    options.value.checksum = val;
    if (val && useLeadingZeros.value) useLeadingZeros.value = false;
  },
});

// Validation rules
// const validateRequiredField = (val: string) => (val && val.trim().length > 0) || t('validation.entity_type_required');
const validateRange = (val: number) =>
  !!!val ||
  (val >= MIN_IDENTIFIER_SIZE && val <= MAX_IDENTIFIER_SIZE) ||
  t('validation.range', { min: MIN_IDENTIFIER_SIZE, max: MAX_IDENTIFIER_SIZE });

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      sampleIdentifier.value = generateIdentifier(4, true, false, 'Test-');
      identifiersStore.getMappingIdentifiersCount(props.identifier.name, props.mapping.name).then((count) => {
        mappingIdentifiersCount.value = count;
      });
      options.value = {
        prefix: `${props.mapping.name}-`,
        size: 10,
        zeros: false,
        checksum: false,
      } as GenerateIdentifiersOptions;
      showDialog.value = value;
    }
  }
);

function onHide() {
  newIdentifier.value = {} as TableDto;
  showDialog.value = false;
  emit('update:modelValue', false);
}

async function onAddMapping() {
  const valid = await formRef.value.validate();
  if (valid) {
    identifiersStore
      .addIdentifier(newIdentifier.value)
      .then(() => {
        emit('update');
        onHide();
      })
      .catch(notifyError);
  }
}
</script>
