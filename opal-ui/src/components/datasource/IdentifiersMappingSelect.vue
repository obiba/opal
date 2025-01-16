<template>
  <div>
    <q-select
      v-model="idConfig.name"
      dense
      :options="mappingNames"
      :label="t('id_mappings.title')"
      :hint="t('importer.id_mappings.hint')"
      class="q-mb-md q-pt-md"
      emit-value
      map-options
      @update:model-value="onMappingConfigUpdated"
    />
    <div class="q-ml-none" v-show="forImport && idConfig.name">
      <q-option-group
        dense
        :options="mappingOptions"
        type="radio"
        v-model="mappingOption"
        @update:model-value="onMappingConfigUpdated"
      >
        <template v-slot:label="opt">
          <div class="row q-pt-md">
            <span>{{ t(opt.label) }}</span>
            <span class="text-caption text-secondary">{{ t(opt.hint) }}</span>
          </div>
        </template>
      </q-option-group>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { IdentifiersMappingDto, IdentifiersMappingConfigDto } from 'src/models/Identifiers';
import type { ProjectDto_IdentifiersMappingDto } from 'src/models/Projects';
import { notifyError } from 'src/utils/notify';

interface Props {
  modelValue: IdentifiersMappingConfigDto | undefined;
  forImport: boolean;
}

defineProps<Props>();
const emit = defineEmits(['update:modelValue']);

const identifiersStore = useIdentifiersStore();
const { t } = useI18n();
const projectsStore = useProjectsStore();

const mappingNames = ref<{ label: string; value: string }[]>([]);
const mappingOption = computed({
  get: () =>
    idConfig.value.allowIdentifierGeneration ? 'allow' : idConfig.value.ignoreUnknownIdentifier ? 'ignore' : 'default',
  set: (value) => {
    idConfig.value.allowIdentifierGeneration = value === 'allow';
    idConfig.value.ignoreUnknownIdentifier = value === 'ignore';
  },
});
const mappingOptions = ref<{ label: string; value: string; hint: string }[]>([
  {
    label: 'importer.id_mappings.mapping_default',
    hint: 'importer.id_mappings.mapping_default_hint',
    value: 'default',
  },
  {
    label: 'importer.id_mappings.mapping_ignore',
    hint: 'importer.id_mappings.mapping_ignore_hint',
    value: 'ignore',
  },
  {
    label: 'importer.id_mappings.mapping_allow',
    hint: 'importer.id_mappings.mapping_allow_hint',
    value: 'allow',
  },
]);
const idConfig = ref({
  name: '',
  allowIdentifierGeneration: false,
  ignoreUnknownIdentifier: false,
} as IdentifiersMappingConfigDto);

function onMappingConfigUpdated() {
  emit('update:modelValue', idConfig.value.name ? idConfig.value : undefined);
}

onMounted(() => {
  // init mappings
  Promise.all([projectsStore.getIdMappings(projectsStore.project.name), identifiersStore.getAllMappings()])
    .then(([projectMappings, allMappings]) => {
      const projectMapping = projectMappings[0] || ({} as ProjectDto_IdentifiersMappingDto);
      mappingNames.value = allMappings.map((mapping: IdentifiersMappingDto) => ({
        label: mapping.name,
        value: mapping.name,
      }));
      mappingNames.value.push({ label: t('none_value'), value: '' });

      idConfig.value.name =
        (allMappings.find((mapping: IdentifiersMappingDto) => mapping.name === projectMapping.mapping) || {}).name ||
        '';
    })
    .catch((err) => {
      notifyError(err);
    });
});
</script>
