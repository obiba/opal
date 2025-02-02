<template>
  <div>
    <div class="text-h6">
      {{ t('plugin.archive_installation') }}
    </div>
    <div class="text-help q-mb-md">
      {{ t('plugin.archive_info') }}
    </div>
    <file-select
      v-model="file"
      :label="t('plugin.archive_file')"
      :folder="filesStore.current"
      selection="single"
      :extensions="['-dist.zip']"
      @select="onFileSelect"
      class="q-mb-md"
    />
    <q-spinner-dots v-if="loading" class="q-mb-md" />
    <div class="text-h6">
      {{ t('plugin.update_site') }}
    </div>
    <div class="text-help q-mb-md">
      {{ t('plugin.update_site_info') }}
    </div>
    <div v-if="pluginsStore.plugins.site">
      <q-markdown :src="siteLinkMd" no-heading-anchor-links />
    </div>
    <div v-if="pluginsStore.plugins.updated">
      <span class="text-hint" :title="getDateLabel(pluginsStore.plugins.updated)">{{
        t('plugin.last_update', { ago: getDateDistanceLabel(pluginsStore.plugins.updated) })
      }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import FileSelect from 'src/components/files/FileSelect.vue';
import type { FileDto } from 'src/models/Opal';
import { notifyError, notifySuccess } from 'src/utils/notify';
import { getDateLabel, getDateDistanceLabel } from 'src/utils/dates';

const filesStore = useFilesStore();
const pluginsStore = usePluginsStore();
const { t } = useI18n();

const file = ref<FileDto>();
const loading = ref(false);

const siteLinkMd = computed(() => {
  return `[${pluginsStore.plugins?.site}](${pluginsStore.plugins?.site})`;
});

function onFileSelect() {
  if (!file.value) {
    return;
  }
  loading.value = true;
  pluginsStore
    .installPluginFile(file.value.path)
    .then(() => {
      file.value = undefined;
      notifySuccess(t('plugin.install_success'));
    })
    .catch((err) => {
      notifyError(err);
    })
    .finally(() => {
      loading.value = false;
    });
}
</script>
