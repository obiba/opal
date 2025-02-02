<template>
  <div>
    <div class="text-help q-mb-md">
      {{ t('attributes_info') }}
    </div>
    <q-tabs v-model="tab" dense class="text-grey" active-color="primary" indicator-color="primary" align="justify">
      <q-tab name="annotations" :label="t('annotations')" />
      <q-tab name="label_description" :label="t('label_description')" />
      <q-tab name="records" :label="t('records')" />
    </q-tabs>
    <q-separator />
    <q-tab-panels v-model="tab">
      <q-tab-panel name="annotations">
        <div class="text-help q-mb-md">
          {{ t('attributes_annotations_info') }}
        </div>
        <div v-if="canUpdate" class="q-mb-sm">
          <q-btn color="primary" icon="edit" :title="t('add')" size="sm" @click="onShowAnnotate(undefined)" />
        </div>
        <q-list separator>
          <q-item
            v-for="annotation in taxonomiesStore.getAnnotations(rows, false)"
            :key="annotation.id"
            class="q-pl-none q-pr-none"
          >
            <q-item-section>
              <annotation-panel :annotation="annotation" header />
            </q-item-section>
            <q-item-section v-if="canUpdate" side>
              <table>
                <tbody>
                  <tr>
                    <td>
                      <q-btn
                        rounded
                        dense
                        flat
                        size="sm"
                        color="secondary"
                        :title="t('search')"
                        icon="search"
                        @click="onSearch(annotation)"
                      />
                    </td>
                    <td>
                      <q-btn
                        rounded
                        dense
                        flat
                        size="sm"
                        color="secondary"
                        :title="t('edit')"
                        icon="edit"
                        @click="onShowAnnotate(annotation)"
                      />
                    </td>
                    <td>
                      <q-btn
                        rounded
                        dense
                        flat
                        size="sm"
                        color="secondary"
                        :title="t('delete')"
                        icon="delete"
                        class="q-ml-xs"
                        @click="onShowDelete(annotation)"
                      />
                    </td>
                  </tr>
                </tbody>
              </table>
            </q-item-section>
          </q-item>
        </q-list>
      </q-tab-panel>
      <q-tab-panel name="label_description">
        <div class="text-bold q-mb-sm">{{ t('label') }}</div>
        <div v-if="canUpdate" class="q-mb-sm">
          <q-btn
            color="primary"
            icon="edit"
            :title="t('add')"
            size="sm"
            @click="onShowAttribute(labelBundle || { id: 'label', attributes: [{ name: 'label' } as AttributeDto] })"
          />
          <q-btn
            v-if="labelBundle"
            outline
            color="red"
            icon="delete"
            size="sm"
            @click="onShowDeleteBundle(labelBundle)"
            class="on-right"
          />
        </div>
        <attributes-bundle-panel :bundle="labelBundle" class="q-mb-md" />
        <div class="text-bold q-mb-sm">{{ t('description') }}</div>
        <div v-if="canUpdate" class="q-mb-sm">
          <q-btn
            color="primary"
            icon="edit"
            :title="t('add')"
            size="sm"
            @click="
              onShowAttribute(
                descriptionBundle || { id: 'description', attributes: [{ name: 'description' } as AttributeDto] },
              )
            "
          />
          <q-btn
            v-if="descriptionBundle"
            outline
            color="red"
            icon="delete"
            size="sm"
            @click="onShowDeleteBundle(descriptionBundle)"
            class="on-right"
          />
        </div>
        <attributes-bundle-panel :bundle="descriptionBundle" />
      </q-tab-panel>
      <q-tab-panel name="records">
        <div class="text-help q-mb-md">
          {{ t('attributes_records_info') }}
        </div>
        <q-table
          ref="tableRef"
          flat
          :rows="bundles"
          :columns="columns"
          row-key="name"
          :pagination="initialPagination"
          :loading="loading"
        >
          <template v-slot:top>
            <q-btn
              v-if="canUpdate"
              color="primary"
              icon="edit"
              :title="t('add')"
              size="sm"
              @click="onShowAttribute(undefined)"
            />
          </template>
          <template v-slot:body-cell-namespace="props">
            <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
              {{ props.value }}
            </q-td>
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
                  :icon="toolsVisible[props.row.id] ? 'edit' : 'none'"
                  class="q-ml-xs"
                  @click="onShowEditBundle(props.row)"
                />
                <q-btn
                  rounded
                  dense
                  flat
                  size="sm"
                  color="secondary"
                  :title="t('delete')"
                  :icon="toolsVisible[props.row.id] ? 'delete' : 'none'"
                  class="q-ml-xs"
                  @click="onShowDeleteBundle(props.row)"
                />
              </div>
            </q-td>
          </template>
          <template v-slot:body-cell-values="props">
            <q-td :props="props" @mouseover="onOverRow(props.row)" @mouseleave="onLeaveRow(props.row)">
              <div v-for="attr in props.value" :key="attr.id">
                <q-badge v-if="attr.locale" color="grey-6" :label="attr.locale" class="on-left" />
                <pre class="q-ma-none">{{ attr.value }}</pre>
              </div>
            </q-td>
          </template>
        </q-table>
      </q-tab-panel>
    </q-tab-panels>

    <confirm-dialog
      v-model="showDeleteAnnotation"
      :title="t('delete')"
      :text="t('delete_annotation_confirm')"
      @confirm="onConfirmDeleteAnnotation"
    />
    <annotate-dialog
      v-model="showAnnotate"
      :table="datasourceStore.table"
      :variables="[datasourceStore.variable]"
      :annotation="annotationSelected"
    />
    <confirm-dialog
      v-model="showDeleteAttributes"
      :title="t('delete')"
      :text="t('delete_attributes_confirm')"
      @confirm="onConfirmDeleteAttributes"
    />
    <attribute-dialog
      v-model="showAttributes"
      :table="datasourceStore.table"
      :variable="datasourceStore.variable"
      :bundle="bundleSelected"
    />
  </div>
