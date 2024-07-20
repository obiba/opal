<template>
  <div>
    <h6 class="q-mt-none q-mb-none q-pa-md">
      {{ $t('taxonomies') }}
    </h6>
    <q-list>
      <q-item
        v-for="summary in summaries"
        :active="taxonomyName === summary.name"
        :key="summary.name"
        :to="`/admin/taxonomies/${summary.name}`"
      >
        <q-item-section avatar>
          <q-icon name="sell" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ summary.name }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item class="q-mt-md">
        <q-btn-dropdown
          no-caps
          color="primary"
          :label="$t('taxonomy.add')"
          :title="$t('user_profile.add_token')"
          icon="add"
          size="md"
        >
          <q-list>
            <q-item clickable v-close-popup @click.prevent="onAddTaxonomy">
              <q-item-section avatar style="min-width: auto; padding-right: 0.8rem">
                <q-icon name="sell" />
              </q-item-section>
              <q-item-section>
                <q-item-label>{{ $t('taxonomy.brand_new') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item clickable v-close-popup @click.prevent="onImportMlstrTaxonomies">
              <q-item-section avatar style="min-width: auto; padding-right: 0.8rem">
                <q-icon name="input" />
              </q-item-section>
              <q-item-section>
                <q-item-label>{{ $t('taxonomy.import_mlstr.label') }}</q-item-label>
              </q-item-section>
            </q-item>
            <!-- <q-item clickable v-close-popup @click.prevent="onImportMlstrTaxonomies">
              <q-item-section avatar style="min-width: auto; padding-right: 0.8rem">
                <q-icon name="fab fa-github" />
              </q-item-section>
              <q-item-section>
                <q-item-label>{{ $t('taxonomy.import_github') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item clickable v-close-popup @click.prevent="onImportMlstrTaxonomies">
              <q-item-section avatar style="min-width: auto; padding-right: 0.8rem">
                <q-icon name="description" />
              </q-item-section>
              <q-item-section>
                <q-item-label>{{ $t('taxonomy.import_file') }}</q-item-label>
              </q-item-section>
            </q-item> -->
          </q-list>
        </q-btn-dropdown>
      </q-item>
    </q-list>
  </div>

  <add-taxonomy-dialog v-model="showAddTaxonomy" :taxonomy="null" @update:modelValue="onClose" @updated="onAdded" />

  <import-mlstr-taxonomies-dialog
    v-model="showImportMlstr"
    @update:modelValue="onCloseMlstr"
    @updated="onImportedMlstrTaxonomies"
  />
</template>

<script lang="ts">
export default defineComponent({
  name: 'TaxonomiesDrawer',
});
</script>

<script setup lang="ts">
import { TaxonomiesDto_TaxonomySummaryDto as TaxonomySummariesDto, TaxonomyDto } from 'src/models/Opal';
import AddTaxonomyDialog from 'src/components/admin/taxonomies/AddTaxonomyDialog.vue';
import ImportMlstrTaxonomiesDialog from 'src/components/admin/taxonomies/ImportMlstrTaxonomiesDialog.vue';
import { notifyError } from 'src/utils/notify';

const route = useRoute();
const router = useRouter();
const taxonomiesStore = useTaxonomiesStore();
const summaries = computed<TaxonomySummariesDto[]>(() => taxonomiesStore.summaries);
const taxonomyName = computed(() => route.params.name as string);
const showAddTaxonomy = ref(false);
const showImportMlstr = ref(false);

function onAddTaxonomy() {
  showAddTaxonomy.value = true;
}

function onImportMlstrTaxonomies() {
  showImportMlstr.value = true;
}

async function onClose() {
  showAddTaxonomy.value = false;
}

async function onCloseMlstr() {
  showImportMlstr.value = false;
}

async function onAdded(updated: TaxonomyDto) {
  if (updated) {
    taxonomiesStore
      .refreshSummaries()
      .then(() => router.push(`/admin/taxonomies/${updated.name}`))
      .catch(notifyError);
  }
}

async function onImportedMlstrTaxonomies() {
  taxonomiesStore
    .refreshSummaries()
    .then(() => {
      console.log('GO to taxonomies');
      // NOTE: trick router to reload the taxonomies, simple push/replace had no effect
      router.push('/admin').then(() => router.push('/admin/taxonomies'));
    })
    .catch(notifyError);
}
</script>
