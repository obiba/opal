<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card class="dialog-md">
      <q-card-section>
        <div class="text-h6">{{ t('user_profile.update_password') }}</div>
      </q-card-section>

      <q-separator />
      <q-card-section>
        <div class="q-mb-lg text-help">{{ t('user_profile.password_dialog.info') }}</div>
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-input
            dense
            autocomplete="off"
            type="password"
            :label="t('user_profile.password_dialog.old_password') + ' *'"
            v-model="password.oldPassword"
            color="grey-10"
            lazy-rules
            :rules="[validateRequiredOldPassword]"
          >
            <template v-slot:prepend>
              <q-icon name="fas fa-lock" size="12px" />
            </template>
          </q-input>

          <q-input
            dense
            autocomplete="off"
            type="password"
            :label="t('user_profile.password_dialog.new_password') + ' *'"
            v-model="password.newPassword"
            color="grey-10"
            lazy-rules
            :rules="[validateRequiredNewPassword]"
          >
            <template v-slot:prepend>
              <q-icon name="fas fa-lock" size="12px" />
            </template>
          </q-input>

          <q-input
            dense
            autocomplete="off"
            v-model="confirmPassword"
            type="password"
            :label="t('password_confirm') + '*'"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredConfirmPassword, validateMatchingPasswords]"
          >
            <template v-slot:prepend>
              <q-icon name="fas fa-lock" size="12px" />
            </template>
          </q-input>
        </q-form>
      </q-card-section>
      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('update')" type="submit" color="primary" @click="onUpdatePassword" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { PasswordDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';

interface DialogProps {
  modelValue: boolean;
  name: string;
}

const usersStore = useUsersStore();
const { t } = useI18n();
const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const formRef = ref();
const emit = defineEmits(['update:modelValue']);
const confirmPassword = ref('');
const password = ref<PasswordDto>({
  name: props.name,
  newPassword: '',
  oldPassword: '',
});

const validateRequiredOldPassword = (val: string) => val || t('validation.update_password.old_password');
const validateRequiredNewPassword = (val: string) =>
  (val && val.length >= 8) || t('validation.update_password.new_password');
const validateMatchingPasswords = () =>
  password.value.newPassword === confirmPassword.value || t('validation.user.passwords_not_matching');
const validateRequiredConfirmPassword = (val: string) =>
  (val && val.trim().length > 0) || t('validation.user.confirm_password_required');

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      showDialog.value = value;
    }
  }
);

function onHide() {
  confirmPassword.value = '';
  password.value = {
    name: props.name,
    newPassword: '',
    oldPassword: '',
  } as PasswordDto;

  emit('update:modelValue', false);
}

async function onUpdatePassword() {
  const valid = await formRef.value.validate();
  if (valid) {
    usersStore
      .updateCurrentPassword(password.value)
      .then(() => {
        showDialog.value = false;
      })
      .catch(notifyError);
  }
}
</script>
