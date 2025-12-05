<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ dialogTitle }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="formRef" persistent>
          <q-input
            v-model="newUser.name"
            autocomplete="nope"
            dense
            type="text"
            :label="t('name') + ' *'"
            lazy-rules
            :rules="[validateRequiredName]"
            :disable="editMode"
          >
          </q-input>
          <template v-if="authPassword">
            <q-input
              v-model="newUser.password"
              :label="t('password') + (editMode ? '' : ' *')"
              :type="passwordVisible ? 'text' : 'password'"
              dense
              lazy-rules
              autocomplete="new-password"
              :rules="[validateRequiredPassword]"
            >
              <template v-slot:after>
                <q-btn
                  round
                  dense
                  size="sm"
                  :title="t('validation.user.show_password')"
                  flat
                  icon="visibility"
                  @click="passwordVisible = !passwordVisible"
                />
                <q-btn
                  round
                  dense
                  size="sm"
                  :title="t('validation.user.copy_password')"
                  flat
                  icon="content_copy"
                  @click="copyPasswordToClipboard"
                />
                <q-btn
                  round
                  dense
                  size="sm"
                  :title="t('validation.user.generate_password')"
                  flat
                  icon="lock_reset"
                  @click="generatePassword"
                />
              </template>
            </q-input>
            <div class="text-hint">
              {{ t('password_hint') }}
            </div>
            <div v-if="editMode" class="text-hint">
              {{ t('edit_password_hint') }}
            </div>
          </template>
          <template v-else>
            <q-input
              v-model="userCertificate"
              dense
              rows="10"
              type="textarea"
              :label="t('certificate') + '*'"
              :placeholder="t('certificate_placeholder')"
              lazy-rules
              :rules="[validateRequiredCertificate]"
            >
            </q-input>
          </template>
          <q-select
            v-model="newUser.groups"
            use-input
            use-chips
            multiple
            input-debounce="0"
            :hint="t('groups_hint')"
            @new-value="addGroup"
            :options="groupFilters"
            @filter="filterGroups"
          ></q-select>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="submitCaption" type="submit" color="primary" @click="onAddUser" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { type SubjectCredentialsDto, SubjectCredentialsDto_AuthenticationType } from 'src/models/Opal';
import { notifyError, notifyInfo } from 'src/utils/notify';
import { copyToClipboard } from 'quasar';

interface DialogProps {
  modelValue: boolean;
  user: SubjectCredentialsDto | null;
  authenticationType: SubjectCredentialsDto_AuthenticationType;
}

const { t } = useI18n();
const usersStore = useUsersStore();
const groupsStore = useGroupsStore();

const formRef = ref();
const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);
const showDialog = ref(props.modelValue);
const passwordVisible = ref(false);

const newUser = ref<SubjectCredentialsDto>({
  name: '',
  authenticationType: SubjectCredentialsDto_AuthenticationType.PASSWORD,
  enabled: true,
  groups: [],
} as SubjectCredentialsDto);

const MIN_PASSWORD_LENGTH = 8;
const confirmPassword = ref('');
const certificate = ref('');
let groupFilterOptions = Array<string>();
const groupFilters = ref(Array<string>());

const userCertificate = computed({
  get() {
    return certificate.value;
  },
  set(value) {
    certificate.value = value;
    // need to send string and not bytes (Uint8Array)
    newUser.value.certificate = new TextEncoder().encode(value);
  },
});

const authPassword = computed(() => props.authenticationType === SubjectCredentialsDto_AuthenticationType.PASSWORD);
const editMode = computed(() => props.user?.name !== undefined);
const submitCaption = computed(() => (editMode.value ? t('update') : t('add')));
const dialogTitle = computed(() => (editMode.value ? t('user_edit') : t('user_add')));

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function filterGroups(val: string, update: any) {
  update(() => {
    if (val.trim().length === 0) {
      groupFilters.value = [...groupFilterOptions];
    } else {
      const needle = val.toLowerCase();
      groupFilters.value = groupFilters.value.filter((v) => v.toLowerCase().indexOf(needle) > -1);
    }
  });
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function addGroup(val: string, done: any) {
  if (val.trim().length > 0) {
    const modelValue = (newUser.value.groups || []).slice();
    if (groupFilterOptions.includes(val) === false) {
      groupFilterOptions.push(val);
      groupFilters.value = [...groupFilterOptions];
    }
    if (modelValue.includes(val) === false) {
      modelValue.push(val);
    }

    done(null);
    newUser.value.groups = modelValue;
  }
}

// Validation rules
const validateRequiredName = (val: string) => (val && val.trim().length > 0) || t('validation.name_required');
const validateRequiredCertificate = (val: string) =>
  (editMode.value && (!val || val.length === 0)) ||
  (val && val.trim().length > 0) ||
  t('validation.user.certificate_required');
const validateRequiredPassword = (val: string) =>
  (editMode.value && (!val || val.length === 0)) || (val && val.length >= MIN_PASSWORD_LENGTH) || t('validation.password_min_length', { min: MIN_PASSWORD_LENGTH });

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      if (props.user) {
        newUser.value = { ...props.user };
      } else {
        newUser.value = {
          name: '',
          authenticationType: props.authenticationType,
          enabled: true,
          groups: [],
        } as SubjectCredentialsDto;
      }

      groupFilterOptions = groupsStore.groups.map((g) => g.name) || [];
      groupFilters.value = [...groupFilterOptions];
      showDialog.value = value;
    }
  },
);

function onHide() {
  confirmPassword.value = '';
  newUser.value = {
    name: '',
    authenticationType: props.authenticationType,
    enabled: true,
    groups: [],
  } as SubjectCredentialsDto;

  emit('update:modelValue', false);
}

async function onAddUser() {
  const valid = await formRef.value.validate();
  if (valid) {
    (editMode.value ? usersStore.updateUser(newUser.value) : usersStore.addUser(newUser.value))
      .then(() => {
        confirmPassword.value = '';
        groupFilterOptions = [];
        groupFilters.value = [];
        certificate.value = '';
        showDialog.value = false;
      })
      .catch(notifyError);
  }
}

function generatePassword() {
  newUser.value.password = usersStore.generatePassword();
}

function copyPasswordToClipboard() {
  if (newUser.value.password) {
    copyToClipboard(newUser.value.password).then(() => {
      notifyInfo(t('password_copied'));
    });
  }
}
</script>
