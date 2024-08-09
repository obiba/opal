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
      @row-click="onRowClick"
      selection="multiple"
      v-model:selected="selected"
    >
      <template v-slot:top>
        <div class="column">
          <div class="row q-gutter-sm">
            <q-btn
              v-if="datasourceStore.perms.variables?.canCreate()"
              color="primary"
              icon="add"
              :title="$t('add_variable')"
              size="sm"
              @click="onShowAddVariable"
            />
            <q-btn
              color="secondary"
              icon="refresh"
              :title="$t('refresh')"
              outline
              size="sm"
              @click="onRefresh"
            />
            <q-btn
              v-if="datasourceStore.perms.table?.canUpdate()"
              outline
              color="red"
              icon="delete"
              size="sm"
              @click="onShowDeleteVariables"
            />
            <div v-if="selected.length === 0" class="text-hint q-pt-xs">
              <q-icon name="info" />
              <span class="q-ml-xs">{{ $t('variables_hint') }}</span>
            </div>
            <q-btn
              v-if="selected.length > 0"
              :label="$t('add_to_view')"
              icon="add_circle"
              no-caps
              dense
              flat
              size="sm"
              @click="onShowAddToView"
            />
            <q-btn
              v-if="selected.length > 0"
              :label="$t('annotate')"
              icon="label"
              no-caps
              dense
              flat
              size="sm"
              @click="onShowAnnotate"
            />
          </div>
        </div>
      </template>
      <template v-slot:body-cell-name="props">
        <q-td :props="props">
          <span class="text-primary">{{ props.value }}</span>
        </q-td>
      </template>
      <template v-slot:body-cell-valueType="props">
        <q-td :props="props">
          <pre class="q-ma-none">{{ props.value }}</pre>
        </q-td>
      </template>
      <template v-slot:body-cell-label="props">
        <q-td :props="props">
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
      <template v-slot:body-cell-categories="props">
        <q-td :props="props">
          <span>{{ getCategoryNames(props.value) }}</span>
        </q-td>
      </template>
      <template v-slot:body-cell-annotations="props">
        <q-td :props="props">

          <template v-for="annotation in taxonomiesStore.getAnnotations(props.value, true)" :key="annotation.id">
            <q-chip
              class="on-left">
              {{ taxonomiesStore.getLabel(annotation.term.title, locale) }}
              <q-tooltip>
                <annotation-panel :annotation="annotation" max-width="400px" class="bg-grey-7" />
              </q-tooltip>
            </q-chip>
          </template>
        </q-td>
      </template>
    </q-table>

    <add-to-view-dialog v-model="showAddToView" :table="datasourceStore.table" :variables="selected" />
    <annotate-dialog v-model="showAnnotate" :table="datasourceStore.table" :variables="selected" />
    <edit-variable-dialog v-model="showEditVariable" :variable="selectedSingle" />
    <confirm-dialog v-model="showDeleteVariables" :title="$t('delete')" :text="$t('delete_variables_confirm', { count: selected.length || rows.length })" @confirm="onDeleteVariables" />
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'TableVariables',
});
</script>
<script setup lang="ts">
import { CategoryDto, VariableDto } from 'src/models/Magma';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import EditVariableDialog from 'src/components/datasource/EditVariableDialog.vue';
import AddToViewDialog from 'src/components/datasource/AddToViewDialog.vue';
import AnnotateDialog from 'src/components/datasource/AnnotateDialog.vue';
import AnnotationPanel from 'src/components/datasource/AnnotationPanel.vue';
import { notifyError } from 'src/utils/notify';
import { getLabels } from 'src/utils/attributes';

const route = useRoute();
const router = useRouter();
const { t } = useI18n();
const datasourceStore = useDatasourceStore();
const taxonomiesStore = useTaxonomiesStore();
const { locale } = useI18n({ useScope: 'global' });

const tableRef = ref();
const loading = ref(false);
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const selected = ref([] as VariableDto[]);
const selectedSingle = ref<VariableDto>({} as VariableDto);
const showDeleteVariables = ref(false);
const showEditVariable = ref(false);
const showAddToView = ref(false);
const showAnnotate = ref(false);

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
    name: 'valueType',
    required: true,
    label: t('value_type'),
    align: 'left',
    field: 'valueType',
    format: (val: string) => t(val),
    sortable: true,
  },
  {
    name: 'categories',
    required: true,
    label: t('categories'),
    align: 'left',
    field: 'categories',
  },
  {
    name: 'annotations',
    required: true,
    label: t('annotations'),
    align: 'left',
    field: 'attributes',
  },
];

const rows = computed(() => datasourceStore.variables);

const dsName = computed(() => route.params.id as string);
const tName = computed(() => route.params.tid as string);

onMounted(() => {
  init();
});

watch([dsName, tName], () => {
  init();
});

function init() {
  loading.value = true;
  selected.value = [];
  datasourceStore
    .initDatasourceTableVariables(dsName.value, tName.value)
    .catch((err) => {
      notifyError(err);
    })
    .finally(() => {
      loading.value = false;
    });
  taxonomiesStore.init();
}

function onRefresh() {
  loading.value = true;
  datasourceStore.loadTableVariables()
    .finally(() => {
      loading.value = false;
    });
}

function getCategoryNames(categories: CategoryDto[]) {
  return categories ? categories.map((c) => c.name).join(', ') : undefined;
}

function onRowClick(evt: unknown, row: { name: string }) {
  router.push(
    `/project/${dsName.value}/table/${tName.value}/variable/${row.name}`
  );
}

function onShowDeleteVariables() {
  showDeleteVariables.value = true;
}

function onDeleteVariables() {
  const names = (selected.value.length === 0 ? rows.value : selected.value).map((v) => v.name);
  datasourceStore
    .deleteVariables(names)
    .then(() => {
      selected.value = [];
      init();
    });
}

function onShowAddVariable() {
  selectedSingle.value = {
    name: '',
    valueType: 'text',
    isRepeatable: false,
    occurrenceGroup: '',
    index: datasourceStore.variables?.length,
  } as VariableDto;
  showEditVariable.value = true;
}

function onShowAddToView() {
  showAddToView.value = true;
}

function onShowAnnotate() {
  showAnnotate.value = true;
}
</script>
