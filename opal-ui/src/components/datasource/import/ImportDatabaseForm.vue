<template>
  <div>
    <span class="text-help">{{ t('no_datasource_configuration') }}</span>
  </div>
</template>

<script setup lang="ts">
import type { DatasourceFactory } from 'src/components/models';
import type { DatabaseDto } from 'src/models/Database';

interface Props {
  modelValue: DatasourceFactory | undefined;
  database: DatabaseDto;
}

const { t } = useI18n();

const props = defineProps<Props>();
const emit = defineEmits(['update:modelValue']);

const name = ref(props.database.name);

onMounted(() => {
  if (props.modelValue) {
    const params = props.modelValue['Magma.JdbcDatasourceFactoryDto.params'];
    if (params) {
      name.value = params.database;
    }
  }
  onUpdate();
});

function onUpdate() {
  emit('update:modelValue', {
    'Magma.JdbcDatasourceFactoryDto.params': {
      database: name.value,
    },
  } as DatasourceFactory);
}
</script>
