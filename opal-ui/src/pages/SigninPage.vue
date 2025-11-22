<template>
  <q-layout>
    <q-page-container>
      <q-page class="flex flex-center bg-blue-grey-1">
        <div class="column" :style="$q.screen.lt.sm ? { width: '80%' } : { width: '360px' }">
          <div class="col text-center">
            <div class="text-h4 q-mb-lg">{{ appName }}</div>
          </div>
          <div class="col">
            <signin-panel @signed-in="onSignedIn" />
          </div>
        </div>
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { useQuasar } from 'quasar';
import SigninPanel from 'src/components/SigninPanel.vue';

const $q = useQuasar();
const router = useRouter();
const { t } = useI18n();

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

const appName = computed(() => systemStore.generalConf.name || t('main.brand'));

onMounted(() => {
  if (!authStore.isAuthenticated) {
    // Reset all stores
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
    identifiersStore.reset();
    appsStore.reset();
    searchStore.reset();
    cartStore.reset();
  }
});

function onSignedIn() {
  router.push(authStore.redirectPath || '/');
}
</script>
