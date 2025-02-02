<template>
  <div class="row">
    <div class="col-12 col-md-6">
      <div class="q-mb-md">
        <q-btn :title="t('edit')" icon="edit" color="primary" size="sm" @click="onShowEdit" />
      </div>
      <fields-list :items="items" :dbobject="systemStore.generalConf" max-width="400"/>
    </div>

    <q-dialog v-model="showEdit">
      <q-card class="dialog-sm">
        <q-card-section>
          <div class="text-h6">{{ t('configuration') }}</div>
        </q-card-section>
        <q-separator />
        <q-card-section>
          <q-input
            v-model="config.name"
            dense
            :label="t('name')"
            :hint="t('app_configuration.name_hint')"
            class="q-mb-md"
          />
          <q-input
            v-model="config.publicURL"
            dense
            :label="t('public_url')"
            :hint="t('app_configuration.public_url_hint')"
            class="q-mb-md"
          />
          <q-input
            v-model="config.logoutURL"
            dense
            :label="t('logout_url')"
            :hint="t('app_configuration.logout_url_hint')"
            class="q-mb-md"
          />
          <q-select
            v-model="config.languages"
            :options="languages"
            dense
            multiple
            use-chips
            :label="t('laguages')"
            :hint="t('app_configuration.languages_hint')"
            class="q-mb-md"
          />
          <q-input
            v-model="config.defaultCharSet"
            dense
            :label="t('default_charset')"
            :hint="t('app_configuration.default_charset_hint')"
            class="q-mb-md"
          />
          <q-toggle v-model="config.enforced2FA" dense :label="t('enforced_2fa')" class="q-mb-sm" />
          <div class="text-hint q-mb-md">
            {{ t('app_configuration.enforced_2fa_hint') }}
          </div>
          <q-toggle v-model="config.allowRPackageManagement" dense :label="t('app_configuration.allow_r_packages_management')" class="q-mb-sm" />
          <div class="text-hint">
            {{ t('app_configuration.allow_r_packages_management_hint') }}
          </div>
        </q-card-section>
        <q-separator />
        <q-card-actions align="right" class="bg-grey-3"
          ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
          <q-btn flat :label="t('save')" color="primary" :disable="isConfigValid" @click="onSave" v-close-popup />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup lang="ts">
import FieldsList, { type FieldItem } from 'src/components/FieldsList.vue';

const { t } = useI18n();

const items: FieldItem[] = [
  { field: 'name',
    label: 'name',
    hint: 'app_configuration.name_hint'
  },
  {
    field: 'publicURL',
    label: 'public_url',
    hint: 'app_configuration.public_url_hint',
    html: (val) => (val.publicURL ? `<a href="${val.publicURL}" target="_blank">${val.publicURL}</a>` : '-'),
  },
  {
    field: 'logoutURL',
    label: 'logout_url',
    hint: 'app_configuration.logout_url_hint',
    html: (val) => (val.logoutURL ? `<a href="${val.logoutURL}" target="_blank">${val.logoutURL}</a>` : '-'),
  },
  {
    field: 'languages',
    label: 'laguages',
    hint: 'app_configuration.languages_hint',
    format: (val) => (val.languages ? (val.languages as string[]).join(', ') : '-'),
  },
  {
    field: 'defaultCharSet',
    label: 'default_charset',
    hint: 'app_configuration.default_charset_hint',
  },
  {
    field: 'enforced2FA',
    label: 'enforced_2fa',
    hint: 'app_configuration.enforced_2fa_hint',
    icon: (val) => (val.enforced2FA ? 'check' : 'close')
  },
  {
    field: 'allowRPackageManagement',
    label: 'app_configuration.allow_r_packages_management',
    hint: 'app_configuration.allow_r_packages_management_hint',
    icon: (val) => (val.allowRPackageManagement ? 'check' : 'close')
  },
];

// list of all the european two-letters language codes
const languages = [
  'bg',
  'cs',
  'da',
  'de',
  'el',
  'en',
  'es',
  'et',
  'fi',
  'fr',
  'ga',
  'hr',
  'hu',
  'it',
  'lt',
  'lv',
  'mt',
  'nl',
  'pl',
  'pt',
  'ro',
  'sk',
  'sl',
  'sv',
];
const systemStore = useSystemStore();

const showEdit = ref(false);
const config = ref({ ...systemStore.generalConf });

const isConfigValid = computed(() => {
  return !config.value.name || !config.value.defaultCharSet;
});

function onShowEdit() {
  config.value = { ...systemStore.generalConf };
  showEdit.value = true;
}

function onSave() {
  systemStore.saveGeneralConf(config.value);
}
</script>
