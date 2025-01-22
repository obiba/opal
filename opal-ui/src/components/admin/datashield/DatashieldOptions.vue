<template>
  <div>
    <div class="text-help">
      {{ t('datashield.options_info') }}
    </div>
    <q-table
      flat
      :rows="datashieldStore.options"
      :columns="columns"
      row-key="name"
      wrap-cells
      :pagination="initialPagination"
      :filter="filter"
      :filter-method="onFilter"
      selection="multiple"
      v-model:selected="selected"
    >
      <template v-slot:top-left>
        <q-btn
          color="primary"
          text-color="white"
          icon="add"
          :title="t('add_option')"
          size="sm"
          @click="onShowEdit(null)"
        />
        <q-btn
          outline
          color="secondary"
          icon="refresh"
          :title="t('refresh')"
          size="sm"
          class="on-right"
          @click="updateOptions"
        />
        <q-btn
          outline
          color="red"
          icon="delete"
          size="sm"
          class="on-right"
          :disable="selected.length === 0"
          @click="onShowDelete"
        />
      </template>
      <template v-slot:top-right>
        <q-input dense debounce="500" v-model="filter">
          <template v-slot:append>
            <q-icon name="search" />
          </template>
        </q-input>
      </template>
      <template v-slot:body-cell-name="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <span class="text-primary">{{ props.row.name }}</span>
          <div class="float-right">
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :icon="toolsVisible[props.row.name] ? 'edit' : 'none'"
              class="q-ml-xs"
              @click="onShowEdit(props.row)"
            />
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="t('delete')"
              :icon="toolsVisible[props.row.name] ? 'delete' : 'none'"
              class="q-ml-xs"
              @click="onShowDeleteSingle(props.row)"
            />
          </div>
        </q-td>
      </template>
      <template v-slot:body-cell-value="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <pre style="margin: 0">{{ props.row.value }}</pre>
        </q-td>
      </template>
    </q-table>
    <confirm-dialog
      v-model="showDelete"
      :title="t('delete')"
      :text="t('datashield.delete_options_confirm', { count: selected.length })"
      @confirm="onDeleteOptions"
    />
    <edit-datashield-option-dialog v-model="showEdit" :option="opt" />
  </div>
</template>

<script setup lang="ts">
import type { DataShieldROptionDto } from 'src/models/DataShield';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import EditDatashieldOptionDialog from 'src/components/admin/datashield/EditDatashieldOptionDialog.vue';
import { DefaultAlignment } from 'src/components/models';

const datashieldStore = useDatashieldStore();
const { t } = useI18n();

const selected = ref<DataShieldROptionDto[]>([]);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const filter = ref('');
const showDelete = ref(false);
const showEdit = ref(false);
const opt = ref<DataShieldROptionDto | null>(null);

function onFilter() {
  if (filter.value.length === 0) {
    return datashieldStore.options;
  }
  const query = filter.value && filter.value.length > 0 ? filter.value.toLowerCase() : '';
  return datashieldStore.options.filter((o) => {
    return o.name.includes(query);
  });
}

const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: DefaultAlignment,
    field: 'name',
    sortable: true,
  },
  {
    name: 'value',
    required: true,
    label: t('value'),
    align: DefaultAlignment,
    field: 'value',
    sortable: true,
  },
]);

function onOverRow(row: DataShieldROptionDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: DataShieldROptionDto) {
  toolsVisible.value[row.name] = false;
}

function onShowEdit(row: DataShieldROptionDto | null) {
  opt.value = row;
  showEdit.value = true;
}

function onShowDeleteSingle(row: DataShieldROptionDto) {
  selected.value = [row];
  onShowDelete();
}

function onShowDelete() {
  showDelete.value = true;
}

function onDeleteOptions() {
  datashieldStore.deleteOptions(selected.value.map((m) => m.name)).then(() => {
    selected.value = [];
    datashieldStore.loadProfileSettings();
  });
}

function updateOptions() {
  datashieldStore.loadProfileSettings();
}
</script>
