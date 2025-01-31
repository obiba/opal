<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ dialogTitle }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section style="max-height: 75vh" class="scroll">
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-input
            v-model="newProvider.name"
            dense
            type="text"
            :label="t('name') + '*'"
            :hint="t('identity_provider.name_hint')"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredField('validation.name_required')]"
            :disable="editMode"
          >
          </q-input>
          <q-input
            v-model="newProvider.clientId"
            dense
            type="text"
            :label="t('identity_provider.client_id') + '*'"
            :hint="t('identity_provider.client_id_hint')"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredField('validation.identity_provider.client_id_required')]"
          >
          </q-input>
          <q-input
            v-model="newProvider.secret"
            dense
            type="text"
            :label="t('identity_provider.secret') + '*'"
            :hint="t('identity_provider.secret_hint')"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredField('validation.identity_provider.secret_required')]"
          >
          </q-input>
          <q-input
            v-model="newProvider.discoveryURI"
            dense
            type="text"
            :label="t('identity_provider.discovery_uri') + '*'"
            class="q-mb-md"
            lazy-rules
            :rules="[
              validateRequiredField('validation.identity_provider.discovery_uri_required'),
              validateUri('validation.identity_provider.discovery_uri_format', true),
            ]"
          >
            <template v-slot:hint>
              <html-anchor-hint
                :tr-key="'identity_provider.discovery_uri_hint'"
                :text="t('identity_provider.discovery_uri_definition')"
                :url="`https://openid.net/specs/openid-connect-discovery-1_0.html`"
              />
            </template>
          </q-input>
          <q-input
            v-model="newProvider.label"
            dense
            type="text"
            :label="t('label')"
            :hint="t('identity_provider.label_hint')"
            class="q-mb-md"
            lazy-rules
          >
          </q-input>
          <q-input
            v-model="newProvider.providerUrl"
            dense
            type="text"
            :label="t('identity_provider.provider_url')"
            :hint="t('identity_provider.provider_url_hint')"
            class="q-mb-md"
            lazy-rules
            :rules="[validateUri('validation.identity_provider.provider_url_format', false)]"
          >
          </q-input>
          <q-input
            v-model="newProvider.groups"
            dense
            type="text"
            :label="t('groups')"
            :hint="t('identity_provider.groups_hint')"
            class="q-mb-md"
            lazy-rules
          >
          </q-input>
          <q-input
            v-model="newProvider.scope"
            dense
            type="text"
            :label="t('identity_provider.scope')"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredField('validation.identity_provider.scope_required')]"
          >
            <!-- NOTE: to render hint in a with HTML the fields needs rules -->
            <template v-slot:hint>
              <div v-html="t('identity_provider.scope_hint', { openid: '<code>openid</code>' })"></div>
            </template>
          </q-input>
          <q-input
            v-model="newProvider.usernameClaim"
            dense
            type="text"
            :label="t('identity_provider.username_claim')"
            :hint="t('identity_provider.username_claim_hint')"
            class="q-mb-md"
            lazy-rules
          >
          </q-input>
          <q-select
            v-model="groupsMapping"
            :options="groupsMappingOptions"
            dense
            :label="t('identity_provider.groups_mapping')"
            class="q-mb-md q-pt-md"
            emit-value
            map-options
          />
          <q-input
            v-if="groupsMapping !== 'groupsScript'"
            v-model="newProvider.groupsClaim"
            dense
            type="text"
            :placeholder="t('groups')"
            :hint="t('identity_provider.groups_claim_hint')"
            class="q-mb-md"
            lazy-rules
          >
          </q-input>
          <q-input
            v-else
            v-model="newProvider.groupsScript"
            dense
            rows="6"
            type="textarea"
            :placeholder="groupsJavascriptPlaceholder"
            :hint="t('identity_provider.groups_javascript_hint')"
            class="q-mb-md"
            lazy-rules
          >
          </q-input>

          <div class="q-py-md">
            <q-list>
              <q-expansion-item
                dense
                switch-toggle-side
                header-class="text-primary text-caption q-pl-none"
                :label="t('advanced_options')"
              >
                <q-checkbox dense v-model="newProvider.useNonce" class="q-my-md">
                  <template v-slot:default>
                    <span
                      class="text-secondary"
                      v-html="t('identity_provider.use_nonce', { url: useNonceDefinition })"
                      @click.stop="openWindow('https://openid.net/specs/openid-connect-core-1_0.html#IDToken')"
                    ></span>
                  </template>
                </q-checkbox>
                <q-checkbox dense v-model="newProvider.useLogout" class="q-mb-md">
                  <template v-slot:default>
                    <span
                      class="text-secondary"
                      v-html="t('identity_provider.use_logout', { url: useLogoutDefinition })"
                      @click.stop="openWindow('https://openid.net/specs/openid-connect-session-1_0-17.html#RPLogout')"
                    ></span>
                  </template>
                </q-checkbox>
                <q-input
                  v-model.number="newProvider.connectTimeout"
                  dense
                  type="number"
                  :label="t('identity_provider.connect_timeout')"
                  :hint="t('identity_provider.connect_timeout_hint')"
                  class="q-mb-md"
                  lazy-rules
                >
                </q-input>
                <q-input
                  v-model.number="newProvider.readTimeout"
                  dense
                  type="number"
                  :label="t('identity_provider.read_timeout')"
                  :hint="t('identity_provider.read_timeout_hint')"
                  class="q-mb-md"
                  lazy-rules
                >
                </q-input>
                <q-input
                  v-model="newProvider.callbackURL"
                  dense
                  type="text"
                  :label="t('identity_provider.callback_url')"
                  :hint="t('identity_provider.callback_url_hint')"
                  class="q-mb-md"
                  lazy-rules
                  :rules="[validateUri('validation.identity_provider.callback_url_format', false)]"
                >
                </q-input>
              </q-expansion-item>
            </q-list>
          </div>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="submitCaption" type="submit" color="primary" @click="onAddProvider" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { IDProviderDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';
import HtmlAnchorHint from 'src/components/HtmlAnchorHint.vue';

interface DialogProps {
  modelValue: boolean;
  provider: IDProviderDto | null;
}

const { t } = useI18n();
const identityProvidersStore = useIdentityProvidersStore();
const formRef = ref();
const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);
const showDialog = ref(props.modelValue);
const emptyProvider = {
  name: '',
  clientId: '',
  secret: '',
  discoveryURI: '',
  scope: 'openid',
  useNonce: true,
  useLogout: true,
  parameters: [],
  enabled: false,
  connectTimeout: 0,
  readTimeout: 0,
} as IDProviderDto;

