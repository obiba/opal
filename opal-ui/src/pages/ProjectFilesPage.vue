<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="t('projects')" to="/projects" />
        <q-breadcrumbs-el :label="name" :to="`/project/${name}`" />
        <q-breadcrumbs-el :label="t('files')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <div class="text-h5 q-mb-md">
        {{ t('files') }}
      </div>
      <div class="text-help">
        {{ t('project_files_info') }}
      </div>

      <div class="row q-gutter-md">
        <div class="col">
          <div class="text-grey-8 q-mt-sm">
            <div>
              <q-btn
                flat
                no-caps
                icon="person"
                color="primary"
                size="12px"
                :label="t('user')"
                align="left"
                class="full-width"
                @click="onFolderSelection(`/home/${username}`)"
              ></q-btn>
            </div>
            <div v-if="name">
              <q-btn
                flat
                no-caps
                icon="table_chart"
                color="primary"
                size="12px"
                :label="t('project')"
                align="left"
                class="full-width"
                @click="onFolderSelection(`/projects/${name}`)"
              ></q-btn>
            </div>
            <q-separator class="q-mt-md q-mb-md" />
            <div>
              <q-btn
                flat
                no-caps
                icon="group"
                color="primary"
                size="12px"
                :label="t('users')"
                align="left"
                class="full-width"
                @click="onFolderSelection('/home')"
              ></q-btn>
            </div>
            <div>
              <q-btn
                flat
                no-caps
                icon="table_chart"
                color="primary"
                size="12px"
                :label="t('projects')"
                align="left"
                class="full-width"
                @click="onFolderSelection('/projects')"
              ></q-btn>
            </div>
            <div>
              <q-btn
                flat
                no-caps
                icon="dns"
                color="primary"
                size="12px"
                :label="t('file_system')"
                align="left"
                class="full-width"
                @click="onFolderSelection('/')"
              ></q-btn>
            </div>
          </div>
        </div>
        <div class="col-10">
          <file-view :file="filesStore.current" />
        </div>
      </div>
    </q-page>
  </div>
</template>

<script setup lang="ts">
import FileView from 'src/components/files/FileView.vue';

const { t } = useI18n();
const route = useRoute();
const projectsStore = useProjectsStore();
const filesStore = useFilesStore();
const authStore = useAuthStore();

const name = computed(() => route.params.id as string);

const username = computed(() => (authStore.profile.principal ? authStore.profile.principal : ''));

onMounted(() => {
  projectsStore.initProject(name.value).then(() => {
    filesStore.loadFiles(`/projects/${name.value}`);
  });
});

function onFolderSelection(path: string) {
  filesStore.loadFiles(path);
}
</script>
