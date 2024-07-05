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
            :label="$t('name') + '*'"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredField('validation.identity_provider.name_required')]"
            :disable="editMode"
          >
          </q-input>
          <q-input
            v-model="newProvider.clientId"
            dense
            type="text"
            :label="$t('identity_provider.client_id') + '*'"
            :hint="$t('identity_provider.client_id_hint')"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredField('validation.identity_provider.client_id_required')]"
          >
          </q-input>
          <q-input
            v-model="newProvider.secret"
            dense
            type="text"
            :label="$t('identity_provider.secret') + '*'"
            :hint="$t('identity_provider.secret_hint')"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredField('validation.identity_provider.secret_required')]"
          >
          </q-input>
          <q-input
            v-model="newProvider.discoveryURI"
            dense
            type="text"
            :label="$t('identity_provider.discovery_uri') + '*'"
            class="q-mb-md"
            lazy-rules
            :rules="[
              validateRequiredField('validation.identity_provider.discovery_uri_required'),
              validateUri('validation.identity_provider.discovery_uri_format', true),
            ]"
          >
            <template v-slot:hint>
              <div v-html="$t('identity_provider.discovery_uri_hint', { url: discoveryURIDefinition })"></div>
            </template>
          </q-input>
          <q-input
            v-model="newProvider.label"
            dense
            type="text"
            :label="$t('label')"
            :hint="$t('identity_provider.label_hint')"
            class="q-mb-md"
            lazy-rules
          >
          </q-input>
          <q-input
            v-model="newProvider.providerUrl"
            dense
            type="text"
            :label="$t('identity_provider.provider_url')"
            :hint="$t('identity_provider.provider_url_hint')"
            class="q-mb-md"
            lazy-rules
            :rules="[validateUri('validation.identity_provider.provider_url_format', false)]"
          >
          </q-input>
          <q-input
            v-model="newProvider.groups"
            dense
            type="text"
            :label="$t('groups')"
            :hint="$t('identity_provider.groups_hint')"
            class="q-mb-md"
            lazy-rules
          >
          </q-input>
          <q-input
            v-model="newProvider.scope"
            dense
            type="text"
            :label="$t('identity_provider.scope')"
            class="q-mb-md"
            lazy-rules
            :rules="[(val) => true]"
          >
            <!-- NOTE: to render hint in a with HTML the fields needs rules -->
            <template v-slot:hint>
              <div v-html="$t('identity_provider.scope_hint', { openid: '<code>openid</code>' })"></div>
            </template>
          </q-input>
          <q-input
            v-model="newProvider.usernameClaim"
            dense
            type="text"
            :label="$t('identity_provider.username_claim')"
            :hint="$t('identity_provider.username_claim_hint')"
            class="q-mb-md"
            lazy-rules
          >
          </q-input>
          <q-select
            v-model="groupsMapping"
            :options="groupsMappingOptions"
            dense
            :label="$t('laguages')"
            class="q-mb-md q-pt-md"
          />
          <q-input
            v-if="isGroupsMappingByClaim"
            v-model="newProvider.groupsClaim"
            dense
            type="text"
            :placeholder="$t('groups')"
            :hint="$t('identity_provider.groups_claim_hint')"
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
            :hint="$t('identity_provider.groups_javascript_hint')"
            class="q-mb-md"
            lazy-rules
          >
          </q-input>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="submitCaption" type="submit" color="primary" @click="onAddProvider" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'AddIdentityProviderDialog',
});
</script>
<script setup lang="ts">
import { IDProviderDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';

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
  parameters: [],
  enabled: false,
} as IDProviderDto;

const groupsMappingOptions = ref([
  { label: t('identity_provider.groups_claim'), value: 'claim_value' },
  { label: t('identity_provider.groups_javascript'), value: 'javascript' },
]);
const groupsMapping = ref(groupsMappingOptions.value[0]);
const newProvider = ref<IDProviderDto>(emptyProvider);
const editMode = computed(() => !!props.provider && !!props.provider.name);
const submitCaption = computed(() => (editMode.value ? t('update') : t('add')));
const dialogTitle = computed(() => (editMode.value ? t('identity_provider_edit') : t('identity_provider_add')));
const isGroupsMappingByClaim = computed(() => groupsMapping.value.value === 'claim_value');
const groupsJavascriptPlaceholder = computed(() => {
  return `// input: userInfo
// output: an array of strings

// example:
userInfo.some.property.map(x => x.split(':')[0])
`;
});
const discoveryURIDefinition = computed(
  () =>
    `<a href="https://openid.net/specs/openid-connect-discovery-1_0.html" target="_blank" tabindex="-1">${t(
      'identity_provider.discovery_uri_definition'
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
      } else {
        newProvider.value = { ...emptyProvider };
      }

      showDialog.value = value;
    }
  }
);

function onHide() {
  console.log('onHide');
  newProvider.value = { ...emptyProvider };
  emit('update:modelValue', false);
}

async function onAddProvider() {
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
