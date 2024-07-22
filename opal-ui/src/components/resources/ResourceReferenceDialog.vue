<template>
  <q-dialog v-model="showDialog" @hide="onHide">
      <q-card class="dialog-sm">
        <q-card-section>
          <div class="text-h6">{{ $t(editMode ? 'edit' : 'add') }}</div>
        </q-card-section>

        <q-separator />

        <q-card-section>
          <div class="q-mb-md">
            <div class="text-bold q-mb-sm">{{ provider.title }}</div>
            <q-markdown :src="provider.description" no-heading-anchor-links/>
            <q-badge :label="provider.name" class="on-left"/><a v-if="provider.web" :href="provider.web" target="_blank" class="q-mt-md">{{ $t('Website') }} <q-icon name="open_in_new" /></a>
          </div>
          
          <q-select
            v-model="category"
            :options="categories"
            :label="$t('resource_ref.category')"
            dense
            @update:model-value="onCategoryUpdated"
            class="q-mb-sm"
          >
            <template v-slot:option="scope">
              <q-item v-bind="scope.itemProps">
                <q-item-section>
                  <q-item-label>{{ scope.opt.title }}</q-item-label>
                  <q-item-label v-if="scope.opt.description" caption style="max-width: 400px;">
                    {{ scope.opt.description }}
                  </q-item-label>
                </q-item-section>
              </q-item>
            </template>
          </q-select>
          <div v-if="category?.description" class="text-hint q-mb-md">
            <q-markdown :src="category.description" />
          </div>

          <q-select
            v-model="factory"
            :options="factories"
            :label="$t('resource_ref.factory')"
            :disable="!category"
            dense
            class="q-mb-sm"
          >
            <template v-slot:option="scope">
              <q-item v-bind="scope.itemProps">
                <q-item-section>
                  <q-item-label>{{ scope.opt.title }}</q-item-label>
                  <q-item-label v-if="scope.opt.description" caption style="max-width: 400px;">
                    {{ scope.opt.description }}
                  </q-item-label>
                </q-item-section>
              </q-item>
            </template>
          </q-select>
          <div v-if="factory?.description" class="text-hint q-mb-md">
            <q-markdown :src="factory.description" />
          </div>

          <div v-if="factory">
            <q-tabs
              v-model="tab"
              dense
              class="text-grey"
              active-color="primary"
              indicator-color="primary"
              align="justify"
              narrow-indicator
            >
              <q-tab name="parameters" :label="$t('parameters')" />
              <q-tab name="credentials" :label="$t('credentials')" />
            </q-tabs>
            <q-separator />
            <q-tab-panels v-model="tab">
              <q-tab-panel name="parameters">
                <schema-form v-model="refParameters" :schema="parametersSchemaForm"/>
              </q-tab-panel>
              <q-tab-panel name="credentials">
                <schema-form v-model="refCredentials" :schema="credentialsSchemaForm" />
              </q-tab-panel>
            </q-tab-panels>
          </div>
        </q-card-section>


        <q-separator />

        <q-card-actions align="right" class="bg-grey-3">
          <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
          <q-btn
            flat
            :label="$t('save')"
            color="primary"
            :disable="!factory"
            @click="onSave"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'ResourceReferenceDialog',
});
</script>
<script setup lang="ts">
import { ResourceProviderDto } from 'src/models/Resources';
import SchemaForm from 'src/components/SchemaForm.vue';

interface DialogProps {
  modelValue: boolean;
  provider: ResourceProviderDto;
}

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue'])

const editMode = ref<boolean>(false);
const showDialog = ref(props.modelValue);
const category = ref();
const factory = ref();
const refParameters = ref();
const refCredentials = ref();
const tab = ref('parameters');

const categories = computed(() => props.provider.categories.map((p) => {
  return {
    label: p.title,
    value: p.name,
    ...p
  }
}).sort(compareTitles));
const factories = computed(() => category.value ? props.provider.resourceFactories.filter((f) => f.tags.includes(category.value.name)).map((f) => {
  return {
    label: f.title,
    value: f.name,
    ...f
  }
}).sort(compareTitles) : [])
const parametersSchemaForm = computed(() => factory.value ? JSON.parse(factory.value.parametersSchemaForm) : {});
const credentialsSchemaForm = computed(() => factory.value ? JSON.parse(factory.value.credentialsSchemaForm) : {});

watch(() => props.modelValue, (value) => {
  if (value) {
    category.value = undefined;
    factory.value = undefined;
  }
  showDialog.value = value;
});

function onHide() {
  emit('update:modelValue', false);
}

function onCategoryUpdated() {
  factory.value = undefined;
}

function compareTitles(a: { title: string }, b: { title: string }) {
  if (a.title < b.title)
    return -1;
  if (a.title > b.title)
    return 1;
  return 0;
}
</script>
