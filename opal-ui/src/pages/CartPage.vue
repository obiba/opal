<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('cart')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        {{ t('variables') }}
      </div>
      <q-table
        v-if="cartStore.variables?.length"
        ref="tableRef"
        flat
        :rows="rows"
        :columns="columns"
        :row-key="(v) => `${v.parentLink.link}/variable/${v.name}`"
        :pagination="initialPagination"
        :loading="loading"
        selection="multiple"
        v-model:selected="selected"
      >
        <template v-slot:top-left>
          <div class="column">
            <div class="row q-gutter-sm">
              <q-btn
                :label="t('add_to_view')"
                icon="add_circle"
                color="primary"
                no-caps
                size="sm"
                @click="onShowAddToView"
              />
              <q-btn-dropdown :label="t('annotate')" icon="label" color="secondary" no-caps size="sm">
                <q-list>
                  <q-item clickable v-close-popup @click.prevent="onShowApplyAnnotation">
                    <q-item-section>
                      <q-item-label>{{ t('apply_annotation') }}</q-item-label>
                    </q-item-section>
                  </q-item>
                  <q-item clickable v-close-popup @click.prevent="onShowDeleteAnnotation">
                    <q-item-section>
                      <q-item-label>{{ t('delete_annotation') }}</q-item-label>
                    </q-item-section>
                  </q-item>
                </q-list>
              </q-btn-dropdown>
              <q-btn color="secondary" icon="refresh" :title="t('refresh')" outline size="sm" @click="onRefresh" />
              <q-btn
                :disable="selected.length === 0"
                :title="t('remove_from_cart')"
                icon="cleaning_services"
                size="sm"
                outline
                @click="onRemoveFromCart"
              />
            </div>
          </div>
        </template>
        <template v-slot:top-right>
          <q-input dense clearable debounce="400" color="primary" v-model="filter">
            <template v-slot:append>
              <q-icon name="search" />
            </template>
          </q-input>
        </template>
        <template v-slot:body-cell-name="props">
          <q-td :props="props">
            <router-link
              :to="`${getTableRoute(props.row.parentLink.link)}/variable/${props.value}`"
              class="text-primary"
              >{{ props.value }}</router-link
            >
          </q-td>
        </template>
        <template v-slot:body-cell-valueType="props">
          <q-td :props="props">
            <pre class="q-ma-none">{{ props.value }}</pre>
          </q-td>
        </template>
        <template v-slot:body-cell-label="props">
          <q-td :props="props">
            <div v-for="(attr, idx) in getLabels(props.value)" :key="idx">
              <q-badge v-if="attr.locale" color="grey-6" :label="attr.locale" class="on-left" />
              <span>{{ attr.value }}</span>
            </div>
          </q-td>
        </template>
        <template v-slot:body-cell-table="props">
          <q-td :props="props">
            <router-link :to="getTableRoute(props.value.link)" class="text-primary">{{
              getTableName(props.value.link)
            }}</router-link>
          </q-td>
        </template>
        <template v-slot:body-cell-annotations="props">
          <q-td :props="props">
            <template v-for="annotation in taxonomiesStore.getAnnotations(props.value, true)" :key="annotation.id">
              <q-chip class="on-left">
                {{ taxonomiesStore.getLabel(annotation.term.title, locale) }}
                <q-tooltip>
                  <annotation-panel :annotation="annotation" max-width="400px" class="bg-grey-7" />
                </q-tooltip>
              </q-chip>
            </template>
          </q-td>
        </template>
      </q-table>
      <div v-else class="text-hint">
        {{ t('empty_cart') }}
      </div>
      <add-to-view-dialog v-model="showAddToView" :variables="selected" />
      <annotate-dialog v-model="showAnnotate" :variables="selected" :operation="annotationOperation" />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import type { VariableDto } from 'src/models/Magma';
import { getLabels } from 'src/utils/attributes';
import AddToViewDialog from 'src/components/datasource/AddToViewDialog.vue';
import AnnotateDialog from 'src/components/datasource/AnnotateDialog.vue';
import AnnotationPanel from 'src/components/datasource/AnnotationPanel.vue';
import { DefaultAlignment } from 'src/components/models';

const cartStore = useCartStore();
const taxonomiesStore = useTaxonomiesStore();
const { t, locale } = useI18n({ useScope: 'global' });

const filter = ref('');
const tableRef = ref();
const loading = ref(false);
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const selected = ref([] as VariableDto[]);
const showAddToView = ref(false);
const showAnnotate = ref(false);
const annotationOperation = ref('apply');

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
    name: 'valueType',
    required: true,
    label: t('value_type'),
    align: DefaultAlignment,
    field: 'valueType',
    format: (val: string) => t(val),
    sortable: true,
  },
  {
    name: 'table',
    required: true,
    label: t('table'),
    align: DefaultAlignment,
    field: 'parentLink',
  },
  {
    name: 'annotations',
    required: true,
    label: t('annotations'),
    align: DefaultAlignment,
    field: 'attributes',
  },
]);

const rows = computed(() => {
  const f = filter.value ? filter.value.toLowerCase() : '';
  return cartStore.variables?.filter((v) => v.name.toLowerCase().includes(f));
});

onMounted(() => {
  loading.value = true;
  cartStore.refresh().finally(() => (loading.value = false));
});

function getTableRoute(link: string) {
  return link.replace('/datasource/', '/project/');
}

function getTableName(link: string) {
  return link.replace('/datasource/', '').replace('/table/', '.');
}

function onRemoveFromCart() {
  cartStore.removeVariables(selected.value.length === 0 ? cartStore.variables : selected.value);
  selected.value = [];
}

function onShowAddToView() {
  if (selected.value.length === 0) {
    selected.value = cartStore.variables;
  }
  showAddToView.value = true;
}

function onShowApplyAnnotation() {
  if (selected.value.length === 0) {
    selected.value = cartStore.variables;
  }
  annotationOperation.value = 'apply';
  showAnnotate.value = true;
}

function onShowDeleteAnnotation() {
  if (selected.value.length === 0) {
    selected.value = cartStore.variables;
  }
  annotationOperation.value = 'delete';
  showAnnotate.value = true;
}

function onRefresh() {
  loading.value = true;
  cartStore.refresh().finally(() => (loading.value = false));
}
</script>
