<template>
  <q-dialog v-model="showDialog" @hide="onHide">
      <q-card>
        <q-card-section>
          <div class="text-h6">{{ $t('add_table') }}</div>
        </q-card-section>

        <q-separator />

        <q-card-section>
          <q-input
            v-model="newTable.name"
            dense
            type="text"
            :label="$t('name')"
            style="width: 300px"
            class="q-mb-md"
          >
          </q-input>
          <q-input
            v-model="newTable.entityType"
            dense
            type="text"
            :label="$t('entity_type')"
            style="width: 300px"
            class="q-mb-md"
          >
          </q-input>
        </q-card-section>

        <q-separator />

        <q-card-actions align="right" class="bg-grey-3">
          <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
          <q-btn
            flat
            :label="$t('add')"
            color="primary"
            :disable="!isTableNameValid || !isEntityTypeValid"
            @click="onAddTable"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
</template>


<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'AddTableDialog',
});
</script>
<script setup lang="ts">
import { Table } from 'src/components/models';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue'])

const datasourceStore = useDatasourceStore();

const showDialog = ref(props.modelValue);
const newTable = ref<Table>({ name: '', entityType: 'Participant' } as Table);

watch(() => props.modelValue, (value) => {
  showDialog.value = value;
});

function onHide() {
  emit('update:modelValue', false);
}

const isTableNameValid = computed(() => datasourceStore.isNewTableNameValid(newTable.value.name));

const isEntityTypeValid = computed(() => newTable.value.entityType.trim() !== '');

function onAddTable() {
  datasourceStore.addTable(newTable.value.name, newTable.value.entityType)
    .then(() => datasourceStore.initDatasourceTables(datasourceStore.datasource.name))
    .catch((err) => {
      notifyError(err);
    });
}
</script>
