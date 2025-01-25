<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="dsName" :to="`/project/${dsName}`" />
        <q-breadcrumbs-el :label="t('tables')" :to="`/project/${dsName}/tables`" />
        <q-breadcrumbs-el :label="tName" :to="`/project/${dsName}/table/${tName}`" />
        <q-breadcrumbs-el :label="vName" />
      </q-breadcrumbs>
      <q-space />
      <q-btn
        outline
        no-caps
        icon="navigate_before"
        size="sm"
        :label="previousVariable?.name"
        :to="`/project/${dsName}/table/${tName}/variable/${previousVariable?.name}`"
        v-if="previousVariable"
        class="on-right"
      />
      <q-btn
        outline
        no-caps
        icon-right="navigate_next"
        size="sm"
        :label="nextVariable?.name"
        :to="`/project/${dsName}/table/${tName}/variable/${nextVariable?.name}`"
        v-if="nextVariable"
        class="on-right"
      />
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h6 q-mb-md">
        <q-icon name="table_chart" size="sm" class="q-mb-xs"></q-icon><span class="on-right">{{ vName }}</span>
        <q-btn
          v-if="datasourceStore.perms.variable?.canUpdate()"
          color="secondary"
          size="sm"
          outline
          icon="edit"
          @click="onShowEditVariable"
          class="on-right"
        />
        <q-btn
          v-if="datasourceStore.perms.variable?.canDelete()"
          outline
          color="red"
          icon="delete"
          size="sm"
          @click="onShowDelete"
          class="on-right"
        />
        <q-btn
          :label="t('add_to_view')"
          icon="add_circle"
          no-caps
          dense
          flat
          size="sm"
          @click="onAddToView"
          class="on-right"
        />
        <q-btn
          v-if="!cartStore.isInCart(datasourceStore.variable)"
          :label="t('add_to_cart')"
          icon="add_shopping_cart"
          no-caps
          dense
          flat
          size="sm"
          @click="onAddToCart"
          class="on-right"
        />
        <q-btn
          v-if="cartStore.isInCart(datasourceStore.variable)"
          :label="t('remove_from_cart')"
          icon="remove_shopping_cart"
          no-caps
          dense
          flat
          size="sm"
          @click="onRemoveFromCart"
          class="on-right"
        />
      </div>

      <div v-if="loading">
        <q-spinner-dots />
      </div>
      <div v-else>
        <attributes-bundle-panel v-if="labelBundle" :bundle="labelBundle" class="q-mb-md" />
        <attributes-bundle-panel v-if="descriptionBundle" :bundle="descriptionBundle" class="q-mb-md text-help" />

        <q-tabs v-model="tab" dense class="text-grey" active-color="primary" indicator-color="primary" align="justify">
          <q-tab name="dictionary" :label="t('dictionary')" />
          <q-tab name="script" :label="t('script')" v-if="withScript" />
          <q-tab name="summary" :label="t('summary')" />
          <q-tab name="values" :label="t('values')" v-if="datasourceStore.perms.tableValueSets?.canRead()" />
          <q-tab
            name="permissions"
            :label="t('permissions')"
            v-if="datasourceStore.perms.variablePermissions?.canRead()"
          />
        </q-tabs>

        <q-separator />

        <q-tab-panels v-model="tab">
          <q-tab-panel name="dictionary">
            <div class="text-h6 q-mb-md">{{ t('properties') }}</div>
            <div class="row q-col-gutter-md q-mb-md">
              <div class="col-12 col-md-6">
                <fields-list :items="items1" :dbobject="datasourceStore.variable" class="" />
              </div>
              <div class="col-12 col-md-6">
                <fields-list :items="items2" :dbobject="datasourceStore.variable" class="" />
              </div>
            </div>
            <div class="row q-col-gutter-md">
              <div class="col-12 col-md-6">
                <div class="text-h6">{{ t('categories') }}</div>
                <variable-categories />
              </div>
              <div class="col-12 col-md-6">
                <div class="text-h6">{{ t('attributes') }}</div>
                <variable-attribues />
              </div>
            </div>
          </q-tab-panel>

          <q-tab-panel name="script" v-if="withScript">
            <variable-script :variable="datasourceStore.variable" />
          </q-tab-panel>

          <q-tab-panel name="summary">
            <variable-summary :variable="datasourceStore.variable" :total="datasourceStore.table.valueSetCount || 0" />
          </q-tab-panel>

          <q-tab-panel name="values" v-if="datasourceStore.perms.tableValueSets?.canRead()">
            <table-values :variable="datasourceStore.variable" />
          </q-tab-panel>

          <q-tab-panel name="permissions" v-if="datasourceStore.perms.variablePermissions?.canRead()">
            <access-control-list
              :resource="`/project/${dsName}/permissions/table/${tName}/variable/${vName}`"
              :options="['VARIABLE_READ']"
            />
          </q-tab-panel>
        </q-tab-panels>
      </div>
      <edit-variable-dialog v-model="showEdit" :variable="datasourceStore.variable" @save="onSaved" />
      <add-to-view-dialog
        v-model="showAddToView"
        :table="datasourceStore.table"
        :variables="[datasourceStore.variable]"
      />
      <confirm-dialog
        v-model="showDelete"
        :title="t('delete')"
        :text="t('delete_variables_confirm', { count: 1 })"
        @confirm="onDeleteVariable"
      />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import FieldsList, { type FieldItem } from 'src/components/FieldsList.vue';
