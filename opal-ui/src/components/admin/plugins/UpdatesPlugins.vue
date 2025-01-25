<template>
  <div>
    <div class="text-help q-mb-md">
      {{ t('plugin.updates_plugins_info') }}
    </div>
    <div v-if="packages">
      <q-list bordered separator>
        <template v-for="pkg in packages" :key="pkg.name">
          <q-item>
            <q-item-section top>
              <q-item-label class="text-subtitle1">
                {{ pkg.title }}
              </q-item-label>
              <q-item-label lines="1">
                <span class="text-weight-medium">{{ pkg.name }}</span>
                <span class="text-help on-right">[{{ pkg.type }}]</span>
              </q-item-label>
              <q-item-label caption>
                {{ pkg.description }}
              </q-item-label>
              <q-item-label lines="1" class="text-body2 text-primary">
                <span v-if="pkg.website && pkg.website.startsWith('http')"
                  ><a :href="pkg.website">{{ pkg.author }}</a></span
                >
                <span v-else>{{ pkg.author }}</span>
                <span class="text-help on-right">{{ pkg.maintainer }}</span>
                <code class="on-right">{{ pkg.license }}</code>
              </q-item-label>
            </q-item-section>
            <q-item-section side>
              <q-item-label caption lines="1">
                <span :title="`Opal: ${pkg.opalVersion}`">{{ pkg.version }}</span>
              </q-item-label>
            </q-item-section>
            <q-item-section side>
              <div class="row">
                <q-btn round flat size="sm" icon="download" :title="t('install')" @click="onUpdate(pkg)" />
              </div>
            </q-item-section>
          </q-item>
        </template>
      </q-list>
    </div>
    <div v-else-if="loading">
      <q-spinner-dots />
    </div>
    <div v-else>
      <div class="text-hint">
        {{ t('plugin.no_updates') }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { PluginPackageDto } from 'src/models/Plugins';
import { notifySuccess, notifyError } from 'src/utils/notify';

const pluginsStore = usePluginsStore();
const { t } = useI18n();

const updates = ref();
const loading = ref(false);

const packages = computed(() => updates.value?.packages);

onMounted(() => {
  loading.value = true;
  updates.value = null;
  pluginsStore
    .getPluginsUpdates()
    .then((data) => {
      updates.value = data;
    })
    .finally(() => {
      loading.value = false;
    });
});

function onUpdate(pkg: PluginPackageDto) {
  pluginsStore
    .installPlugin(pkg.name, pkg.version)
    .then(() => {
      notifySuccess(t('plugin.install_success'));
    })
    .catch((err) => {
      notifyError(err);
    });
}
</script>