const groupsMappingOptions = [
  { label: t('identity_provider.groups_claim'), value: 'groupsClaim' },
  { label: t('identity_provider.groups_javascript'), value: 'groupsScript' },
];
const groupsMapping: Ref<string | null> = ref(null);
const newProvider = ref<IDProviderDto>({ ...emptyProvider });
const editMode = computed(() => props.provider?.name !== undefined);
const submitCaption = computed(() => (editMode.value ? t('update') : t('add')));
const dialogTitle = computed(() => (editMode.value ? t('identity_provider_edit') : t('identity_provider_add')));
const groupsJavascriptPlaceholder = computed(() => {
  return `// input: userInfo
// output: an array of strings

// example:
userInfo.some.property.map(x => x.split(':')[0])
`;
});

const useNonceDefinition = computed(
  () =>
    `<a href="https://openid.net/specs/openid-connect-core-1_0.html#IDToken" target="_blank" tabindex="-1">${t(
      'identity_provider.use_nonce_definition'
    )}</a>`
);
const useLogoutDefinition = computed(
  () =>
    `<a href="https://openid.net/specs/openid-connect-session-1_0-17.html#RPLogout" target="_blank" tabindex="-1">${t(
      'identity_provider.use_logout_definition'
    )}</a>`
);

// Validation rules
const validateRequiredField = (id: string) => (val: string) => (val && val.trim().length > 0) || t(id);
const validateUri = (id: string, required: boolean) => (val: string) => {
  if (!val && !required) {
    return true;
  }

  return new RegExp(/^(http|https):\/\/[^ "]+$/).test(val) || t(id);
};

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      if (props.provider) {
        newProvider.value = { ...props.provider };
        if (newProvider.value.groupsClaim || newProvider.value.groupsScript) {
          groupsMapping.value = newProvider.value.groupsClaim ? 'groupsClaim' : 'groupsScript';
        }
      } else {
        newProvider.value = { ...emptyProvider };
      }

      showDialog.value = value;
    }
  }
);

function openWindow(url: string) {
  window.open(url, '_blank');
}

function onHide() {
  newProvider.value = { ...emptyProvider };
  groupsMapping.value = 'groupsClaim';
  emit('update:modelValue', false);
}

function cleanupFields() {
  if (!newProvider.value.label || newProvider.value.label.trim().length === 0) delete newProvider.value.label;
  if (!newProvider.value.providerUrl || newProvider.value.providerUrl.trim().length === 0)
    delete newProvider.value.providerUrl;
  if (!newProvider.value.groups || newProvider.value.groups.trim().length === 0) delete newProvider.value.groups;
  if (!newProvider.value.groupsClaim || newProvider.value.groupsClaim.trim().length === 0)
    delete newProvider.value.groupsClaim;
  if (!newProvider.value.groupsScript || newProvider.value.groupsScript.trim().length === 0)
    delete newProvider.value.groupsScript;

  if (groupsMapping.value === 'groupsClaim') {
    delete newProvider.value.groupsScript;
  } else {
    delete newProvider.value.groupsClaim;
  }

  if (!newProvider.value.usernameClaim || newProvider.value.usernameClaim.trim().length === 0)
    delete newProvider.value.usernameClaim;
  if (!newProvider.value.callbackURL || newProvider.value.callbackURL.trim().length === 0)
    delete newProvider.value.callbackURL;
}

async function onAddProvider() {
  cleanupFields();
  const valid = await formRef.value.validate();
  if (valid) {
    (editMode.value
      ? identityProvidersStore.updateProvider(newProvider.value)
      : identityProvidersStore.addProvider(newProvider.value)
    )
      .then(() => {
        newProvider.value = { ...emptyProvider };
        showDialog.value = false;
      })
      .catch(notifyError);
  }
}
</script>