</template>

<script setup lang="ts">
import type { Annotation, AttributesBundle } from 'src/components/models';
import AnnotationPanel from 'src/components/datasource/AnnotationPanel.vue';
import AttributesBundlePanel from 'src/components/datasource/AttributesBundlePanel.vue';
import AnnotateDialog from 'src/components/datasource/AnnotateDialog.vue';
import AttributeDialog from 'src/components/datasource/AttributeDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import type { AttributeDto } from 'src/models/Magma';
import { DefaultAlignment } from 'src/components/models';

const { t } = useI18n();
const router = useRouter();
const datasourceStore = useDatasourceStore();
const taxonomiesStore = useTaxonomiesStore();
const searchStore = useSearchStore();

const tab = ref('annotations');
const tableRef = ref();
const loading = ref(false);
const initialPagination = ref({
  descending: false,
  page: 1,
  rowsPerPage: 20,
});
const showAnnotate = ref(false);
const showDeleteAnnotation = ref(false);
const annotationSelected = ref<Annotation>();
const showAttributes = ref(false);
const showDeleteAttributes = ref(false);
const bundleSelected = ref<AttributesBundle | undefined>();
const toolsVisible = ref<{ [key: string]: boolean }>({});

const canUpdate = computed(() => datasourceStore.perms.variable?.canUpdate());

const columns = computed(() => [
  {
    name: 'namespace',
    required: true,
    align: DefaultAlignment,
    label: t('namespace'),
    field: (row: AttributesBundle) => row.attributes[0]?.namespace,
    sortable: true,
  },
  {
    name: 'name',
    required: true,
    align: DefaultAlignment,
    label: t('name'),
    field: (row: AttributesBundle) => row.attributes[0]?.name,
    sortable: true,
  },
  {
    name: 'values',
    required: true,
    align: DefaultAlignment,
    label: t('values'),
    field: (row: AttributesBundle) => row.attributes,
  },
]);

const rows = computed(() => datasourceStore.variable?.attributes || []);

const bundles = computed(() => datasourceStore.variableAttributesBundles || []);

const labelBundle = computed(() => bundles.value.find((bndl) => bndl.id === 'label'));
const descriptionBundle = computed(() => bundles.value.find((bndl) => bndl.id === 'description'));

function onSearch(annotation: Annotation | undefined) {
  if (annotation) {
    searchStore.reset();
    searchStore.variablesQuery.criteria['project'] = [datasourceStore.datasource.name];
    searchStore.variablesQuery.criteria['table'] = [datasourceStore.table.name];
    searchStore.variablesQuery.criteria[`${annotation.taxonomy.name}-${annotation.vocabulary.name}`] = [
      annotation.term.name,
    ];
    router.push('/search/variables');
  }
}

function onShowAnnotate(annotation: Annotation | undefined) {
  annotationSelected.value = annotation;
  showAnnotate.value = true;
}

function onShowDelete(annotation: Annotation) {
  annotationSelected.value = annotation;
  showDeleteAnnotation.value = true;
}

function onConfirmDeleteAnnotation() {
  if (annotationSelected.value) {
    datasourceStore
      .deleteAnnotation(
        [datasourceStore.variable],
        annotationSelected.value.taxonomy.name,
        annotationSelected.value.vocabulary.name,
      )
      .then(() => {
        datasourceStore.loadTableVariables();
      });
  }
}

function onShowAttribute(bundle: AttributesBundle | undefined) {
  // find the attributes with same namespace and name
  bundleSelected.value = bundle ? bundle : undefined;
  showAttributes.value = true;
}

function onOverRow(row: AttributesBundle) {
  toolsVisible.value[row.id] = true;
}

function onLeaveRow(row: AttributesBundle) {
  toolsVisible.value[row.id] = false;
}

function onShowEditBundle(row: AttributesBundle) {
  bundleSelected.value = row;
  showAttributes.value = true;
}

function onShowDeleteBundle(row: AttributesBundle) {
  bundleSelected.value = row;
  showDeleteAttributes.value = true;
}

function onConfirmDeleteAttributes() {
  if (bundleSelected.value) {
    datasourceStore
      .deleteAttributes(
        datasourceStore.variable,
        bundleSelected.value.attributes[0]?.namespace,
        bundleSelected.value.attributes[0]?.name || '',
      )
      .then(() => {
        datasourceStore.loadTableVariables();
      });
  }
}
</script>
