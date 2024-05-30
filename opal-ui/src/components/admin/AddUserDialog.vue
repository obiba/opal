<template>
  <q-dialog v-model="showDialog" @hide="onHide" persistent>
    <q-card style="width: 60vw">
      <q-card-section>
        <div class="text-h6">{{ dialogTitle }}</div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <q-form ref="formRef" class="q-gutter-md" persistent>
          <q-input
            v-model="newUser.name"
            dense
            type="text"
            :label="$t('name') + '*'"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredName]"
          >
          </q-input>

          <q-input
            autocomplete="off"
            type="password"
            :label="$t('password') + '*'"
            v-model="newUser.password"
            color="grey-10"
            :hint="$t('password_hint')"
            lazy-rules
            :rules="[validateRequiredPassword]"
          >
            <template v-slot:prepend>
              <q-icon name="fas fa-lock" size="xs" />
            </template>
          </q-input>

          <q-input
            autocomplete="off"
            v-model="confirmPassword"
            dense
            type="password"
            :label="$t('password_confirm') + '*'"
            class="q-mb-md"
            lazy-rules
            :rules="[validateRequiredConfirmPassword, validateMatchingPasswords]"
          >
          </q-input>

          <!-- TODO: create a comp with q-input and q-chip -->
          <q-input
            v-model="userGroups"
            dense
            type="text"
            :label="$t('groups')"
            :hint="$t('groups_hint')"
            class="q-mb-md"
            lazy-rules
          >
          </q-input>
        </q-form>
      </q-card-section>

      <q-separator />

      <q-card-actions align="right" class="bg-grey-3"
        ><q-btn flat :label="$t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="submitCaption" type="submit" color="primary" @click="onAddUser" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { SubjectCredentialsDto } from 'src/models/Opal';
import { notifyError } from 'src/utils/notify';
import { onMounted, onUnmounted } from 'vue';

interface DialogProps {
  modelValue: boolean;
  user: SubjectCredentialsDto | null;
}

const { t } = useI18n();
const usersStore = useUsersStore();
const formRef = ref();
const props = defineProps<DialogProps>();
const emit = defineEmits(['update:modelValue']);
const showDialog = ref(props.modelValue);

const newUser = ref<SubjectCredentialsDto>({
  name: '',
  authenticationType: 'PASSWORD',
  enabled: true,
  groups: [],
} as SubjectCredentialsDto);

const confirmPassword = ref('');
const groups = ref('');

const userGroups = computed({
  get() {
    return groups.value;
  },
  set(value) {
    groups.value = value;
    newUser.value.groups = value.split(',').map((g) => g.trim());
  },
});

const editMode = computed(() => !!props.user && !!props.user.name);
const submitCaption = computed(() => (editMode.value ? t('update') : t('add')));
const dialogTitle = computed(() => (editMode.value ? t('user_edit') : t('user_add')));

// Validation rules
const validateRequiredName = (val: string) => (val && val.trim().length > 0) || t('validation.user.name_required');
const validateRequiredPassword = (val: string) =>
  (editMode.value && (!val || val.length === 0)) || (val && val.length >= 8) || t('password_hint');
const validateMatchingPasswords = () =>
  !confirmPassword.value ||
  newUser.value.password === confirmPassword.value ||
  t('validation.user.passwords_not_matching');
const validateRequiredConfirmPassword = (val: string) =>
  (editMode.value && (!val || val.length === 0)) ||
  (val && val.trim().length > 0) ||
  t('validation.user.confirm_password_required');

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      if (props.user) {
        newUser.value = { ...props.user };
      } else {
        newUser.value = {
          name: '',
          authenticationType: 'PASSWORD',
          enabled: true,
          groups: [],
        } as SubjectCredentialsDto;
      }
      userGroups.value = newUser.value.groups.join(', ');
      showDialog.value = value;
    }
  }
);

onMounted(() => {
  console.log('mounted');
});
onUnmounted(() => {
  console.log('unmounted');
});

function onHide() {
  confirmPassword.value = '';
  newUser.value = {
    name: '',
    authenticationType: 'PASSWORD',
    enabled: true,
    groups: [],
  } as SubjectCredentialsDto;

  emit('update:modelValue', false);
}

async function onAddUser() {
  const valid = await formRef.value.validate();
  if (valid) {
    try {
      editMode.value ? await usersStore.updateUser(newUser.value) : await usersStore.addUser(newUser.value);
      confirmPassword.value = '';
      groups.value = '';
      emit('update:modelValue', false);
      showDialog.value = false;
    } catch (err) {
      notifyError(err);
    }
  }
}
</script>
