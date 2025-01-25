<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t('configure') }}</div>
      </q-card-section>
      <q-separator />
      <q-card-section>
        <q-input
          v-model="config"
          :label="t('plugin.site_properties')"
          :hint="t('plugin.site_properties_hint')"
          type="textarea"
          dense
          autogrow
          class="q-mb-md"
        />
      </q-card-section>
      <q-separator />
      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn flat :label="t('submit')" color="primary" @click="onSubmit" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { PluginPackageDto, PluginDto } from 'src/models/Plugins';

const { t } = useI18n();

interface DialogProps {
  modelValue: boolean;
  plugin: PluginPackageDto;
}

const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const emit = defineEmits(['update:modelValue']);

const pluginsStore = usePluginsStore();

const config = ref<string>('');

watch(
  () => props.modelValue,
  () => {
    showDialog.value = props.modelValue;
    if (props.modelValue) init();
  }
);

onMounted(init);

function init() {
  config.value = '';
  pluginsStore.getPlugin(props.plugin.name).then((data: PluginDto) => {
    config.value = data.siteProperties;
  });
}

function onHide() {
  emit('update:modelValue', false);
}

function onSubmit() {
  pluginsStore.configurePlugin(props.plugin.name, config.value).then(() => {
    emit('update:modelValue', false);
  });
}
</script>
