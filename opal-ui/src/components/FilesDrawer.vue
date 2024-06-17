<template>
  <div>
    <h6 class="q-mt-none q-mb-none q-pa-md">
      {{ $t('files') }}
    </h6>
    <q-list>
      <q-item :to="`/files/home/${username}`">
        <q-item-section avatar>
          <q-icon name="person" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $t('user') }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-item-label header class="text-weight-bolder">{{
        $t('content')
      }}</q-item-label>

      <q-item to="/files/home">
        <q-item-section avatar>
          <q-icon name="group" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $t('users') }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-item to="/files/projects">
        <q-item-section avatar>
          <q-icon name="table_chart" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $t('projects') }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-item to="/files/reports">
        <q-item-section avatar>
          <q-icon name="summarize" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $t('reports') }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-item to="/files/">
        <q-item-section avatar>
          <q-icon name="dns" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ $t('file_system') }}</q-item-label>
        </q-item-section>
      </q-item>
    </q-list>
  </div>
</template>

<script lang="ts">
export default defineComponent({
  name: 'FilesDrawer',
});
</script>
<script setup lang="ts">
const route = useRoute();
const filesStore = useFilesStore();
const authStore = useAuthStore();

const username = computed(() =>
  authStore.profile.principal ? authStore.profile.principal : '?'
);

watch(
  () => route.params.path,
  () => {
    const path = [...route.params.path].join('/');
    filesStore.loadFiles(`/${path}`);
  }
);
</script>