import VariableCategories from 'src/components/datasource/VariableCategories.vue';
import VariableAttribues from 'src/components/datasource/VariableAttributes.vue';
import VariableSummary from 'src/components/datasource/VariableSummary.vue';
import VariableScript from 'src/components/datasource/VariableScript.vue';
import AccessControlList from 'src/components/permissions/AccessControlList.vue';
import EditVariableDialog from 'src/components/datasource/EditVariableDialog.vue';
import AddToViewDialog from 'src/components/datasource/AddToViewDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import TableValues from 'src/components/datasource/TableValues.vue';
import AttributesBundlePanel from 'src/components/datasource/AttributesBundlePanel.vue';
import type { VariableDto } from 'src/models/Magma';
import { notifyError } from 'src/utils/notify';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const datasourceStore = useDatasourceStore();
const taxonomiesStore = useTaxonomiesStore();
const cartStore = useCartStore();

const showEdit = ref(false);
const showAddToView = ref(false);
const showDelete = ref(false);
const tab = ref('dictionary');
const loading = ref(false);

const bundles = computed(() => datasourceStore.variableAttributesBundles || []);

const labelBundle = computed(() => bundles.value.find((bndl) => bndl.id === 'label'));
const descriptionBundle = computed(() => bundles.value.find((bndl) => bndl.id === 'description'));
const withScript = computed(() => datasourceStore.table.viewType === 'View');

const previousVariable = computed(() => {
  const idx = datasourceStore.variables.findIndex((v) => v.name === vName.value);
  return idx > 0 ? datasourceStore.variables[idx - 1] : null;
});

const nextVariable = computed(() => {
  const idx = datasourceStore.variables.findIndex((v) => v.name === vName.value);
  return idx === datasourceStore.variables.length - 1 ? null : datasourceStore.variables[idx + 1];
});

const items1: FieldItem[] = [
  {
    field: 'name',
  },
  {
    field: 'name',
    label: 'full_name',
    html: (val) => (val ? `<code>${dsName.value}.${tName.value}:${val.name}</code>` : ''),
  },
  {
    field: 'entityType',
    label: 'entity_type',
  },
  {
    field: 'referencedEntityType',
    label: 'referenced_entity_type',
  },
  {
    field: 'valueType',
    label: 'value_type',
  },
];

const items2: FieldItem[] = [
  {
    field: 'isRepeatable',
    label: 'repeatable',
  },
  {
    field: 'occurrenceGroup',
    label: 'occurrence_group',
  },
  {
    field: 'unit',
  },
  {
    field: 'mimeType',
    label: 'mime_type',
  },
  {
    field: 'index',
  },
];

onMounted(() => {
  init();
});

watch([() => route.params.vid], () => {
  init();
});

const dsName = computed(() => route.params.id as string);
const tName = computed(() => route.params.tid as string);
const vName = computed(() => route.params.vid as string);

function init() {
  loading.value = true;
  datasourceStore
    .initDatasourceTableVariable(dsName.value, tName.value, vName.value)
    .catch((err) => {
      notifyError(err);
      if (err.response?.status === 404) router.push(`/project/${dsName.value}/table/${tName.value}`);
    })
    .finally(() => {
      loading.value = false;
    });
  taxonomiesStore.init();
}

function onShowEditVariable() {
  showEdit.value = true;
}

function onShowDelete() {
  showDelete.value = true;
}

function onAddToView() {
  showAddToView.value = true;
}

function onAddToCart() {
  cartStore.addVariables([datasourceStore.variable]);
}

function onRemoveFromCart() {
  cartStore.removeVariables([datasourceStore.variable]);
}

function onDeleteVariable() {
  datasourceStore
    .deleteVariables([vName.value])
    .then(() => router.push(`/project/${dsName.value}/table/${tName.value}`));
}

function onSaved(variable: VariableDto) {
  if (variable.name !== vName.value) {
    router.push(`/project/${dsName.value}/table/${tName.value}/variable/${variable.name}`);
  } else {
    datasourceStore.loadTableVariable(variable.name);
  }
}
</script>
