<template>
  <q-layout>
    <q-page-container>
      <q-page class="flex flex-center bg-blue-grey-1">
        <div class="column" :style="$q.screen.lt.sm ? { width: '80%' } : { width: '360px' }">
          <div class="col text-center">
            <div class="text-h4 q-mb-lg">{{ appName }}</div>
          </div>
          <div v-if="!showForm" class="col text-center q-mt-md">
            <q-spinner color="primary" size="4em" :thickness="10" />
          </div>
          <div class="col">
            <q-card class="bg-white text-dark">
              <q-card-section class="q-pb-none">
                <div class="text-help text-center q-pt-xs q-pb-xs">{{ t('auth.title') }}</div>
                <q-card-section v-show="!withToken">
                  <q-form @submit="onSubmit" class="q-gutter-md">
                    <q-input autofocus color="grey-10" v-model="username" :label="t('auth.username')" lazy-rules>
                      <template v-slot:prepend>
                        <q-icon name="fas fa-user" size="xs" />
                      </template>
                    </q-input>

                    <q-input type="password" color="grey-10" v-model="password" :label="t('auth.password')" lazy-rules>
                      <template v-slot:prepend>
                        <q-icon name="fas fa-lock" size="xs" />
                      </template>
                    </q-input>

                    <div>
                      <q-btn :label="t('auth.signin')" type="submit" color="primary" :disable="disableSubmit" />
                    </div>
                    <div v-if="authProviders.length > 0">
                      <q-separator class="q-mb-md" />
                      <div v-for="provider in authProviders" :key="provider.name">
                        <q-btn no-caps :label="t('signin_with', { provider: provider.name })"
                          @click="onSigninProvider(provider)" color="primary" class="full-width" stretch />
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
                    <q-input autofocus type="number" color="grey-10" v-model="token" :label="t('auth.code')"
                      :hint="t('auth.code_hint')">
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
              <q-card-section class="q-pt-none">
                <q-btn-dropdown flat :label="t(locale)">
                  <q-list>
                    <q-item clickable v-close-popup @click="onLocaleSelection(localeOpt)"
                      v-for="localeOpt in localeOptions" :key="localeOpt.value">
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
          </div>
        </div>
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import type { AxiosError } from 'axios';
import { Cookies, useQuasar } from 'quasar';
import { locales } from 'src/boot/i18n';
import type { AuthProviderDto } from 'src/models/Opal';
import { baseUrl } from 'src/boot/api';
import { notifyError } from 'src/utils/notify';

interface AuthResponse {
  image?: string;
  status?: string;
}

const $q = useQuasar();
const systemStore = useSystemStore();
const authStore = useAuthStore();
const authzStore = useAuthzStore();
const datasourceStore = useDatasourceStore();
const resourcesStore = useResourcesStore();
const filesStore = useFilesStore();
const projectsStore = useProjectsStore();
const commandsStore = useCommandsStore();
const pluginsStore = usePluginsStore();
const transientDatasourceStore = useTransientDatasourceStore();
const usersStore = useUsersStore();
const groupsStore = useGroupsStore();
const rStore = useRStore();
const datashieldStore = useDatashieldStore();
const profilesStore = useProfilesStore();
const profileAclsStore = useProfileAclsStore();
const profileActivityStore = useProfileActivityStore();
const identityProvidersStore = useIdentityProvidersStore();
const tokensStore = useTokensStore();
const identifiersStore = useIdentifiersStore();
const appsStore = useAppsStore();
const searchStore = useSearchStore();
const cartStore = useCartStore();

const { locale, t } = useI18n({ useScope: 'global' });
const router = useRouter();
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
const showForm = ref(true);
const authProviders = ref<AuthProviderDto[]>([]);

const appName = computed(() => systemStore.generalConf.name || t('main.brand'));

const disableSubmit = computed(() => {
  return !username.value || !password.value;
});

onMounted(() => {
  systemStore.initGeneralConf();
  authStore.reset();
  authzStore.reset();
  commandsStore.reset();
  datasourceStore.reset();
  resourcesStore.reset();
  filesStore.reset();
  pluginsStore.reset();
  projectsStore.reset();
  transientDatasourceStore.reset();
  usersStore.reset();
  groupsStore.reset();
  rStore.reset();
  datashieldStore.reset();
  profilesStore.reset();
  profileAclsStore.reset();
  profileActivityStore.reset();
  identityProvidersStore.reset();
  tokensStore.reset();
  authStore.getProviders().then((providers) => {
    authProviders.value = providers;
  });
  identifiersStore.reset();
  appsStore.reset();
  searchStore.reset();
  cartStore.reset();
});

function onLocaleSelection(localeOpt: { label: string; value: string }) {
  locale.value = localeOpt.value;
  Cookies.set('locale', localeOpt.value);
}

async function onSubmit() {
  try {
    await authStore.signin(username.value, password.value, authMethod.value, token.value);
    if (authStore.sid) {
      router.push('/');
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
