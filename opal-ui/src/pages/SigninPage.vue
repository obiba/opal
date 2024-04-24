<template>
  <q-layout>
    <q-page-container>
      <q-page class="flex flex-center bg-grey-4">
        <div
          class="column"
          :style="
            $q.screen.lt.sm
              ? { width: '80%' }
              : $q.screen.lt.md
              ? { width: '50%' }
              : { width: '30%' }
          "
        >
          <div class="col text-center">
            <h4>{{ $t('main.brand') }}</h4>
          </div>
          <div v-if="!showForm" class="col text-center q-mt-md">
            <q-spinner color="primary" size="4em" :thickness="10" />
          </div>
          <div class="col">
            <q-card class="bg-white text-dark">
              <q-card-section>
                <q-card-section v-show="!withToken">
                  <q-form @submit="onSubmit" class="q-gutter-md">
                    <q-input
                      autofocus
                      color="grey-10"
                      v-model="username"
                      :label="$t('auth.username')"
                      lazy-rules
                    >
                      <template v-slot:prepend>
                        <q-icon name="fas fa-user" size="xs" />
                      </template>
                    </q-input>

                    <q-input
                      type="password"
                      color="grey-10"
                      v-model="password"
                      :label="$t('auth.password')"
                      lazy-rules
                    >
                      <template v-slot:prepend>
                        <q-icon name="fas fa-lock" size="xs" />
                      </template>
                    </q-input>

                    <div>
                      <q-btn
                        :label="$t('auth.signin')"
                        type="submit"
                        color="primary"
                        :disable="disableSubmit"
                      />
                    </div>
                  </q-form>
                </q-card-section>
                <q-card-section v-show="secret">
                  <div class="col text-subtitle">
                    {{ $t('login.totp') }}
                  </div>
                  <div class="text-center q-mt-md">
                    <img :src="qr" />
                  </div>
                  <div class="col text-subtitle q-mt-md">
                    {{ $t('login.totp_secret') }}
                  </div>
                  <q-input dense color="grey-10" v-model="secret" readonly>
                    <template v-slot:after>
                      <q-btn
                        round
                        dense
                        flat
                        icon="content_copy"
                        @click="onCopySecret"
                      />
                    </template>
                  </q-input>
                  <div class="col text-subtitle q-mt-md">
                    {{ $t('login.email_otp') }}
                  </div>
                  <div class="q-mt-md">
                    <q-btn
                      :label="$t('login.send_email_token')"
                      @click="onEmailToken"
                      color="info"
                      stretch
                      class="text-bold"
                    />
                  </div>
                </q-card-section>
                <q-card-section v-if="withToken">
                  <q-form @submit="onSubmit" class="q-gutter-md">
                    <q-input
                      autofocus
                      type="number"
                      color="grey-10"
                      v-model="token"
                      :label="$t('login.token')"
                      lazy-rules
                      class="no-spinner"
                    >
                      <template v-slot:prepend>
                        <q-icon name="fas fa-mobile" size="xs" />
                      </template>
                    </q-input>
                    <div>
                      <q-btn
                        :label="$t('login.validate')"
                        type="submit"
                        color="secondary"
                        :disable="disableValidate"
                      />
                      <q-btn
                        :label="$t('cancel')"
                        @click="onCancelToken"
                        flat
                        stretch
                        class="text-bold q-ml-md"
                      />
                    </div>
                  </q-form>
                </q-card-section>
              </q-card-section>
              <q-card-section>
                <q-btn-dropdown flat :label="$t(locale)">
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
          </div>
        </div>
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { useCookies } from 'vue3-cookies';
import { locales } from 'src/boot/i18n';

const authStore = useAuthStore();
const datasourceStore = useDatasourceStore();
const filesStore = useFilesStore();
const projectsStore = useProjectsStore();

const { cookies } = useCookies();
const { locale } = useI18n({ useScope: 'global' });
const router = useRouter();
const localeOptions = computed(() => {
  return locales.map((key) => ({
    label: key.toUpperCase(),
    value: key,
  }));
});

const username = ref('');
const password = ref('');
const showForm = ref(true);

const disableSubmit = computed(() => {
  return !username.value || !password.value;
});

onMounted(() => {
  authStore.reset();
  datasourceStore.reset();
  filesStore.reset();
  projectsStore.reset();
});

function onLocaleSelection(localeOpt: { label: string; value: string }) {
  locale.value = localeOpt.value;
  cookies.set('locale', localeOpt.value);
}

function onSubmit() {
  authStore.signin(username.value, password.value).then(() => {
    if (authStore.sid) {
      router.push('/');
    }
  });
}
</script>
