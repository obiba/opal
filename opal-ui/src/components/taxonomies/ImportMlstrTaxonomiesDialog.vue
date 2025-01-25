<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('taxonomy.import_mr.title') }}></div>
      </q-card-section>

      <q-separator />

      <q-card-section class="text-help">
        <span v-html="t('taxonomy.import_mr.info', { mlstr_url: mlstr_url, mica_url: mica_url })"></span>
      </q-card-section>

      <q-card-section style="max-height: 75vh" class="scroll">
        <q-form class="q-gutter-md" persistent>
          <q-select
            emit-value
            v-model="version"
            :label="t('taxonomy.import_mr.versions')"
            :options="versions"
            input-debounce="0"
            :rules="[() => version]"
          >
            <template v-slot:hint>
              <html-anchor-hint
                :tr-key="'taxonomy.import_mr.versions_hint'"
                :text="t('taxonomy.import_mr.versions_hint_url')"
                :url="licenseUrl"
              />
            </template>
          </q-select>

          <a :href="licenseUrl" target="_blank" class="flex q-mt-md"
            ><img
              :alt="t('taxonomy.creative_commons_licenses')"
              src="https://i.creativecommons.org/l/by-nc-nd/4.0/88x31.png"
            />
          </a>

          <q-checkbox
            class="q-ml-sm"
            v-model="acceptedAgreement"
            :label="t('taxonomy.import_mr.license_agreement')"
            :disable="version.length === 0"
          />
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup @click="onHide" />
        <q-btn
          flat
          :label="t('import')"
          type="submit"
          color="primary"
          :disable="!acceptedAgreement"
          @click="onImportTaxonomy"
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { notifyError } from 'src/utils/notify';
import type { VcsTagsInfoDto } from 'src/models/Opal';
import HtmlAnchorHint from 'src/components/HtmlAnchorHint.vue';

interface DialogProps {
  modelValue: boolean;
}

interface VersionOptions {
  label: string;
  value: string;
}

const { t, locale } = useI18n();
const taxonomiesStore = useTaxonomiesStore();
const version = ref('');
const acceptedAgreement = ref(false);
const versions = ref<VersionOptions[]>([]);
const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'updated']);
const showDialog = ref(props.modelValue);
const mlstr_url = computed(() => {
  return '<a href="https://www.maelstrom-research.org" target="_blank">Maelstrom Research</a>';
});
const mica_url = computed(() => {
  return '<a href="http://www.obiba.org/pages/products/mica/" target="_blank">Mica</a>';
});
const licenseUrl = computed(() => {
  return `https://creativecommons.org/licenses/by-nc-nd/4.0/deed.${locale.value}`;
});

// Handlers

function onHide() {
  showDialog.value = false;
  versions.value = [];
  version.value = '';
  acceptedAgreement.value = false;
  emit('update:modelValue', false);
}

async function onImportTaxonomy() {
  try {
    await taxonomiesStore.importMlstrTaxonomies(version.value);
    onHide();
    emit('updated');
  } catch (error) {
    notifyError(error);
  }
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      taxonomiesStore
        .getMlstrTaxonomies()
        .then((response: VcsTagsInfoDto) => {
          versions.value = (response.names || []).map((v) => ({ label: v, value: v } as VersionOptions));
          if (versions.value.length > 0 && versions.value[0]) {
            versions.value[0].label = `${versions.value[0].label} (${t('latest').toLowerCase()})`;
            version.value = versions.value[0].value;
          }

          showDialog.value = value;
        })
        .catch(notifyError);
    }
  }
);
</script>
