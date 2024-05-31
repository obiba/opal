<template>
  <div>
    <q-table
      ref="tableRef"
      flat
      :rows="rows"
      :columns="columns"
      row-key="name"
      :pagination="initialPagination"
      :loading="loading"
      :selection="canUpdate ? 'multiple' : 'none'"
      v-model:selected="selected"
    >
      <template v-slot:top>
        <q-btn-dropdown v-if="canUpdate" color="primary" icon="add" :label="$t('add')" size="sm">
          <q-list> </q-list>
        </q-btn-dropdown>
        <q-btn
          v-if="canUpdate"
          outline
          color="red"
          icon="delete"
          size="sm"
          @click="onShowDelete"
          :disable="selected.length === 0"
          class="on-right" />
      </template>
      <template v-slot:body-cell-name="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <span class="text-primary">{{ props.value }}</span>
          <div v-if="canUpdate" class="float-right">
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="$t('edit')"
              :icon="toolsVisible[props.row.name] ? 'edit' : 'none'"
              class="q-ml-xs"
              @click="onShowEdit(props.row)" />
            <q-btn
              rounded
              dense
              flat
              size="sm"
              color="secondary"
              :title="$t('delete')"
              :icon="toolsVisible[props.row.name] ? 'delete' : 'none'"
              class="q-ml-xs"
              @click="onShowDeleteSingle(props.row)" />
          </div>
        </q-td>
      </template>
      <template v-slot:body-cell-missing="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-icon v-if="props.value" name="done"></q-icon>
        </q-td>
      </template>
      <template v-slot:body-cell-label="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <div v-for="attr in getLabels(props.value)" :key="attr.locale">
            <q-badge
              v-if="attr.locale"
              color="grey-6"
              :label="attr.locale"
              class="on-left"
            />
            <span>{{ attr.value }}</span>
          </div>
        </q-td>
      </template>
    </q-table>

    <confirm-dialog
      v-model="showDelete"
      :title="$t('delete')"
      :text="$t('delete_categories_confirm', { count: selected.length })"
      @confirm="onDelete" />

    <category-dialog
      v-model="showEdit"
      :variable="datasourceStore.variable"
      :category="selected[0]" />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { getLabels } from 'src/utils/attributes';
import { CategoryDto } from 'src/models/Magma';
export default defineComponent({
  name: 'VariableCategories',
});
</script>
<script setup lang="ts">
import CategoryDialog from 'src/components/datasource/CategoryDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
const { t } = useI18n();
const datasourceStore = useDatasourceStore();

const tableRef = ref();
const loading = ref(false);
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const selected = ref<CategoryDto[]>([]);
const toolsVisible = ref<{ [key: string]: boolean }>({});
const showDelete = ref(false);
const showEdit = ref(false);

const columns = [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: 'left',
    field: 'name',
    format: (val: string) => val,
    sortable: true,
  },
  {
    name: 'label',
    required: true,
    label: t('label'),
    align: 'left',
    field: 'attributes',
  },
  {
    name: 'missing',
    required: true,
    label: t('is_missing'),
    align: 'left',
    field: 'isMissing',
    sortable: true,
  },
];

const rows = computed(() => datasourceStore.variable?.categories ? datasourceStore.variable.categories : []);

const canUpdate = computed(() => datasourceStore.perms.variable?.canUpdate());

function onOverRow(row: CategoryDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: CategoryDto) {
  toolsVisible.value[row.name] = false;
}

function onShowEdit(row: CategoryDto) {
  selected.value = [row];
  showEdit.value = true;
}

function onShowDeleteSingle(row: CategoryDto) {
  selected.value = [row];
  onShowDelete();
}

function onShowDelete() {
  showDelete.value = true;
}

function onDelete() {
  const newVariable = {
    ...datasourceStore.variable,
    categories: datasourceStore.variable.categories.filter((c) => !selected.value.includes(c)),
  };
  datasourceStore.saveVariable(newVariable);
}
</script>
