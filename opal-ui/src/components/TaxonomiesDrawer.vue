<template>
  <div>
    <h6 class="q-mt-none q-mb-none q-pa-md">
      <q-btn
        flat
        round
        dense
        icon="arrow_back"
        to="/" />
      <span class="q-ml-md">
        {{ t('taxonomies') }}
      </span>
    </h6>
    <q-list>
      <q-item
        v-for="summary in summaries"
        :active="taxonomyName === summary.name"
        :key="summary.name"
        :to="`/taxonomy/${summary.name}`"
      >
        <q-item-section avatar>
          <q-icon name="sell" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ summary.name }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item v-if="taxonomiesStore.canAdd" class="q-mt-md">
        <q-btn-dropdown
          no-caps
          color="primary"
          :label="t('taxonomy.add')"
          :title="t('user_profile.add_token')"
          icon="add"
          size="sm"
        >
          <q-list>
            <q-item clickable v-close-popup @click.prevent="onAddTaxonomy">
              <q-item-section>
                <q-item-label>{{ t('taxonomy.new_taxonomy') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item clickable v-close-popup @click.prevent="onImportMlstrTaxonomies">
              <q-item-section>
                <q-item-label>{{ t('taxonomy.import_mr.label') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item clickable v-close-popup @click.prevent="onImportGithubTaxonomies">
              <q-item-section>
                <q-item-label>{{ t('taxonomy.import_gh.label') }}</q-item-label>
              </q-item-section>
            </q-item>
            <q-item clickable v-close-popup @click.prevent="onImportFileTaxonomy">
              <q-item-section>
                <q-item-label>{{ t('taxonomy.import.label') }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-btn-dropdown>
      </q-item>
    </q-list>
  </div>

  <add-taxonomy-dialog v-model="showAddTaxonomy" :taxonomy="null" @update:modelValue="onClose" @updated="onAdded" />

  <import-mlstr-taxonomies-dialog
    v-model="showImportMlstr"
    @update:modelValue="onCloseMlstr"
    @updated="onImportedTaxonomies"
  />

  <import-github-taxonomies-dialog
    v-model="showImportGithub"
    @update:modelValue="onCloseGithub"
    @updated="onImportedTaxonomies"
  />

  <import-taxonomy-file-dialog
    v-model="showImportFile"
    @update:modelValue="onCloseFile"
    @updated="onImportedTaxonomies"
  />
</template>

<script setup lang="ts">
import type { TaxonomiesDto_TaxonomySummaryDto as TaxonomySummariesDto, TaxonomyDto } from 'src/models/Opal';
import AddTaxonomyDialog from 'src/components/taxonomies/AddTaxonomyDialog.vue';
import ImportMlstrTaxonomiesDialog from 'src/components/taxonomies/ImportMlstrTaxonomiesDialog.vue';
import ImportGithubTaxonomiesDialog from 'src/components/taxonomies/ImportGithubTaxonomiesDialog.vue';
import ImportTaxonomyFileDialog from 'src/components/taxonomies/ImportTaxonomyFileDialog.vue';
import { notifyError } from 'src/utils/notify';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const taxonomiesStore = useTaxonomiesStore();
const summaries = computed<TaxonomySummariesDto[]>(() => taxonomiesStore.summaries);
const taxonomyName = computed(() => route.params.name as string);
const showAddTaxonomy = ref(false);
const showImportMlstr = ref(false);
const showImportGithub = ref(false);
const showImportFile = ref(false);

function onAddTaxonomy() {
  showAddTaxonomy.value = true;
}

function onImportMlstrTaxonomies() {
  showImportMlstr.value = true;
}

function onImportGithubTaxonomies() {
  showImportGithub.value = true;
}

function onImportFileTaxonomy() {
  showImportFile.value = true;
}

async function onClose() {
  showAddTaxonomy.value = false;
}

async function onCloseMlstr() {
  showImportMlstr.value = false;
}

async function onCloseGithub() {
  showImportGithub.value = false;
}

async function onCloseFile() {
  showImportFile.value = false;
}

async function onAdded(updated: TaxonomyDto) {
  if (updated) {
    taxonomiesStore
      .refreshSummaries()
      .then(() => router.push(`/taxonomy/${updated.name}`))
      .catch(notifyError);
  }
}

async function onImportedTaxonomies() {
  taxonomiesStore
    .refreshSummaries()
    .then(() => {
      // NOTE: trick router to reload the taxonomies, simple push/replace had no effect
      router.push('/admin').then(() => router.push('/taxonomies'));
    })
    .catch(notifyError);
}
</script>
