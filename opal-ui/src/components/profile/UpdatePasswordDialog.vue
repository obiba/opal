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
            autocomplete="new-password"
            type="password"
            :label="t('user_profile.password_dialog.old_password') + ' *'"
            v-model="password.oldPassword"
            color="grey-10"
            lazy-rules
            :rules="[validateRequiredOldPassword]"
          />

          <q-input
              v-model="password.newPassword"
              :label="t('password') + ' *'"
              :type="passwordVisible ? 'text' : 'password'"
              dense
              lazy-rules
              autocomplete="new-password"
              :rules="[validateRequiredNewPassword]"
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
import { notifyError, notifyInfo } from 'src/utils/notify';
import { copyToClipboard } from 'quasar';

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
const password = ref<PasswordDto>({
  name: props.name,
  newPassword: '',
  oldPassword: '',
});
const passwordVisible = ref(false);

const validateRequiredOldPassword = (val: string) =>
  (val && val.length > 0) || t('validation.update_password.old_password');
const validateRequiredNewPassword = (val: string) =>
  (val && val.length >= 8) || t('validation.update_password.new_password');

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      showDialog.value = value;
    }
  },
);

function onHide() {
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

function generatePassword() {
  password.value.newPassword = usersStore.generatePassword();
}

function copyPasswordToClipboard() {
  if (password.value.newPassword) {
    copyToClipboard(password.value.newPassword).then(() => {
      notifyInfo(t('password_copied'));
    });
  }
}
</script>
