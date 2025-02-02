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
        <q-btn-dropdown v-if="canUpdate" color="primary" icon="add" :title="t('add')" size="sm">
          <q-list>
            <q-item clickable @click="onShowAddSingle">
              <q-item-section>
                <q-item-label>{{ t('add_category') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item clickable @click="onShowAddRange">
              <q-item-section>
                <q-item-label>{{ t('add_categories_range') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>
        <q-btn
          v-if="canUpdate"
          color="secondary"
          icon="arrow_upward"
          size="sm"
          :title="t('move_up')"
          @click="onUp"
          :disable="selected.length === 0 || !moveEnabled"
          class="on-right"
        />
        <q-btn
          v-if="canUpdate"
          color="secondary"
          icon="arrow_downward"
          size="sm"
          :title="t('move_down')"
          @click="onDown"
          :disable="selected.length === 0 || !moveEnabled"
          class="on-right"
        />
        <q-btn
          v-if="canUpdate"
          outline
          color="red"
          icon="delete"
          size="sm"
          @click="onShowDelete"
          :disable="selected.length === 0"
          class="on-right"
        />
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
              :title="t('edit')"
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
      <template v-slot:body-cell-missing="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <q-icon v-if="props.value" name="check" size="sm"></q-icon>
        </q-td>
      </template>
      <template v-slot:body-cell-label="props">
        <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
          <div v-for="(attr, idx) in getLabels(props.value)" :key="idx">
            <q-badge v-if="attr.locale" color="grey-6" :label="attr.locale" class="on-left" />
            <span>{{ attr.value }}</span>
          </div>
        </q-td>
      </template>
    </q-table>

    <confirm-dialog
      v-model="showDelete"
      :title="t('delete')"
      :text="t('delete_categories_confirm', { count: selected.length })"
      @confirm="onDelete"
    />

    <category-dialog
      v-model="showEdit"
      :variable="datasourceStore.variable"
      :category="selectedSingle"
      @saved="onUpdate"
    />

    <categories-range-dialog v-model="showAddRange" :variable="datasourceStore.variable" @saved="onUpdate" />
  </div>
</template>

<script setup lang="ts">
import { getLabels } from 'src/utils/attributes';
import type { CategoryDto } from 'src/models/Magma';
import CategoryDialog from 'src/components/datasource/CategoryDialog.vue';
import CategoriesRangeDialog from 'src/components/datasource/CategoriesRangeDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { DefaultAlignment } from 'src/components/models';

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
const selectedSingle = ref<CategoryDto>({ name: '', attributes: [], isMissing: false });
const toolsVisible = ref<{ [key: string]: boolean }>({});
const showDelete = ref(false);
const showEdit = ref(false);
const showAddRange = ref(false);
const moveEnabled = ref(true);

const columns = computed(() => [
  {
    name: 'name',
    required: true,
    label: t('name'),
    align: DefaultAlignment,
    field: 'name',
    format: (val: string) => val,
    sortable: true,
  },
  {
    name: 'label',
    required: true,
    label: t('label'),
    align: DefaultAlignment,
    field: 'attributes',
  },
  {
    name: 'missing',
    required: true,
    label: t('is_missing'),
    align: DefaultAlignment,
    field: 'isMissing',
    sortable: true,
  },
]);

const rows = computed(() => (datasourceStore.variable?.categories ? datasourceStore.variable.categories : []));

const canUpdate = computed(() => datasourceStore.perms.variable?.canUpdate());

function onOverRow(row: CategoryDto) {
  toolsVisible.value[row.name] = true;
}

function onLeaveRow(row: CategoryDto) {
  toolsVisible.value[row.name] = false;
}

function onShowEdit(row: CategoryDto) {
  selectedSingle.value = row;
  showEdit.value = true;
}

function onShowAddSingle() {
  selectedSingle.value = { name: '', attributes: [], isMissing: false } as CategoryDto;
  showEdit.value = true;
}

function onShowAddRange() {
  showAddRange.value = true;
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
    categories: datasourceStore.variable.categories.filter(
      (c) => !selected.value.map((cs) => cs.name).includes(c.name)
    ),
  };
  datasourceStore
    .updateVariable(newVariable)
    .then(onUpdate)
    .then(() => {
      selected.value = [];
    });
}

function onUp() {
  const categories = [...datasourceStore.variable.categories];
  const indices = selected.value.map((c) => categories.findIndex((cc) => cc.name === c.name)).sort();
  for (let i = 0; i < indices.length; i++) {
    const idx = indices[i];
    if (idx === 0 || idx === undefined) {
      continue;
    }
    const c = categories[idx];
    categories.splice(idx, 1);
    if (c)
      categories.splice(idx - 1, 0, c);
  }
  moveEnabled.value = false;
  datasourceStore
    .updateVariable({ ...datasourceStore.variable, categories })
    .then(onUpdate)
    .finally(() => {
      moveEnabled.value = true;
    });
}

function onDown() {
  const categories = [...datasourceStore.variable.categories];
  const indices = selected.value.map((c) => categories.findIndex((cc) => cc.name === c.name)).sort((a, b) => b - a);
  for (let i = 0; i < indices.length; i++) {
    const idx = indices[i];
    if (idx === categories.length - 1 || idx === undefined) {
      continue;
    }
    const c = categories[idx];
    categories.splice(idx, 1);
    if (c)
      categories.splice(idx + 1, 0, c);
  }
  moveEnabled.value = false;
  datasourceStore
    .updateVariable({ ...datasourceStore.variable, categories })
    .then(onUpdate)
    .finally(() => {
      moveEnabled.value = true;
    });
}

function onUpdate() {
  datasourceStore.loadTableVariable(datasourceStore.variable.name);
}
</script>
