<template>
  <div>
    <div class="text-help">
      {{ t(`datashield.${env}_methods_info`) }}
    </div>
    <q-table
      flat
      :rows="methods"
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
          :title="t('add_method')"
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
          @click="updateMethods"
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
              v-if="isEditable(props.row)"
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
      <template v-slot:body-cell-type="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <span class="text-caption">{{ t(getType(props.row)) }}</span>
        </q-td>
      </template>
      <template v-slot:body-cell-code="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <pre style="margin: 0">{{ getCode(props.row) }}</pre>
        </q-td>
      </template>
      <template v-slot:body-cell-package="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          {{ getPackageName(props.row) }}
        </q-td>
      </template>
      <template v-slot:body-cell-version="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          {{ getPackageVersion(props.row) }}
        </q-td>
      </template>
    </q-table>
    <confirm-dialog
      v-model="showDelete"
      :title="t('delete')"
      :text="t('datashield.delete_methods_confirm', { count: selected.length })"
      @confirm="onDeleteMethods"
    />
    <edit-datashield-method-dialog v-model="showEdit" :env="env" :method="method" />
  </div>
</template>

<script setup lang="ts">
import type { DataShieldMethodDto, RFunctionDataShieldMethodDto, RScriptDataShieldMethodDto } from 'src/models/DataShield';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import EditDatashieldMethodDialog from 'src/components/admin/datashield/EditDatashieldMethodDialog.vue';
import { DefaultAlignment } from 'src/components/models';

interface Props {
  env: string;
}

const props = defineProps<Props>();

const datashieldStore = useDatashieldStore();
const { t } = useI18n();

const selected = ref<DataShieldMethodDto[]>([]);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const filter = ref('');
const showDelete = ref(false);
const showEdit = ref(false);
const method = ref<DataShieldMethodDto | null>(null);

const methods = computed(() => {
  return datashieldStore.methods[props.env] || [];
});

function onFilter() {
  if (filter.value.length === 0) {
    return methods.value;
  }
  const query = filter.value && filter.value.length > 0 ? filter.value.toLowerCase() : '';
  return methods.value?.filter((m) => {
    const desc = `${m.name} ${getCode(m)} ${getPackageName(m)}`.toLowerCase();
    return desc.includes(query);
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
    name: 'type',
    required: true,
    label: t('type'),
    field: 'type',
    align: DefaultAlignment,
  },
  {
    name: 'code',
    required: true,
    label: t('code'),
    field: 'code',
    align: DefaultAlignment,
  },
  {
    name: 'package',
    required: true,
    label: t('package'),
    field: 'package',
    align: DefaultAlignment,
  },
  {
    name: 'version',
    required: true,
    label: t('version'),
    field: 'version',
    align: DefaultAlignment,
  },
]);

function isEditable(row: DataShieldMethodDto) {
  if (getType(row) === 'r_script') return true;
  return getPackageVersion(row) === '';
}

function getType(row: DataShieldMethodDto) {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  return (row as any)['DataShield.RFunctionDataShieldMethodDto.method'] ? 'r_func' : 'r_script';
}

function getCode(row: DataShieldMethodDto) {
  if (getType(row) === 'r_func') {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const funcMethod = (row as any)['DataShield.RFunctionDataShieldMethodDto.method'] as RFunctionDataShieldMethodDto;
    return funcMethod.func;
  } else {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const scriptMethod = (row as any)['DataShield.RScriptDataShieldMethodDto.method'] as RScriptDataShieldMethodDto;
    return scriptMethod.script;
  }
}

function getPackageName(row: DataShieldMethodDto) {
  if (getType(row) !== 'r_func') return '';
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const funcMethod = (row as any)['DataShield.RFunctionDataShieldMethodDto.method'] as RFunctionDataShieldMethodDto;
  return funcMethod.rPackage;
}

function getPackageVersion(row: DataShieldMethodDto) {
  if (getType(row) !== 'r_func') return '';
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const funcMethod = (row as any)['DataShield.RFunctionDataShieldMethodDto.method'] as RFunctionDataShieldMethodDto;
  return funcMethod.version;
}

function onOverRow(row: DataShieldMethodDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: DataShieldMethodDto) {
  toolsVisible.value[row.name] = false;
}

function onShowEdit(row: DataShieldMethodDto | null) {
  method.value = row;
  showEdit.value = true;
}

function onShowDeleteSingle(row: DataShieldMethodDto) {
  selected.value = [row];
  onShowDelete();
}

function onShowDelete() {
  showDelete.value = true;
}

function onDeleteMethods() {
  datashieldStore
    .deleteMethods(
      props.env,
      selected.value.map((m) => m.name)
    )
    .then(() => {
      selected.value = [];
      datashieldStore.loadProfileSettings();
    });
}

function updateMethods() {
  datashieldStore.loadProfileSettings();
}
</script>
