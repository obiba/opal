<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('files')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        {{ t('files') }}
      </div>
      <file-view :file="filesStore.current" />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import FileView from 'src/components/files/FileView.vue';

const { t } = useI18n();
const route = useRoute();
const filesStore = useFilesStore();
const authStore = useAuthStore();

const username = computed(() => (authStore.profile.principal ? authStore.profile.principal : '?'));

onMounted(() => {
  if (route.params.path) {
    const path = [...route.params.path].join('/');
    filesStore.loadFiles(`/${path}`);
  } else {
    filesStore.loadFiles(`/home/${username.value}`);
  }
});
</script>
