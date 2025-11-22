<template>
  <q-card class="bg-white text-dark">
    <q-card-section class="q-pb-none">
      <div class="text-help text-center q-pt-xs q-pb-xs">{{ t(authStore.isAuthenticated ? 'auth.confirm_title' : 'auth.title') }}</div>
      <q-card-section v-show="!withToken">
        <q-form @submit="onSubmit">
          <div v-if="!authStore.isAuthenticated || isCredentialsRealm" class="q-gutter-md q-mb-md">
            <q-input autofocus color="grey-10" v-model="username" :disable="authStore.isAuthenticated" :label="t('auth.username')" autocomplete="nope">
              <template v-slot:prepend>
                <q-icon name="fas fa-user" size="xs" />
              </template>
            </q-input>
            <q-input type="password" color="grey-10" v-model="password" :label="t('auth.password')" autocomplete="new-password">
              <template v-slot:prepend>
                <q-icon name="fas fa-lock" size="xs" />
              </template>
            </q-input>
            <div>
              <q-btn :label="t('auth.signin')" type="submit" color="primary" :disable="disableSubmit" />
            </div>
          </div>
          <div v-if="authProviders.length > 0">
            <q-separator v-if="!authStore.isAuthenticated" class="q-mb-md" />
            <div v-for="provider in authProviders" :key="provider.name">
              <q-btn
                no-caps
                :label="t('signin_with', { provider: provider.label || provider.name })"
                @click="onSigninProvider(provider)"
                color="primary"
                class="full-width q-mb-sm"
                stretch
              />
            </div>
          </div>
        </q-form>
      </q-card-section>
      <q-card-section v-show="qr" class="q-pb-none">
        <div class="col text-subtitle">
          {{ t('auth.totp_help') }}
        </div>
        <div class="text-center q-mt-md">
          <img :src="qr" />
        </div>
      </q-card-section>
      <q-card-section v-if="withToken">
        <q-form @submit="onSubmit" class="q-gutter-md">
          <q-input
            autofocus
            type="number"
            color="grey-10"
            v-model="token"
            :label="t('auth.code')"
            :hint="t('auth.code_hint')"
          >
            <template v-slot:prepend>
              <q-icon name="fas fa-mobile" size="xs" />
            </template>
          </q-input>
          <div>
            <q-btn :label="t('auth.validate')" type="submit" color="primary" :disable="token.length !== 6" />
            <q-btn :label="t('cancel')" @click="onCancelToken" flat stretch class="text-bold q-ml-md" />
          </div>
        </q-form>
      </q-card-section>
    </q-card-section>
    <q-card-section v-if="!authStore.isAuthenticated" class="q-pt-none">
      <q-btn-dropdown flat :label="t(locale)">
        <q-list>
          <q-item
            clickable
            v-close-popup
            @click="onLocaleSelection(localeOpt)"
            v-for="localeOpt in localeOptions"
            :key="localeOpt.value"
          >
            <q-item-section>
              <q-item-label>{{ localeOpt.label }}</q-item-label>
            </q-item-section>
            <q-item-section avatar v-if="locale === localeOpt.value">
              <q-icon color="primary" name="check" />
            </q-item-section>
          </q-item>
        </q-list>
      </q-btn-dropdown>
    </q-card-section>
  </q-card>
</template>

<script setup lang="ts">
import type { AxiosError } from 'axios';
import { Cookies } from 'quasar';
import { locales } from 'src/boot/i18n';
import type { AuthProviderDto } from 'src/models/Opal';
import { baseUrl } from 'src/boot/api';
import { notifyError } from 'src/utils/notify';

interface AuthResponse {
  image?: string;
  status?: string;
}

const emits = defineEmits<{
  (e: 'signed-in'): void;
}>();

const authStore = useAuthStore();

const { locale, t } = useI18n({ useScope: 'global' });
const localeOptions = computed(() => {
  return locales.map((key) => ({
    label: key.toUpperCase(),
    value: key,
  }));
});

const username = ref('');
const password = ref('');
const token = ref('');
const qr = ref('');
const authMethod = ref('');
const withToken = ref(false);
const authProviders = ref<AuthProviderDto[]>([]);

const isCredentialsRealm = computed(
  () => authStore.profile && ['opal-user-realm', 'opal-ini-realm', 'obiba-realm'].includes(authStore.profile.realm || ''),
);

const disableSubmit = computed(() => {
  return !username.value || !password.value;
});

onMounted(() => {
  authStore.getProviders().then((providers) => {
    if (authStore.profile.realm) {
      providers = providers.filter((p) => p.name === authStore.profile.realm);
    }
    authProviders.value = providers;
  });
  // check is already authenticated
  if (authStore.isAuthenticated) {
    username.value = authStore.profile.principal || '';
    password.value = '';
  }
});

function onLocaleSelection(localeOpt: { label: string; value: string }) {
  locale.value = localeOpt.value;
  Cookies.set('locale', localeOpt.value);
}

async function onSubmit() {
  try {
    await authStore.signin(username.value, password.value, authMethod.value, token.value);
    if (authStore.sid) {
      emits('signed-in');
    }
  } catch (err) {
    const error = err as AxiosError;
    authMethod.value = error.response?.headers['www-authenticate'];
    const data = error.response?.data as AuthResponse;
    if (authMethod.value) {
      withToken.value = true;
      if (data?.image) {
        qr.value = data.image;
      }
    } else if (error.response?.status === 403 && data?.status === undefined) {
      notifyError('error.InvalidCredentials');
    } else {
      notifyError(err);
    }
  }
}

function onSigninProvider(provider: AuthProviderDto) {
  window.open(`${baseUrl}/../auth/login/${provider.name}`, '_self');
}

function onCancelToken() {
  withToken.value = false;
  qr.value = '';
  token.value = '';
  authMethod.value = '';
}
</script>
