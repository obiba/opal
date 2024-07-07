<template>
  <q-dialog v-model="showDialog" @hide="onHide">
      <q-card class="dialog-sm">
        <q-card-section>
          <div class="text-h6">{{ $t('annotate') }}</div>
        </q-card-section>

        <q-separator />

        <q-card-section>
          <div class="q-mb-md box-info">
            <q-icon name="info" size="1.2rem"/>
            <span class="on-right">
              {{ $t('annotate_info', { count: props.variables.length }) }}
            </span>
          </div>

          <q-select
            v-model="taxonomyName"
            :options="taxonomiesOptions"
            :label="$t('taxonomy')"
            dense
          />

          <pre>{{ taxonomiesStore.taxonomies }}</pre>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="bg-grey-3">
          <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
          <q-btn
            flat
            :label="$t('apply')"
            color="primary"
            @click="onApply"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'AnnotateDialog',
});
</script>
<script setup lang="ts">
import { TableDto, VariableDto } from 'src/models/Magma';
import { LocaleTextDto } from 'src/models/Opal';

interface DialogProps {
  modelValue: boolean;
  table: TableDto;
  variables: VariableDto[];
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue'])

const taxonomiesStore = useTaxonomiesStore();

const taxonomyName = ref<string>('');

const taxonomiesOptions = computed(() => {
  return taxonomiesStore.taxonomies.map((taxonomy) => {
    return {
      label: taxonomy.title ? getLabel(taxonomy.title) : taxonomy.name,
      value: taxonomy.name,
    };
  });
});

const showDialog = ref(props.modelValue);

watch(() => props.modelValue, (value) => {
  if (value) {
    taxonomiesStore.init();
  }
  showDialog.value = value;
});

function onHide() {
  emit('update:modelValue', false);
}

function onApply() {
  console.log('apply');
}

function getLabel(messages: LocaleTextDto[]): string {
  const msg = messages.find((t) => t.locale === 'en');
  return msg ? msg.text : '';
}
</script>
