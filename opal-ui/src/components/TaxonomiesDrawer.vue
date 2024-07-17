<template>
  <div v-if="summaries.length > 0">
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
      <q-item>
        <q-btn-dropdown
          no-caps
          color="primary"
          :label="$t('taxonomy.add')"
          :title="$t('user_profile.add_token')"
          icon="add"
          size="md"
        >
          <q-list>
            <!--
            <q-item clickable v-close-popup @click.prevent="">
              <q-item-section>
                <q-item-label>{{ $t('user_profile.add_datashield_token') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item clickable v-close-popup @click.prevent="">
              <q-item-section>
                <q-item-label>{{ $t('user_profile.add_r_token') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item clickable v-close-popup @click.prevent="">
              <q-item-section>
                <q-item-label>{{ $t('user_profile.add_sql_token') }}</q-item-label>
              </q-item-section>
            </q-item>
            -->
            <q-item clickable v-close-popup @click.prevent="onAddTaxonomy">
              <q-item-section>
                <q-item-label>{{ $t('user_profile.add_custom_token') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>
      </q-item>
    </q-list>
  </div>

  <add-taxonomy-dialog v-model="showAddTaxonomy" :taxonomy="null" @update:modelValue="onClose" @updated="onUpdated" />
</template>

<script lang="ts">
export default defineComponent({
  name: 'TaxonomiesDrawer',
});
</script>

<script setup lang="ts">
import { TaxonomiesDto_TaxonomySummaryDto as TaxonomySummariesDto, TaxonomyDto } from 'src/models/Opal';
import AddTaxonomyDialog from 'src/components/admin/taxonomies/AddTaxonomyDialog.vue';
import { notifyError } from 'src/utils/notify';

const route = useRoute();
const router = useRouter();
const taxonomiesStore = useTaxonomiesStore();
const summaries = computed<TaxonomySummariesDto[]>(() => taxonomiesStore.summaries);
const taxonomyName = computed(() => route.params.name as string);
const showAddTaxonomy = ref(false);

function onAddTaxonomy() {
  showAddTaxonomy.value = true;
}

async function onClose() {
  showAddTaxonomy.value = false;
}

async function onUpdated(updated: TaxonomyDto) {
  if (updated) {
    taxonomiesStore
      .refreshSummaries()
      .then(() => router.push(`/admin/taxonomies/${updated.name}`))
      .catch(notifyError);
  }
}

</script>
