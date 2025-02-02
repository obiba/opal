<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ dialogTitle }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-input
            v-model="newRockAppConfig.host"
            dense
            type="text"
            :label="t('host') + ' *'"
            :hint="t('apps.host_hint')"
            :disable="editMode"
            style="min-width: 300px"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredField('validation.apps.host_required'), validateUri]"
          >
          </q-input>

          <q-select
            v-model="credentialType"
            :options="credentialOptions"
            dense
            :label="t('credentials')"
            :hint="credentialTypeHint"
            class="q-mb-lg q-pt-md"
            emit-value
            map-options
            @update:model-value="onUpdateCredentialType"
          />

          <q-card flat v-if="adminCredentials">
            <q-card-section class="q-px-none">
              <div class="text-bold">{{ t('administrator') }}</div>
              <app-credentials-form type="administrator" v-model="newRockAppConfig.administratorCredentials" />
            </q-card-section>
          </q-card>
          <q-card flat v-else-if="managerCredentials">
            <q-card-section class="q-px-none">
              <div class="text-bold">{{ t('manager') }}</div>
              <app-credentials-form type="manager" v-model="newRockAppConfig.managerCredentials" />
            </q-card-section>
            <q-card-section class="q-px-none">
              <div class="text-bold">{{ t('user') }}</div>
              <app-credentials-form type="user" v-model="newRockAppConfig.userCredentials" />
            </q-card-section>
          </q-card>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="submitCaption" type="submit" color="primary" @click="onAddConfig" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { notifyError } from 'src/utils/notify';
import type { RockAppConfigDto, AppsConfigDto, AppCredentialsDto } from 'src/models/Apps';
import AppCredentialsForm from 'src/components/admin/apps/AppCredentialsForm.vue';

interface DialogProps {
  modelValue: boolean;
  config: AppsConfigDto;
  rockAppConfig?: RockAppConfigDto;
}

const emptyRockAppConfig: RockAppConfigDto = {
  host: '',
  administratorCredentials: {} as AppCredentialsDto,
  managerCredentials: {} as AppCredentialsDto,
  userCredentials: {} as AppCredentialsDto,
} as RockAppConfigDto;

const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue', 'update']);
const appsStore = useAppsStore();
const { t } = useI18n();
const formRef = ref();
const showDialog = ref(props.modelValue);
const newRockAppConfig = ref<RockAppConfigDto>({} as RockAppConfigDto);
const credentialOptions = [
  {
    label: t('default'),
    value: '',
  },
  {
    label: t('administrator'),
    value: 'administrator',
  },
  {
    label: t('apps.manager_user'),
    value: 'manager_user',
  },
];
const credentialType = ref(credentialOptions[0]?.value);
const credentialTypeHint = computed(() =>
  credentialType.value ? t(`apps.credential_hints.${credentialType.value}`) : t('apps.credential_hints.default')
);
const adminCredentials = computed(() => credentialType.value === 'administrator');
const managerCredentials = computed(() => credentialType.value === 'manager_user');
const editMode = computed(() => props.rockAppConfig !== undefined && props.rockAppConfig.host !== undefined);
const submitCaption = computed(() => (editMode.value ? t('update') : t('add')));
const dialogTitle = computed(() => (editMode.value ? t('apps.edit_service') : t('apps.add_service')));

// Validators
const validateRequiredField = (id: string) => (val: string) => (val && val.trim().length > 0) || t(id);
const validateUri = (val: string) => {
  if (!val) {
    return true;
  }

  return new RegExp(/^(http|https):\/\/[^ "]+$/).test(val) || t('validation.apps.host_uri_format');
};
// Handlers

function onHide() {
  newRockAppConfig.value = { ...emptyRockAppConfig };
  credentialType.value = credentialOptions[0]?.value;
  showDialog.value = false;
  emit('update:modelValue', false);
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      if (props.rockAppConfig) {
        newRockAppConfig.value = { ...props.rockAppConfig };
        if (props.rockAppConfig.administratorCredentials) {
          credentialType.value = 'administrator';
        } else if (props.rockAppConfig.managerCredentials || props.rockAppConfig.userCredentials) {
          credentialType.value = 'manager_user';
        } else {
          credentialType.value = '';
        }
      } else {
        newRockAppConfig.value = { ...emptyRockAppConfig };
        credentialType.value = credentialOptions[0]?.value;
      }

      showDialog.value = value;
    }
  }
);

function onUpdateCredentialType(value: string) {
  if (value === '') {
    delete newRockAppConfig.value.managerCredentials;
    delete newRockAppConfig.value.userCredentials;
    delete newRockAppConfig.value.administratorCredentials;
  }
}

async function onAddConfig() {
  const valid = await formRef.value.validate();
  if (valid) {
    try {
      const newConfig = { ...props.config };
      if (editMode.value) {
        newConfig.rockConfigs = newConfig.rockConfigs.filter((c) => c.host !== newRockAppConfig.value.host);
      }

      newConfig.rockConfigs = newConfig.rockConfigs || [];
      newConfig.rockConfigs.push(newRockAppConfig.value);
      await appsStore.updateConfig(newConfig);
      emit('update');
      onHide();
    } catch (err) {
      notifyError(err);
    }
  }
}
</script>
