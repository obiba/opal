<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="dsName" :to="`/project/${dsName}`" />
        <q-breadcrumbs-el
          :label="$t('tables')"
          :to="`/project/${dsName}/tables`"
        />
        <q-breadcrumbs-el
          :label="tName"
          :to="`/project/${dsName}/table/${tName}`"
        />
        <q-breadcrumbs-el :label="vName" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h6">
        <q-icon name="table_chart" size="sm" class="q-mb-xs"></q-icon
        ><span class="on-right">{{ vName }}</span>
      </div>
      <q-card flat bordered class="bg-grey-3 q-mt-md q-mb-md">
        <q-card-section>
          <div
            v-for="attr in getLabels(datasourceStore.variable?.attributes)"
            :key="attr.locale"
          >
            <q-badge
              v-if="attr.locale"
              color="grey-6"
              :label="attr.locale"
              class="on-left"
            />
            <span>{{ attr.value }}</span>
          </div>
        </q-card-section>
      </q-card>
      <q-tabs
        v-model="tab"
        dense
        class="text-grey"
        active-color="primary"
        indicator-color="primary"
        align="justify"
        narrow-indicator
      >
        <q-tab name="dictionary" :label="$t('dictionary')" />
        <q-tab name="script" :label="$t('script')" v-if="datasourceStore.table.viewType"/>
        <q-tab name="summary" :label="$t('summary')" />
        <q-tab name="values" :label="$t('values')" v-if="datasourceStore.perms.tableValueSets?.canRead()"/>
        <q-tab name="permissions" :label="$t('permissions')" v-if="datasourceStore.perms.variablePermissions?.canRead()"/>
      </q-tabs>

      <q-separator />

      <q-tab-panels v-model="tab">
        <q-tab-panel name="dictionary">
          <div class="text-h6">{{ $t('properties') }}</div>
          <div class="row q-col-gutter-md q-mt-md q-mb-md">
            <div class="col-12 col-md-6">
              <fields-list
                :items="items1"
                :dbobject="datasourceStore.variable"
                class=""
              />
            </div>
            <div class="col-12 col-md-6">
              <fields-list
                :items="items2"
                :dbobject="datasourceStore.variable"
                class=""
              />
            </div>
          </div>
          <div class="row q-col-gutter-md">
            <div class="col-12 col-md-6">
              <div class="text-h6">{{ $t('categories') }}</div>
              <variable-categories />
            </div>
            <div class="col-12 col-md-6">
              <div class="text-h6">{{ $t('attributes') }}</div>
              <variable-attribues />
            </div>
          </div>
        </q-tab-panel>

        <q-tab-panel name="script" v-if="datasourceStore.table.viewType">
          <variable-script :variable="datasourceStore.variable" />
        </q-tab-panel>

        <q-tab-panel name="summary">
          <variable-summary :variable="datasourceStore.variable" :total="datasourceStore.table.valueSetCount"/>
        </q-tab-panel>

        <q-tab-panel name="values" v-if="datasourceStore.perms.tableValueSets?.canRead()">
          <table-values :variable="datasourceStore.variable"/>
        </q-tab-panel>

        <q-tab-panel name="permissions" v-if="datasourceStore.perms.variablePermissions?.canRead()">
          <div class="text-h6">{{ $t('permissions') }}</div>
        </q-tab-panel>
      </q-tab-panels>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import FieldsList, { FieldItem } from 'src/components/FieldsList.vue';
import VariableCategories from 'src/components/datasource/VariableCategories.vue';
import VariableAttribues from 'src/components/datasource/VariableAttributes.vue';
import VariableSummary from 'src/components/datasource/VariableSummary.vue';
import VariableScript from 'src/components/datasource/VariableScript.vue';
import TableValues from 'src/components/datasource/TableValues.vue';
import { VariableDto } from 'src/models/Magma';
import { getLabels } from 'src/utils/attributes';

const route = useRoute();
const datasourceStore = useDatasourceStore();

const tab = ref('dictionary');

const items1: FieldItem<VariableDto>[] = [
  {
    field: 'name',
  },
  {
    field: 'name',
    label: 'full_name',
    html: (val) =>
      val ? `<code>${dsName.value}.${tName.value}:${val.name}</code>` : '',
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

const items2: FieldItem<VariableDto>[] = [
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

const dsName = computed(() => route.params.id as string);
const tName = computed(() => route.params.tid as string);
const vName = computed(() => route.params.vid as string);

function init() {
  datasourceStore.initDatasourceTableVariable(
    dsName.value,
    tName.value,
    vName.value
  );
}
</script>
