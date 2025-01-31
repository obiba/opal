<template>
  <div>
    <div class="text-help q-mb-md">
      {{ t('plugin.installed_plugins_info') }}
    </div>
    <q-select
      v-model="type"
      :options="typeOptions"
      :label="t('type')"
      clearable
      dense
      class="q-mb-md"
      style="max-width: 200px"
    />
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
              <q-icon v-if="pkg.uninstalled" class="on-left" name="close" color="negative" />
              <span :title="`Opal: ${pkg.opalVersion}`">{{ pkg.version }}</span>
            </q-item-label>
          </q-item-section>
          <q-item-section side>
            <div v-if="!pkg.uninstalled" class="row">
              <q-btn round flat size="sm" icon="refresh" :title="t('plugin.restart')" @click="onRestart(pkg)" />
              <q-btn round flat size="sm" icon="edit" :title="t('plugin.configure')" @click="onConfigure(pkg)" />
              <q-btn
                round
                flat
                size="sm"
                icon="delete"
                :title="t('delete')"
                @click="pluginsStore.uninstallPlugin(pkg.name)"
              />
            </div>
            <div v-else>
              <q-btn
                round
                flat
                size="sm"
                icon="undo"
                :title="t('cancel')"
                @click="pluginsStore.cancelUninstallPlugin(pkg.name)"
              />
            </div>
          </q-item-section>
        </q-item>
      </template>
    </q-list>
    <plugin-configuration-dialog v-if="selected" v-model="showConfigurations" :plugin="selected" />
  </div>
</template>

<script setup lang="ts">
import PluginConfigurationDialog from 'src/components/admin/plugins/PluginConfigurationDialog.vue';
import type { PluginPackageDto } from 'src/models/Plugins';
import { notifyError, notifySuccess } from 'src/utils/notify';

const pluginsStore = usePluginsStore();
const { t } = useI18n();

const selected = ref<PluginPackageDto | undefined>();
const showConfigurations = ref(false);
const type = ref<string | undefined>();

const typeOptions = ['opal-analysis-r', 'opal-datasource', 'vcf-store'];

const packages = computed(() =>
  pluginsStore.plugins.packages?.filter((pkg) => (type.value ? pkg.type === type.value : true))
);

onMounted(() => {
  pluginsStore.loadPlugins();
});

function onRestart(pkg: PluginPackageDto) {
  pluginsStore
    .restartPlugin(pkg.name)
    .then(() => notifySuccess(t('plugin.restarted')))
    .catch(() => notifyError(t('plugin.restart_failed')));
}

function onConfigure(pkg: PluginPackageDto) {
  selected.value = pkg;
  showConfigurations.value = true;
}
</script>
