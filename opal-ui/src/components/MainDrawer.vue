<template>
  <div>
    <div v-if="authStore.isAuthenticated" class="q-mt-none q-mb-none q-pa-md">
      <span class="text-bold text-grey-6">{{ username }}</span>
    </div>
    <q-list>
      <q-item to="/profile" v-if="authStore.isAuthenticated">
        <q-item-section avatar>
          <q-icon name="person" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ t('my_profile') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item clickable @click="onSignout" v-if="authStore.isAuthenticated">
        <q-item-section avatar>
          <q-icon name="logout" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ t('auth.signout') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-separator v-if="authStore.isAuthenticated" />
      <q-item to="/projects">
        <q-item-section avatar>
          <q-icon name="table_chart" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ t('projects') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item to="/files">
        <q-item-section avatar>
          <q-icon name="folder" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ t('files') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item :to="`/tasks`">
        <q-item-section avatar>
          <q-icon name="splitscreen" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ t('tasks') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item :to="`/taxonomies`">
        <q-item-section avatar>
          <q-icon name="sell" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ t('taxonomies') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item :to="`/search`">
        <q-item-section avatar>
          <q-icon name="search" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ t('search') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item v-if="authStore.isAdministrator" to="/admin">
        <q-item-section avatar>
          <q-icon name="admin_panel_settings" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ t('administration') }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item-label header>{{ t('other_links') }}</q-item-label>
      <EssentialLink v-for="link in essentialLinks" :key="link.title" v-bind="link" />
      <q-item class="fixed-bottom text-caption">
        <div>
          {{ t('main.powered_by') }}
          <a class="text-weight-bold" href="https://www.obiba.org/pages/products/opal" target="_blank">OBiBa Opal</a>
          <span class="q-ml-xs" style="font-size: smaller">{{ authStore.version }}</span>
        </div>
      </q-item>
    </q-list>
  </div>
</template>

<script setup lang="ts">
import EssentialLink, { type EssentialLinkProps } from 'components/EssentialLink.vue';
const { t } = useI18n();

const router = useRouter();
const authStore = useAuthStore();
const systemStore = useSystemStore();

const username = computed(() => (authStore.profile.principal ? authStore.profile.principal : '?'));

const essentialLinks: EssentialLinkProps[] = [
  {
    title: t('docs'),
    caption: t('documentation_cookbook'),
    icon: 'school',
    link: 'https://opaldoc.obiba.org',
  },
  {
    title: t('source_code'),
    caption: 'github.com/obiba/opal',
    icon: 'code',
    link: 'https://github.com/obiba/opal',
  },
];

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
</script>
