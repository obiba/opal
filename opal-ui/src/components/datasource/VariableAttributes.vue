<template>
  <div>
    <div class="text-help q-mb-md">
      {{ $t('attributes_info') }}
    </div>
    <q-tabs
        v-model="tab"
        dense
        class="text-grey"
        active-color="primary"
        indicator-color="primary"
        align="justify"
        narrow-indicator
      >
        <q-tab name="annotations" :label="$t('annotations')" />
        <q-tab name="records" :label="$t('records')" />
    </q-tabs>
    <q-separator />
    <q-tab-panels v-model="tab">
      <q-tab-panel name="annotations">
        <div class="text-help q-mb-md">
          {{ $t('attributes_annotations_info') }}
        </div>
        <div v-if="datasourceStore.perms.variable?.canUpdate()" class="q-mb-sm">
          <q-btn
            color="primary"
            icon="edit"
            :title="$t('add')"
            size="sm"
            @click="onShowAnnotate(undefined)" />
        </div>
        <q-list separator>
          <q-item v-for="annotation in taxonomiesStore.getAnnotations(rows, false)" :key="annotation.id" class="q-pl-none q-pr-none">
            <q-item-section>
              <annotation-panel :annotation="annotation" header/>
            </q-item-section>
            <q-item-section v-if="datasourceStore.perms.variable?.canUpdate()" side>
              <table>
                <tr>
                  <td>
                    <q-btn
                      rounded
                      dense
                      flat
                      size="sm"
                      color="secondary"
                      :title="$t('edit')"
                      icon="edit"
                      @click="onShowAnnotate(annotation)" />
                  </td>
                  <td>
                    <q-btn
                      rounded
                      dense
                      flat
                      size="sm"
                      color="secondary"
                      :title="$t('delete')"
                      icon="delete"
                      class="q-ml-xs"
                      @click="onShowDelete(annotation)" />
                  </td>
                </tr>
              </table>
            </q-item-section>
          </q-item>
        </q-list>
      </q-tab-panel>
      <q-tab-panel name="records">
        <div class="text-help q-mb-md">
          {{ $t('attributes_records_info') }}
        </div>
        <q-table
          ref="tableRef"
          flat
          :rows="rows"
          :columns="columns"
          row-key="name"
          :pagination="initialPagination"
          :loading="loading"
        >
          <template v-slot:top>
            <q-btn
               v-if="datasourceStore.perms.variable?.canUpdate()"
              color="primary"
              icon="edit"
              :title="$t('add')"
              size="sm"
              @click="onShowAttribute(undefined)" />
          </template>
          <template v-slot:body-cell-name="props">
            <q-td :props="props">
              <span class="text-primary">{{ props.value }}</span>
            </q-td>
          </template>
          <template v-slot:body-cell-locale="props">
            <q-td :props="props">
              <q-badge
                v-if="props.value"
                color="grey-6"
                :label="props.value"
                class="on-left"
              />
            </q-td>
          </template>
        </q-table>

      </q-tab-panel>
    </q-tab-panels>

    <confirm-dialog v-model="showDeleteAnnotation" :title="$t('delete')" :text="$t('delete_annotation_confirm')" @confirm="onConfirmDeleteAnnotation" />
    <annotate-dialog v-model="showAnnotate" :table="datasourceStore.table" :variables="[datasourceStore.variable]" :annotation="annotationSelected"/>
    <attribute-dialog v-model="showAttribute" :table="datasourceStore.table" :variable="datasourceStore.variable" :attributes="attributesSelected" />
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'VariableAttributes',
});
</script>
<script setup lang="ts">
import { Annotation } from 'src/components/models';
import AnnotationPanel from 'src/components/datasource/AnnotationPanel.vue';
import AnnotateDialog from 'src/components/datasource/AnnotateDialog.vue';
import AttributeDialog from 'src/components/datasource/AttributeDialog.vue';
import ConfirmDialog from 'src/components/ConfirmDialog.vue';
import { AttributeDto } from 'src/models/Magma';

const { t } = useI18n();
const datasourceStore = useDatasourceStore();
const taxonomiesStore = useTaxonomiesStore();

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
const showAttribute = ref(false);
const attributesSelected = ref<AttributeDto[]>([]);

const columns = [
  {
    name: 'namespace',
    required: true,
    align: 'left',
    label: t('namespace'),
    field: 'namespace',
    format: (val: string) => val,
    sortable: true,
  },
  {
    name: 'name',
    required: true,
    align: 'left',
    label: t('name'),
    field: 'name',
    format: (val: string) => val,
    sortable: true,
  },
  {
    name: 'locale',
    required: true,
    align: 'left',
    label: t('locale'),
    field: 'locale',
  },
  {
    name: 'value',
    required: true,
    align: 'left',
    label: t('value'),
    field: 'value',
  },
];

const rows = computed(() => datasourceStore.variable?.attributes ? datasourceStore.variable.attributes : []);

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
    datasourceStore.deleteAnnotation([datasourceStore.variable], annotationSelected.value.taxonomy.name, annotationSelected.value.vocabulary.name);
  }
}

function onShowAttribute(attribute: AttributeDto | undefined) {
  // find the attributes with same namespace and name
  if (attribute) {
    attributesSelected.value = datasourceStore.variable.attributes.filter(attr => attr.namespace === attribute.namespace && attr.name === attribute.name);
  } else {
    attributesSelected.value = [];
  }
  showAttribute.value = true;
}
</script>
