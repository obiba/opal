<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card>
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
            :label="$t('host')"
            :hint="$t('apps.host_hint')"
            :disable="editMode"
            style="min-width: 300px"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredField, validateUri]"
          >
          </q-input>

          <!-- <q-select
            v-model="newConfig.database"
            :options="databases"
            dense
            :label="$t('database')"
            :disable="hasTables"
            class="q-mb-md q-pt-md"
            emit-value
            map-options
          /> -->
          <pre>{{ newRockAppConfig }}</pre>
          <app-credentials-form v-model="newRockAppConfig.administratorCredentials" />
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="submitCaption" type="submit" color="primary" @click="onAddConfig" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
export default defineComponent({
  name: 'AddRServerDialog',
});
</script>
<script setup lang="ts">
import { notifyError } from 'src/utils/notify';
import { RockAppConfigDto, AppsConfigDto, AppCredentialsDto} from 'src/models/Apps';
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

const editMode = computed(() => !!props.rockAppConfig && !!props.rockAppConfig.host);
const submitCaption = computed(() => (editMode.value ? t('update') : t('add')));
const dialogTitle = computed(() => (editMode.value ? t('apps.edit_service') : t('apps.add_service')));

// Validators
const validateRequiredField = (val: string) => (val && val.trim().length > 0) || t('validation.name_required');
const validateUri = (id: string, required: boolean) => (val: string) => {
  if (!val && !required) {
    return true;
  }

  return new RegExp(/^(http|https):\/\/[^ "]+$/).test(val) || t(id);
};
// Handlers

function onHide() {
  newRockAppConfig.value = { ...emptyRockAppConfig };
  showDialog.value = false;
  emit('update:modelValue', false);
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      if (props.rockAppConfig) {
        newRockAppConfig.value = props.rockAppConfig;
      } else {
        newRockAppConfig.value = { ...emptyRockAppConfig };
      }

      showDialog.value = value;
    }
  }
);

async function onAddConfig() {
  const valid = await formRef.value.validate();
  if (valid) {
    try {
      const newConfig = {...props.config};
      if (!editMode.value) {
        newConfig.rockConfigs.push(newRockAppConfig.value);
      }

      await appsStore.updateConfig(newConfig);
      emit('update');
      onHide();
    } catch (err) {
      notifyError(err);
    }
  }
}
</script>
