<template>
  <q-layout v-show="authStore.isAuthenticated" view="lHh Lpr lFf">
    <q-header elevated class="bg-dark text-white">
      <q-toolbar>
        <q-btn
          flat
          dense
          round
          icon="menu"
          aria-label="Menu"
          @click="toggleLeftDrawer"
        />

        <q-toolbar-title>
          <q-btn flat to="/" no-caps size="lg">
            {{ appName }}
          </q-btn>
        </q-toolbar-title>

        <div class="q-gutter-sm row items-center no-wrap">
          <q-btn to="/admin" no-caps>
            {{ $t('administration') }}
          </q-btn>
          <q-btn no-caps @click="onHelp">
            {{ $t('help') }}
          </q-btn>
          <q-btn-dropdown flat :label="locale">
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
          <q-btn-dropdown flat no-caps :label="username">
            <q-list>
              <q-item
                clickable
                v-close-popup
                to="/profile"
                v-if="authStore.isAuthenticated"
              >
                <q-item-section>
                  <q-item-label>{{ $t('my_profile') }}</q-item-label>
                </q-item-section>
              </q-item>
              <q-item
                clickable
                v-close-popup
                @click="onSignout"
                v-if="authStore.isAuthenticated"
              >
                <q-item-section>
                  <q-item-label>{{ $t('auth.signout') }}</q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-btn-dropdown>
        </div>
      </q-toolbar>
    </q-header>

    <q-drawer v-model="leftDrawerOpen" show-if-above bordered>

      <div v-if="projectPage">
        <project-drawer />
      </div>
      <div v-else-if="filesPage">
        <files-drawer />
      </div>
      <div v-else-if="taxonomiesPage">
        <taxonomies-drawer />
      </div>
      <div v-else>
        <main-drawer />
      </div>
    </q-drawer>

    <q-page-container>
      <router-view/>
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { useCookies } from 'vue3-cookies';
import { locales } from 'boot/i18n';
import { ref } from 'vue';

import MainDrawer from 'src/components/MainDrawer.vue';
import ProjectDrawer from 'src/components/ProjectDrawer.vue';
import FilesDrawer from 'src/components/FilesDrawer.vue';
import TaxonomiesDrawer from 'src/components/TaxonomiesDrawer.vue';
import { computed } from 'vue';

const router = useRouter();
const systemStore = useSystemStore();
const resourcesStore = useResourcesStore();
const authStore = useAuthStore();
const { cookies } = useCookies();
const { locale, t } = useI18n({ useScope: 'global' });
const localeOptions = computed(() => {
  return locales.map((key) => ({
    label: key.toUpperCase(),
    value: key,
  }));
});


const leftDrawerOpen = ref(false);

onMounted(() => {
  authStore.userProfile().then(() => {
    systemStore.initGeneralConf();
    resourcesStore.initResourceProviders();
    authStore.loadBookmarks();
  }).catch(() => {
    router.push('/signin');
  });
});

const appName = computed(() => systemStore.generalConf.name || t('main.brand'));

const username = computed(() =>
  authStore.profile.principal ? authStore.profile.principal : '?'
);

const taxonomiesPage = computed(() => {
  return router.currentRoute.value.path.startsWith('/admin/taxonomies');
});

const projectPage = computed(() => {
  return router.currentRoute.value.path.startsWith('/project/');
});

const filesPage = computed(() => {
  return router.currentRoute.value.path.startsWith('/files');
});

function toggleLeftDrawer() {
  leftDrawerOpen.value = !leftDrawerOpen.value;
}

function onLocaleSelection(localeOpt: { label: string; value: string }) {
  locale.value = localeOpt.value;
  cookies.set('locale', localeOpt.value);
}

function onSignout() {
  const logoutURL = systemStore.generalConf.logoutURL;
  authStore
    .signout()
    .then(() => {
      if (logoutURL) {
        window.location.href = logoutURL;
        return;
      }
      router.push('/signin');
    })
    .catch(() => {
      if (logoutURL) {
        window.location.href = logoutURL;
        return;
      }
      router.push('/signin');
    });
}

function onHelp() {
  window.open('https://opaldoc.obiba.org', '_blank');
}
</script>
