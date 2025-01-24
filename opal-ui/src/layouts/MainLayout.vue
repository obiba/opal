<template>
  <q-layout v-show="authStore.isAuthenticated" view="lHh Lpr lFf">
    <q-header elevated class="bg-dark text-white">
      <q-toolbar>
        <q-btn flat dense round icon="menu" aria-label="Menu" @click="toggleLeftDrawer" />
        <q-btn flat to="/" no-caps size="lg">
          {{ appName }}
        </q-btn>
        <q-space />
        <div class="q-gutter-sm row items-center no-wrap">
          <q-btn icon="shopping_cart" size="sm" to="/cart" class="q-mr-sm">
            <q-badge v-if="cartStore.variables?.length" color="orange" floating>{{
              cartStore.variables.length
              }}</q-badge>
          </q-btn>
          <q-input v-model="query" dense filled :placeholder="t('search') + '...'" bg-color="grey-2" debounce="300"
            @update:model-value="onSearch" style="width: 200px">
            <q-menu v-model="showResults" no-parent-event no-focus auto-close>
              <q-list style="min-width: 100px">
                <q-item clickable v-close-popup v-for="item in itemResults" :key="item.identifier"
                  @click="goToVariable(item)">
                  <q-item-section class="text-caption">
                    <span>{{ searchStore.getField(item, 'name') }}</span>
                    <div>
                      <span class="text-hint text-primary">{{ searchStore.getField(item, 'project') }}.{{
                        searchStore.getField(item, 'table') }}</span>
                    </div>
                    <div v-for="(attr, idx) in searchStore.getLabels(item)" :key="idx" class="text-hint">
                      <q-badge v-if="attr.locale" color="grey-3" :label="attr.locale" class="q-mr-xs text-grey-6" />
                      <span>{{ attr.value }}</span>
                    </div>
                  </q-item-section>
                </q-item>
                <q-item v-if="itemResults.length > 0" clickable class="bg-grey-2">
                  <q-item-section>
                    <router-link :to="`/search/variables?q=${query}`" class="text-primary">
                      <q-icon name="arrow_circle_right" size="sm" class="on-left" />{{ t('advanced_search') }}
                    </router-link>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-menu>
          </q-input>
          <q-btn-dropdown flat :label="locale">
            <q-list>
              <q-item clickable v-close-popup @click="onLocaleSelection(localeOpt)" v-for="localeOpt in localeOptions"
                :key="localeOpt.value">
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
              <q-item clickable v-close-popup to="/profile" v-if="authStore.isAuthenticated">
                <q-item-section>
                  <q-item-label>{{ t('my_profile') }}</q-item-label>
                </q-item-section>
              </q-item>
              <q-item clickable v-close-popup @click="onSignout" v-if="authStore.isAuthenticated">
                <q-item-section>
                  <q-item-label>{{ t('auth.signout') }}</q-item-label>
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
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { Cookies } from 'quasar';
import { locales } from 'boot/i18n';

import MainDrawer from 'src/components/MainDrawer.vue';
import ProjectDrawer from 'src/components/ProjectDrawer.vue';
import FilesDrawer from 'src/components/FilesDrawer.vue';
import TaxonomiesDrawer from 'src/components/TaxonomiesDrawer.vue';
import type { QueryResultDto } from 'src/models/Search';
import type { ItemFieldsResultDto } from 'src/stores/search';

const router = useRouter();
const systemStore = useSystemStore();
const resourcesStore = useResourcesStore();
const authStore = useAuthStore();
const searchStore = useSearchStore();
const cartStore = useCartStore();

const { locale, t } = useI18n({ useScope: 'global' });
const localeOptions = computed(() => {
  return locales.map((key) => ({
    label: key.toUpperCase(),
    value: key,
  }));
});

const leftDrawerOpen = ref(false);
const query = ref('');
const showResults = ref(false);
const results = ref<QueryResultDto>();

const itemResults = computed(() => (results.value?.hits as ItemFieldsResultDto[]) || []);

onMounted(() => {
  router.beforeEach((to, from, next) => {
    if (to.path.startsWith('/admin') && !authStore.isAdministrator) {
      next('/');
    } else {
      next();
    }
  });
  authStore
    .userProfile()
    .then(() => {
      Promise.all([
        authStore.checkIsAdministrator(),
        systemStore.initGeneralConf(),
        resourcesStore.initResourceProviders(),
        authStore.loadBookmarks(),
      ]);
    })
    .catch(() => {
      router.push('/signin');
    });
});

const appName = computed(() => systemStore.generalConf.name || t('main.brand'));

const username = computed(() => (authStore.profile.principal ? authStore.profile.principal : '?'));

const taxonomiesPage = computed(() => {
  return router.currentRoute.value.path.startsWith('/taxo');
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
  Cookies.set('locale', localeOpt.value);
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

function onSearch() {
  if (!query.value || query.value.length < 3) {
    showResults.value = false;
    return;
  }
  searchStore.search(query.value, 10, ['label', 'label-en'], undefined).then((res) => {
    showResults.value = res.totalHits > 0;
    results.value = res;
  });
}

function goToVariable(item: ItemFieldsResultDto) {
  const fields = item['Search.ItemFieldsDto.item'].fields;
  let project;
  let table;
  let variable;
  for (const field of fields) {
    if (field.key === 'project') {
      project = field.value;
    } else if (field.key === 'table') {
      table = field.value;
    } else if (field.key === 'name') {
      variable = field.value;
    }
  }
  if (project || table || variable) {
    router.push(`/project/${project}/table/${table}/variable/${variable}`);
  }
}
</script>
